# Prompt: Implementar la capa de dominio

Se ejecuta una sola vez, antes de implementar operaciones (ver `01-implementar-operacion.md`).

```
Implementa la capa domain siguiendo CLAUDE.md.

1. ANALIZA (sin escribir código): lee specs/architecture/domain-model.md, specs/domain/product-lifecycle.md,
   specs/domain/catalogs.md, specs/adr/0001, 0003 y 0004, y las historias en specs/user-stories/.
   Reporta vacíos o contradicciones. Si hay alguno, DETENTE.

2. PLANIFICA: lista cada clase a crear en domain.model y domain.exception, indicando qué invariantes
   INV-xx garantiza y de qué spec sale. Espera mi aprobación.

3. IMPLEMENTA en Java puro (sin Spring, sin JPA, sin puertos).

4. PRUEBA con JUnit puro: una prueba por cada criterio de aceptación que dependa solo del dominio,
   con nombre USxx_CAyy_descripcion. Lista los criterios que NO son del dominio y en qué capa se probarán.

5. VERIFICA: ejecuta ./mvnw test y python tools/spec_lint.py, y reporta el resultado.
```