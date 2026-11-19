# Documento de diseño — Plantilla básica de Phantom Store

**Proyecto:** Desarrollo Web Integrado (31595)  
**Aplicación:** Phantom Store (`client-front`)  
**Fecha:** 17 de noviembre de 2026  
**Estado:** plantilla base (color, botones y frames)

## Cómo se elaboró

Este documento se redactó **después** de la configuración de diseño, leyendo los archivos que ya fijan la plantilla. El orden habitual es el inverso: primero el documento y después el código. En esta entrega se hizo al revés para dejar por escrito lo que la configuración ya decide.

Archivos de origen:

| Archivo | Qué fija |
|---|---|
| `ProyectoGeneral/client-front/src/styles/colores.css` | Paleta, tipografía, espacios, radios, sombra y textos de estado |
| `ProyectoGeneral/client-front/src/styles/botones.css` | Botón base y cuatro variantes |
| `ProyectoGeneral/client-front/src/styles/frames.css` | Marco base y cuatro frames |
| `ProyectoGeneral/client-front/src/styles/plantilla.css` | Composición de la página que muestra esa configuración |
| `ProyectoGeneral/client-front/src/styles.css` | Importa los cuatro archivos |

La pantalla de la plantilla está en `src/app/app.html`. No describe todavía el catálogo, el login ni el back-office: solo el lenguaje visual con el que se van a armar esas pantallas.

## Principio

Toda la interfaz usa las variables de `colores.css`. Un botón o un frame no elige un color suelto: toma `--color-primario`, `--color-superficie`, `--color-linea`, etc. Si más adelante cambia la paleta, se cambia el token y la plantilla sigue igual.

## Color

Los colores de texto (`--color-tinta`, `--color-tinta-suave` y las utilidades de estado) van sobre `--color-fondo` o `--color-superficie`. El texto de un botón usa el token “sobre” de ese botón (`--color-sobre-primario` o `--color-sobre-peligro`), que es blanco.

| Token | Valor | Uso |
|---|---|---|
| `--color-fondo` | `#f3f5f8` | Fondo de la página |
| `--color-superficie` | `#ffffff` | Frames, botón secundario, muestras |
| `--color-superficie-suave` | `#e8edf3` | Frame encajado y hover del botón secundario |
| `--color-tinta` | `#121826` | Texto principal |
| `--color-tinta-suave` | `#5b677a` | Texto secundario y etiquetas de apoyo |
| `--color-linea` | `#d7dee7` | Bordes de frames y del botón secundario |
| `--color-primario` | `#2f3e8f` | Marca y botón primario |
| `--color-primario-hover` | `#243273` | Hover del botón primario |
| `--color-primario-suave` | `#e8ebf8` | Hover del botón fantasma |
| `--color-sobre-primario` | `#ffffff` | Texto del botón primario |
| `--color-acento` | `#0e7490` | Precio y anillo de foco |
| `--color-exito` | `#067647` | Estado positivo (hay stock) |
| `--color-aviso` | `#b54708` | Estado de atención (quedan pocas unidades) |
| `--color-peligro` | `#b42318` | Botón destructivo y texto de error |
| `--color-peligro-hover` | `#912018` | Hover del botón de peligro |
| `--color-sobre-peligro` | `#ffffff` | Texto del botón de peligro |

Utilidades definidas en el mismo archivo:

| Clase | Color | Cuándo usarla |
|---|---|---|
| `.texto-suave` | tinta suave | Ayudas, categorías, metadatos |
| `.texto-acento` | acento, negrita | Precio |
| `.texto-exito` | éxito | Confirmación o stock disponible |
| `.texto-aviso` | aviso | Advertencia que no bloquea |
| `.texto-peligro` | peligro | Error o acción irreversible en texto |

## Tipografía, espacio y forma

También salen de `colores.css`.

| Token | Valor |
|---|---|
| `--fuente-base` | Inter, luego Segoe UI, Roboto, Helvetica, Arial, sans-serif |
| `--texto-sm` | `0.875rem` (botones y etiquetas) |
| `--texto-md` | `1rem` (texto de lectura) |
| `--texto-lg` | `1.25rem` (títulos de sección y marca) |
| `--texto-xl` | `1.75rem` (reservado; la plantilla aún no lo usa) |
| `--espacio-xs` … `--espacio-xl` | 4, 8, 16, 24 y 40 px |
| `--radio-sm` | 6 px (botones y muestras) |
| `--radio-md` | 12 px (frames) |
| `--sombra-frame` | `0 8px 24px rgba(18, 24, 38, 0.08)` (solo la tarjeta) |

`index.html` carga Inter. Si la fuente no llega, el stack del token sigue siendo legible.

La página base (`body`) ya aplica fondo, tinta, fuente y tamaño. El margen del body queda en 0 para que la barra superior llegue al borde.

## Botones

Clase base: `.btn`. Siempre se combina con una variante. Alto mínimo 40 px, padding horizontal 16 px, texto seminegrita de 14 px, radio 6 px.

| Clase | Fondo | Texto | Borde | Hover |
|---|---|---|---|---|
| `.btn-primario` | primario | blanco | ninguno | primario hover |
| `.btn-secundario` | superficie | tinta | línea | superficie suave |
| `.btn-fantasma` | transparente | primario | ninguno | primario suave |
| `.btn-peligro` | peligro | blanco | ninguno | peligro hover |

Estados comunes:

- **Foco:** anillo de 2 px en `--color-acento`, separado 2 px del borde. Vale para teclado (`:focus-visible`).
- **Deshabilitado:** opacidad 0.5 y cursor `not-allowed`. El hover no cambia el color.
- El hover solo actúa si el botón no está deshabilitado.

Cuándo usar cada uno:

- **Primario:** una acción principal por zona (Ingresar, Agregar, Pagar).
- **Secundario:** la alternativa (Seguir viendo, Cancelar que no borra).
- **Fantasma:** acción de barra o de poco peso (Catálogo, enlaces que se ven como botón).
- **Peligro:** quitar, anular o borrar.

## Frames

Clase base: `.frame`. Fondo de superficie, texto de tinta, borde de línea y radio de 12 px. Por dentro se parte en cabecera, cuerpo y pie.

| Clase | Qué cambia respecto al frame base |
|---|---|
| `.frame-barra` | Fila (marca a la izquierda, acciones a la derecha), sin radio y sin borde salvo el inferior. En pantallas de hasta 640 px pasa a columna. |
| `.frame-tarjeta` | Añade `--sombra-frame`. Sirve para un producto u otro ítem suelto. |
| `.frame` solo (panel) | El marco base, con cabecera y pie separados por línea. Sirve para un bloque de resumen. |
| `.frame-encajado` | Fondo suave y borde discontinuo. Va dentro de otro bloque (aviso, dato anidado, zona de formulario). |

Partes internas:

| Clase | Regla |
|---|---|
| `.frame__cabecera` | Padding 16 px, borde inferior, texto en seminegrita |
| `.frame__cuerpo` | Padding 16 px |
| `.frame__pie` | Padding 16 px, borde superior, botones alineados a la derecha con 8 px de separación |

## Composición de la plantilla

La página de `app.html` enseña las tres piezas en este orden:

```text
+--------------------------------------------------+
| Phantom Store              [Catálogo] [Ingresar] |  frame-barra
+--------------------------------------------------+
| Plantilla de diseño                             |
| Color, botones y frames de la tienda.           |
|                                                  |
| Color                                            |
| [muestras de la paleta]                          |
|                                                  |
| Botones                                          |
| [Primario] [Secundario] [Fantasma] [Peligro]     |
| [Deshabilitado]                                  |
|                                                  |
| Frames                                           |
| +------------+ +------------+ +--------------+   |
| | Tarjeta    | | Panel      | | Encajado     |   |
| | producto   | | resumen    | | aviso stock  |   |
| | [Agregar]  | | [Seguir]   | | [Quitar]     |   |
| +------------+ | [Pagar]    | +--------------+   |
|                +------------+                    |
+--------------------------------------------------+
```

Los tres frames de ejemplo usan datos de muestra (un producto, un resumen y un aviso de stock). No llaman al API.

## Reglas para las pantallas que vengan

1. No inventar hex nuevos en un componente. Si falta un rol (por ejemplo un fondo de error), se agrega el token en `colores.css` y se actualiza este documento.
2. Un bloque visual nuevo se arma con `.frame` y, si hace falta, una sola variante.
3. En una misma zona hay un solo `.btn-primario`.
4. `.btn-peligro` queda para acciones que quitan o anulan.
5. El precio usa `.texto-acento`. El stock disponible usa `.texto-exito`. La escasez usa `.texto-aviso`.
6. El foco del teclado no se quita: el anillo de acento se mantiene.

## Fuera de alcance

Esta plantilla no define pantallas, flujo de compra, navegación real ni componentes de formulario (campos, selects, tablas). Esos documentos, cuando existan, deben partir de estos mismos tokens.
