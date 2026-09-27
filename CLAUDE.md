# CLAUDE.md - Catálogo de Productos

Proyecto desarrollado con **Spec-Driven Development (SDD)**. La carpeta `specs/` es la fuente única
de verdad: nada se implementa si no está especificado primero.

## Fuentes de verdad (leer antes de implementar)
1. `specs/vision.md` - alcance y fuera de alcance.
2. `specs/requirements/functional.md` - RF-01 a RF-05.
3. `specs/domain/product-lifecycle.md` y `specs/domain/catalogs.md` - estados y categorías.
4. `specs/user-stories/` - criterios de aceptación (CA) por historia.
5. `specs/architecture/domain-model.md` - invariantes INV-01 a INV-11 y dónde se garantiza cada una.
6. `specs/architecture/architecture.md` - capas, puertos, modelos por frontera y paquetes.
7. `specs/adr/` - decisiones aceptadas; no contradecirlas.
8. `specs/api/catalog-openapi.yaml` - contrato autoritativo.
9. `specs/traceability.md` - operación -> caso de uso -> historia -> invariantes -> pruebas.

## Reglas obligatorias
1. **Si la spec no alcanza, DETENTE.** No inventes campos, reglas, estados, rutas ni códigos de error.
   Explica el vacío y propone el cambio a la spec; solo se continúa cuando la spec esté actualizada.
2. **Contrato 1:1:** el controlador implementa la interfaz generada `ProductsApi`. Nunca escribas
   a mano DTO web ni cambies rutas, nombres u operationId.
3. **Arquitectura hexagonal (ADR-0001):**
   - `domain`: Java puro. Sin Spring, sin JPA, sin puertos. Las reglas INV-xx viven aquí.
   - `application.port.in`: interfaces `*UseCase` + Commands, Queries y Results (`record`).
   - `application.port.out`: `ProductPersistencePort`.
   - `application.service`: un servicio por caso de uso; orquesta, no contiene reglas de negocio.
   - `adapter.in.web`: solo traduce HTTP <-> Command/Result y excepciones -> ErrorResponse.
   - `adapter.out.persistence`: implementa el puerto con JPA.
4. **El dominio nunca sale de `application`:** el controlador recibe `ProductResult`, nunca `Product`.
5. **Enums** idénticos a `specs/domain/` (mismos valores, mismo orden).
6. **Errores** exactamente como ADR-0004 (HTTP + `code`).
7. **Pruebas:** al menos una por criterio de aceptación, con el nombre
   `USxx_CAyy_descripcionEnIngles` (ej. `US03_CA02_activateWithoutPrice_isRejected`).
   Las reglas de dominio se prueban con JUnit puro, sin Spring.
8. **Idioma (`specs/conventions.md`):** código en inglés; documentación en castellano.
9. **Antes de cada commit:** `python tools/spec_lint.py` debe dar OK y `mvn test` debe pasar.
10. **Commits:** prefijos `spec:`, `design:`, `contract:`, `tooling:`, `feat:`, `test:`, `fix:`.

## Stack y comandos
Stack: `specs/architecture/tech-stack.md`. No agregues dependencias sin un ADR.

| Acción | Comando |
| :--- | :--- |
| Validar specs y contrato | `python tools/spec_lint.py` |
| Regenerar la API desde el contrato | `./mvnw clean generate-sources` |
| Compilar y probar | `./mvnw test` |
| Ejecutar la aplicación | `./mvnw spring-boot:run` |

Nunca edites archivos dentro de `target/generated-sources`: se regeneran desde el contrato.