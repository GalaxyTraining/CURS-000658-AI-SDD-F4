# US04 - Descontinuar producto

**Como** Administrador de catálogo,
**quiero** descontinuar un producto,
**para** que deje de ofrecerse sin perder su historial.

Referencias: RF-04, `domain/product-lifecycle.md`

## Criterios de aceptación
- **CA-01:** Dado un producto en `DRAFT` o `ACTIVE` y un motivo de al menos 10 caracteres,
  cuando lo descontinúo, entonces pasa a `DISCONTINUED` y el motivo queda registrado.
- **CA-02:** Dado un motivo vacío o de menos de 10 caracteres, cuando descontinúo,
  entonces se rechaza.
- **CA-03:** Dado un producto en `DISCONTINUED`, cuando intento descontinuarlo otra vez,
  entonces se rechaza por transición de estado inválida.
- **CA-04:** Dado un producto descontinuado, cuando lo consulto,
  entonces sigue existiendo (nunca se elimina).