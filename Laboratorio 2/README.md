# 🧪 Laboratorio 2 - Arquitectura de Capas e Inyección de Dependencias en Spring Boot

Este directorio está dedicado al desarrollo de la segunda sesión práctica del curso **Desarrollo Web Integrado (31595)**. En esta práctica profundizamos en las buenas prácticas de diseño backend, implementando la separación de responsabilidades.

---

## 🎯 Objetivo de la Sesión

Al finalizar esta práctica, el estudiante será capaz de:

- **Configurar endpoints REST** avanzados en Spring Boot mediante controladores para gestionar operaciones sobre recursos.
- **Aplicar inyección de dependencias por constructor**, promoviendo un código acoplado de forma débil y altamente testeable.
- **Separar responsabilidades** de manera estricta dividiendo el proyecto en tres capas fundamentales: **Controller** (controladores), **Service** (servicios de lógica de negocio) y **Modelo** (entidades de datos).
- **Construir una API básica de productos** funcional que sirva como base para arquitecturas más complejas.

---

## 🏗️ Arquitectura del Proyecto (Separación de Capas)

Durante esta sesión, los proyectos individuales dentro de las carpetas de los estudiantes deberán estructurar el código siguiendo este patrón de paquetes:

```text
📂 tu-proyecto/src/main/java/com/clase/
├── 📂 controller/   # Expone los endpoints REST y gestiona las peticiones HTTP.
├── 📂 service/      # Contiene la lógica de negocio y las interfaces/implementaciones.
└── 📂 model/        # Define la estructura de los datos (la entidad Producto).
```

---

## ⚙️ Conceptos Clave Evaluados

- **`@RestController` y `@RequestMapping`:** Gestión de rutas y verbos HTTP (GET, POST, etc.).
- **Inyección por Constructor:** Uso de constructores en Java para inyectar componentes sin depender directamente de `@Autowired` sobre atributos.
- **`@Service` y `@Component`:** Anotaciones de Spring para registrar clases en el contenedor de inversión de control (IoC).

---

## 📝 Guía de Entregas

1. Ubica tu carpeta personal en la raíz de este directorio.
2. Desarrolla la API de productos solicitada en la guía de clase, asegurando que cada clase esté en su capa correspondiente.
3. Verifica que la inyección de dependencias funcione correctamente al levantar el servidor.
4. Realiza tu `commit` y `push` con un mensaje descriptivo (por ejemplo: `feat: implementada capa de servicio y api de productos`).
