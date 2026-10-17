-- =============================================================================
-- MIGRACIÓN V4 - FLYWAY
-- Nuevo rol TRABAJADOR (personal de la tienda con acceso limitado al back-office).
-- Roles: USER (comprador), TRABAJADOR (personal), ADMIN (administrador).
-- =============================================================================
ALTER TABLE "public"."usuarios" DROP CONSTRAINT IF EXISTS "chk_usuarios_rol";
ALTER TABLE "public"."usuarios"
  ADD CONSTRAINT "chk_usuarios_rol" CHECK ("rol" IN ('USER', 'TRABAJADOR', 'ADMIN'));
