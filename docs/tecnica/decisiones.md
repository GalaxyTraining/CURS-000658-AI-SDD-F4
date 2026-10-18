<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# Decisiones de arquitectura (ADR)

Resumen de una línea por ADR. El texto que vale es el del ADR enlazado.
Un ADR aceptado no se edita: si una decisión cambia, se escribe un ADR nuevo que lo reemplaza
([plantilla](../../specs/adr/0000-template.md)).

| ADR | Estado | Decisión |
| :--- | :--- | :--- |
| [ADR-0001](../../specs/adr/0001-arquitectura-hexagonal.md) | Aceptado | Arquitectura hexagonal: `domain` en Java puro, puertos `in`/`out` en `application`, un servicio por caso de uso y un modelo de datos por frontera. |
| [ADR-0002](../../specs/adr/0002-transiciones-como-acciones.md) | Aceptado | Cada transición de estado es un `POST` a un sub-recurso con verbo de negocio (`/activate`, `/discontinue`); el estado nunca cambia por `PATCH`. |
| [ADR-0003](../../specs/adr/0003-identificador-uuid.md) | Aceptado | El `id` técnico es un UUID generado por el sistema y se usa en las rutas; el SKU es un atributo de negocio único. |
| [ADR-0004](../../specs/adr/0004-codigos-de-error.md) | Aceptado | Cada tipo de regla incumplida tiene un HTTP y un `code` propios (400, 404, 409, 422, 500), todos con `ErrorResponse { code, message, details }`. |
| [ADR-0005](../../specs/adr/0005-base-de-datos-h2.md) | Aceptado | H2 en memoria con Spring Data JPA, solo dentro de `adapter.out.persistence`; consola H2 en `/h2-console`, solo para desarrollo. |
| [ADR-0006](../../specs/adr/0006-spring-boot-4.md) | Aceptado | Spring Boot 4.1.x con Java 25. |
| [ADR-0007](../../specs/adr/0007-error-interno-generico.md) | Aceptado | Toda excepción no mapeada responde 500 `INTERNAL_ERROR` con mensaje genérico; el detalle solo va al log con nivel `ERROR`. Lo complementa ADR-0010. |
| [ADR-0008](../../specs/adr/0008-swagger-ui-con-contrato.md) | Aceptado | Swagger UI con springdoc, cargando el contrato autoritativo copiado desde `specs/api/` al compilar. |
| [ADR-0009](../../specs/adr/0009-null-explicito-en-requests.md) | Aceptado | Un `null` explícito en cualquier request y un `PATCH` sin campos responden 400 `VALIDATION_ERROR` desde el adaptador web; el dominio no cambia. |
| [ADR-0010](../../specs/adr/0010-errores-propios-de-spring-mvc.md) | Aceptado | Los errores propios de Spring MVC (ruta inexistente 404, 405, 406, 415) conservan su HTTP y el cuerpo por defecto de Spring Boot; quedan fuera del contrato y nunca responden 500. |
| [ADR-0012](../../specs/adr/0012-colecciones-postman.md) | Aceptado | Postman como verificación complementaria a `mvn test`: colección de contrato generada desde el OpenAPI y colección de flujo escrita a mano. |

Índice de ADR del proyecto: [specs/adr/README.md](../../specs/adr/README.md).
