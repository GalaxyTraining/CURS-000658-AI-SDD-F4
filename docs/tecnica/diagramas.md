<!-- Generado con /diagramas desde specs/ y src/. No editar a mano: cambia la spec o el código y regenera. -->

# Diagramas por operación

Documentación derivada: un diagrama de secuencia por `operationId` del
[contrato](../../specs/api/catalog-openapi.yaml), con el formato de
[architecture.md §6](../../specs/architecture/architecture.md), y un diagrama de componentes
adaptador → puerto → servicio. Si contradicen una spec, manda la spec y este archivo se regenera.

Convenciones comunes a todas las secuencias:

- `GlobalExceptionHandler` traduce cada excepción a HTTP + `code` según
  [ADR-0004](../../specs/adr/0004-codigos-de-error.md).
- Los 400 por restricciones del contrato sobre los DTO generados (patrón, longitud, tipos, null
  explícito) los rechaza Spring antes de llegar al controlador ([ADR-0009](../../specs/adr/0009-null-explicito-en-requests.md),
  [ADR-0010](../../specs/adr/0010-errores-propios-de-spring-mvc.md)); no se repiten en cada diagrama.
- Cualquier error no previsto responde 500 `INTERNAL_ERROR` ([ADR-0007](../../specs/adr/0007-error-interno-generico.md)).

## Componentes: adaptador → puerto → servicio

```mermaid
flowchart LR
    subgraph AIN["adapter.in.web"]
        API["ProductsApi<br/>(interfaz generada)"]
        CTRL["ProductController"]
        WM["ProductWebMapper"]
        GEH["GlobalExceptionHandler"]
    end
    subgraph PIN["application.port.in.usecase"]
        UC1["RegisterProductUseCase"]
        UC2["UpdateProductUseCase"]
        UC3["ActivateProductUseCase"]
        UC4["DiscontinueProductUseCase"]
        UC5["GetProductUseCase"]
        UC6["SearchProductsUseCase"]
    end
    subgraph SVC["application.service"]
        S1["RegisterProductService"]
        S2["UpdateProductService"]
        S3["ActivateProductService"]
        S4["DiscontinueProductService"]
        S5["GetProductService"]
        S6["SearchProductsService"]
    end
    subgraph POUT["application.port.out"]
        PP["ProductPersistencePort"]
    end
    subgraph AOUT["adapter.out.persistence"]
        PAD["ProductPersistenceAdapter"]
        PM["ProductPersistenceMapper"]
        JPA["SpringDataProductJpa"]
    end
    DB[("H2")]

    CTRL -. "implementa" .-> API
    CTRL -- "usa" --> WM
    CTRL -- "usa" --> UC1 & UC2 & UC3 & UC4 & UC5 & UC6
    UC1 -. "implementada por" .-> S1
    UC2 -. "implementada por" .-> S2
    UC3 -. "implementada por" .-> S3
    UC4 -. "implementada por" .-> S4
    UC5 -. "implementada por" .-> S5
    UC6 -. "implementada por" .-> S6
    S1 & S2 & S3 & S4 & S5 & S6 -- "usa" --> PP
    PP -. "implementada por" .-> PAD
    PAD -- "usa" --> PM
    PAD -- "usa" --> JPA
    JPA --> DB
```

## registerProduct (US01)

`POST /api/v1/products` · [operaciones/registerProduct.md](operaciones/registerProduct.md)

```mermaid
sequenceDiagram
    autonumber
    actor A as Administrador
    participant C as ProductController
    participant H as GlobalExceptionHandler
    participant UC as RegisterProductUseCase
    participant S as RegisterProductService
    participant PP as ProductPersistencePort
    participant P as Product

    A->>C: POST /api/v1/products
    C->>C: ProductWebMapper a RegisterProductCommand
    C->>UC: register(RegisterProductCommand)
    UC->>S: (implementación)
    S->>S: Sku.of(sku)
    alt formato de SKU inválido
        S-->>H: ProductValidationException (INV-01)
        H-->>A: 400
    else SKU válido
        S->>PP: existsBySku(sku)
        alt SKU ya existe
            PP-->>S: true
            S-->>H: DuplicateSkuException (INV-02)
            H-->>A: 409
        else SKU libre
            PP-->>S: false
            S->>S: Price.of(price) si se envió
            alt precio inválido
                S-->>H: ProductValidationException (INV-05)
                H-->>A: 400
            else precio válido o ausente
                S->>P: register(sku, name, description, price, category, clock)
                alt nombre o descripción inválidos
                    P-->>S: ProductValidationException (INV-04)
                    S-->>H: excepción
                    H-->>A: 400
                else válido
                    P-->>S: Product (status = DRAFT)
                    S->>PP: save(product)
                    alt SKU duplicado por concurrencia
                        PP-->>S: DuplicateSkuException (INV-02, restricción única)
                        S-->>H: excepción
                        H-->>A: 409
                    else guardado
                        PP-->>S: Product
                        S->>S: ProductResult.from(product)
                        S-->>C: ProductResult
                        C->>C: ProductWebMapper a ProductResponse
                        C-->>A: 201 ProductResponse + Location
                    end
                end
            end
        end
    end
```

## updateProduct (US02)

`PATCH /api/v1/products/{productId}` · [operaciones/updateProduct.md](operaciones/updateProduct.md)

```mermaid
sequenceDiagram
    autonumber
    actor A as Administrador
    participant C as ProductController
    participant H as GlobalExceptionHandler
    participant UC as UpdateProductUseCase
    participant S as UpdateProductService
    participant PP as ProductPersistencePort
    participant P as Product

    A->>C: PATCH /api/v1/products/{id}
    C->>C: ProductWebMapper a UpdateProductCommand
    alt cuerpo sin ningún campo
        C-->>H: InvalidRequestException (ADR-0009)
        H-->>A: 400
    else al menos un campo
        C->>UC: update(UpdateProductCommand)
        UC->>S: (implementación)
        S->>PP: findById(id)
        alt no existe
            PP-->>S: vacío
            S-->>H: ProductNotFoundException
            H-->>A: 404
        else existe
            PP-->>S: Product
            S->>S: Price.of(price) si se envió
            alt precio inválido
                S-->>H: ProductValidationException (INV-05)
                H-->>A: 400
            else precio válido o ausente
                S->>P: update(name, description, price, category, clock)
                alt estado es DISCONTINUED
                    P-->>S: InvalidStateTransitionException (INV-08)
                    S-->>H: excepción
                    H-->>A: 409
                else nombre o descripción inválidos
                    P-->>S: ProductValidationException (INV-04)
                    S-->>H: excepción
                    H-->>A: 400
                else válido
                    P->>P: aplica los campos enviados (null = conservar, INV-11)
                    S->>PP: save(product)
                    PP-->>S: Product
                    S->>S: ProductResult.from(product)
                    S-->>C: ProductResult
                    C->>C: ProductWebMapper a ProductResponse
                    C-->>A: 200 ProductResponse
                end
            end
        end
    end
```

## activateProduct (US03)

`POST /api/v1/products/{productId}/activate` · [operaciones/activateProduct.md](operaciones/activateProduct.md)

```mermaid
sequenceDiagram
    autonumber
    actor A as Administrador
    participant C as ProductController
    participant H as GlobalExceptionHandler
    participant UC as ActivateProductUseCase
    participant S as ActivateProductService
    participant PP as ProductPersistencePort
    participant P as Product

    A->>C: POST /api/v1/products/{id}/activate
    C->>UC: activate(ActivateProductCommand)
    UC->>S: (implementación)
    S->>PP: findById(id)
    alt no existe
        PP-->>S: vacío
        S-->>H: ProductNotFoundException
        H-->>A: 404
    else existe
        PP-->>S: Product
        S->>P: activate(clock)
        alt estado no es DRAFT
            P-->>S: InvalidStateTransitionException (INV-06)
            S-->>H: excepción
            H-->>A: 409
        else falta precio o categoría
            P-->>S: ProductIncompleteException (INV-07)
            S-->>H: excepción
            H-->>A: 422
        else válido
            P->>P: status = ACTIVE
            S->>PP: save(product)
            PP-->>S: Product
            S->>S: ProductResult.from(product)
            S-->>C: ProductResult
            C->>C: ProductWebMapper a ProductResponse
            C-->>A: 200 ProductResponse
        end
    end
```

## discontinueProduct (US04)

`POST /api/v1/products/{productId}/discontinue` · [operaciones/discontinueProduct.md](operaciones/discontinueProduct.md)

```mermaid
sequenceDiagram
    autonumber
    actor A as Administrador
    participant C as ProductController
    participant H as GlobalExceptionHandler
    participant UC as DiscontinueProductUseCase
    participant S as DiscontinueProductService
    participant PP as ProductPersistencePort
    participant P as Product

    A->>C: POST /api/v1/products/{id}/discontinue
    C->>C: ProductWebMapper a DiscontinueProductCommand
    C->>UC: discontinue(DiscontinueProductCommand)
    UC->>S: (implementación)
    S->>PP: findById(id)
    alt no existe
        PP-->>S: vacío
        S-->>H: ProductNotFoundException
        H-->>A: 404
    else existe
        PP-->>S: Product
        S->>P: discontinue(reason, clock)
        alt estado es DISCONTINUED
            P-->>S: InvalidStateTransitionException (INV-06)
            S-->>H: excepción
            H-->>A: 409
        else motivo fuera de 10 a 500 caracteres
            P-->>S: ProductValidationException (INV-09)
            S-->>H: excepción
            H-->>A: 400
        else válido
            P->>P: status = DISCONTINUED, guarda el motivo
            S->>PP: save(product)
            PP-->>S: Product
            S->>S: ProductResult.from(product)
            S-->>C: ProductResult
            C->>C: ProductWebMapper a ProductResponse
            C-->>A: 200 ProductResponse
        end
    end
```

## getProduct (US05)

`GET /api/v1/products/{productId}` · [operaciones/getProduct.md](operaciones/getProduct.md)

```mermaid
sequenceDiagram
    autonumber
    actor V as Área de ventas
    participant C as ProductController
    participant H as GlobalExceptionHandler
    participant UC as GetProductUseCase
    participant S as GetProductService
    participant PP as ProductPersistencePort

    V->>C: GET /api/v1/products/{id}
    C->>UC: get(GetProductQuery)
    UC->>S: (implementación)
    S->>PP: findById(id)
    alt no existe
        PP-->>S: vacío
        S-->>H: ProductNotFoundException
        H-->>V: 404
    else existe
        PP-->>S: Product
        S->>S: ProductResult.from(product)
        S-->>C: ProductResult
        C->>C: ProductWebMapper a ProductResponse
        C-->>V: 200 ProductResponse
    end
```

## searchProducts (US05)

`GET /api/v1/products` · [operaciones/searchProducts.md](operaciones/searchProducts.md)

```mermaid
sequenceDiagram
    autonumber
    actor V as Área de ventas
    participant C as ProductController
    participant UC as SearchProductsUseCase
    participant S as SearchProductsService
    participant PP as ProductPersistencePort

    V->>C: GET /api/v1/products?status&category&page&size
    C->>C: ProductWebMapper a SearchProductsQuery
    C->>UC: search(SearchProductsQuery)
    UC->>S: (implementación)
    S->>PP: search(status, category, page, size)
    PP-->>S: ProductPage(items, totalElements)
    S->>S: ProductResult.from por item y cálculo de totalPages
    S-->>C: ProductPageResult
    C->>C: ProductWebMapper a ProductPageResponse
    C-->>V: 200 ProductPageResponse
```

Un filtro ausente significa "sin filtro"; el orden de los resultados es el de RF-05
([architecture.md §4](../../specs/architecture/architecture.md)).
