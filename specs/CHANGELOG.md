# Registro de cambios de las especificaciones

Cada entrada documenta un cambio sobre `specs/` gestionado con `/cambio-spec`.

## 2026-10-18 - Sincronizar specs de diseño con el código

- **Descripción:** se corrige la documentación de diseño para que refleje el código existente, según
  la revisión `/sincronizar` del 2026-10-18 (`docs/sync-report.md`, hallazgos H-01, H-03, H-04,
  H-06, H-07 y H-08).
  - H-01: `tech-stack.md` registra springdoc-openapi 3.x (Swagger UI con el contrato), ya decidido en ADR-0008.
  - H-03: `tech-stack.md` cambia "JUnit 5" por "JUnit 6 (Jupiter)", la versión que trae Spring Boot 4.1 (ADR-0006).
  - H-04: `architecture.md` §5 añade `DomainException`, `RejectExplicitNullsModule`, `InvalidRequestException`
    y el paquete `config` (`ClockConfiguration`).
  - H-06: el `classDiagram` de `domain-model.md` §2 muestra el `Clock` en los métodos que modifican el
    producto y el método `restore`, como ya decía §4.
  - H-07: en `domain-model.md` §3, la columna "Dónde se garantiza" de INV-11 nombra también el rechazo
    del null explícito en `adapter.in.web` (ADR-0009), como ya decía §4.
  - H-08: se corrige la sangría del árbol de paquetes de `architecture.md` §5.
- **Motivo:** `spec_sync.py` informaba SYNC-10 (error) y SYNC-09 (3 advertencias), y la revisión manual
  encontró diagramas y textos desactualizados. No hay decisiones nuevas: el código y los ADR aceptados
  ya respaldaban cada punto.
- **Archivos de spec modificados:** `specs/architecture/tech-stack.md`, `specs/architecture/architecture.md`,
  `specs/architecture/domain-model.md`.
- **Compatibilidad del contrato:** sin cambios en `catalog-openapi.yaml`; `info.version` no cambia.
- **Commits previstos:**
  1. `design:` sincronizar tech-stack, architecture y domain-model con el código (este cambio).
  2. `refactor:` quitar INV-11 del javadoc de `ProductValidationException` (H-07).
  3. `docs:` regenerar `docs/sync-report.md` y mover la revisión manual a
     `docs/revisiones/2026-10-18-sincronizar.md`.
- **Fuera de alcance:** H-02, H-05 y H-10 (requieren decisiones nuevas) y H-09 (opcional).
