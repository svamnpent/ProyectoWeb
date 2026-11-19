-- El bloqueo evita escrituras entre la carga histórica y la instalación de los triggers.
LOCK TABLE ventas, productos IN SHARE ROW EXCLUSIVE MODE;

CREATE TABLE auditoria_comercial (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha timestamptz NOT NULL DEFAULT statement_timestamp(),
    tipo text NOT NULL,
    referencia text NOT NULL,
    recurso_id bigint NOT NULL,
    cliente_id bigint,
    unidades integer NOT NULL,
    importe numeric(12,2),
    stock_final integer
);
CREATE INDEX auditoria_comercial_fecha ON auditoria_comercial (fecha DESC, id DESC);

INSERT INTO auditoria_comercial (fecha,tipo,referencia,recurso_id,cliente_id,unidades,importe)
SELECT fecha,'HISTORICA',numero_orden,id,usuario_id,cantidad_items,total FROM ventas;

CREATE FUNCTION auditar_venta() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO auditoria_comercial(tipo,referencia,recurso_id,cliente_id,unidades,importe)
        VALUES ('VENTA',NEW.numero_orden,NEW.id,NEW.usuario_id,NEW.cantidad_items,NEW.total);
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO auditoria_comercial(tipo,referencia,recurso_id,cliente_id,unidades,importe)
        VALUES ('ANULACION',OLD.numero_orden,OLD.id,OLD.usuario_id,-OLD.cantidad_items,-OLD.total);
    ELSIF (NEW.total, NEW.cantidad_items) IS DISTINCT FROM (OLD.total, OLD.cantidad_items) THEN
        INSERT INTO auditoria_comercial(tipo,referencia,recurso_id,cliente_id,unidades,importe)
        VALUES ('AJUSTE',NEW.numero_orden,NEW.id,NEW.usuario_id,
                NEW.cantidad_items-OLD.cantidad_items,NEW.total-OLD.total);
    END IF;
    RETURN NULL;
END;
$$;
CREATE TRIGGER ventas_auditoria AFTER INSERT OR UPDATE OR DELETE ON ventas
FOR EACH ROW EXECUTE FUNCTION auditar_venta();

CREATE FUNCTION auditar_stock() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        INSERT INTO auditoria_comercial(tipo,referencia,recurso_id,unidades,stock_final)
        VALUES ('BAJA',OLD.nombre,OLD.id,-OLD.stock,0);
    ELSIF TG_OP = 'INSERT' THEN
        INSERT INTO auditoria_comercial(tipo,referencia,recurso_id,unidades,stock_final)
        VALUES ('ALTA',NEW.nombre,NEW.id,NEW.stock,NEW.stock);
    ELSIF NEW.stock <> OLD.stock THEN
        INSERT INTO auditoria_comercial(tipo,referencia,recurso_id,unidades,stock_final)
        VALUES ('STOCK',NEW.nombre,NEW.id,NEW.stock-OLD.stock,NEW.stock);
    END IF;
    RETURN NULL;
END;
$$;
CREATE TRIGGER productos_auditoria AFTER INSERT OR UPDATE OR DELETE ON productos
FOR EACH ROW EXECUTE FUNCTION auditar_stock();
