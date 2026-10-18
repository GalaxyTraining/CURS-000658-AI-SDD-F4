<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# discontinueProduct

`POST /api/v1/products/{productId}/discontinue`. Contrato: [catalog-openapi.yaml](../../../specs/api/catalog-openapi.yaml) · API: [api.md](../api.md#discontinueproduct) · Trazabilidad: [traceability.md](../../../specs/traceability.md)

| Elemento | Valor |
| :--- | :--- |
| Requerimiento | [RF-04](../../../specs/requirements/functional.md) |
| Historia | [US04 - Descontinuar producto](../../../specs/user-stories/US04-descontinuar-producto.md) |
| Caso de uso (`port.in`) | [`DiscontinueProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/DiscontinueProductUseCase.java) |
| Servicio | [`DiscontinueProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/DiscontinueProductService.java) |
| Command | [`DiscontinueProductCommand`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/command/DiscontinueProductCommand.java) |
| Result | [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| Request web | `DiscontinueProductRequest` (generado) |
| Respuesta | 200 · `ProductResponse` |
| Errores (ADR-0004) | 400 `VALIDATION_ERROR` · 404 `PRODUCT_NOT_FOUND` · 409 `INVALID_STATE_TRANSITION` · 500 `INTERNAL_ERROR` |
| Transición | `DRAFT|ACTIVE -> DISCONTINUED` |

## Historia

**Como** Administrador de catálogo, **quiero** descontinuar un producto, **para** que deje de ofrecerse sin perder su historial.

| Criterio | Descripción |
| :--- | :--- |
| CA-01 | Dado un producto en `DRAFT` o `ACTIVE` y un motivo de al menos 10 caracteres, cuando lo descontinúo, entonces pasa a `DISCONTINUED` y el motivo queda registrado. |
| CA-02 | Dado un motivo vacío o de menos de 10 caracteres, cuando descontinúo, entonces se rechaza. |
| CA-03 | Dado un producto en `DISCONTINUED`, cuando intento descontinuarlo otra vez, entonces se rechaza por transición de estado inválida. |
| CA-04 | Dado un producto descontinuado, cuando lo consulto, entonces sigue existiendo (nunca se elimina). |

## Flujo

1. `ProductWebMapper` traduce `DiscontinueProductRequest` a `DiscontinueProductCommand`; el contrato exige `reason` (10 a 500 caracteres).
2. `DiscontinueProductService` carga el producto (`findById`, 404 si no existe).
3. `Product.discontinue(reason, clock)` rechaza un producto ya `DISCONTINUED` (INV-06, 409) y valida el motivo recortado (INV-09, 400).
4. `ProductPersistencePort.save` persiste: el producto no se elimina (INV-10) y se sigue consultando con `getProduct`.

El diagrama de secuencia de esta operación está en [diagramas.md](../diagramas.md#discontinueproduct-us04).

## Invariantes

| ID | Regla | Dónde se garantiza |
| :--- | :--- | :--- |
| INV-06 | Solo se permiten las transiciones de `product-lifecycle.md`. | `Product.activate()` / `discontinue()` |
| INV-09 | Descontinuar exige motivo de 10 a 500 caracteres (medidos y guardados sin espacios al inicio y al final). | `Product.discontinue()` |
| INV-10 | Un producto nunca se elimina. | No existe operación de borrado |

Fuente: [domain-model.md §3](../../../specs/architecture/domain-model.md).

## Archivos por capa

| Capa | Clases |
| :--- | :--- |
| `adapter.in.web` | [`ProductController`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductController.java), [`ProductWebMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductWebMapper.java), [`GlobalExceptionHandler`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/GlobalExceptionHandler.java) |
| `application.port.in` | [`DiscontinueProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/DiscontinueProductUseCase.java), [`DiscontinueProductCommand`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/command/DiscontinueProductCommand.java), [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| `application.service` | [`DiscontinueProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/DiscontinueProductService.java) |
| `application.port.out` | [`ProductPersistencePort`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/out/ProductPersistencePort.java) |
| `domain` | [`Product`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/Product.java), [`ProductValidationException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/ProductValidationException.java), [`InvalidStateTransitionException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/InvalidStateTransitionException.java), [`ProductNotFoundException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/ProductNotFoundException.java) |
| `adapter.out.persistence` | [`ProductPersistenceAdapter`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapter.java), [`ProductPersistenceMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceMapper.java) |

## Pruebas

| Criterio | Clase | Prueba |
| :--- | :--- | :--- |
| CA-01 | [`DiscontinueProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/DiscontinueProductServiceTest.java) | `US04_CA01_draftOrActive_becomesDiscontinuedWithReason` |
| CA-01 | [`ProductControllerDiscontinueTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerDiscontinueTest.java) | `US04_CA01_validReason_returns200DiscontinuedWithReason` |
| CA-01 | [`US04DiscontinueProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US04DiscontinueProductTest.java) | `US04_CA01_draftOrActiveWithValidReason_becomesDiscontinued` |
| CA-01 | [`US04DiscontinueProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US04DiscontinueProductTest.java) | `US04_CA01_reasonWithSurroundingSpaces_isStoredStripped` |
| CA-02 | [`DiscontinueProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/DiscontinueProductServiceTest.java) | `US04_CA02_blankOrShortReason_isRejectedAndUnchanged` |
| CA-02 | [`ProductControllerDiscontinueTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerDiscontinueTest.java) | `US04_CA02_emptyOrShortReason_returns400` |
| CA-02 | [`ProductControllerDiscontinueTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerDiscontinueTest.java) | `US04_CA02_missingOrNullReason_returns400` |
| CA-02 | [`ProductControllerDiscontinueTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerDiscontinueTest.java) | `US04_CA02_reasonLongerThan500_returns400` |
| CA-02 | [`US04DiscontinueProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US04DiscontinueProductTest.java) | `US04_CA02_emptyOrShorterThanTenCharactersReason_isRejected` |
| CA-02 | [`US04DiscontinueProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US04DiscontinueProductTest.java) | `US04_CA02_reasonLongerThanFiveHundredCharacters_isRejected` |
| CA-03 | [`DiscontinueProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/DiscontinueProductServiceTest.java) | `US04_CA03_alreadyDiscontinued_isRejectedAsInvalidTransition` |
| CA-03 | [`ProductControllerDiscontinueTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerDiscontinueTest.java) | `US04_CA03_alreadyDiscontinued_returns409InvalidStateTransition` |
| CA-03 | [`US04DiscontinueProductTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US04DiscontinueProductTest.java) | `US04_CA03_discontinuedProduct_isRejectedAsInvalidTransition` |
| CA-04 | [`DiscontinueProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/DiscontinueProductServiceTest.java) | `US04_CA04_discontinued_isStillReturnedByGetProduct` |

14 pruebas `US04_CAyy` de esta operación. Las pruebas de dominio son JUnit puro y las de aplicación usan `InMemoryProductPersistence`, sin Spring ([pruebas.md](../pruebas.md)).
