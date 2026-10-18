<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# updateProduct

`PATCH /api/v1/products/{productId}`. Contrato: [catalog-openapi.yaml](../../../specs/api/catalog-openapi.yaml) · API: [api.md](../api.md#updateproduct) · Trazabilidad: [traceability.md](../../../specs/traceability.md)

| Elemento | Valor |
| :--- | :--- |
| Requerimiento | [RF-02](../../../specs/requirements/functional.md) |
| Historia | [US02 - Actualizar producto](../../../specs/user-stories/US02-actualizar-producto.md) |
| Caso de uso (`port.in`) | [`UpdateProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/UpdateProductUseCase.java) |
| Servicio | [`UpdateProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/UpdateProductService.java) |
| Command | [`UpdateProductCommand`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/command/UpdateProductCommand.java) |
| Result | [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| Request web | `UpdateProductRequest` (generado) |
| Respuesta | 200 · `ProductResponse` |
| Errores (ADR-0004) | 400 `VALIDATION_ERROR` · 404 `PRODUCT_NOT_FOUND` · 409 `INVALID_STATE_TRANSITION` · 500 `INTERNAL_ERROR` |
| Transición | — (el estado no cambia) |

## Historia

**Como** Administrador de catálogo, **quiero** modificar los datos de un producto, **para** completarlo o corregirlo.

| Criterio | Descripción |
| :--- | :--- |
| CA-01 | Dado un producto en `DRAFT` o `ACTIVE`, cuando actualizo nombre, descripción, precio o categoría con datos válidos, entonces los cambios se guardan y el estado no cambia. |
| CA-02 | Dado un producto en `DISCONTINUED`, cuando intento actualizarlo, entonces se rechaza porque el producto está descontinuado. |
| CA-03 | Dado cualquier producto, cuando intento cambiar su SKU, entonces el SKU no se modifica. |
| CA-04 | Dados datos inválidos (mismas reglas de US01 CA-04 y CA-05), cuando actualizo, entonces se rechaza y el producto queda sin cambios. |
| CA-05 | Dado un producto con precio o categoría informados, cuando intento dejarlos vacíos, entonces se rechaza y el producto conserva sus valores. |

## Flujo

1. `RejectExplicitNullsModule` rechaza un `null` explícito y `ProductWebMapper` rechaza un cuerpo sin campos con `InvalidRequestException` (ADR-0009): 400.
2. `ProductWebMapper` traduce `UpdateProductRequest` a `UpdateProductCommand`; un campo no enviado llega como `null`.
3. `UpdateProductService` carga el producto (`findById`, 404 si no existe) y construye `Price` si se envió (INV-05).
4. `Product.update(..., clock)` rechaza un producto `DISCONTINUED` (INV-08), valida textos (INV-04) y conserva los valores no enviados (INV-11). El SKU no se toca (INV-03).
5. `ProductPersistencePort.save` persiste y el controlador responde 200.

## Invariantes

| ID | Regla | Dónde se garantiza |
| :--- | :--- | :--- |
| INV-03 | El SKU nunca cambia. | `Product` (no expone método para cambiarlo) |
| INV-04 | Nombre de 3 a 100 caracteres; descripción de máx. 500 (medidos y guardados sin espacios al inicio y al final). | `Product` |
| INV-05 | Precio mayor a 0 y máximo 2 decimales, sin contar ceros finales (`10.500` válido, `10.505` no). | `Price` (al construirse) |
| INV-08 | Un producto descontinuado no se modifica. | `Product.update()` |
| INV-11 | Precio y categoría no pueden eliminarse una vez informados (un producto activo siempre está completo). | `Product.update()` (null = conservar el valor); el null explícito lo rechaza `adapter.in.web` con 400 antes de llegar al dominio (ADR-0009) |

Fuente: [domain-model.md §3](../../../specs/architecture/domain-model.md).

## Archivos por capa

| Capa | Clases |
| :--- | :--- |
| `adapter.in.web` | [`ProductController`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductController.java), [`ProductWebMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductWebMapper.java), [`GlobalExceptionHandler`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/GlobalExceptionHandler.java), [`RejectExplicitNullsModule`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/RejectExplicitNullsModule.java), [`InvalidRequestException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/InvalidRequestException.java) |
| `application.port.in` | [`UpdateProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/UpdateProductUseCase.java), [`UpdateProductCommand`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/command/UpdateProductCommand.java), [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| `application.service` | [`UpdateProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/UpdateProductService.java) |
| `application.port.out` | [`ProductPersistencePort`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/out/ProductPersistencePort.java) |
| `domain` | [`Product`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/Product.java), [`Price`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/Price.java), [`ProductValidationException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/ProductValidationException.java), [`InvalidStateTransitionException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/InvalidStateTransitionException.java), [`ProductNotFoundException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/ProductNotFoundException.java) |
| `adapter.out.persistence` | [`ProductPersistenceAdapter`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapter.java), [`ProductPersistenceMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceMapper.java) |

## Pruebas

| Criterio | Clase | Prueba |
| :--- | :--- | :--- |
| CA-01 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA01_validRequest_returns200AndStatusDoesNotChange` |
| CA-01 | [`US02UpdateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US02UpdateProductTest.java) | `US02_CA01_nameAndDescriptionWithSurroundingSpaces_areStoredStripped` |
| CA-01 | [`US02UpdateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US02UpdateProductTest.java) | `US02_CA01_onlySentFields_areModified` |
| CA-01 | [`US02UpdateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US02UpdateProductTest.java) | `US02_CA01_validData_isSavedAndStatusDoesNotChange` |
| CA-01 | [`UpdateProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/UpdateProductServiceTest.java) | `US02_CA01_validData_isSavedAndStatusDoesNotChange` |
| CA-02 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA02_discontinued_returns409InvalidStateTransition` |
| CA-02 | [`US02UpdateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US02UpdateProductTest.java) | `US02_CA02_discontinuedProduct_isRejected` |
| CA-02 | [`UpdateProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/UpdateProductServiceTest.java) | `US02_CA02_discontinued_isRejected` |
| CA-03 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA03_onlySkuInBody_returns400` |
| CA-03 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA03_skuInBody_isIgnored` |
| CA-03 | [`US02UpdateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US02UpdateProductTest.java) | `US02_CA03_update_neverChangesSku` |
| CA-03 | [`UpdateProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/UpdateProductServiceTest.java) | `US02_CA03_skuIsNeverChanged` |
| CA-04 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA04_categoryOutsideCatalog_returns400` |
| CA-04 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA04_invalidName_returns400` |
| CA-04 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA04_priceWithThreeDecimals_returns400` |
| CA-04 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA04_priceZeroOrNegative_returns400` |
| CA-04 | [`US02UpdateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US02UpdateProductTest.java) | `US02_CA04_invalidData_isRejectedAndProductIsUnchanged` |
| CA-04 | [`US02UpdateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US02UpdateProductTest.java) | `US02_CA04_invalidPrice_isRejected` |
| CA-04 | [`UpdateProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/UpdateProductServiceTest.java) | `US02_CA04_invalidPrice_isRejectedAndUnchanged` |
| CA-05 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA05_explicitNullOnly_returns400` |
| CA-05 | [`ProductControllerUpdateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerUpdateTest.java) | `US02_CA05_explicitNullPriceOrCategory_returns400` |
| CA-05 | [`US02UpdateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US02UpdateProductTest.java) | `US02_CA05_notSentPriceAndCategory_keepCurrentValues` |
| CA-05 | [`UpdateProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/UpdateProductServiceTest.java) | `US02_CA05_notSentPriceAndCategory_keepCurrentValues` |

23 pruebas `US02_CAyy` de esta operación. Las pruebas de dominio son JUnit puro y las de aplicación usan `InMemoryProductPersistence`, sin Spring ([pruebas.md](../pruebas.md)).
