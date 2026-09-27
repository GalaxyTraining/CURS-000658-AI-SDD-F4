# ADR-0008: Swagger UI con springdoc mostrando el contrato autoritativo

- **Estado:** Aceptado
- **Fecha:** 2026-09-27
- **Specs relacionadas:** `api/catalog-openapi.yaml`, `architecture/tech-stack.md`

## Contexto
Se necesita probar la API desde el navegador. springdoc genera por defecto la documentación
desde el código (code-first), lo que contradice el enfoque spec-first.

## Decisión
Agregar `springdoc-openapi-starter-webmvc-ui` y configurar Swagger UI para que cargue
`catalog-openapi.yaml`, copiado desde `specs/api/` al compilar (no se duplica a mano).

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Solo Postman | Sin dependencias. | No hay documentación navegable dentro de la aplicación. |
| springdoc en modo por defecto | Cero configuración. | Muestra un contrato deducido del código, no el autoritativo. |

## Consecuencias
- Una dependencia más; se actualiza en lockstep con Spring Boot.
- El endpoint `/v3/api-docs` (generado desde el código) puede compararse con el contrato para detectar desvíos.