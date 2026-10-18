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
| [diagramas.md](diagramas.md) | Componentes adaptador → puerto → servicio y una secuencia por operación (generado con `/diagramas`) |

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
| Base de datos | H2 en memoria (+ consola H2, solo para desarrollo) | ADR-0005 |
| Contrato | OpenAPI 3.0.3, interfaz `ProductsApi` generada con openapi-generator | ADR-0006 |
| Documentación de la API | springdoc-openapi 3.x (Swagger UI con el contrato) | ADR-0008 |
| Pruebas | JUnit 6 (Jupiter) + Spring Boot Test | ADR-0006 |
| Pruebas de API (complementarias) | Postman: colección de contrato (generada) y colección de flujo | ADR-0012 |
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
- **Consola H2 (solo para desarrollo):** `http://localhost:8080/h2-console`, con JDBC URL `jdbc:h2:mem:catalogo`,
  usuario `sa` y contraseña vacía (ADR-0005, `spring.h2.console.*` en
  [application.properties](../../src/main/resources/application.properties)). Está activa en cualquier
  ejecución, porque el proyecto solo se ejecuta en desarrollo.
- **Swagger UI:** springdoc carga el contrato autoritativo `/catalog-openapi.yaml`, que Maven copia desde
  `specs/api/` a `static/` al compilar (ADR-0008, `springdoc.swagger-ui.url`). La interfaz queda en la
  ruta por defecto de springdoc, `http://localhost:8080/swagger-ui.html`.
- **Postman (complementario a `mvn test`, ADR-0012):** dos colecciones con funciones distintas, que se
  ejecutan contra la aplicación en marcha.

  | Colección | Origen | Uso |
  | :--- | :--- | :--- |
  | [`catalogo-productos-contrato`](../../postman/catalogo-productos-contrato.postman_collection.json) | Generada en Postman desde el contrato. No se edita a mano: se regenera cuando cambia el contrato | Una petición por `operationId`, con las respuestas de ejemplo del contrato |
  | [`catalogo-productos-flujo`](../../postman/catalogo-productos-flujo.postman_collection.json) | Escrita a mano | Ciclo de vida encadenado por `sku` y `productId`, con aserciones `pm.test` por criterio |

  Para ejecutar la de flujo desde la línea de comandos:
  `postman collection run postman/catalogo-productos-flujo.postman_collection.json --env-var baseUrl=http://localhost:8080`.

## Divergencias detectadas

Ninguna. Las divergencias D-1 a D-4 de la versión anterior de este documento se resolvieron con ADR-0005
(consola H2), ADR-0010 (errores de Spring MVC), ADR-0012 (Postman) y la corrección del diagrama de
[architecture.md §6](../../specs/architecture/architecture.md). Detalle en
[2026-10-18-sincronizar.md](../revisiones/2026-10-18-sincronizar.md),
[2026-10-18-sincronizar-2.md](../revisiones/2026-10-18-sincronizar-2.md) y
[2026-10-18-sincronizar-3.md](../revisiones/2026-10-18-sincronizar-3.md).
