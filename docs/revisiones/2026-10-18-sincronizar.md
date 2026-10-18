# Revisión con criterio (`/sincronizar`, 2026-10-18)

> Revisión manual que antes estaba al final de `docs/sync-report.md`. Se trasladó aquí para que `spec_sync.py --md` no la sobrescriba.
> El informe automático vigente está en `docs/sync-report.md`.

## Estado de los hallazgos (actualizado 2026-10-18)

| ID | Estado | Commit |
| :--- | :--- | :--- |
| H-01, H-03, H-04, H-06, H-08 | Resuelto | `326ad24` (`design:`) |
| H-07 | Resuelto | `326ad24` (`design:`) + `d36f47e` (`refactor:`) |
| H-02, H-05, H-10 | Pendiente: requiere decisión nueva | — |
| H-09 | Pendiente: opcional | — |

El resto del documento conserva la revisión tal como se hizo, antes de las correcciones.

## Paso 1. Verificación automática

| Herramienta | Resultado |
| :--- | :--- |
| `python tools/spec_lint.py` | **OK**: 6 operaciones, 0 errores, 0 advertencias |
| `python tools/spec_sync.py` | **ERROR**: 1 error (SYNC-10) y 3 advertencias (SYNC-09); 22/22 CA con prueba, 89 pruebas `USxx_CAyy` |
| `./mvnw test` | **OK**: 147 pruebas, 0 fallos, 0 errores, 0 omitidas (exit 0). El `ERROR ... Unexpected error` del log es la prueba de ADR-0007, a propósito |

## Paso 2. Verificación con criterio

### Reglas (domain-model.md §3): todas en su lugar

| INV | Dónde dice la spec | Evidencia |
| :--- | :--- | :--- |
| INV-01 | `Sku` | `domain/model/Sku.java:12-17` |
| INV-02 | `RegisterProductService` + BD única traducida por el adaptador | `RegisterProductService.java:31-33`, `ProductJpaEntity.java:18-19`, `ProductPersistenceAdapter.java:38-41` |
| INV-03 | `Product` sin método para cambiar el SKU | `Product.java:25` (`final`, sin método que lo cambie) |
| INV-04 | `Product` | `Product.java:54-59`, `82-91`, `150-161` |
| INV-05 | `Price` | `Price.java:12-19` |
| INV-06 | `Product.activate()/discontinue()` | `Product.java:110-113`, `130-133` |
| INV-07 | `Product.activate()` | `Product.java:114-123` |
| INV-08 | `Product.update()` | `Product.java:78-81` |
| INV-09 | `Product.discontinue()` | `Product.java:134-139` |
| INV-10 | No hay borrado | El contrato no tiene `delete` y el puerto no tiene operación de borrado (`ProductPersistencePort.java`) |
| INV-11 | `Product.update()` | `Product.java:99-104` (null = conservar). El 400 lo produce `adapter.in.web` (ver H-07) |

Ningún servicio contiene reglas de negocio: todos solo orquestan (`application/service/*`).

### Errores (ADR-0004 / ADR-0007): correcto

`GlobalExceptionHandler.java:32-55` traduce cada excepción a una sola fila: 400 `VALIDATION_ERROR`, 404 `PRODUCT_NOT_FOUND`,
409 `DUPLICATE_SKU`, 409 `INVALID_STATE_TRANSITION` y 422 `PRODUCT_INCOMPLETE` con los datos faltantes en `details`.
El 500 `INTERNAL_ERROR` (`:117-124`) se registra con `log.error` y no expone el mensaje interno. Lo verifica `GlobalExceptionHandlerTest:80-108`.
Excepción: H-05.

### Arquitectura (architecture.md §2): sin violaciones

- `domain` solo importa el JDK y `domain`.
- `port.in` y `port.out` no importan `adapter` ni Spring.
- `service` no importa `adapter`.
- `adapter.in.web` usa de `domain` solo sus excepciones (`GlobalExceptionHandler`) y `ProductStatus`/`ProductCategory` en `ProductWebMapper.java:82-92`, que está permitido.
- `adapter.out` no importa `adapter.in` ni `port.in`.
- Nadie depende de `config`.

### ADR aceptados

| ADR | Estado | Evidencia |
| :--- | :--- | :--- |
| 0001 Hexagonal | Cumple | Ver Arquitectura |
| 0002 Transiciones POST | Cumple | Contrato: `post` en `/activate` y `/discontinue` (`catalog-openapi.yaml:186,224`) |
| 0003 UUID | Cumple | `ProductId.java:13-15`, rutas `{productId}` UUID |
| 0004 Errores | Cumple, salvo H-05 | Ver Errores |
| 0005 H2 + JPA | Cumple | `application.properties` (`jdbc:h2:mem`) |
| 0006 Spring Boot 4.1 / Java 25 | Cumple | `pom.xml:8` (4.1.1), `:31` (25) |
| 0007 500 genérico | Cumple, salvo el alcance de H-05 | `GlobalExceptionHandler.java:117-124` |
| 0008 Swagger UI con el contrato | Cumple | `pom.xml:83-89` copia el YAML a `static/`, `springdoc.swagger-ui.url=/catalog-openapi.yaml`, `target/classes/static/catalog-openapi.yaml` existe |
| 0009 null explícito y PATCH vacío | Cumple | `RejectExplicitNullsModule.java:27-42`, `ProductWebMapper.java:35-41`. Pruebas: `ProductControllerUpdateTest:169-185`, `ProductControllerRegisterTest:144`, `ProductControllerDiscontinueTest:101` |

### Pruebas USxx_CAyy

Las pruebas comprueban el comportamiento, no solo el nombre:

- Estado resultante y valores sin cambios tras un rechazo: `US02UpdateProductTest:82-93`.
- Datos faltantes de CA-02: `ProductControllerActivateTest:80-90`.
- Mensaje con el formato esperado: `US01RegisterProductTest:62-66`.
- Orden y paginación: `ProductPersistenceAdapterTest:107`.

Las pruebas de dominio y de aplicación no importan Spring. Hay observaciones menores en H-09.

### Documentación

`docs/tecnica/` no existe: no hay nada que contrastar. El único artefacto derivado es `postman/` (H-10).

## Paso 3/4. Hallazgos

| ID | Tipo | Evidencia | Quién tiene razón | Resolución propuesta | Commit |
| :--- | :--- | :--- | :--- | :--- | :--- |
| H-01 | Spec incompleta (SYNC-10) | `pom.xml:51-54` springdoc; `tech-stack.md` no lo lista, aunque ADR-0008 (aceptado) lo decidió | El código: la decisión ya está aprobada en ADR-0008 | Añadir una fila a `tech-stack.md`: "Documentación API: springdoc-openapi 3.x, Swagger UI con el contrato, ADR-0008". No es una decisión nueva | `design:` |
| H-02 | Dependencia sin ADR | `pom.xml:34-37` `spring-boot-h2console`. No figura en `tech-stack.md` ni en ningún ADR, y la consola no se habilita (`spring.h2.console.enabled` ausente), así que no tiene efecto | La spec ("no se agregan dependencias sin un ADR") | **Recomendado:** quitar la dependencia. Alternativa: documentarla con un ADR y habilitar la consola, que **es una decisión nueva** y requiere tu aprobación | `fix:` (o `design:` + ADR) |
| H-03 | Spec desactualizada | `tech-stack.md:13` dice "JUnit 5". Spring Boot 4.1.1 trae JUnit Jupiter 6.0.3 (`junit-platform-launcher-6.0.3` en el log de pruebas) | El código: es consecuencia de ADR-0006 | Cambiar a "JUnit 6 (Jupiter)" | `design:` |
| H-04 | Spec incompleta (SYNC-09) | `DomainException`, `InvalidRequestException` y `RejectExplicitNullsModule` no aparecen en `architecture.md` §5 (`:114`, `:129`) | El código: base de `details` y la implementación de ADR-0009 | Añadirlos al árbol de paquetes de §5 (documenta estructura existente; confírmalo) | `design:` |
| H-05 | Decisión no documentada | `GlobalExceptionHandler.java:117-121` relanza las excepciones de Spring MVC (405, 415, ruta inexistente 404, 406), que responden con el formato de Spring y no con `ErrorResponse`. ADR-0007 dice "toda excepción no mapeada responde 500" y "el cliente recibe siempre `ErrorResponse`". Ninguna prueba lo cubre | Ninguno del todo: responder 500 a un 405 sería peor | **Recomendado:** enmendar ADR-0007 (o un ADR nuevo) para excluir los errores HTTP del framework y añadir una prueba (p. ej. 405). Alternativa: mapearlos a `ErrorResponse`, que exige nuevos `ErrorCode` en el contrato. **Las dos son decisiones nuevas** | `design:` + `test:` (alternativa: `contract:` + `fix:`) |
| H-06 | Diagrama desactualizado | `domain-model.md:36-39` muestra `register/update/activate/discontinue` sin `Clock` y omite `restore`. §4 de la misma spec y `Product.java:51,66,77,109,129` sí los tienen | El código (lo respalda §4 de la spec) | Actualizar el `classDiagram` | `design:` |
| H-07 | Spec imprecisa + comentario erróneo | `domain-model.md:94` pone INV-11 solo en `Product.update()`, pero el 400 lo produce `adapter.in.web` (`RejectExplicitNullsModule` → `GlobalExceptionHandler.java:76-80`, ADR-0009). Además `ProductValidationException.java:5` cita INV-11, aunque el dominio nunca la lanza por INV-11 | La spec (§4 ya lo explica) | Añadir "+ rechazo de null explícito en `adapter.in.web` (ADR-0009)" a la columna "Dónde" y quitar INV-11 del javadoc | `design:` + `refactor:` |
| H-08 | Formato de la spec | `architecture.md:116,126-127`: `application`, `service` y `adapter` están mal anidados en el árbol `text` de §5 | La spec (contenido correcto, formato roto) | Corregir la sangría (puede ir junto con H-04) | `design:` |
| H-09 | Pruebas débiles (observación) | `ProductControllerUpdateTest:148-158` (`US02_CA04_priceWithThreeDecimals`) simula la excepción y solo verifica el mapeo; la regla real está en `US01RegisterProductTest:69` y `UpdateProductServiceTest:92`. `US02UpdateProductTest:100` (`US02_CA05_emptyPrice...`) prueba "null = no enviado", no el rechazo, que se prueba en la capa web | La spec, y se cumple entre capas | Sin acción obligatoria. Opcional: renombrar `US02_CA05_emptyPriceAndCategory_keepCurrentValues` → `US02_CA05_notSentPriceAndCategory_keepCurrentValues` (como en el servicio) | `refactor:` (opcional) |
| H-10 | Artefacto no especificado | `postman/catalogo-productos.postman_collection.json` no aparece en ninguna spec (`tech-stack.md`, `traceability.md`) y tiene carpetas vacías (`api/v1/products/{productId}/activate`, `discontinue`) | Indeterminado | Decidir si Postman es parte oficial de la verificación (registrarlo en `tech-stack.md`/`traceability.md`, **decisión nueva**) y eliminar las carpetas vacías | `design:` + `test:` |

**Nada se ha corregido.** Elige qué hallazgos resolver; H-02, H-05 y H-10 necesitan tu decisión explícita.
