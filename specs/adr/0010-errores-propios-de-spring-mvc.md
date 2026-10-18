# ADR-0010: Errores propios de Spring MVC fuera de las operaciones del contrato

- **Estado:** Aceptado
- **Fecha:** 2026-10-18
- **Specs relacionadas:** `adr/0004`, `adr/0007`, `api/API-GUIDELINES.md`, `api/catalog-openapi.yaml`

## Contexto
ADR-0007 dice que toda excepción no mapeada por ADR-0004 responde 500 `INTERNAL_ERROR`. Spring MVC
también lanza excepciones cuando la petición ni siquiera llega a una operación del contrato:
ruta inexistente, método HTTP no permitido, `Content-Type` no soportado o `Accept` no aceptable.
Aplicando ADR-0007 al pie de la letra, una ruta inexistente respondería 500, lo que culpa al servidor
de un error del cliente. El enum `ErrorCode` del contrato no tiene códigos para estos casos.

## Decisión
1. Las excepciones de Spring MVC que ya traen su propio código HTTP (las que implementan
   `org.springframework.web.ErrorResponse`: ruta inexistente 404, método no permitido 405,
   media type no aceptable 406, media type no soportado 415) conservan ese código y el cuerpo de error
   por defecto de Spring Boot. No se traducen a `ErrorResponse` ni a `INTERNAL_ERROR`.
2. Las que corresponden a una operación del contrato con datos inválidos (cuerpo ilegible, Bean Validation,
   parámetro de tipo incorrecto) siguen respondiendo 400 `VALIDATION_ERROR` (ADR-0004).
3. ADR-0007 se aplica a todo lo demás.

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| 500 `INTERNAL_ERROR` (ADR-0007 literal) | Sin excepciones a la regla. | Código HTTP falso: el error es del cliente; dificulta el diagnóstico. |
| Nuevos códigos en `ErrorCode` (`ROUTE_NOT_FOUND`, `METHOD_NOT_ALLOWED`, `UNSUPPORTED_MEDIA_TYPE`) | El cliente siempre recibe `ErrorResponse`. | Cambia el contrato y obliga a declarar 404/405/415 en todas las operaciones por casos ajenos al negocio. |
| `ErrorResponse` con códigos existentes (ej. `PRODUCT_NOT_FOUND` para ruta inexistente) | Sin cambiar el contrato. | Semántica engañosa: el producto no es lo que falta. |

## Consecuencias
- Estos errores quedan fuera del contrato: no se declaran en `catalog-openapi.yaml`.
- El cuerpo por defecto de Spring Boot no expone mensaje ni traza (configuración por defecto), así que
  se mantiene la intención de ADR-0007 de no filtrar detalles internos.
- `GlobalExceptionHandler` devuelve estas excepciones a Spring en lugar de convertirlas en 500.
- Verificación: prueba web con ruta inexistente (404), método no permitido (405), `Accept` no
  aceptable (406) y `Content-Type` no soportado (415), comprobando que ninguno responde 500.
