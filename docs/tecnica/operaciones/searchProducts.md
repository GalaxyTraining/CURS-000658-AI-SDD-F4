<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# searchProducts

`GET /api/v1/products`. Contrato: [catalog-openapi.yaml](../../../specs/api/catalog-openapi.yaml) · API: [api.md](../api.md#searchproducts) · Trazabilidad: [traceability.md](../../../specs/traceability.md)

| Elemento | Valor |
| :--- | :--- |
| Requerimiento | [RF-05](../../../specs/requirements/functional.md) |
| Historia | [US05 - Consultar y listar productos](../../../specs/user-stories/US05-consultar-productos.md) |
| Caso de uso (`port.in`) | [`SearchProductsUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/SearchProductsUseCase.java) |
| Servicio | [`SearchProductsService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsService.java) |
| Query | [`SearchProductsQuery`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/query/SearchProductsQuery.java) |
| Result | [`ProductPageResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductPageResult.java) |
| Request web | — (sin cuerpo) |
| Respuesta | 200 · `ProductPageResponse` |
| Errores (ADR-0004) | 400 `VALIDATION_ERROR` · 500 `INTERNAL_ERROR` |
| Transición | — |

## Historia

**Como** integrante del Área de ventas, **quiero** consultar y filtrar productos, **para** saber qué productos puedo ofrecer.

Esta operación cubre CA-03, CA-04 de US05; el resto lo cubre [getProduct](getProduct.md).

| Criterio | Descripción |
| :--- | :--- |
| CA-03 | Dado un filtro por estado y/o categoría, cuando listo, entonces solo obtengo los productos que cumplen el filtro. |
| CA-04 | Dado un listado con más resultados que el tamaño de página, cuando listo, entonces los resultados vienen paginados. |

## Flujo

1. `ProductWebMapper` crea `SearchProductsQuery` con los filtros `status` y `category` (enums del dominio), `page` (por defecto 0) y `size` (por defecto 20, de 1 a 100). Un valor fuera de rango o de catálogo responde 400.
2. `SearchProductsService` llama a `ProductPersistencePort.search`; un filtro `null` significa "sin filtro".
3. `ProductPersistenceAdapter` pagina y ordena por `createdAt` ascendente y luego por `id` (RF-05).
4. El servicio devuelve `ProductPageResult` y el controlador responde 200 con `ProductPageResponse`.

## Invariantes

Ninguna ([traceability.md](../../../specs/traceability.md)). El orden del listado viene de RF-05.

## Archivos por capa

| Capa | Clases |
| :--- | :--- |
| `adapter.in.web` | [`ProductController`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductController.java), [`ProductWebMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductWebMapper.java), [`GlobalExceptionHandler`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/GlobalExceptionHandler.java) |
| `application.port.in` | [`SearchProductsUseCase`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/usecase/SearchProductsUseCase.java), [`SearchProductsQuery`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/query/SearchProductsQuery.java), [`ProductPageResult`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/in/result/ProductPageResult.java) |
| `application.service` | [`SearchProductsService`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsService.java) |
| `application.port.out` | [`ProductPersistencePort`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/out/ProductPersistencePort.java), [`ProductPage`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/application/port/out/ProductPage.java) |
| `domain` | [`ProductStatus`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/ProductStatus.java), [`ProductCategory`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/ProductCategory.java) |
| `adapter.out.persistence` | [`ProductPersistenceAdapter`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapter.java), [`SpringDataProductJpa`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/SpringDataProductJpa.java), [`ProductPersistenceMapper`](../../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceMapper.java) |

## Pruebas

| Criterio | Clase | Prueba |
| :--- | :--- | :--- |
| CA-03 | [`ProductControllerSearchTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerSearchTest.java) | `US05_CA03_filters_arePassedToQueryAndReturn200` |
| CA-03 | [`ProductControllerSearchTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerSearchTest.java) | `US05_CA03_unknownCategory_returns400ValidationError` |
| CA-03 | [`ProductControllerSearchTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerSearchTest.java) | `US05_CA03_unknownStatus_returns400ValidationError` |
| CA-03 | [`ProductPersistenceAdapterTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapterTest.java) | `US05_CA03_searchByStatusAndCategory_returnsOnlyMatches` |
| CA-03 | [`SearchProductsServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsServiceTest.java) | `US05_CA03_filterByCategory_returnsOnlyMatching` |
| CA-03 | [`SearchProductsServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsServiceTest.java) | `US05_CA03_filterByStatusAndCategory_returnsOnlyMatching` |
| CA-03 | [`SearchProductsServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsServiceTest.java) | `US05_CA03_filterByStatus_returnsOnlyMatching` |
| CA-03 | [`SearchProductsServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsServiceTest.java) | `US05_CA03_withoutFilters_returnsAllOrderedByCreation` |
| CA-04 | [`ProductControllerSearchTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerSearchTest.java) | `US05_CA04_defaultPageAndSize_areApplied` |
| CA-04 | [`ProductControllerSearchTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerSearchTest.java) | `US05_CA04_negativePage_returns400ValidationError` |
| CA-04 | [`ProductControllerSearchTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerSearchTest.java) | `US05_CA04_pageMetadata_isReturned` |
| CA-04 | [`ProductControllerSearchTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/ProductControllerSearchTest.java) | `US05_CA04_sizeOutOfRange_returns400ValidationError` |
| CA-04 | [`ProductPersistenceAdapterTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapterTest.java) | `US05_CA04_search_isPagedAndOrderedByCreatedAtThenId` |
| CA-04 | [`SearchProductsServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsServiceTest.java) | `US05_CA04_moreResultsThanPageSize_returnsPaginated` |
| CA-04 | [`SearchProductsServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsServiceTest.java) | `US05_CA04_noMatches_hasZeroPages` |
| CA-04 | [`SearchProductsServiceTest`](../../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsServiceTest.java) | `US05_CA04_pageBeyondLast_isEmpty` |

16 pruebas `US05_CAyy` de esta operación. Las pruebas de dominio son JUnit puro y las de aplicación usan `InMemoryProductPersistence`, sin Spring ([pruebas.md](../pruebas.md)).
