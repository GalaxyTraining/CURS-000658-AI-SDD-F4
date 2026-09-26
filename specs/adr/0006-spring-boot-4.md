# ADR-0006: Spring Boot 4.1 en lugar de 3.5

- **Estado:** Aceptado
- **Fecha:** 2026-09-26
- **Specs relacionadas:** `architecture/tech-stack.md`

## Contexto
El stack definía Spring Boot 3.5.x, pero Spring Initializr ya no ofrece la línea 3.x para proyectos nuevos.

## Decisión
Usaremos Spring Boot 4.1.x (versión estable) con Java 25.

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Mantener 3.5.x creando el pom a mano | Coincide con el diseño original. | Versión fuera de la oferta actual; menor tiempo de soporte. |
| Spring Boot 4.0.x | También estable. | Sin ventaja frente a 4.1.x para un proyecto nuevo. |

## Consecuencias
- La arquitectura no cambia: la hexagonal (ADR-0001) no depende de la versión del framework.
- Se debe verificar la compatibilidad de openapi-generator con Spring Boot 4 al generar la API.