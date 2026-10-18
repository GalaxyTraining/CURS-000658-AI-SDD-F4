<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# Pruebas

Fuentes: CLAUDE.md (regla 7), [traceability.md](../../specs/traceability.md) y `python tools/spec_sync.py --md`.

## Reglas

- Cada criterio de aceptación tiene al menos una prueba, con el nombre `USxx_CAyy_descripcionEnIngles`
  (por ejemplo `US03_CA02_activateWithoutPrice_isRejectedIndicatingPrice`). `spec_sync.py` lo comprueba.
- Las reglas de dominio se prueban con JUnit puro, sin Spring.
- Las pruebas de error verifican el HTTP **y** el `code` de ADR-0004; no dependen del texto de `message`,
  salvo los datos faltantes de `PRODUCT_INCOMPLETE` ([API-GUIDELINES.md](../../specs/api/API-GUIDELINES.md)).
- El tiempo se controla con un `Clock` fijo, nunca con `Instant.now()`.
- Cuando el constructor de `ProductController` recibe un `*UseCase` nuevo, se añade su `@MockitoBean` en
  **todas** las clases `@WebMvcTest(ProductController.class)`.
- La prueba de 500 escribe `ERROR ... Unexpected error` en el log a propósito (ADR-0007). El resultado lo da el
  código de salida de Maven o `target/surefire-reports`.

## Estrategia por capa

| Capa | Clases | Tipo | Ejecuciones |
| :--- | :--- | :--- | :---: |
| Dominio (`domain.model`) | [US01RegisterProductTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US01RegisterProductTest.java), [US02UpdateProductTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US02UpdateProductTest.java), [US03ActivateProductTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US03ActivateProductTest.java), [US04DiscontinueProductTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/US04DiscontinueProductTest.java) (+ [ProductFixtures](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/domain/model/ProductFixtures.java)) | JUnit puro, con casos `@ParameterizedTest` | 50 |
| Aplicación (`application.service`) | [RegisterProductServiceTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/RegisterProductServiceTest.java), [UpdateProductServiceTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/UpdateProductServiceTest.java), [ActivateProductServiceTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/ActivateProductServiceTest.java), [DiscontinueProductServiceTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/DiscontinueProductServiceTest.java), [GetProductServiceTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/GetProductServiceTest.java), [SearchProductsServiceTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/SearchProductsServiceTest.java) | JUnit sin Spring, con el doble [InMemoryProductPersistence](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/application/service/InMemoryProductPersistence.java) del puerto de salida | 32 |
| Web (`adapter.in.web`) | ProductController{Register,Update,Activate,Discontinue,Get,Search}Test, [GlobalExceptionHandlerTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/GlobalExceptionHandlerTest.java), [WebFrameworkErrorsTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/WebFrameworkErrorsTest.java) | `@WebMvcTest(ProductController.class)` con los `*UseCase` como `@MockitoBean`: mapeo HTTP ↔ Command/Result, validaciones del contrato y errores | 62 |
| Persistencia (`adapter.out.persistence`) | [ProductPersistenceAdapterTest](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/out/persistence/ProductPersistenceAdapterTest.java) | `@DataJpaTest` sobre H2: SKU único, filtros, paginación y orden | 6 |
| Contexto | [CatalogoProductosApplicationTests](../../src/test/java/pe/edu/galaxy/training/java/sdd/catalog/CatalogoProductosApplicationTests.java) | `@SpringBootTest`: arranque completo | 1 |

**Total: 151 ejecuciones** en la última corrida de `./mvnw test` (0 fallos), de las cuales 89 son métodos
`USxx_CAyy`. "Ejecuciones" cuenta cada caso parametrizado por separado, como hace surefire.

## Reglas transversales

| Regla | Clase de prueba |
| :--- | :--- |
| ADR-0004 y ADR-0007: cada fila HTTP + `code`, e `INTERNAL_ERROR` sin detalles internos | GlobalExceptionHandlerTest |
| ADR-0009: `null` explícito en un request | ProductControllerUpdateTest (US02_CA05), ProductControllerRegisterTest, ProductControllerDiscontinueTest |
| ADR-0009: `PATCH` sin campos | ProductControllerUpdateTest |
| ADR-0010: errores propios de Spring MVC (404, 405, 406, 415) conservan su HTTP y nunca responden 500 | WebFrameworkErrorsTest |
| Arranque del contexto completo | CatalogoProductosApplicationTests |

Fuente: [traceability.md](../../specs/traceability.md), "Pruebas de reglas transversales".
El detalle por operación está en [operaciones/](operaciones/).

## Cobertura de criterios de aceptación

| Criterio | Pruebas | Clases |
| :--- | :---: | :--- |
| US01_CA01 | 7 | ProductControllerRegisterTest, RegisterProductServiceTest, US01RegisterProductTest |
| US01_CA02 | 3 | ProductControllerRegisterTest, ProductPersistenceAdapterTest, RegisterProductServiceTest |
| US01_CA03 | 2 | ProductControllerRegisterTest, US01RegisterProductTest |
| US01_CA04 | 3 | ProductControllerRegisterTest, US01RegisterProductTest |
| US01_CA05 | 1 | ProductControllerRegisterTest |
| US01_CA06 | 3 | ProductControllerRegisterTest, RegisterProductServiceTest, US01RegisterProductTest |
| US02_CA01 | 5 | ProductControllerUpdateTest, US02UpdateProductTest, UpdateProductServiceTest |
| US02_CA02 | 3 | ProductControllerUpdateTest, US02UpdateProductTest, UpdateProductServiceTest |
| US02_CA03 | 4 | ProductControllerUpdateTest, US02UpdateProductTest, UpdateProductServiceTest |
| US02_CA04 | 7 | ProductControllerUpdateTest, US02UpdateProductTest, UpdateProductServiceTest |
| US02_CA05 | 4 | ProductControllerUpdateTest, US02UpdateProductTest, UpdateProductServiceTest |
| US03_CA01 | 3 | ActivateProductServiceTest, ProductControllerActivateTest, US03ActivateProductTest |
| US03_CA02 | 5 | ActivateProductServiceTest, ProductControllerActivateTest, US03ActivateProductTest |
| US03_CA03 | 3 | ActivateProductServiceTest, ProductControllerActivateTest, US03ActivateProductTest |
| US04_CA01 | 4 | DiscontinueProductServiceTest, ProductControllerDiscontinueTest, US04DiscontinueProductTest |
| US04_CA02 | 6 | DiscontinueProductServiceTest, ProductControllerDiscontinueTest, US04DiscontinueProductTest |
| US04_CA03 | 3 | DiscontinueProductServiceTest, ProductControllerDiscontinueTest, US04DiscontinueProductTest |
| US04_CA04 | 1 | DiscontinueProductServiceTest |
| US05_CA01 | 3 | GetProductServiceTest, ProductControllerGetTest |
| US05_CA02 | 3 | GetProductServiceTest, ProductControllerGetTest |
| US05_CA03 | 8 | ProductControllerSearchTest, ProductPersistenceAdapterTest, SearchProductsServiceTest |
| US05_CA04 | 8 | ProductControllerSearchTest, ProductPersistenceAdapterTest, SearchProductsServiceTest |

Resultado de `spec_sync.py` al generar este documento: **OK**, 22 de 22 criterios con prueba, 0 errores y
0 advertencias. Informe vigente: [docs/sync-report.md](../sync-report.md).

## Verificación complementaria con Postman (ADR-0012)

No forma parte de `mvn test` ni es requisito para hacer commit. Se ejecuta contra la aplicación en marcha
(`./mvnw spring-boot:run`). Qué cubre cada colección: [traceability.md](../../specs/traceability.md),
"Colecciones Postman".

| Colección | Qué verifica |
| :--- | :--- |
| `postman/catalogo-productos-contrato.postman_collection.json` | Generada desde el contrato: una petición por `operationId`, sin aserciones |
| `postman/catalogo-productos-flujo.postman_collection.json` | Ciclo de vida encadenado: US01 CA-01 y CA-02, US05 CA-01, US02 CA-01, US03 CA-01, US04 CA-01 y US05 CA-03 (7 aserciones `pm.test`) |

Comando: `postman collection run postman/catalogo-productos-flujo.postman_collection.json --env-var baseUrl=http://localhost:8080`.

## Observaciones

Sin acción obligatoria ([revisión del 2026-10-18](../revisiones/2026-10-18-sincronizar-2.md)):

- `ProductControllerUpdateTest.US02_CA04_priceWithThreeDecimals_returns400` simula la excepción y solo verifica
  el mapeo; la regla real se prueba en `US01RegisterProductTest` y `UpdateProductServiceTest`.
- `WebFrameworkErrorsTest.ADR0010_notAcceptableMediaType_returns406` no puede fallar aunque el handler deje de
  relanzar: con `Accept: application/xml` tampoco se puede escribir un 500 en JSON (S-05). Las pruebas de 404,
  405 y 415 sí lo detectan.
