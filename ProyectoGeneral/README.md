# 🚀 Proyecto General - Desarrollo Web Integrado (31595)

Este directorio es el espacio centralizado para el diseño, desarrollo e integración del proyecto principal del curso. A diferencia de las carpetas de laboratorios, este módulo reúne el trabajo conjunto o los entregables finales evaluados en la asignatura.

---

## 📌 Objetivos del Proyecto

- Implementar una aplicación web funcional integrando las tecnologías aprendidas en clase.
- Aplicar buenas prácticas de arquitectura de software, patrones de diseño y orden de código.
- Mantener un control de versiones limpio y colaborativo mediante ramas organizadas.

---

## 📂 Estructura Sugerida del Directorio

Para mantener el código ordenado y escalable, se recomienda seguir una estructura estándar (sera modificado segun se contemple):

```text
ProyectoGeneral/
├── README.md               # Este archivo informativo
├── client-front/                 # Código fuente del Frontend (Angular, HTML/CSS/JS, etc.)
│   ├── public/
│   └── src/
├── api/                 # Código fuente del Backend / API (Java)
│   ├── config/
│   ├── controllers/
│   └── models/
└── database/               # Scripts de base de datos, diagramas o respaldos (.sql)
```

---

## 📄 Documentación Vinculada

Recuerda que toda la documentación formal, reportes de avance, diagramas de arquitectura y manuales de usuario **no deben saturar esta carpeta de código**.

- Todo archivo documental debe ser subido estrictamente a la carpeta **`/docs`** ubicada en la raíz del repositorio.
- Puedes enlazar los documentos aquí abajo para un acceso rápido si lo deseas (ejemplo: `[Plan de Proyecto](../docs/plan.pdf)`).

---

## 💻 Flujo de Trabajo y Ramas (Git)

Para evitar conflictos al subir cambios en el proyecto general, el equipo seguirá estas pautas de trabajo:

1. **Ramas Protegidas:** Está prohibido programar o subir cambios directamente a la rama principal (`main` o `master`).
2. **Creación de Ramas:** Crea una rama independiente para cada funcionalidad o tarea específica utilizando nombres descriptivos en español (por ejemplo: `caracteristica/inicio-sesion`, `caracteristica/conexion-bd` o `correccion/error-registro`).
3. **Solicitudes de Integración:** Cuando termines tu tarea, sube tu rama y abre una **Solicitud de Extracción** (`Pull Request`).
4. **Revisión por Pares:** Al menos un compañero del equipo deberá revisar y aprobar tu código antes de fusionarlo con la rama principal.
5. **Pruebas Locales:** Asegúrate siempre de que el proyecto compile y funcione correctamente en tu computadora antes de enviar tus cambios al repositorio remoto.
