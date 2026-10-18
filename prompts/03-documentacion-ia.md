# Módulo: AI Project Documentation — guía del caso práctico

**Caso práctico:** generar la documentación técnica del proyecto y verificar la consistencia entre las
especificaciones y la implementación.

## Herramientas

| Ítem del módulo | Comando / herramienta | Produce |
| :--- | :--- | :--- |
| 1. Generar documentación técnica automáticamente | `/documentar` | `docs/tecnica/` |
| 2. Actualizar diagramas y modelos arquitectónicos | `/diagramas` | Diagramas de `specs/` corregidos (`design:`) y `docs/tecnica/diagramas.md` |
| 3. Sincronizar especificaciones con la implementación | `python tools/spec_sync.py` + `/sincronizar` | `docs/sync-report.md` |
| 4. Gestionar cambios sobre las especificaciones | `/cambio-spec <cambio>` | Specs actualizadas + `specs/CHANGELOG.md` |
| 5. Preparar la documentación final | `/documentacion-final` | `docs/entrega/` |

`spec_lint.py` valida el contrato **antes** de implementar. `spec_sync.py` verifica **después** que el
código, las pruebas y la documentación siguen diciendo lo mismo que las specs. Es determinista y se puede
usar en CI. Lo que requiere criterio, como "¿esta prueba verifica de verdad el criterio?", lo revisa
`/sincronizar`.

## Secuencia

Usa `/clear` entre pasos: cada paso empieza leyendo las specs, no el historial.

| # | Acción | Resultado esperado en `catalogo-productos-v2` |
| :--- | :--- | :--- |
| 1 | `python tools/spec_sync.py` | **1 error:** `SYNC-10`, springdoc está en `pom.xml` pero no en `tech-stack.md` (ADR-0008 se implementó sin actualizar el stack). **3 avisos:** `SYNC-09` (`DomainException`, `InvalidRequestException` y `RejectExplicitNullsModule` no aparecen en ninguna spec). |
| 2 | `/sincronizar` | Clasifica los hallazgos. Esperado: SYNC-10 → `spec:` (documentar springdoc en `tech-stack.md`); SYNC-09 → decidir si `architecture.md` §5 los menciona (los dos últimos implementan ADR-0009). |
| 3 | `/cambio-spec Registrar springdoc-openapi en tech-stack.md según ADR-0008` | Análisis de impacto, commit `spec:` y entrada en `specs/CHANGELOG.md`. Después `spec_sync` debe dar **OK**. |
| 4 | `/diagramas` | Detecta al menos un error de forma: el árbol de paquetes de `architecture.md` §5 tiene `application` fuera de la raíz `pe.edu...catalog`. Commit `design:`. |
| 5 | `/documentar` | `docs/tecnica/` completa, con fuente citada en cada sección. Commit `docs:`. |
| 6 | Demostrar la gestión de un cambio de negocio, por ejemplo: `/cambio-spec Agregar la categoría SPORTS al catálogo` | Impacto completo: `catalogs.md` → `domain-model.md` → contrato (cambio compatible, versión menor) → enum Java → pruebas → docs → Postman. Ejecuta luego `/implementar` o el `fix:` que indique, `/sincronizar` y `/documentar`. |
| 7 | `/documentacion-final` | `docs/entrega/` con la evidencia de `spec_lint`, `spec_sync` y las pruebas. Commit `docs:` y tag propuesto `v1.0.0`. |

## Criterio de éxito del caso práctico
- `python tools/spec_lint.py` y `python tools/spec_sync.py` dan **OK**.
- 22/22 criterios de aceptación con al menos una prueba (`docs/sync-report.md`).
- `./mvnw test` en verde.
- La documentación de `docs/` cita su fuente y no contiene reglas que no estén en `specs/`.
- Cada cambio de spec tiene su commit `spec:`/`design:`/`contract:` antes del código y su entrada en el CHANGELOG.
