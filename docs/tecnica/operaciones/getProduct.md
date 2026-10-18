<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# getProduct

`GET /api/v1/products/{productId}`. Contrato: [catalog-openapi.yaml](../../../specs/api/catalog-openapi.yaml) · API: [api.md](../api.md#getproduct) · Trazabilidad: [traceability.md](../../../specs/traceability.md)

| Elemento | Valor |
| :--- | :--- |
| Requerimiento | [RF-05](../../../specs/requirements/functional.md) |
| Historia | [US05 - Consultar y listar productos](../../../specs/user-stories/US05-consultar-productos.md) |
| Caso de uso (`port.in`) | [`GetProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/GetProductUseCase.java) |
| Servicio | [`GetProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/GetProductService.java) |
| Query | [`GetProductQuery`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/query/GetProductQuery.java) |
| Result | [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| Request web | — (sin cuerpo) |
| Respuesta | 200 · `ProductResponse` |
| Errores (ADR-0004) | 400 `VALIDATION_ERROR` · 404 `PRODUCT_NOT_FOUND` · 500 `INTERNAL_ERROR` |
| Transición | — |

## Historia

**Como** integrante del Área de ventas, **quiero** consultar y filtrar productos, **para** saber qué productos puedo ofrecer.

Esta operación cubre CA-01, CA-02 de US05; el resto lo cubre [searchProducts](searchProducts.md).

| Criterio | Descripción |
| :--- | :--- |
| CA-01 | Dado un identificador existente, cuando consulto el producto, entonces obtengo todos sus datos y su estado. |
| CA-02 | Dado un identificador inexistente, cuando consulto, entonces se informa que el producto no existe. |

## Flujo

1. `ProductController.getProduct` crea `GetProductQuery`; un `productId` que no es UUID responde 400.
2. `GetProductService` busca con `findById` y lanza `ProductNotFoundException` (404) si no existe.
3. Devuelve `ProductResult`, también para productos `DISCONTINUED` (INV-10), y el controlador responde 200.

## Invariantes

| ID | Regla | Dónde se garantiza |
| :--- | :--- | :--- |
| INV-10 | Un producto nunca se elimina. | No existe operación de borrado |

Fuente: [domain-model.md §3](../../../specs/architecture/domain-model.md).

## Archivos por capa

| Capa | Clases |
| :--- | :--- |
| `adapter.in.web` | [`ProductController`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductController.java), [`ProductWebMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductWebMapper.java), [`GlobalExceptionHandler`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/GlobalExceptionHandler.java) |
| `application.port.in` | [`GetProductUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/GetProductUseCase.java), [`GetProductQuery`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/query/GetProductQuery.java), [`ProductResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductResult.java) |
| `application.service` | [`GetProductService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/GetProductService.java) |
| `application.port.out` | [`ProductPersistencePort`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/out/ProductPersistencePort.java) |
| `domain` | [`Product`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/Product.java), [`ProductId`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/ProductId.java), [`ProductNotFoundException`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/exception/ProductNotFoundException.java) |
| `adapter.out.persistence` | [`ProductPersistenceAdapter`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapter.java), [`ProductPersistenceMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceMapper.java), [`SpringDataProductJpa`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/SpringDataProductJpa.java) |

## Pruebas

| Criterio | Clase | Prueba |
| :--- | :--- | :--- |
| CA-01 | [`GetProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/GetProductServiceTest.java) | `US05_CA01_discontinuedProduct_isStillReturned` |
| CA-01 | [`GetProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/GetProductServiceTest.java) | `US05_CA01_existingId_returnsAllDataAndStatus` |
| CA-01 | [`ProductControllerGetTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerGetTest.java) | `US05_CA01_existingId_returns200WithAllDataAndStatus` |
| CA-02 | [`GetProductServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/GetProductServiceTest.java) | `US05_CA02_unknownId_isRejectedAsNotFound` |
| CA-02 | [`ProductControllerGetTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerGetTest.java) | `US05_CA02_malformedId_returns400ValidationError` |
| CA-02 | [`ProductControllerGetTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerGetTest.java) | `US05_CA02_unknownId_returns404ProductNotFound` |

6 pruebas `US05_CAyy` de esta operación. Las pruebas de dominio son JUnit puro y las de aplicación usan `InMemoryProductPersistence`, sin Spring ([pruebas.md](../pruebas.md)).
