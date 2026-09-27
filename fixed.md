# Vacíos y contradicciones detectados antes de implementar el dominio

Origen: paso 1 (ANALIZA) de `prompts/00-implementar-dominio.md`, ejecutado el 2026-09-26.
Resultado: **implementación detenida** hasta que la spec se corrija (CLAUDE.md, regla 1).
Actualización 2026-09-26: todos los hallazgos se resolvieron en la spec; `spec_lint.py` da OK.

`python tools/spec_lint.py` da OK porque solo compara `domain/catalogs.md` con el contrato;
los hallazgos están entre otros documentos que el lint no cruza.

## Bloqueantes

| # | Hallazgo | Dónde | Cambio propuesto a la spec | Estado |
| :--- | :--- | :--- | :--- | :--- |
| 1 | La categoría `TOYS` existe en el catálogo y en el contrato, pero no en el modelo de dominio. Su descripción está en inglés ("Toys") y entró en el commit `176f2b4` (ADR-0006, `design:`), sin un commit `spec:` que la respalde. | `domain/catalogs.md:14`, `api/catalog-openapi.yaml:293`, `architecture/domain-model.md` §2 | Agregar `TOYS` al diagrama de `domain-model.md` y describirla como "Juguetes" en `catalogs.md`. Si no se quiere la categoría, quitarla también del contrato. | Resuelto: se quitó `TOYS` del catálogo y del contrato |
| 2 | El contrato limita el motivo de descontinuación a 500 caracteres, pero RF-04, INV-09 y US04 solo fijan el mínimo de 10. | `api/catalog-openapi.yaml` (`DiscontinueProductRequest.reason`), `requirements/functional.md` RF-04, `architecture/domain-model.md` INV-09 | Agregar "máximo 500 caracteres" a RF-04 y a INV-09. | Resuelto |
| 3 | INV-11 (no quitar precio ni categoría) no tiene código de error asignado. El contrato responde 400, pero ningún ADR lo dice. ADR-0001 y `architecture.md` siguen diciendo "INV-01 a INV-10". | `adr/0004-codigos-de-error.md`, `adr/0001-arquitectura-hexagonal.md`, `architecture/architecture.md` §2 | Añadir INV-11 a la fila `VALIDATION_ERROR` (400) de ADR-0004 y cambiar "INV-01 a INV-10" por "INV-01 a INV-11". | Resuelto |
| 4 | El diagrama de clases usa el tipo `ProductId`, que no figura en el glosario ni en la lista de clases de `domain.model`. | `architecture/domain-model.md` §1 y §2, `architecture/architecture.md` §5 | Definir `ProductId` como objeto de valor con un `UUID` (ADR-0003) y agregarlo al glosario y a §5. | Resuelto |
| 5 | La lista de excepciones de dominio está incompleta ("..."). No se sabe dónde van `ProductNotFoundException` y `DuplicateSkuException` ni cuál es la excepción de `VALIDATION_ERROR`. | `architecture/architecture.md` §5 | Una excepción por cada código de ADR-0004, todas en `domain.exception`: `ProductValidationException`, `ProductNotFoundException`, `DuplicateSkuException`, `InvalidStateTransitionException`, `ProductIncompleteException` (con la lista de datos faltantes, US03 CA-02). | Resuelto |

## Menores (supuestos que conviene fijar)

| # | Hallazgo | Dónde | Cambio propuesto a la spec | Estado |
| :--- | :--- | :--- | :--- | :--- |
| 6 | No está claro si `10.500` cumple "máximo 2 decimales". El `multipleOf: 0.01` del contrato lo acepta. | RF-01, INV-05 | Contar los decimales sin los ceros finales: `10.500` es válido y `10.505` no. | Resuelto |
| 7 | No se dice si el nombre y el motivo se validan sin los espacios del inicio y el final. Por ejemplo, `"   "` mide 3 caracteres pero está vacío. | RF-01, RF-04, INV-04, INV-09 | Validar las longitudes sin esos espacios. | Resuelto |
| 8 | Al final de `domain-model.md` hay texto de otro documento (un bloque `git commit` y el título "Paso 6"). | `architecture/domain-model.md` | Eliminar ese texto. | Resuelto |

## Siguiente paso
1. Aplicar los cambios aprobados en un commit `spec:`.
2. Verificar con `python tools/spec_lint.py`.
3. Retomar el paso 2 (PLANIFICA) de `prompts/00-implementar-dominio.md`.
