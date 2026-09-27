# Prompt: Implementar una operación desde el contrato

```
Implementa la operación {{OPERATION_ID}} siguiendo CLAUDE.md.

1. ANALIZA (no escribas código todavía):
   - Busca {{OPERATION_ID}} en specs/api/catalog-openapi.yaml y lee sus x-spec-refs.
   - Lee esos RF e historias, y sus criterios de aceptación.
   - En specs/traceability.md identifica caso de uso, servicio e invariantes.
   - En specs/architecture/domain-model.md lee esas invariantes y dónde se garantizan.
   - Reporta cualquier vacío o contradicción. Si existe alguno, DETENTE aquí.
   - Revisa lo ya implementado (dominio, puertos, adaptadores) para reutilizarlo.

2. PLANIFICA: lista cada archivo a crear o modificar con su capa, paquete y la spec que lo origina.
   Espera mi aprobación antes de continuar.

3. IMPLEMENTA en este orden: dominio (solo si falta algo; si ya existe, reutilízalo y no lo dupliques)
   -> puertos -> servicio -> adaptadores.

4. PRUEBA: una prueba por criterio de aceptación (nombre USxx_CAyy_...), más los errores de ADR-0004
   que declare la operación.

5. VERIFICA: ejecuta python tools/spec_lint.py y ./mvnw test, y reporta el resultado.
```