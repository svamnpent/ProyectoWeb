# API de la tienda Phantom

## Uso desde Angular

El cliente utiliza 34 de los 44 endpoints (77,3 %). Otros 2 tienen un método HTTP implementado sin una pantalla que lo invoque y 8 carecen de llamada en los servicios del cliente. Se cuenta cada combinación de verbo y ruta de los controladores bajo `/api/v1` una vez. La revisión sigue el código desde las rutas, los componentes y el inicializador de sesión hasta `HttpClient`. Las pruebas, Swagger y Actuator quedan fuera del recuento.

Inicio filtra por categoría, nombre o marca sobre los productos ya descargados. Pedido recibe los detalles dentro de la venta. Estas dos pantallas cubren sus funciones sin llamar a las rutas específicas de búsqueda ni de líneas de venta.

| Método | Ruta relativa a `/api/v1`               | Uso en Angular                                                  |
| ------ | --------------------------------------- | --------------------------------------------------------------- |
| POST   | `/auth/register`                        | Solo servicio · `AuthService.registrar`                         |
| POST   | `/auth/login`                           | Login inicia la sesión.                                         |
| GET    | `/auth/me`                              | Carga el usuario tras el login o al restaurar un token vigente. |
| POST   | `/backoffice/productos`                 | Catálogo interno de productos.                                  |
| PUT    | `/backoffice/productos/{id}`            | Catálogo interno de productos.                                  |
| DELETE | `/backoffice/productos/{id}`            | Catálogo interno de productos.                                  |
| POST   | `/backoffice/categorias`                | Mantenimiento de categorías.                                    |
| PUT    | `/backoffice/categorias/{id}`           | Mantenimiento de categorías.                                    |
| DELETE | `/backoffice/categorias/{id}`           | Mantenimiento de categorías.                                    |
| POST   | `/backoffice/etiquetas`                 | Mantenimiento de etiquetas.                                     |
| PUT    | `/backoffice/etiquetas/{id}`            | Mantenimiento de etiquetas.                                     |
| DELETE | `/backoffice/etiquetas/{id}`            | Mantenimiento de etiquetas.                                     |
| GET    | `/backoffice/ventas`                    | Gestión de ventas de mostrador y sus líneas.                    |
| GET    | `/backoffice/ventas/{id}`               | Gestión de ventas de mostrador y sus líneas.                    |
| POST   | `/backoffice/ventas`                    | Gestión de ventas de mostrador y sus líneas.                    |
| DELETE | `/backoffice/ventas/{id}`               | Gestión de ventas de mostrador y sus líneas.                    |
| GET    | `/backoffice/ventas/{ventaId}/detalles` | Gestión de ventas de mostrador y sus líneas.                    |
| POST   | `/backoffice/ventas/{ventaId}/detalles` | Gestión de ventas de mostrador y sus líneas.                    |
| DELETE | `/backoffice/detalles/{id}`             | El administrador retira una línea de la venta.                  |
| GET    | `/backoffice/usuarios`                  | Administración de usuarios.                                     |
| GET    | `/backoffice/usuarios/buscar`           | Administración de usuarios.                                     |
| GET    | `/backoffice/usuarios/{id}`             | Administración de usuarios.                                     |
| POST   | `/backoffice/usuarios`                  | Administración de usuarios.                                     |
| PUT    | `/backoffice/usuarios/{id}`             | Administración de usuarios.                                     |
| DELETE | `/backoffice/usuarios/{id}`             | Administración de usuarios.                                     |
| GET    | `/backoffice/perfiles`                  | El administrador consulta los perfiles.                         |
| GET    | `/categorias`                           | Inicio carga las opciones del filtro por categoría.             |
| GET    | `/categorias/{id}`                      | Sin llamada                                                     |
| GET    | `/categorias/buscar`                    | Sin llamada                                                     |
| GET    | `/ventas/{ventaId}/detalles`            | Sin llamada                                                     |
| GET    | `/etiquetas`                            | El personal lista las etiquetas y las asigna a productos.       |
| GET    | `/etiquetas/{id}`                       | Sin llamada                                                     |
| GET    | `/perfiles/{id}`                        | Sin llamada                                                     |
| POST   | `/perfiles`                             | Perfil crea el perfil público si la cuenta aún no tiene uno.    |
| PUT    | `/perfiles/{id}`                        | Perfil actualiza avatar y biografía.                            |
| DELETE | `/perfiles/{id}`                        | Sin llamada                                                     |
| GET    | `/productos`                            | Inicio carga el catálogo y Carrito sincroniza las existencias.  |
| GET    | `/productos/{id}`                       | Solo servicio · `ProductoService.obtener`                       |
| GET    | `/productos/buscar`                     | Sin llamada                                                     |
| GET    | `/usuarios/{id}`                        | Sin llamada                                                     |
| PUT    | `/usuarios/{id}`                        | Perfil actualiza los datos de la cuenta propia.                 |
| GET    | `/ventas/mias`                          | Perfil carga el historial de compras.                           |
| GET    | `/ventas/{id}`                          | Pedido recupera la compra por su ID.                            |
| POST   | `/ventas`                               | Carrito confirma la compra y abre el pedido creado.             |

## Acceso a la tienda y al back-office

El catálogo admite lectura pública. El registro público asigna el rol `USER`. El login devuelve un JWT cuyo campo `sub` identifica al usuario y cuyo campo `roles` se convierte en autoridades de Spring con el prefijo `ROLE_`. La restricción `chk_usuarios_rol` de la migración V4 admite `USER`, `TRABAJADOR` y `ADMIN`.

`SecurityConfig` restringe el prefijo `/api/v1/backoffice` al personal. Las anotaciones de `BackOfficeController` precisan los permisos de cada operación.

| Operación interna                                   | TRABAJADOR | ADMIN |
| --------------------------------------------------- | ---------- | ----- |
| Crear o editar productos, categorías y etiquetas    | Sí         | Sí    |
| Eliminar productos, categorías y etiquetas          | No         | Sí    |
| Listar ventas, consultar una venta y ver sus líneas | Sí         | Sí    |
| Registrar una venta de mostrador                    | Sí         | Sí    |
| Anular una venta o agregar y quitar líneas          | No         | Sí    |
| Gestionar usuarios y listar perfiles                | No         | Sí    |

`CurrentUser` permite comprobar la titularidad desde los servicios y desde expresiones de autorización. Los compradores consultan sus propias ventas y el personal puede atender ventas de clientes. Para acceder a datos personales de usuarios y perfiles se requiere ser el titular o administrador. El resumen de cliente incluido en una venta omite DNI, dirección y teléfono.

### Inicio de sesión y administrador inicial

El contador de intentos fallidos se mantiene en Caffeine por correo y por instancia. Tras alcanzar el límite, el bloqueo dura el intervalo configurado desde el último fallo. Un login correcto reinicia el contador. Al desplegar varias réplicas, cada una mantiene su propio contador. El bloqueo por correo también permite que varios intentos fallidos afecten al titular legítimo. Cuando el correo no existe, el servicio compara la contraseña con un hash de relleno para reducir diferencias de tiempo.

El arranque crea al administrador inicial cuando están definidos `ADMIN_EMAIL` y `ADMIN_PASSWORD` y el correo aún no existe. Si ya existe, conserva su cuenta.

## Catálogo, ventas y fechas

- **Caché.** Caffeine mantiene categorías, etiquetas y productos en memoria. El tamaño y el tiempo de vida se configuran con `spring.cache.*`. El interceptor de caché envuelve al transaccional para que las invalidaciones ocurran después de confirmar la escritura. Así se evita que una lectura concurrente repueble la caché con el valor anterior mientras la escritura sigue pendiente.
- **Precarga.** El arranque puede cargar el catálogo público para atender las primeras consultas desde memoria. Un fallo de precarga se registra y permite continuar el arranque. Se desactiva con `app.cache.warmup.enabled=false`.
- **Productos.** Al asignar etiquetas, el servicio resuelve sus identificadores y rechaza la operación si falta alguna.
- **Compra y mostrador.** El comprador se asigna desde el JWT. En mostrador, el personal puede indicar un cliente u omitirlo. Ambos flujos descuentan stock. Las líneas repetidas se agrupan por producto y las actualizaciones se ordenan por ID para reducir bloqueos entre compras concurrentes. El descuento en la base de datos exige stock suficiente. Si falla una línea, se revierte la transacción completa. Al anular una venta se reponen las unidades. Los cambios administrativos en las líneas mantienen el stock y los totales.
- **Hora de la tienda.** PostgreSQL almacena instantes con `timestamptz`. La API usa `AppTime` para expresarlos en `America/Lima`, con desfase UTC−05:00.

## Datos de demostración

El perfil `demo` activa `app.seed.enabled`. Si ya hay categorías, el cargador omite la siembra. En una tienda vacía crea categorías, etiquetas, productos, usuarios, perfiles y ventas. Las cuentas de demostración usan una contraseña conocida y están destinadas al entorno local.

| Rol        | Cuentas de demostración                                                          |
| ---------- | -------------------------------------------------------------------------------- |
| Comprador  | `maria.quispe@demo.local`, `carlos.huaman@demo.local`, `lucia.torres@demo.local` |
| Trabajador | `trabajador@demo.local`                                                          |

La contraseña de estas cuentas es `Demo12345`. El administrador usa las variables de arranque indicadas arriba.

## Qué comprueban las pruebas

- **Autorización HTTP.** Las pruebas MVC cargan la configuración de seguridad y simulan identidades con `JwtTestUtils`, usando el ID como `sub`. `SecurityEndpointsTest` recorre la matriz de permisos de anónimo, comprador, trabajador y administrador con servicios simulados. `BackOfficeControllerTest` comprueba las operaciones y que una petición denegada no llegue al servicio. `GlobalExceptionHandlerTest` revisa el estado HTTP y el formato de error, incluidos el acceso denegado y el JSON inválido.
- **Integración.** La suite prevé arrancar la aplicación con PostgreSQL en Docker mediante Testcontainers y `@ServiceConnection`. Los casos usan datos con nombres únicos. `DatabaseConnectionIT` comprueba conexión, migraciones y validación del esquema. `VentaFlowIT` cubre transacciones, reversión, titularidad y concurrencia. `DemoDataIT` activa la siembra y la precarga en su propio contexto.
- **Caché.** `CacheBehaviorTest` combina Caffeine con repositorios simulados. `CacheIT` modifica una fila por JDBC al margen de la API para distinguir una respuesta almacenada de una lectura nueva. Después comprueba que una escritura por la API invalide la entrada.
