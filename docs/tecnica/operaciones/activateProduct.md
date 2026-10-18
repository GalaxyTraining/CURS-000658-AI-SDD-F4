<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# activateProduct

`POST /api/v1/products/{productId}/activate`. Contrato: [catalog-openapi.yaml](../../../specs/api/catalog-openapi.yaml) · API: [api.md](../api.md#activateproduct) · Trazabilidad: [traceability.md](../../../specs/traceability.md)

| Elemento | Valor |
| :--- | :--- |
| Requerimiento | [RF-03](../../../specs/requirements/functional.md) |
| Historia | [US03 - Activar producto](../../../specs/user-stories/US03-activar-producto.md) |
| Caso de uso (`port.in`) | [`ActivateProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/ActivateProductUseCase.java) |
| Servicio | [`ActivateProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/ActivateProductService.java) |
| Command | [`ActivateProductCommand`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/command/ActivateProductCommand.java) |
| Result | [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| Request web | — (sin cuerpo) |
| Respuesta | 200 · `ProductResponse` |
| Errores (ADR-0004) | 400 `VALIDATION_ERROR` · 404 `PRODUCT_NOT_FOUND` · 409 `INVALID_STATE_TRANSITION` · 422 `PRODUCT_INCOMPLETE` · 500 `INTERNAL_ERROR` |
| Transición | `DRAFT -> ACTIVE` |

## Historia

**Como** Administrador de catálogo, **quiero** activar un producto completo, **para** que esté disponible para la venta.

| Criterio | Descripción |
| :--- | :--- |
| CA-01 | Dado un producto en `DRAFT` con precio y categoría, cuando lo activo, entonces pasa a `ACTIVE`. |
| CA-02 | Dado un producto en `DRAFT` sin precio o sin categoría, cuando lo activo, entonces se rechaza indicando qué dato falta. |
| CA-03 | Dado un producto en `ACTIVE` o `DISCONTINUED`, cuando intento activarlo, entonces se rechaza por transición de estado inválida. |

## Flujo

1. `ProductController.activateProduct` crea `ActivateProductCommand` con el `productId`.
2. `ActivateProductService` carga el producto (`findById`, 404 si no existe).
3. `Product.activate(clock)` rechaza un estado distinto de `DRAFT` (INV-06, 409) y exige precio y categoría (INV-07, 422 con los datos faltantes en `details`).
4. `ProductPersistencePort.save` persiste y el controlador responde 200 con `status: ACTIVE`.

El diagrama de secuencia de esta operación está en [architecture.md §6](../../../specs/architecture/architecture.md).

## Invariantes

| ID | Regla | Dónde se garantiza |
| :--- | :--- | :--- |
| INV-06 | Solo se permiten las transiciones de `product-lifecycle.md`. | `Product.activate()` / `discontinue()` |
| INV-07 | Para activar se requiere precio y categoría. | `Product.activate()` |

Fuente: [domain-model.md §3](../../../specs/architecture/domain-model.md).

## Archivos por capa

| Capa | Clases |
| :--- | :--- |
| `adapter.in.web` | [`ProductController`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductController.java), [`ProductWebMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductWebMapper.java), [`GlobalExceptionHandler`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/GlobalExceptionHandler.java) |
| `application.port.in` | [`ActivateProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/ActivateProductUseCase.java), [`ActivateProductCommand`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/command/ActivateProductCommand.java), [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| `application.service` | [`ActivateProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/ActivateProductService.java) |
| `application.port.out` | [`ProductPersistencePort`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/out/ProductPersistencePort.java) |
| `domain` | [`Product`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/Product.java), [`InvalidStateTransitionException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/InvalidStateTransitionException.java), [`ProductIncompleteException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/ProductIncompleteException.java), [`ProductNotFoundException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/ProductNotFoundException.java) |
| `adapter.out.persistence` | [`ProductPersistenceAdapter`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapter.java), [`ProductPersistenceMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceMapper.java) |

## Pruebas

| Criterio | Clase | Prueba |
| :--- | :--- | :--- |
| CA-01 | [`ActivateProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/ActivateProductServiceTest.java) | `US03_CA01_completeDraft_becomesActiveAndIsSaved` |
| CA-01 | [`ProductControllerActivateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerActivateTest.java) | `US03_CA01_completeDraft_returns200Active` |
| CA-01 | [`US03ActivateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US03ActivateProductTest.java) | `US03_CA01_draftWithPriceAndCategory_becomesActive` |
| CA-02 | [`ActivateProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/ActivateProductServiceTest.java) | `US03_CA02_withoutPriceNorCategory_isRejectedIndicatingMissingAndUnchanged` |
| CA-02 | [`ProductControllerActivateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerActivateTest.java) | `US03_CA02_incomplete_returns422WithMissingFields` |
| CA-02 | [`US03ActivateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US03ActivateProductTest.java) | `US03_CA02_activateWithoutCategory_isRejectedIndicatingCategory` |
| CA-02 | [`US03ActivateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US03ActivateProductTest.java) | `US03_CA02_activateWithoutPriceNorCategory_isRejectedIndicatingBoth` |
| CA-02 | [`US03ActivateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US03ActivateProductTest.java) | `US03_CA02_activateWithoutPrice_isRejectedIndicatingPrice` |
| CA-03 | [`ActivateProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/ActivateProductServiceTest.java) | `US03_CA03_activeOrDiscontinued_isRejectedAsInvalidTransition` |
| CA-03 | [`ProductControllerActivateTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerActivateTest.java) | `US03_CA03_invalidTransition_returns409InvalidStateTransition` |
| CA-03 | [`US03ActivateProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US03ActivateProductTest.java) | `US03_CA03_activeOrDiscontinuedProduct_isRejectedAsInvalidTransition` |

11 pruebas `US03_CAyy` de esta operación. Las pruebas de dominio son JUnit puro y las de aplicación usan `InMemoryProductPersistence`, sin Spring ([pruebas.md](../pruebas.md)).
