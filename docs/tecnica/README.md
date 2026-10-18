<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# Catálogo de Productos: documentación técnica

## Propósito

Mantener un catálogo centralizado y confiable de los productos que la empresa ofrece, de modo que solo
los productos completos y aprobados estén disponibles para la venta
([vision.md §1](../../specs/vision.md)). El alcance y lo que queda fuera de él están en
[vision.md §3-§4](../../specs/vision.md).

El proyecto sigue **Spec-Driven Development**: [`specs/`](../../specs/) es la fuente única de verdad.
Esta documentación explica y enlaza las specs; si las contradice, manda la spec y esta carpeta se regenera
con `/documentar`.

## Índice

| Documento | Contenido |
| :--- | :--- |
| [arquitectura.md](arquitectura.md) | Capas hexagonales, regla de dependencia, paquetes y clases, flujo de una petición |
| [api.md](api.md) | Contrato REST: una sección por `operationId`, errores y ejemplos |
| [dominio.md](dominio.md) | Glosario, estados, categorías e invariantes INV-01 a INV-11 |
| [operaciones/](operaciones/) | Trazabilidad completa por operación: historia, criterios, clases y pruebas |
| [pruebas.md](pruebas.md) | Estrategia de pruebas por capa y cobertura de criterios de aceptación |
| [decisiones.md](decisiones.md) | Resumen de los ADR |

Operaciones: [registerProduct](operaciones/registerProduct.md) ·
[updateProduct](operaciones/updateProduct.md) · [activateProduct](operaciones/activateProduct.md) ·
[discontinueProduct](operaciones/discontinueProduct.md) · [getProduct](operaciones/getProduct.md) ·
[searchProducts](operaciones/searchProducts.md)

## Stack

Resumen de [tech-stack.md](../../specs/architecture/tech-stack.md). Las versiones exactas están en
[`pom.xml`](../../pom.xml).

| Elemento | Tecnología | Decisión |
| :--- | :--- | :--- |
| Lenguaje | Java 25 | ADR-0006 |
| Framework | Spring Boot 4.1.x (Web MVC, Validation, Data JPA) | ADR-0001, ADR-0006 |
| Base de datos | H2 en memoria | ADR-0005 |
| Contrato | OpenAPI 3.0.3, interfaz `ProductsApi` generada con openapi-generator | ADR-0006 |
| Documentación de la API | springdoc-openapi 3.x (Swagger UI con el contrato) | ADR-0008 |
| Pruebas | JUnit 6 (Jupiter) + Spring Boot Test | ADR-0006 |
| Validación de specs | Python 3 + PyYAML | — |

## Comandos

Desde la raíz del proyecto (`CLAUDE.md`, "Stack y comandos"):

| Acción | Comando |
| :--- | :--- |
| Validar specs y contrato | `python tools/spec_lint.py` |
| Verificar specs ↔ implementación | `python tools/spec_sync.py` (informe: `--md docs/sync-report.md`) |
| Regenerar la API desde el contrato | `./mvnw clean generate-sources` |
| Compilar y probar | `./mvnw test` |
| Ejecutar la aplicación | `./mvnw spring-boot:run` |

Antes de cada commit, `spec_lint.py` y `spec_sync.py` deben dar OK y `mvn test` debe pasar.
Las pruebas de error 500 escriben `ERROR ... Unexpected error` en el log a propósito (ADR-0007): el resultado
lo da el código de salida de Maven o `target/surefire-reports`.

No se editan archivos en `target/generated-sources`: se regeneran desde
[catalog-openapi.yaml](../../specs/api/catalog-openapi.yaml).

## Ejecutar y probar a mano

- **URL base:** `http://localhost:8080` (`servers` del contrato). Todas las rutas empiezan por `/api/v1`.
- **Base de datos:** H2 en memoria (`jdbc:h2:mem:catalogo`); los datos se pierden al reiniciar
  ([application.properties](../../src/main/resources/application.properties), ADR-0005).
- **Swagger UI:** springdoc carga el contrato autoritativo `/catalog-openapi.yaml`, que Maven copia desde
  `specs/api/` a `static/` al compilar (ADR-0008, `springdoc.swagger-ui.url`). La interfaz queda en la
  ruta por defecto de springdoc, `http://localhost:8080/swagger-ui.html`.
- **Postman:** [`postman/catalogo-productos.postman_collection.json`](../../postman/catalogo-productos.postman_collection.json)
  contiene la carpeta "Flujo ciclo de vida" (registrar, SKU duplicado, consultar, actualizar, activar,
  descontinuar y listar), con las variables `baseUrl`, `sku` y `productId`. No es parte de ninguna spec
  (ver Divergencias).

## Divergencias detectadas

Hechos del código o del repositorio que ninguna spec describe. Aquí no se documentan como reglas.
El detalle está en [docs/revisiones/2026-10-18-sincronizar.md](../revisiones/2026-10-18-sincronizar.md).

| ID | Divergencia | Hallazgo |
| :--- | :--- | :--- |
| D-1 | `pom.xml` incluye `spring-boot-h2console`, pero ninguna spec ni ADR lo menciona y la consola no está habilitada (`spring.h2.console.enabled` no existe). **La consola H2 no está disponible.** | H-02 |
| D-2 | `GlobalExceptionHandler` relanza las excepciones del framework (405, 415, 406 y ruta inexistente 404), que responden con el formato de Spring y no con `ErrorResponse`. ADR-0007 dice que toda excepción no mapeada responde 500. | H-05 |
| D-3 | La colección de Postman no figura en ninguna spec y tiene carpetas vacías (`api/v1/products/{productId}/activate` y `discontinue`). | H-10 |
| D-4 | El diagrama de secuencia de [architecture.md §6](../../specs/architecture/architecture.md) llama a `activate()` sin `Clock`; el código usa `Product.activate(Clock)` ([domain-model.md §4](../../specs/architecture/domain-model.md)). | Nuevo (similar a H-06) |
