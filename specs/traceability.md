# Matriz de Trazabilidad

| operationId | Caso de uso (port.in) | Servicio | RF / US | Invariantes | Pruebas mínimas |
| :--- | :--- | :--- | :--- | :--- | :--- |
| registerProduct | RegisterProductUseCase | RegisterProductService | RF-01 / US01 | INV-01, INV-02, INV-04, INV-05 | US01_CA01 a US01_CA06 |
| updateProduct | UpdateProductUseCase | UpdateProductService | RF-02 / US02 | INV-03, INV-04, INV-05, INV-08, INV-11 | US02_CA01 a US02_CA05 |
| activateProduct | ActivateProductUseCase | ActivateProductService | RF-03 / US03 | INV-06, INV-07 | US03_CA01 a US03_CA03 |
| discontinueProduct | DiscontinueProductUseCase | DiscontinueProductService | RF-04 / US04 | INV-06, INV-09, INV-10 | US04_CA01 a US04_CA04 |
| getProduct | GetProductUseCase | GetProductService | RF-05 / US05 | INV-10 | US05_CA01, US05_CA02 |
| searchProducts | SearchProductsUseCase | SearchProductsService | RF-05 / US05 | — | US05_CA03, US05_CA04 |

## Clases de prueba por operación

Dónde están las pruebas `USxx_CAyy_...` de cada operación, por capa (`src/test/java/.../catalog/`).
Las pruebas de dominio son JUnit puro; las de aplicación usan `InMemoryProductPersistence` sin Spring.

| operationId | Dominio (`domain.model`) | Aplicación (`application.service`) | Web (`adapter.in.web`) | Persistencia (`adapter.out.persistence`) |
| :--- | :--- | :--- | :--- | :--- |
| registerProduct | US01RegisterProductTest | RegisterProductServiceTest | ProductControllerRegisterTest | ProductPersistenceAdapterTest (US01_CA02) |
| updateProduct | US02UpdateProductTest | UpdateProductServiceTest | ProductControllerUpdateTest | — |
| activateProduct | US03ActivateProductTest | ActivateProductServiceTest | ProductControllerActivateTest | — |
| discontinueProduct | US04DiscontinueProductTest | DiscontinueProductServiceTest | ProductControllerDiscontinueTest | — |
| getProduct | — | GetProductServiceTest | ProductControllerGetTest | — |
| searchProducts | — | SearchProductsServiceTest | ProductControllerSearchTest | ProductPersistenceAdapterTest (US05_CA03, US05_CA04) |

US04 CA-04 (un producto descontinuado se sigue consultando, INV-10) se prueba en
`DiscontinueProductServiceTest` usando `GetProductService`.

## Pruebas de reglas transversales

| Regla | Clase de prueba |
| :--- | :--- |
| ADR-0004 y ADR-0007: cada fila HTTP + `code` e `INTERNAL_ERROR` sin detalles internos | GlobalExceptionHandlerTest |
| ADR-0009: `null` explícito en un request | ProductControllerUpdateTest (US02_CA05), ProductControllerRegisterTest, ProductControllerDiscontinueTest |
| ADR-0009: `PATCH` sin campos | ProductControllerUpdateTest |
| Arranque del contexto completo | CatalogoProductosApplicationTests |

## Colecciones Postman (ADR-0012)

Verificación complementaria contra la aplicación en ejecución; no sustituye las pruebas `USxx_CAyy`.

| Colección | Origen | Cubre |
| :--- | :--- | :--- |
| `postman/catalogo-productos-contrato.postman_collection.json` | Generada desde `catalog-openapi.yaml` (no se edita) | Las 6 operaciones del contrato, una petición por `operationId` |
| `postman/catalogo-productos-flujo.postman_collection.json` | Escrita a mano | Ciclo de vida de `product-lifecycle.md`: US01 CA-01 y CA-02, US05 CA-01, US02 CA-01, US03 CA-01, US04 CA-01 y US05 CA-03 |
