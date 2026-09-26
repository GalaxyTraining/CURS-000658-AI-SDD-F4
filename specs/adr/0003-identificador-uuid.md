# ADR-0003: Identificador técnico UUID; el SKU es identificador de negocio

- **Estado:** Aceptado
- **Fecha:** 2026-09-26
- **Specs relacionadas:** RF-01, INV-01, INV-02, INV-03

## Contexto
El SKU es único e inmutable, así que podría usarse como identificador en las rutas.

## Decisión
Cada producto tiene un `id` UUID generado por el sistema, usado en las rutas (`/products/{productId}`).
El SKU es un atributo de negocio con restricción de unicidad.

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| SKU como identificador en la ruta | URLs legibles. | Acopla las rutas a una regla de negocio que podría cambiar (formato del SKU). |
| ID numérico autoincremental | Simple. | Expone volumen del catálogo; depende de la base de datos para generarse. |

## Consecuencias
- El dominio puede crear el `id` sin depender de la base de datos.
- La búsqueda por SKU, si se necesita, será un filtro del listado y no una ruta.