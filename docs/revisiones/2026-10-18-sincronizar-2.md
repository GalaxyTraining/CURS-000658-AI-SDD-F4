# Revisión con criterio (`/sincronizar`, 2026-10-18, segunda pasada)

> Revisión manual posterior a `spec_sync.py`. Va en un archivo propio porque `spec_sync.py --md`
> sobrescribe `docs/sync-report.md`. El informe automático vigente está en `docs/sync-report.md`.
> Revisión anterior: [2026-10-18-sincronizar.md](2026-10-18-sincronizar.md).

## Estado de los hallazgos (actualizado 2026-10-18)

| ID | Estado | Commit |
| :--- | :--- | :--- |
| S-01 | Resuelto: `docs/tecnica/` regenerada | commit `docs:` de esta revisión |
| S-02 | Resuelto | `6d54885` (`refactor:`) |
| S-03 | Sin acción (observación) | — |
| S-04 | Resuelto | `2317508` (`test:`) |
| S-05 | Sin acción (observación) | — |
| S-06 | Resuelto | `1e2d5ea` (`refactor:`) |

El resto del documento conserva la revisión tal como se hizo, antes de las correcciones.

Alcance: estado del repositorio en el commit `552c501`, después de resolver H-01 a H-08 y H-10 y de
adoptar ADR-0010 y ADR-0012.

## Paso 1. Verificación automática

| Herramienta | Resultado |
| :--- | :--- |
| `python tools/spec_lint.py` | **OK**: 6 operaciones, 0 errores, 0 advertencias |
| `python tools/spec_sync.py --md docs/sync-report.md` | **OK**: 0 errores, 0 advertencias; 22/22 CA con prueba, 89 pruebas `USxx_CAyy` |
| `./mvnw test` | **OK**: 151 pruebas, 0 fallos, 0 errores, 0 omitidas (exit 0). El `ERROR ... Unexpected error` del log es la prueba de ADR-0007, a propósito |

Verificación adicional con la aplicación en ejecución (`./mvnw spring-boot:run`):

| Comprobación | Resultado |
| :--- | :--- |
| `GET /h2-console/` (ADR-0005) | 200 |
| `GET /swagger-ui.html` (ADR-0008) | 200 |
| `DELETE /api/v1/products/{id}` (ADR-0010) | 405 con el cuerpo por defecto de Spring Boot (`timestamp`, `status`, `error`, `path`), sin mensaje ni traza |
| `GET /api/v1/unknown` (ADR-0010) | 404 con el mismo cuerpo por defecto |
| `postman collection run postman/catalogo-productos-flujo.postman_collection.json` (ADR-0012) | 7 peticiones, 7 aserciones, 0 fallos |

## Paso 2. Verificación con criterio

### Reglas: sin cambios desde la revisión anterior

Desde `8aaa32b`, el único cambio en `src/main/java` es el javadoc de `ProductValidationException` (`d36f47e`).
Las invariantes siguen donde dice [domain-model.md §3](../../specs/architecture/domain-model.md):
INV-01 en `Sku.java:10-15`, INV-05 en `Price.java:16`, INV-03 en `Product.java:25` (`final`),
INV-08 en `Product.java:78`, INV-06/INV-07 en `Product.java:110-111` y INV-06/INV-09 en `Product.java:130-141`.
INV-11 coincide ahora con la spec: `Product.update()` conserva los null y `adapter.in.web` rechaza el null explícito (ADR-0009).

### Errores: correcto

`GlobalExceptionHandler` mapea cada excepción de dominio a una sola fila de ADR-0004. Las excepciones de Spring MVC
que implementan `org.springframework.web.ErrorResponse` se relanzan (`GlobalExceptionHandler.java:118-120`), como
pide ADR-0010. Lo cubre `WebFrameworkErrorsTest`. Hay un comentario desactualizado: ver S-02.

### Arquitectura: sin violaciones

- `domain` solo importa el JDK y `domain`.
- `application.port` no importa `adapter` ni Spring; `application.service` no importa `adapter`.
- `adapter.in.web` importa de `domain` solo excepciones (`DomainException` y las cinco de ADR-0004).
- `adapter.out` no importa `adapter.in` ni `port.in`.
- Nadie depende de `config`.

### ADR aceptados

| ADR | Estado | Evidencia |
| :--- | :--- | :--- |
| 0001-0004, 0006-0009 | Cumplen | Sin cambios desde la revisión anterior |
| 0005 H2 + consola | Cumple | `application.properties:19-20`, `pom.xml:36`; consola en 200. Ver la observación S-03 |
| 0010 Errores de Spring MVC | Cumple | `GlobalExceptionHandler.java:118-120`; `WebFrameworkErrorsTest` (4 pruebas); 404 y 405 verificados en ejecución |
| 0012 Colecciones Postman | Cumple | `postman/catalogo-productos-contrato.postman_collection.json` (generada, 6 operaciones) y `postman/catalogo-productos-flujo.postman_collection.json` (7/7 en ejecución); `tech-stack.md` y `traceability.md` las registran |

### Pruebas

Las pruebas `USxx_CAyy` no cambiaron desde la revisión anterior. `WebFrameworkErrorsTest` se validó quitando
temporalmente el relanzamiento del handler: las pruebas de 404, 405 y 415 fallaron (respondieron 500). La de 406 no
puede fallar por diseño (ver S-05).

### Documentación

`docs/tecnica/` se generó antes de los commits `3e1fff2` a `552c501` y quedó desactualizada (S-01).

## Estado de los hallazgos de la revisión anterior

| ID | Estado | Commit |
| :--- | :--- | :--- |
| H-01, H-03, H-04, H-06, H-08 | Resuelto | `326ad24` |
| H-07 | Resuelto | `326ad24`, `d36f47e` |
| H-02 | Resuelto: la consola H2 forma parte de ADR-0005 | `3e1fff2`, `c52e8c1` |
| H-05 | Resuelto: ADR-0010 | `3e1fff2`, `19185a1` |
| H-10 | Resuelto: ADR-0012 | `3e1fff2`, `0bc5a8a`, `552c501` |
| H-09 | Pendiente en el momento de la revisión, opcional (ver S-06) | — |

## Paso 3/4. Hallazgos

| ID | Tipo | Evidencia | Quién tiene razón | Resolución propuesta | Commit |
| :--- | :--- | :--- | :--- | :--- | :--- |
| S-01 | Documentación derivada desactualizada | `docs/tecnica/README.md:86-89` sigue listando D-1 a D-4, que ya están resueltas. `README.md:74` enlaza `postman/catalogo-productos.postman_collection.json`, que ya no existe (**enlace roto**). La tabla de stack (`README.md:41-44`) no menciona la consola H2 ni Postman. `decisiones.md` termina en ADR-0009 (`:19`), sin ADR-0010 ni ADR-0012. `pruebas.md:30` dice 147 pruebas (son 151) y no incluye `WebFrameworkErrorsTest` ni las colecciones. `api.md:60` remite a D-2. Además, la tabla de estado de `docs/revisiones/2026-10-18-sincronizar.md:12` sigue con H-02, H-05 y H-10 como pendientes | La spec | Regenerar con `/documentar` y actualizar la tabla de estado de la revisión anterior | `docs:` |
| S-02 | Comentario desactualizado | `GlobalExceptionHandler.java:26`: "Maps every error to ErrorResponse", pero según ADR-0010 los errores de Spring MVC no se mapean. `:113-116` justifica el relanzamiento solo con ADR-0007 y no cita ADR-0010 | La spec (ADR-0010) | Javadoc de la clase: "Maps domain and request errors to ErrorResponse (ADR-0004, ADR-0007); Spring MVC's own errors keep their status (ADR-0010)". Citar ADR-0010 en `:114`. Sin cambio de comportamiento | `refactor:` |
| S-03 | Observación | La consola H2 se habilita en `application.properties:19` sin perfil, así que está activa en cualquier ejecución. `tech-stack.md` y ADR-0005 dicen "solo para desarrollo". Hoy es coherente, porque el proyecto solo se ejecuta en desarrollo y no hay perfiles ni despliegue especificados | Ninguno: no hay desvío hoy | Sin acción. Si algún día se despliega, habría que limitarla a un perfil `dev` (**decisión nueva**, requiere ADR) | — |
| S-04 | Metadato engañoso | La colección de flujo tiene la descripción del contrato: `postman/catalogo-productos-flujo.postman_collection.json:5` ("Contrato REST del Catálogo de Productos (Spec-First)... Extensiones propias: x-spec-refs...") | La spec (ADR-0012: es un escenario del ciclo de vida) | Cambiar `info.description` por una que describa el flujo y remita a ADR-0012 y `product-lifecycle.md` | `test:` |
| S-05 | Limitación de prueba (observación) | `WebFrameworkErrorsTest.java:61`: con `Accept: application/xml` tampoco se puede escribir un 500 en JSON, así que la prueba de 406 pasa aunque el handler no relance. Está documentado en la propia prueba | La spec, y se cumple | Sin acción | — |
| S-06 | Nombre de prueba (H-09, opcional) | `US02UpdateProductTest.java:100` `US02_CA05_emptyPriceAndCategory_keepCurrentValues` prueba "null = no enviado", no "dejarlos vacíos" | La spec, y se cumple entre capas | Opcional: renombrar a `US02_CA05_notSentPriceAndCategory_keepCurrentValues` | `refactor:` (opcional) |

**Nada se ha corregido.** S-03 y S-05 no requieren acción. S-01, S-02, S-04 y S-06 esperan tu elección.
