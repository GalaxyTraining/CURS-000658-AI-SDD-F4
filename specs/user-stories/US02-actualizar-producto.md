# US02 - Actualizar producto

**Como** Administrador de catálogo,
**quiero** modificar los datos de un producto,
**para** completarlo o corregirlo.

Referencias: RF-02, `domain/product-lifecycle.md`

## Criterios de aceptación
- **CA-01:** Dado un producto en `DRAFT` o `ACTIVE`, cuando actualizo nombre, descripción, precio o categoría
  con datos válidos, entonces los cambios se guardan y el estado no cambia.
- **CA-02:** Dado un producto en `DISCONTINUED`, cuando intento actualizarlo,
  entonces se rechaza porque el producto está descontinuado.
- **CA-03:** Dado cualquier producto, cuando intento cambiar su SKU,
  entonces el SKU no se modifica.
- **CA-04:** Dados datos inválidos (mismas reglas de US01 CA-04 y CA-05), cuando actualizo,
  entonces se rechaza y el producto queda sin cambios.
- **CA-05:** Dado un producto con precio o categoría informados, cuando intento dejarlos vacíos,
  entonces se rechaza y el producto conserva sus valores.