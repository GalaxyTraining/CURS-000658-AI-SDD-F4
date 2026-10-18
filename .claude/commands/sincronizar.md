---
description: Verifica la consistencia entre especificaciones e implementación y propone cómo resolver cada desvío (ítem 3)
---

Verifica que specs/, código, pruebas y documentación digan lo mismo, siguiendo CLAUDE.md.

## Paso 1 — Verificación automática (determinista)
Ejecuta y reporta la salida completa:
1. `python tools/spec_lint.py` — contrato consistente con las specs de negocio.
2. `python tools/spec_sync.py --md docs/sync-report.md` — operaciones implementadas, traceability,
   criterios de aceptación con prueba, invariantes, índice de ADR, tipos de architecture.md, stack.
3. `./mvnw test` (en Windows `.\mvnw.cmd test`) — número de pruebas, fallos y omitidas.

## Paso 2 — Verificación con criterio (lo que un script no puede juzgar)
Revisa y reporta con evidencia (archivo:línea):
- **Reglas:** cada INV-xx se garantiza donde `domain-model.md` §3 dice que se garantiza, y no en otra capa.
- **Errores:** cada excepción se traduce al HTTP y `code` de ADR-0004 en `GlobalExceptionHandler`.
- **Arquitectura:** ningún `import` viola la tabla de `architecture.md` §2 (p. ej. `adapter.in.web` → `domain.model`).
- **ADR aceptados:** cada ADR se cumple en el código (p. ej. ADR-0007 sin detalles internos en el 500,
  ADR-0009 rechazo de null explícito, ADR-0008 Swagger UI con el contrato).
- **Pruebas:** que las pruebas `USxx_CAyy` realmente verifiquen lo que dice el criterio (no solo el nombre).
- **Documentación:** si existe `docs/tecnica/`, que no contradiga las specs.

## Paso 3 — Clasificación de cada desvío
| Desvío | Quién tiene razón | Resolución | Commit |
| :--- | :--- | :--- | :--- |
| El código hace lo que la spec dice, pero la spec está incompleta o desactualizada (p. ej. una dependencia sin documentar) | El código, **si** la decisión fue legítima | Actualizar la spec (y un ADR si es una decisión técnica) | `spec:` / `design:` |
| El código no cumple la spec | La spec | Corregir el código con su prueba | `fix:` |
| Estructura distinta con el mismo comportamiento | La spec | Refactor (usar `/refactorizar`) | `refactor:` |
| Falta una prueba de un criterio | La spec | Agregar la prueba | `test:` |
| La documentación derivada no coincide | La spec | Regenerar con `/documentar` | `docs:` |

Nunca resuelvas un desvío "ajustando la spec al código" sin decir explícitamente que es una decisión
nueva: en SDD eso requiere mi aprobación.

## Paso 4 — Informe
Tabla final de hallazgos (ID, tipo, evidencia, resolución propuesta, commit) añadida al final de
`docs/sync-report.md`. **No corrijas nada** hasta que yo elija qué hallazgos resolver.
