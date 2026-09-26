# US01 - Registrar producto

**Como** Administrador de catálogo,
**quiero** registrar un producto nuevo,
**para** empezar a prepararlo antes de ponerlo a la venta.

Referencias: RF-01, `domain/product-lifecycle.md`, `domain/catalogs.md`

## Criterios de aceptación
- **CA-01:** Dado un SKU con formato válido y un nombre válido, cuando registro el producto,
  entonces se crea en estado `DRAFT`.
- **CA-02:** Dado un SKU que ya existe en el catálogo, cuando registro el producto,
  entonces se rechaza indicando que el SKU está duplicado.
- **CA-03:** Dado un SKU con formato distinto a `AAA-9999` (ej. `ele-01`), cuando registro el producto,
  entonces se rechaza indicando el formato esperado.
- **CA-04:** Dado un precio menor o igual a 0 o con más de 2 decimales, cuando registro el producto,
  entonces se rechaza.
- **CA-05:** Dada una categoría que no está en el catálogo, cuando registro el producto,
  entonces se rechaza.
- **CA-06:** Dado que no informo precio ni categoría, cuando registro el producto,
  entonces se crea igualmente en `DRAFT`.