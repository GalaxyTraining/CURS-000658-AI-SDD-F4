# US05 - Consultar y listar productos

**Como** integrante del Área de ventas,
**quiero** consultar y filtrar productos,
**para** saber qué productos puedo ofrecer.

Referencias: RF-05, `domain/product-lifecycle.md`, `domain/catalogs.md`

## Criterios de aceptación
- **CA-01:** Dado un identificador existente, cuando consulto el producto,
  entonces obtengo todos sus datos y su estado.
- **CA-02:** Dado un identificador inexistente, cuando consulto,
  entonces se informa que el producto no existe.
- **CA-03:** Dado un filtro por estado y/o categoría, cuando listo,
  entonces solo obtengo los productos que cumplen el filtro.
- **CA-04:** Dado un listado con más resultados que el tamaño de página,
  cuando listo, entonces los resultados vienen paginados.