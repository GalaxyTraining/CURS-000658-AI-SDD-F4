# ADR-0009: Rechazo de null explícito y de cuerpos sin campos en los requests

- **Estado:** Aceptado
- **Fecha:** 2026-09-27
- **Specs relacionadas:** US02 CA-05, INV-11, `adr/0004`, `api/catalog-openapi.yaml`, `api/API-GUIDELINES.md`

## Contexto
El contrato no admite `null` en ningún `*Request` (ningún campo de request es `nullable`) y
`UpdateProductRequest` exige al menos un campo (`minProperties: 1`). ADR-0004 asigna 400
`VALIDATION_ERROR` al intento de quitar precio o categoría (INV-11).

El código generado desde el contrato (`openApiNullable=false`) usa campos simples: `{"price": null}`
y `{}` llegan igual (`price == null`), y `Product.update(...)` interpreta `null` como "no enviado".
Tampoco se genera validación para `minProperties`. Sin una decisión, ambos casos responderían 200.

## Decisión
1. El adaptador web configura Jackson para que falle al deserializar un `null` explícito en
   cualquier cuerpo de request. El error se traduce a 400 `VALIDATION_ERROR`, igual que un JSON inválido.
2. Si `UpdateProductRequest` llega sin ninguno de sus campos (`name`, `description`, `price`,
   `category`), el adaptador web responde 400 `VALIDATION_ERROR`. Las propiedades que no pertenecen
   al esquema (por ejemplo `sku`) se ignoran y no cuentan como campo enviado.

El dominio no cambia: en `Product.update(...)` un `null` sigue significando "no enviado".

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Marcar los campos `nullable` y activar `openApiNullable` (`JsonNullable`) | Distingue ausente de `null` por campo. | Contradice el contrato ("no admite null") y agrega la dependencia `jackson-databind-nullable`. |
| Rechazar `null` solo en `UpdateProductRequest` | No cambia `registerProduct`. | Regla distinta por request para un mismo contrato; más configuración. |
| No hacer nada | Sin código. | US02 CA-05 e INV-11 (ADR-0004) no se cumplen por HTTP. |

## Consecuencias
- Se aplica a todos los `*Request`: `registerProduct` con `"price": null` pasa de 201 a 400.
  Omitir el campo sigue siendo válido.
- Un request que solo envía `sku` a `updateProduct` se rechaza con 400; el SKU no cambia (US02 CA-03).
- Se verifica con pruebas web: `null` explícito en `updateProduct` y `registerProduct`, y cuerpo vacío en `updateProduct`.
