# Registro de cambios de las especificaciones

Cada entrada documenta un cambio sobre `specs/` gestionado con `/cambio-spec`.

## 2026-10-18 - Categoría SPORTS en el catálogo

- **Descripción:** se agrega la categoría `SPORTS` ("Artículos deportivos.") al final del catálogo cerrado
  de categorías, sin reordenar los valores existentes.
- **Motivo:** cambio de negocio solicitado (ejemplo de gestión de cambios del ítem 4 del curso).
- **Archivos de spec modificados:** `specs/domain/catalogs.md` (§1), `specs/architecture/domain-model.md`
  (§2, enum `ProductCategory`), `specs/api/catalog-openapi.yaml` (`info.version` y `ProductCategory.enum`).
- **Compatibilidad del contrato:** compatible (se agrega un valor de enum; no se elimina ni renombra nada).
  `info.version` pasa de `1.0.0` a `1.1.0`. Un cliente que valide el enum de forma estricta en las
  respuestas debe aceptar el nuevo valor.
- **Sin cambios:** visión, RF, historias y criterios (no enumeran categorías), invariantes, ADR,
  `architecture.md` y `traceability.md` (no hay operaciones ni criterios nuevos).
- **Commits previstos:**
  1. `spec:` categoría SPORTS en `catalogs.md`.
  2. `design:` SPORTS en el `classDiagram` de `domain-model.md`.
  3. `contract:` SPORTS en el contrato, versión 1.1.0 (este commit incluye esta entrada).
  4. `feat:` SPORTS en `ProductCategory.java` y prueba de registro con la nueva categoría.
  5. `docs:` regenerar `docs/tecnica/` (`dominio.md`, `api.md`).
- **Pendiente (resuelto):** `postman/catalogo-productos-contrato.postman_collection.json` se regeneró en
  Postman desde el contrato 1.1.0 (ADR-0012), en un commit `docs:` posterior.
- **Nota:** `spec_lint.py` falla en los commits intermedios (DOM-02 tras `spec:`, CODE-01 tras `contract:`)
  porque compara catálogo, contrato y enum Java; el conjunto se fusiona solo cuando todo da OK.

## 2026-10-18 - Sincronizar diagramas de arquitectura con el código

- **Descripción:** revisión `/diagramas` de `architecture.md`; los diagramas habían quedado atrás del
  código, que cumple la spec escrita.
  - D-01: §2 dibuja las dependencias hacia `domain` que la tabla de §2 ya permitía: `port.in`,
    `adapter.in.web` (excepciones y enums) y `adapter.out.persistence`.
  - D-02: §2 nombra `GlobalExceptionHandler` en `adapter.in.web`.
  - D-03: §2 agrega `config` (`ClockConfiguration`), que provee el `Clock` a los servicios.
  - D-04: §3 muestra que `ProductPersistenceMapper` convierte en ambos sentidos (`toEntity`/`toDomain`).
  - D-05: §3 nombra `ProductResult.from` como la conversión de `Product` a `ProductResult`.
  - D-06: §5 agrega `CatalogoProductosApplication` en la raíz y el subpaquete `generated.model`.
  - D-07: §6 muestra que `GlobalExceptionHandler`, no `ProductController`, responde 404/409/422.
  - D-08: §6 muestra el `Product` devuelto por `save` y `ProductResult.from`.
- **Motivo:** diagramas incompletos frente al código y a las tablas del mismo archivo. No hay
  decisiones nuevas ni desvíos del código.
- **Archivos de spec modificados:** `specs/architecture/architecture.md`.
- **Compatibilidad del contrato:** sin cambios en `catalog-openapi.yaml`; `info.version` no cambia.
- **Commits previstos:**
  1. `design:` diagramas sincronizados con la implementación (este cambio).
  2. `docs:` diagramas de secuencia por operación en `docs/tecnica/diagramas.md`.

## 2026-10-18 - Sincronizar specs de diseño con el código

- **Descripción:** se corrige la documentación de diseño para que refleje el código existente, según
  la revisión `/sincronizar` del 2026-10-18 (`docs/sync-report.md`, hallazgos H-01, H-03, H-04,
  H-06, H-07 y H-08).
  - H-01: `tech-stack.md` registra springdoc-openapi 3.x (Swagger UI con el contrato), ya decidido en ADR-0008.
  - H-03: `tech-stack.md` cambia "JUnit 5" por "JUnit 6 (Jupiter)", la versión que trae Spring Boot 4.1 (ADR-0006).
  - H-04: `architecture.md` §5 añade `DomainException`, `RejectExplicitNullsModule`, `InvalidRequestException`
    y el paquete `config` (`ClockConfiguration`).
  - H-06: el `classDiagram` de `domain-model.md` §2 muestra el `Clock` en los métodos que modifican el
    producto y el método `restore`, como ya decía §4.
  - H-07: en `domain-model.md` §3, la columna "Dónde se garantiza" de INV-11 nombra también el rechazo
    del null explícito en `adapter.in.web` (ADR-0009), como ya decía §4.
  - H-08: se corrige la sangría del árbol de paquetes de `architecture.md` §5.
- **Motivo:** `spec_sync.py` informaba SYNC-10 (error) y SYNC-09 (3 advertencias), y la revisión manual
  encontró diagramas y textos desactualizados. No hay decisiones nuevas: el código y los ADR aceptados
  ya respaldaban cada punto.
- **Archivos de spec modificados:** `specs/architecture/tech-stack.md`, `specs/architecture/architecture.md`,
  `specs/architecture/domain-model.md`.
- **Compatibilidad del contrato:** sin cambios en `catalog-openapi.yaml`; `info.version` no cambia.
- **Commits previstos:**
  1. `design:` sincronizar tech-stack, architecture y domain-model con el código (este cambio).
  2. `refactor:` quitar INV-11 del javadoc de `ProductValidationException` (H-07).
  3. `docs:` regenerar `docs/sync-report.md` y mover la revisión manual a
     `docs/revisiones/2026-10-18-sincronizar.md`.
- **Fuera de alcance:** H-02, H-05 y H-10 (requieren decisiones nuevas) y H-09 (opcional).

## 2026-10-18 - Pendientes de la revisión: consola H2, errores de Spring MVC y Postman

- **Descripción:** se resuelven los hallazgos pendientes de la revisión `/sincronizar` del 2026-10-18
  (`docs/revisiones/2026-10-18-sincronizar.md`) y la divergencia D-4 de `docs/tecnica/README.md`.
  - D-4: el diagrama de secuencia de `architecture.md` §6 llama a `activate(clock)`.
  - H-02: la consola H2 se conserva como parte de ADR-0005, solo para desarrollo, sin ADR nuevo:
    fila de Base de datos en `tech-stack.md` y una consecuencia nueva en ADR-0005.
  - H-05: nuevo ADR-0010 (adoptado de la versión original del proyecto). Los errores propios de
    Spring MVC (404, 405, 406, 415) conservan su HTTP y quedan fuera del contrato. Complementa a
    ADR-0007, que sigue Aceptado y gana una referencia a ADR-0010 en Consecuencias.
  - H-10: nuevo ADR-0012. Postman como verificación complementaria con dos colecciones: la de contrato,
    generada desde `catalog-openapi.yaml`, y la de flujo, escrita a mano. Se añade una fila en
    `tech-stack.md` y una sección en `traceability.md`.
- **Motivo:** cerrar los hallazgos que requerían decisión. ADR-0005 y ADR-0007 se amplían a pedido
  explícito, sin cambiar su decisión. El número 0011 de ADR queda reservado.
- **Archivos de spec modificados:** `specs/architecture/architecture.md`, `specs/architecture/tech-stack.md`,
  `specs/adr/0005-base-de-datos-h2.md`, `specs/adr/0007-error-interno-generico.md`,
  `specs/adr/0010-errores-propios-de-spring-mvc.md` (nuevo), `specs/adr/0012-colecciones-postman.md` (nuevo),
  `specs/adr/README.md`, `specs/traceability.md`.
- **Compatibilidad del contrato:** sin cambios en `catalog-openapi.yaml`; `info.version` sigue en 1.0.0.
- **Commits previstos:**
  1. `design:` este cambio de specs.
  2. `tooling:` `spring.h2.console.enabled=true` y `spring.h2.console.path=/h2-console`.
  3. `test:` `WebFrameworkErrorsTest` (404, 405, 406, 415, ninguno responde 500) y su fila en
     `traceability.md` ("Pruebas de reglas transversales").
  4. `test:` renombrar la colección a `catalogo-productos-flujo.postman_collection.json` y quitar la
     carpeta `api/` vacía.
  5. `docs:` exportar desde Postman `catalogo-productos-contrato.postman_collection.json` (lo hace el autor).
  6. `docs:` `/sincronizar` y `/documentar`.
