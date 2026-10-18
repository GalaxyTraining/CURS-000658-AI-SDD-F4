<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# registerProduct

`POST /api/v1/products`. Contrato: [catalog-openapi.yaml](../../../specs/api/catalog-openapi.yaml) · API: [api.md](../api.md#registerproduct) · Trazabilidad: [traceability.md](../../../specs/traceability.md)

| Elemento | Valor |
| :--- | :--- |
| Requerimiento | [RF-01](../../../specs/requirements/functional.md) |
| Historia | [US01 - Registrar producto](../../../specs/user-stories/US01-registrar-producto.md) |
| Caso de uso (`port.in`) | [`RegisterProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/RegisterProductUseCase.java) |
| Servicio | [`RegisterProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/RegisterProductService.java) |
| Command | [`RegisterProductCommand`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/command/RegisterProductCommand.java) |
| Result | [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| Request web | `RegisterProductRequest` (generado) |
| Respuesta | 201 + `Location` · `ProductResponse` |
| Errores (ADR-0004) | 400 `VALIDATION_ERROR` · 409 `DUPLICATE_SKU` · 500 `INTERNAL_ERROR` |
| Transición | `- -> DRAFT` |

## Historia

**Como** Administrador de catálogo, **quiero** registrar un producto nuevo, **para** empezar a prepararlo antes de ponerlo a la venta.

| Criterio | Descripción |
| :--- | :--- |
| CA-01 | Dado un SKU con formato válido y un nombre válido, cuando registro el producto, entonces se crea en estado `DRAFT`. |
| CA-02 | Dado un SKU que ya existe en el catálogo, cuando registro el producto, entonces se rechaza indicando que el SKU está duplicado. |
| CA-03 | Dado un SKU con formato distinto a `AAA-9999` (ej. `ele-01`), cuando registro el producto, entonces se rechaza indicando el formato esperado. |
| CA-04 | Dado un precio menor o igual a 0 o con más de 2 decimales, cuando registro el producto, entonces se rechaza. |
| CA-05 | Dada una categoría que no está en el catálogo, cuando registro el producto, entonces se rechaza. |
| CA-06 | Dado que no informo precio ni categoría, cuando registro el producto, entonces se crea igualmente en `DRAFT`. |

## Flujo

1. `ProductController.registerProduct` traduce `RegisterProductRequest` a `RegisterProductCommand` con `ProductWebMapper`.
2. `RegisterProductService` construye `Sku` (INV-01), comprueba `existsBySku` (INV-02, 409) y construye `Price` si se envió (INV-05).
3. `Product.register(..., clock)` valida nombre y descripción (INV-04) y crea el producto en `DRAFT`.
4. `ProductPersistencePort.save` persiste; una violación de la restricción única del SKU se traduce a `DuplicateSkuException` (INV-02).
5. El controlador responde 201 con `Location: /api/v1/products/{id}` y el `ProductResponse`.

El diagrama de secuencia de esta operación está en [diagramas.md](../diagramas.md#registerproduct-us01).

## Invariantes

| ID | Regla | Dónde se garantiza |
| :--- | :--- | :--- |
| INV-01 | El SKU tiene formato `AAA-9999`. | `Sku` (al construirse) |
| INV-02 | El SKU es único en el catálogo. | `RegisterProductService` (application), consultando `ProductPersistencePort.existsBySku`; respaldada por una restricción única en BD que el adaptador de persistencia traduce a `DuplicateSkuException` |
| INV-04 | Nombre de 3 a 100 caracteres; descripción de máx. 500 (medidos y guardados sin espacios al inicio y al final). | `Product` |
| INV-05 | Precio mayor a 0 y máximo 2 decimales, sin contar ceros finales (`10.500` válido, `10.505` no). | `Price` (al construirse) |

Fuente: [domain-model.md §3](../../../specs/architecture/domain-model.md).

## Archivos por capa

| Capa | Clases |
| :--- | :--- |
| `adapter.in.web` | [`ProductController`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductController.java), [`ProductWebMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductWebMapper.java), [`GlobalExceptionHandler`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/GlobalExceptionHandler.java) |
| `application.port.in` | [`RegisterProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/RegisterProductUseCase.java), [`RegisterProductCommand`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/command/RegisterProductCommand.java), [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| `application.service` | [`RegisterProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/RegisterProductService.java) |
| `application.port.out` | [`ProductPersistencePort`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/out/ProductPersistencePort.java) |
| `domain` | [`Product`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/Product.java), [`Sku`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/Sku.java), [`Price`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/Price.java), [`ProductValidationException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/ProductValidationException.java), [`DuplicateSkuException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/DuplicateSkuException.java) |
| `adapter.out.persistence` | [`ProductPersistenceAdapter`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapter.java), [`ProductJpaEntity`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductJpaEntity.java), [`ProductPersistenceMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceMapper.java), [`SpringDataProductJpa`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/SpringDataProductJpa.java) |

## Pruebas

| Criterio | Clase | Prueba |
| :--- | :--- | :--- |
| CA-01 | [`ProductControllerRegisterTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerRegisterTest.java) | `US01_CA01_validRequest_returns201WithLocationAndBody` |
| CA-01 | [`RegisterProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/RegisterProductServiceTest.java) | `US01_CA01_validCommand_isSavedAsDraftWithClockTime` |
| CA-01 | [`US01RegisterProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java) | `US01_CA01_descriptionLongerThanFiveHundredCharacters_isRejected` |
| CA-01 | [`US01RegisterProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java) | `US01_CA01_nameAndDescriptionWithSurroundingSpaces_areStoredStripped` |
| CA-01 | [`US01RegisterProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java) | `US01_CA01_nameLongerThanHundredCharacters_isRejected` |
| CA-01 | [`US01RegisterProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java) | `US01_CA01_nameShorterThanThreeCharactersWithoutSpaces_isRejected` |
| CA-01 | [`US01RegisterProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java) | `US01_CA01_validSkuAndName_createsDraftProduct` |
| CA-02 | [`ProductControllerRegisterTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerRegisterTest.java) | `US01_CA02_duplicateSku_returns409DuplicateSku` |
| CA-02 | [`ProductPersistenceAdapterTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapterTest.java) | `US01_CA02_duplicateSkuAtDatabase_isTranslatedToDuplicateSku` |
| CA-02 | [`RegisterProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/RegisterProductServiceTest.java) | `US01_CA02_existingSku_isRejectedAsDuplicate` |
| CA-03 | [`ProductControllerRegisterTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerRegisterTest.java) | `US01_CA03_invalidSkuFormat_returns400ValidationError` |
| CA-03 | [`US01RegisterProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java) | `US01_CA03_skuWithInvalidFormat_isRejectedWithExpectedFormat` |
| CA-04 | [`ProductControllerRegisterTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerRegisterTest.java) | `US01_CA04_priceZeroOrNegative_returns400ValidationError` |
| CA-04 | [`US01RegisterProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java) | `US01_CA04_priceWithTrailingZeros_isAccepted` |
| CA-04 | [`US01RegisterProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java) | `US01_CA04_priceZeroNegativeOrWithMoreThanTwoDecimals_isRejected` |
| CA-05 | [`ProductControllerRegisterTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerRegisterTest.java) | `US01_CA05_categoryOutsideCatalog_returns400ValidationError` |
| CA-06 | [`ProductControllerRegisterTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerRegisterTest.java) | `US01_CA06_withoutPriceNorCategory_returns201` |
| CA-06 | [`RegisterProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/RegisterProductServiceTest.java) | `US01_CA06_withoutPriceNorCategory_isSavedAsDraft` |
| CA-06 | [`US01RegisterProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java) | `US01_CA06_withoutPriceNorCategory_createsDraftProduct` |

19 pruebas `US01_CAyy` de esta operación. Las pruebas de dominio son JUnit puro y las de aplicación usan `InMemoryProductPersistence`, sin Spring ([pruebas.md](../pruebas.md)).
