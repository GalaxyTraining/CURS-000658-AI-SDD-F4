# ADR-0012: Colecciones Postman como verificación complementaria

- **Estado:** Aceptado
- **Fecha:** 2026-10-18
- **Specs relacionadas:** `api/catalog-openapi.yaml`, `domain/product-lifecycle.md`, `architecture/tech-stack.md`, `traceability.md`, ADR-0008

## Contexto
`mvn test` verifica cada capa con MockMvc, H2 y dobles de prueba, pero no ejercita la aplicación
desplegada de extremo a extremo. El repositorio ya tenía una colección de Postman sin respaldo en ninguna
spec, mezclada con carpetas vacías de una importación anterior.

## Decisión
1. Postman es una verificación **complementaria** contra la aplicación en ejecución
   (`./mvnw spring-boot:run`). No reemplaza `mvn test` ni es requisito para hacer commit.
2. `postman/catalogo-productos-contrato.postman_collection.json` se **genera** en Postman desde
   `specs/api/catalog-openapi.yaml`. Es un artefacto derivado: no se edita a mano. Cuando cambia el
   contrato se regenera y se vuelve a exportar (commit `contract:` o `docs:`).
3. `postman/catalogo-productos-flujo.postman_collection.json` recorre a mano el ciclo de vida de
   `domain/product-lifecycle.md` (registrar, SKU duplicado, consultar, actualizar, activar, descontinuar
   y listar), con aserciones `pm.test` y las variables `baseUrl`, `sku` y `productId`.

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Sin Postman | Una sola fuente de verificación. | No hay prueba manual ni E2E contra la aplicación desplegada. |
| Una sola colección mantenida a mano | Simple. | Se desincroniza del contrato sin que nadie lo note. |
| Postman como verificación obligatoria | Más cobertura E2E. | Requiere la aplicación levantada; duplica lo que ya cubre `mvn test`. |

## Consecuencias
- La colección de contrato sigue al contrato, y la de flujo sigue al ciclo de vida. Si cambia una
  transición, se revisa la colección de flujo.
- `traceability.md` indica qué cubre cada colección.
- Swagger UI (ADR-0008) sigue siendo la exploración interactiva; Postman añade escenarios encadenados.
