# ADR-0004: Códigos HTTP de error según el tipo de regla incumplida

- **Estado:** Aceptado
- **Fecha:** 2026-09-26
- **Specs relacionadas:** US01–US05, invariantes INV-01 a INV-11

## Contexto
Los criterios de aceptación dicen "se rechaza" sin indicar cómo. El cliente necesita distinguir
entre error de datos, recurso inexistente, conflicto de estado y regla de negocio no cumplida.

## Decisión
| HTTP | Código de error | Cuándo | Invariantes |
| :--- | :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Formato, longitud, rango, valor fuera de catálogo o intento de quitar precio o categoría. | INV-01, INV-04, INV-05, INV-09, INV-11 |
| 404 | `PRODUCT_NOT_FOUND` | El producto no existe. | — |
| 409 | `DUPLICATE_SKU` | El SKU ya existe. | INV-02 |
| 409 | `INVALID_STATE_TRANSITION` | Transición no permitida o producto descontinuado. | INV-06, INV-08 |
| 422 | `PRODUCT_INCOMPLETE` | Activación sin precio o sin categoría. | INV-07 |

Todas las respuestas de error usan el mismo esquema `ErrorResponse { code, message, details }`.

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Todo como 400 | Simple. | El cliente no distingue datos inválidos de conflictos de estado. |

## Consecuencias
- Cada fila tiene una excepción propia en `domain.exception`: `ProductValidationException` (400),
  `ProductNotFoundException` (404), `DuplicateSkuException` (409), `InvalidStateTransitionException` (409)
  y `ProductIncompleteException` (422, con la lista de datos faltantes, US03 CA-02).
- El `GlobalExceptionHandler` mapea cada excepción de dominio a exactamente una fila de esta tabla.
- Las pruebas verifican tanto el HTTP como el `code`.