# Stack Tecnológico - Catálogo de Productos

Fuente única del stack. Las versiones exactas están en `pom.xml`.
Agregar o cambiar una tecnología requiere un ADR.

| Elemento | Tecnología | Uso | Decisión |
| :--- | :--- | :--- | :--- |
| Lenguaje | Java (versión en `pom.xml`, `java.version`) | Todo el código | — |
| Framework | Spring Boot 4.1.x | Web, Validation, Data JPA | ADR-0001, ADR-0006 |
| Base de datos | H2 en memoria | Persistencia en `adapter.out.persistence` | ADR-0005 |
| Build | Maven con wrapper (`./mvnw`) | Compilación, generación y pruebas | — |
| Contrato | OpenAPI 3.0.3 | `specs/api/catalog-openapi.yaml` | — |
| Pruebas | JUnit 6 (Jupiter) + Spring Boot Test | Dominio sin Spring; adaptadores con Spring | ADR-0006 |
| Validación de specs | Python 3 + PyYAML (`tools/spec_lint.py`) | Consistencia contrato ↔ dominio | — |
| Generación de API | openapi-generator ≥ 7.20 (`spring`, `interfaceOnly`, `useSpringBoot4`, `useJackson3`) | `ProductsApi` y DTO web en `adapter.in.web.generated` | ADR-0006 |
| Documentación de la API | springdoc-openapi 3.x (`springdoc-openapi-starter-webmvc-ui`) | Swagger UI que carga el contrato autoritativo (`/catalog-openapi.yaml`) | ADR-0008 |

## Restricciones
- El paquete `domain` solo usa el JDK (sin Spring, JPA ni librerías externas).
- No se agregan dependencias (ej. Lombok, MapStruct) sin un ADR que las justifique.