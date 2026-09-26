# US03 - Activar producto

**Como** Administrador de catálogo,
**quiero** activar un producto completo,
**para** que esté disponible para la venta.

Referencias: RF-03, `domain/product-lifecycle.md`

## Criterios de aceptación
- **CA-01:** Dado un producto en `DRAFT` con precio y categoría, cuando lo activo,
  entonces pasa a `ACTIVE`.
- **CA-02:** Dado un producto en `DRAFT` sin precio o sin categoría, cuando lo activo,
  entonces se rechaza indicando qué dato falta.
- **CA-03:** Dado un producto en `ACTIVE` o `DISCONTINUED`, cuando intento activarlo,
  entonces se rechaza por transición de estado inválida.