# Overview (contrato, código, pruebas)

# Fase 1 (especificación del negocio)

## Paso 1: Documento Vision
### Step 1
mkdir catalogo-productos && cd catalogo-productos
git init
mkdir -p specs/requirements specs/domain specs/user-stories specs/api specs/adr specs/architecture

### 2

git add specs/
git commit -m "spec: visión y alcance del catálogo de productos"

### 3
.gitignore

echo "stepts-f2.md" >> .gitignore
git add .gitignore
git commit -m "chore: ignorar notas personales"

## Paso 2: Requerimientos funcionales

### Modificar documento vision

- Actualizar los datos de un producto mientras no esté descontinuado.
git add specs/vision.md
git commit -m "spec: agregar edición de productos al alcance (vacío detectado al definir RF)"

### *
add specs/requirements/functional.md
git add specs/requirements/functional.md
git commit -m "spec: requerimientos funcionales RF-01 a RF-05"

## Paso 3: Dominio (convenciones, ciclo de vida y catálogos)
add specs/conventions.md

### *
add specs/conventions.md

````
git add specs/conventions.md
git commit -m "spec: convenciones de idioma y códigos de dominio"
````
add specs/domain/product-lifecycle.md
add specs/domain/catalogs.md

### *
````
git add specs/domain/
git commit -m "spec: ciclo de vida del producto y catálogo de categorías"
````
## Paso 4: Historias de usuario con criterios de aceptación

specs/user-stories/*
````
git add specs/user-stories/
git commit -m "spec: historias de usuario US01 a US05 con criterios de aceptación"
````


# Fase 1 (especificación del negocio)

## Paso 5: Modelo de dominio

add architecture/domain-model.md

## Paso 6: Arquitectura Hexagonal 

add specs/architecture/architecture.md

````
git add specs/architecture/architecture.md
git commit -m "design: arquitectura hexagonal con puertos in/out y modelos por frontera"
````

## Paso 7: Decisiones de arquitectura (ADR)

git add specs/adr/

````
git add specs/adr/
git commit -m "design: ADR 0001 a 0005 (hexagonal, acciones, UUID, errores, H2)"
````

## Paso 8: Diseño del contrato OpenAPI

git add specs/api/

````
git add specs/api/
git commit -m "contract: guía de diseño y contrato OpenAPI v1.0.0 (6 operaciones)"
````

## Paso 9: Validar el contrato antes de implementar

````
pip install pyyaml
python tools/spec_lint.py
````

## Paso 10: Preparar las specs para la generación con IA

CLAUDE.md
.github/copilot-instructions.md
specs/traceability.md
prompts/01-implementar-operacion.md

# Fase 3: implementación

java -version

