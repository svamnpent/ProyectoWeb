-- =============================================================================
-- MIGRACIÓN V2 - FLYWAY
-- Índices en claves foráneas (PostgreSQL no los crea automáticamente) y
-- restricciones de integridad que respaldan las reglas de negocio de la API.
-- =============================================================================

-- 1. Índices para JOINs y búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_productos_categoria   ON "public"."productos" ("categoria_id");
CREATE INDEX IF NOT EXISTS idx_pe_etiqueta           ON "public"."producto_etiqueta" ("etiqueta_id");
CREATE INDEX IF NOT EXISTS idx_ventas_usuario_fecha  ON "public"."ventas" ("usuario_id", "fecha" DESC);
CREATE INDEX IF NOT EXISTS idx_detalle_venta         ON "public"."detalle_ventas" ("venta_id");
CREATE INDEX IF NOT EXISTS idx_detalle_producto      ON "public"."detalle_ventas" ("producto_id");

-- 2. Roles: normaliza datos existentes (cualquier valor distinto de ADMIN pasa a USER,
--    criterio de mínimo privilegio) y restringe los valores permitidos.
UPDATE "public"."usuarios"
   SET "rol" = CASE WHEN UPPER("rol") = 'ADMIN' THEN 'ADMIN' ELSE 'USER' END;

ALTER TABLE "public"."usuarios"
  ADD CONSTRAINT "chk_usuarios_rol" CHECK ("rol" IN ('USER', 'ADMIN'));

-- 3. Integridad de stock, precios y cantidades.
--    NOT VALID: se aplican a todo INSERT/UPDATE nuevo sin fallar por datos históricos.
--    Cuando verifiques que tus datos son correctos puedes ejecutar, por ejemplo:
--      ALTER TABLE productos VALIDATE CONSTRAINT chk_productos_stock;
ALTER TABLE "public"."productos"
  ADD CONSTRAINT "chk_productos_stock" CHECK ("stock" >= 0) NOT VALID;
ALTER TABLE "public"."productos"
  ADD CONSTRAINT "chk_productos_precio" CHECK ("precio" >= 0) NOT VALID;
ALTER TABLE "public"."detalle_ventas"
  ADD CONSTRAINT "chk_detalle_cantidad" CHECK ("cantidad" > 0) NOT VALID;
