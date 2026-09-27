# ADR-0007: Error interno genérico (500 `INTERNAL_ERROR`)

- **Estado:** Aceptado
- **Fecha:** 2026-09-26
- **Specs relacionadas:** ADR-0004, `api/API-GUIDELINES.md`, `api/catalog-openapi.yaml`

## Contexto
ADR-0004 define los errores que corresponden a reglas de negocio, pero no qué ocurre ante una excepción
no prevista (fallo de base de datos, error de programación). Sin una decisión, Spring responde con su
formato por defecto, distinto de `ErrorResponse`, que además puede exponer detalles internos.

## Decisión
- Toda excepción no mapeada por ADR-0004 responde `500` con
  `ErrorResponse { code: INTERNAL_ERROR, message: "Unexpected error", details: [] }`.
- La respuesta nunca incluye el mensaje de la excepción ni el stack trace; la excepción completa se
  registra en el log con nivel `ERROR`.
- Se agrega `INTERNAL_ERROR` al enum `ErrorCode` y a la tabla de ADR-0004; todas las operaciones del
  contrato declaran la respuesta `500` (`components/responses/InternalError`).
- Es el único código sin excepción de dominio: lo produce el `GlobalExceptionHandler` como último recurso.

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Respuesta por defecto de Spring | Sin código adicional. | Formato distinto de `ErrorResponse`; puede exponer detalles internos. |
| 500 con el mensaje de la excepción | Facilita depurar. | Filtra información interna al cliente. |

## Consecuencias
- El cliente recibe siempre `ErrorResponse`, también ante fallos inesperados.
- El diagnóstico se hace con el log, no con la respuesta.
- Verificación: una prueba web provoca una excepción no prevista y comprueba HTTP 500, `code` y que el
  mensaje interno no aparece en la respuesta.
