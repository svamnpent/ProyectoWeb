# Laboratorio 13 — Formularios en Angular

Dos enfoques de formularios **sin consumo del API**. `HttpClient` y Spring Boot quedan para la semana 14 o como actividad complementaria.

## Requisitos

- Node.js 22.22.3 o compatible con Angular 22
- npm
- Angular CLI 22

En esta máquina: `nvm use` (el `.nvmrc` fija `22.22.3`).

```bash
cd laboratorio-formularios
nvm use
npm start
```

Abre `http://localhost:4200/` (redirige a `/plantilla`).

Build de evidencia:

```bash
cd laboratorio-formularios
npm run build
```

## Qué demuestra

- Formulario de contacto basado en plantillas (`FormsModule`, `[(ngModel)]`, `NgForm`)
- Formulario de registro reactivo (`ReactiveFormsModule`, `FormBuilder`, `FormArray`)
- Validaciones incorporadas: `required`, `email`, `minLength`, `pattern`, `requiredTrue`
- Validadores personalizados: `edadMinimaValidator(18)` y `passwordsIgualesValidator`
- Errores visibles solo si el control está `touched` o el formulario se envió
- Sección de depuración con `valid`, `touched`, `dirty` y el valor en JSON
- Carga diferida con `loadComponent`

## Matriz de pruebas

| Prueba | Acción | Resultado esperado |
| --- | --- | --- |
| Plantilla vacía | Enviar sin completar | Aparecen errores de campos obligatorios |
| Nombre corto | Escribir 2 caracteres | Error de longitud mínima |
| Correo inválido | Escribir texto sin `@` | Error de formato |
| Envío válido | Completar y aceptar datos | Mensaje de éxito y `console.log` |
| Reactivo vacío | Registrar sin datos | Todos los controles quedan `touched` |
| Edad | Ingresar 16 | Error `edadMinima` |
| Teléfono | Menos o más de 9 dígitos | Error `pattern` |
| Contraseña | Menos de 8, sin mayúscula o sin número | Errores de `minlength` / `pattern` |
| Confirmación | Contraseñas distintas | Error `passwordsNoCoinciden` |
| FormArray | Agregar 3 habilidades | Se crean controles y se pueden eliminar |
| Registro válido | Completar y aceptar términos | Mensaje de éxito y `console.log` |

Los datos no se envían a un backend: solo se muestran en consola y en la sección de depuración.
