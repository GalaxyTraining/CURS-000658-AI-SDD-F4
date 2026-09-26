# ADR-0002: Transiciones de estado como acciones (`POST /products/{id}/activate`)

- **Estado:** Aceptado
- **Fecha:** 2026-09-26
- **Specs relacionadas:** `domain/product-lifecycle.md`, US03, US04

## Contexto
El producto cambia de estado por acciones de negocio con reglas distintas: activar exige precio y categoría;
descontinuar exige motivo.

## Decisión
Cada transición es un `POST` a un sub-recurso con verbo de negocio:
`POST /api/v1/products/{id}/activate` y `POST /api/v1/products/{id}/discontinue`.
El estado nunca se modifica directamente en `PUT`/`PATCH` del producto.

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| `PATCH /products/{id}` con `{ "status": "ACTIVE" }` | Un solo endpoint. | Cada transición exige datos distintos; validación condicional difícil de expresar en OpenAPI; permite saltarse reglas. |

## Consecuencias
- Cada acción tiene su propio `operationId`, request y errores, y mapea 1:1 a un caso de uso.
- La actualización de datos (US02) nunca puede cambiar el estado.