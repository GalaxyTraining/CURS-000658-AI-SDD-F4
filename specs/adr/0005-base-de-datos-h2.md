# ADR-0005: Base de datos H2 en memoria con Spring Data JPA

- **Estado:** Aceptado
- **Fecha:** 2026-09-26
- **Specs relacionadas:** `architecture/architecture.md`

## Contexto
Proyecto de aprendizaje que debe ejecutarse sin instalar infraestructura adicional.

## Decisión
Usaremos H2 en memoria con Spring Data JPA dentro de `adapter.out.persistence`.

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| PostgreSQL | Realista para producción. | Requiere instalación o Docker. |
| Persistencia en memoria (Map) | Cero dependencias. | No ejercita un adaptador real ni la restricción de unicidad del SKU. |

## Consecuencias
- Los datos se pierden al reiniciar (aceptable en este contexto).
- Cambiar a PostgreSQL solo afecta `adapter.out.persistence` y la configuración, gracias a ADR-0001.
- La consola web de H2 (`spring-boot-h2console`, en `/h2-console`) se habilita para inspeccionar los datos
  durante el desarrollo, en coherencia con el objetivo de ejecutar sin infraestructura. No forma parte del
  contrato ni de la verificación.
