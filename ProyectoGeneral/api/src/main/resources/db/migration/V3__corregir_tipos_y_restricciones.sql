-- =============================================================================
-- MIGRACIÓN V3 - FLYWAY
-- Corrección de tipos de datos y restricciones que no coincidían con el modelo.
-- (V1 y V2 no se tocan: Flyway valida su checksum.)
-- =============================================================================

-- 1. VENTAS.NUMERO_ORDEN: era "text" sin límite y NULL, pero la entidad lo trataba
--    como un código corto obligatorio y único. Pasa a varchar(30) NOT NULL.
UPDATE "public"."ventas"
   SET "numero_orden" = UPPER(SUBSTR(MD5(RANDOM()::text || "id"::text), 1, 12))
 WHERE "numero_orden" IS NULL;
ALTER TABLE "public"."ventas" ALTER COLUMN "numero_orden" TYPE varchar(30);
ALTER TABLE "public"."ventas" ALTER COLUMN "numero_orden" SET NOT NULL;

-- 2. FECHAS: "timestamp without time zone" + DEFAULT now() guardaba una hora local
--    ambigua (dependía de la zona de la sesión de la BD y de la JVM). Pasan a
--    "timestamp with time zone" (instante absoluto) y a NOT NULL.
--    SUPUESTO: los valores existentes se guardaron en UTC (lo habitual en contenedores).
UPDATE "public"."usuarios" SET "fecha_registro" = (NOW() AT TIME ZONE 'UTC') WHERE "fecha_registro" IS NULL;
UPDATE "public"."ventas"   SET "fecha"          = (NOW() AT TIME ZONE 'UTC') WHERE "fecha" IS NULL;

ALTER TABLE "public"."usuarios" ALTER COLUMN "fecha_registro" DROP DEFAULT;
ALTER TABLE "public"."usuarios" ALTER COLUMN "fecha_registro" TYPE timestamptz USING "fecha_registro" AT TIME ZONE 'UTC';
ALTER TABLE "public"."usuarios" ALTER COLUMN "fecha_registro" SET DEFAULT now();
ALTER TABLE "public"."usuarios" ALTER COLUMN "fecha_registro" SET NOT NULL;

ALTER TABLE "public"."ventas" ALTER COLUMN "fecha" DROP DEFAULT;
ALTER TABLE "public"."ventas" ALTER COLUMN "fecha" TYPE timestamptz USING "fecha" AT TIME ZONE 'UTC';
ALTER TABLE "public"."ventas" ALTER COLUMN "fecha" SET DEFAULT now();
ALTER TABLE "public"."ventas" ALTER COLUMN "fecha" SET NOT NULL;

-- 3. PERFILES: una URL de avatar de 255 caracteres se queda corta (URLs firmadas de CDN);
--    productos.imagen_url ya admitía 1000. Se igualan y se amplía la biografía.
ALTER TABLE "public"."perfiles" ALTER COLUMN "avatar_url" TYPE varchar(1000);
ALTER TABLE "public"."perfiles" ALTER COLUMN "biografia"  TYPE varchar(1000);

-- 4. UNICIDAD DE NOMBRES: la entidad Etiqueta declaraba unique=true y los servicios
--    rechazan duplicados, pero la BD no lo garantizaba (dos peticiones simultáneas podían
--    duplicar). Se renombran los duplicados existentes (sin borrar nada, hay FKs) y se
--    crea un índice único sin distinguir mayúsculas, igual que el chequeo de los servicios.
UPDATE "public"."categorias" c
   SET "nombre" = LEFT(c."nombre", 100 - LENGTH(' (' || c."id" || ')')) || ' (' || c."id" || ')'
  FROM (SELECT "id", ROW_NUMBER() OVER (PARTITION BY LOWER("nombre") ORDER BY "id") AS rn
          FROM "public"."categorias") d
 WHERE c."id" = d."id" AND d.rn > 1;
CREATE UNIQUE INDEX "uq_categorias_nombre_lower" ON "public"."categorias" (LOWER("nombre"));

UPDATE "public"."etiquetas" e
   SET "nombre" = LEFT(e."nombre", 50 - LENGTH(' (' || e."id" || ')')) || ' (' || e."id" || ')'
  FROM (SELECT "id", ROW_NUMBER() OVER (PARTITION BY LOWER("nombre") ORDER BY "id") AS rn
          FROM "public"."etiquetas") d
 WHERE e."id" = d."id" AND d.rn > 1;
CREATE UNIQUE INDEX "uq_etiquetas_nombre_lower" ON "public"."etiquetas" (LOWER("nombre"));

-- 5. COHERENCIA DE IMPORTES (NOT VALID: se exige en todo dato nuevo sin fallar por datos históricos)
ALTER TABLE "public"."detalle_ventas"
  ADD CONSTRAINT "chk_detalle_subtotal" CHECK ("subtotal" = "cantidad" * "precio_unitario") NOT VALID;
ALTER TABLE "public"."ventas"
  ADD CONSTRAINT "chk_ventas_importes" CHECK ("total" >= 0 AND "cantidad_items" >= 0) NOT VALID;
