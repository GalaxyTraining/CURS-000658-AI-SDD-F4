# Revisión con criterio (`/sincronizar`, 2026-10-18, tercera pasada)

> Revisión manual posterior a `spec_sync.py`. Va en un archivo propio porque `spec_sync.py --md`
> sobrescribe `docs/sync-report.md`. El informe automático vigente está en `docs/sync-report.md`.
> Revisiones anteriores: [2026-10-18-sincronizar.md](2026-10-18-sincronizar.md),
> [2026-10-18-sincronizar-2.md](2026-10-18-sincronizar-2.md).

## Estado de los hallazgos (actualizado 2026-10-18)

| ID | Estado | Commit |
| :--- | :--- | :--- |
| SYNC-20261018-01 | Resuelto: diagramas de `docs/tecnica/arquitectura.md` alineados con `architecture.md` §2 y §3 | commit `docs:` de esta revisión |
| SYNC-20261018-02 | Resuelto: `/documentar` conserva y enlaza `diagramas.md`; enlaces agregados en `arquitectura.md` y en cada `operaciones/<operationId>.md` | `d270784` (`tooling:`) + commit `docs:` de esta revisión |
| S-03 | Sin acción (observación) | — |
| S-05 | Sin acción (observación) | — |

El resto del documento conserva la revisión tal como se hizo, antes de las correcciones.

Alcance: `master` en `a6f2d3f` (merge del PR #2, revisión `/diagramas`). Desde la revisión anterior
(`552c501`) cambiaron: el javadoc de `GlobalExceptionHandler` (`6d54885`), la descripción de la colección de
flujo (`2317508`), el nombre de una prueba de US02 (`1e2d5ea`), `docs/tecnica/` (`edd72ed`), los diagramas de
`architecture.md` y `specs/CHANGELOG.md` (`e57e021`) y el nuevo `docs/tecnica/diagramas.md` (`d59dc21`).

## Paso 1. Verificación automática

| Herramienta | Resultado |
| :--- | :--- |
| `python tools/spec_sync.py --md docs/sync-report.md` | **OK**: 6 operaciones, 22/22 CA con prueba, 89 pruebas `USxx_CAyy`, 0 errores, 0 advertencias. El informe regenerado no cambia de contenido (solo fin de línea) |
| `./mvnw test` | **OK**: 151 pruebas, 0 fallos, 0 errores, 0 omitidas (`BUILD SUCCESS`) |

## Paso 2. Verificación con criterio

### Reglas: correcto

El código de dominio y de servicios no cambió desde la revisión anterior. Las invariantes siguen donde dice
[domain-model.md §3](../../specs/architecture/domain-model.md): INV-01 en `Sku.java:12-17`, INV-02 en
`RegisterProductService.java` (`existsBySku`) y `ProductPersistenceAdapter.java:35-44` (restricción única),
INV-03 en `Product.java:25` (`final`), INV-04 en `Product.java:150-162`, INV-05 en `Price.java:12-19`,
INV-06/INV-07 en `Product.java:109-124`, INV-08 en `Product.java:78-81`, INV-06/INV-09 en `Product.java:129-142`,
INV-10 sin operación de borrado, INV-11 en `Product.java:99-104` y en `RejectExplicitNullsModule` (ADR-0009).

### Errores: correcto

`GlobalExceptionHandler.java:35-59` asigna cada excepción de dominio a una sola fila de
[ADR-0004](../../specs/adr/0004-codigos-de-error.md): `ProductValidationException` → 400 `VALIDATION_ERROR`,
`ProductNotFoundException` → 404 `PRODUCT_NOT_FOUND`, `DuplicateSkuException` → 409 `DUPLICATE_SKU`,
`InvalidStateTransitionException` → 409 `INVALID_STATE_TRANSITION`, `ProductIncompleteException` → 422
`PRODUCT_INCOMPLETE`. El 500 (`:120-127`) responde mensaje genérico sin `details` (ADR-0007) y relanza los
errores propios de Spring MVC (ADR-0010).

### Arquitectura: sin violaciones

- `domain` solo importa el JDK y `domain.exception`.
- `application.port.in`/`port.out` importan solo `domain.model` y `application`; `application.service` no importa `adapter`.
- `adapter.in.web` usa de `domain` las excepciones (`GlobalExceptionHandler`) y los enums por nombre calificado
  (`ProductWebMapper.java:82-91`), como permite la tabla de `architecture.md` §2; no importa `application.service`.
- `adapter.out.persistence` importa `application.port.out` y `domain`, nunca `adapter.in`.
- `config` no importa ninguna capa y nadie depende de él.

Los diagramas de `architecture.md` §2, §3, §5 y §6 coinciden con el código desde `e57e021`.

### ADR aceptados: se cumplen

| ADR | Evidencia |
| :--- | :--- |
| 0001 Hexagonal | Ver "Arquitectura" |
| 0004 Códigos de error | Ver "Errores" |
| 0005 H2 + consola | `application.properties:5,19-20` (ver S-03) |
| 0007 Error interno genérico | `GlobalExceptionHandler.java:120-127`; `GlobalExceptionHandlerTest` |
| 0008 Swagger UI con el contrato | `application.properties:16` (`springdoc.swagger-ui.url=/catalog-openapi.yaml`) |
| 0009 Null explícito | `RejectExplicitNullsModule`, `ProductWebMapper.java:35-40` (`minProperties`) |
| 0010 Errores de Spring MVC | `GlobalExceptionHandler.java:117-123`; `WebFrameworkErrorsTest` (ver S-05) |
| 0012 Colecciones Postman | `postman/catalogo-productos-contrato…` y `postman/catalogo-productos-flujo…` |
| 0002, 0003, 0006 | Sin cambios desde la revisión anterior |

### Pruebas: sin cambios de fondo

Desde la revisión anterior solo cambió el nombre de `US02_CA05_notSentPriceAndCategory_keepCurrentValues`
(`1e2d5ea`), que ahora describe lo que verifica. El contenido de las pruebas `USxx_CAyy` no cambió y ya se
revisó en las dos pasadas anteriores.

### Documentación: desactualizada tras `/diagramas`

`docs/tecnica/` se regeneró en `edd72ed`, antes de los cambios de `e57e021` y `d59dc21`. Ver SYNC-20261018-01
y SYNC-20261018-02.

## Paso 3/4. Hallazgos

| ID | Estado | Tipo | Evidencia | Resolución propuesta | Commit |
| :--- | :--- | :--- | :--- | :--- | :--- |
| SYNC-20261018-01 | Nuevo | Documentación derivada desactualizada | `docs/tecnica/arquitectura.md:37-44`: el diagrama de capas solo dibuja `SVC → AGG` y no las dependencias hacia `domain` de `port.in`, `adapter.in.web` y `adapter.out.persistence` que `architecture.md` §2 dibuja desde `e57e021`. `:100`: `P -->│ProductPersistenceMapper│ E` en un solo sentido; la spec (§3) ya muestra `P <--> E` | Regenerar `arquitectura.md` con `/documentar` | `docs:` |
| SYNC-20261018-02 | Nuevo | Documentación derivada editada a mano / generador incompleto | `docs/tecnica/README.md:26`: la fila de `diagramas.md` se agregó a mano (`d59dc21`) en un archivo marcado "No editar a mano", y `.claude/commands/documentar.md:24-35` no menciona `diagramas.md`, así que la próxima regeneración la borraría. Además, `arquitectura.md:119-120`, `README.md:95` y `operaciones/activateProduct.md:37` remiten solo a `architecture.md` §6, aunque ahora cada operación tiene su secuencia en `diagramas.md` | Que `/documentar` enlace `diagramas.md` desde el índice y desde cada `operaciones/<operationId>.md` cuando exista; luego regenerar junto con SYNC-20261018-01 | `tooling:` + `docs:` |
| S-03 | Sigue abierto (observación) | Observación | La consola H2 está activa sin perfil (`application.properties:19`); ADR-0005 dice "solo para desarrollo" y hoy solo se ejecuta en desarrollo | Sin acción. Si se despliega, limitarla a un perfil `dev` (**decisión nueva**, requiere ADR) | — |
| S-05 | Sigue abierto (observación) | Limitación de prueba | `WebFrameworkErrorsTest.java:61`: la prueba de 406 pasa aunque el handler no relance; está documentado en la prueba | Sin acción | — |

Los hallazgos de las revisiones anteriores no listados aquí (H-01 a H-10, S-01, S-02, S-04, S-06) están resueltos.

**Nada se ha corregido.** S-03 y S-05 no requieren acción. SYNC-20261018-01 y SYNC-20261018-02 esperan tu elección.
