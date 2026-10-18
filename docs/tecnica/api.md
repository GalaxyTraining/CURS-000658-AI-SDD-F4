<!-- Generado con /documentar desde specs/ y src/. No editar a mano: cambia la spec y regenera. -->

# API REST

El contrato autoritativo es [catalog-openapi.yaml](../../specs/api/catalog-openapi.yaml) (versión 1.0.0,
OpenAPI 3.0.3). Este documento lo resume. Si hay diferencias, manda el contrato.
`ProductController` implementa la interfaz generada `ProductsApi`, así que las rutas, los nombres y los
`operationId` son exactamente los del contrato.

- **URL base:** `http://localhost:8080`
- **Prueba interactiva:** Swagger UI con este mismo contrato (ADR-0008, ver [README](README.md#ejecutar-y-probar-a-mano)).
- **Seguridad:** fuera de alcance en este release ([API-GUIDELINES.md](../../specs/api/API-GUIDELINES.md)).

## Resumen de operaciones

| `operationId` | Método y ruta | Éxito | Errores | Transición | Historia |
| :--- | :--- | :---: | :--- | :--- | :--- |
| [registerProduct](#registerproduct) | `POST /api/v1/products` | 201 | 400, 409, 500 | `- -> DRAFT` | RF-01 / US01 |
| [searchProducts](#searchproducts) | `GET /api/v1/products` | 200 | 400, 500 | — | RF-05 / US05 |
| [getProduct](#getproduct) | `GET /api/v1/products/{productId}` | 200 | 400, 404, 500 | — | RF-05 / US05 |
| [updateProduct](#updateproduct) | `PATCH /api/v1/products/{productId}` | 200 | 400, 404, 409, 500 | — | RF-02 / US02 |
| [activateProduct](#activateproduct) | `POST /api/v1/products/{productId}/activate` | 200 | 400, 404, 409, 422, 500 | `DRAFT -> ACTIVE` | RF-03 / US03 |
| [discontinueProduct](#discontinueproduct) | `POST /api/v1/products/{productId}/discontinue` | 200 | 400, 404, 409, 500 | `DRAFT\|ACTIVE -> DISCONTINUED` | RF-04 / US04 |

## Convenciones

Resumen de [API-GUIDELINES.md](../../specs/api/API-GUIDELINES.md):

- Prefijo con versión `/api/v1`. El `{productId}` de las rutas es un UUID (ADR-0003); si no lo es, la respuesta
  es 400.
- `POST` a la colección crea y responde `201` con el header `Location`.
- `PATCH` es parcial: solo cambian los campos enviados. Un `null` explícito en cualquier request, o un `PATCH`
  sin campos, responde 400 `VALIDATION_ERROR` (ADR-0009). Las propiedades que no son del esquema (por ejemplo
  `sku`) se ignoran.
- Las transiciones de estado son `POST` a un sub-recurso con verbo de negocio (ADR-0002).
- Los campos que decide el servidor (`id`, `status`, fechas) nunca aparecen en un `*Request`.
- `minLength` y `maxLength` se aplican al texto tal como se envía. Las reglas del dominio, que miden el texto
  sin espacios al inicio y al final, se aplican después.

## Errores

Todas las respuestas de error usan `ErrorResponse { code, message, details }`
([ADR-0004](../../specs/adr/0004-codigos-de-error.md), [ADR-0007](../../specs/adr/0007-error-interno-generico.md)).

| HTTP | `code` | Cuándo | Invariantes | Excepción |
| :---: | :--- | :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Formato, longitud, rango, valor fuera de catálogo, intento de quitar precio o categoría, null explícito o `PATCH` sin campos | INV-01, INV-04, INV-05, INV-09, INV-11 | `ProductValidationException` y errores de validación del adaptador web |
| 404 | `PRODUCT_NOT_FOUND` | El producto no existe | — | `ProductNotFoundException` |
| 409 | `DUPLICATE_SKU` | El SKU ya existe | INV-02 | `DuplicateSkuException` |
| 409 | `INVALID_STATE_TRANSITION` | Transición no permitida o producto descontinuado | INV-06, INV-08 | `InvalidStateTransitionException` |
| 422 | `PRODUCT_INCOMPLETE` | Activación sin precio o sin categoría | INV-07 | `ProductIncompleteException` |
| 500 | `INTERNAL_ERROR` | Error inesperado no mapeado | — | Cualquier otra excepción |

- El contrato es el HTTP y el `code`. `message` y `details` son informativos y están en inglés. La única
  excepción son los datos faltantes de `PRODUCT_INCOMPLETE`, que van en `details` (US03 CA-02).
- El 500 nunca expone el mensaje interno ni el stack trace: responde `message: "Unexpected error"` y
  `details: []`, y la excepción se registra en el log con nivel `ERROR` (ADR-0007).
- La traducción la hace [`GlobalExceptionHandler`](../../src/main/java/pe/edu/galaxy/training/java/sdd/catalog/adapter/in/web/GlobalExceptionHandler.java).

### Errores fuera del contrato (ADR-0010)

Cuando la petición ni siquiera llega a una operación del contrato, Spring MVC responde con su propio HTTP
y con el cuerpo de error por defecto de Spring Boot, no con `ErrorResponse`. Estos errores no se declaran
en el contrato ([ADR-0010](../../specs/adr/0010-errores-propios-de-spring-mvc.md)):

| HTTP | Cuándo |
| :---: | :--- |
| 404 | Ruta inexistente (no confundir con `PRODUCT_NOT_FOUND`, que es un producto inexistente en una ruta válida) |
| 405 | Método HTTP no permitido en una ruta existente |
| 406 | `Accept` que la operación no puede producir |
| 415 | `Content-Type` no soportado |

Nunca responden 500 `INTERNAL_ERROR`, y el cuerpo por defecto no expone mensaje ni traza
(`{"timestamp", "status", "error", "path"}`). Lo verifica `WebFrameworkErrorsTest`.

Ejemplo (`components/responses/ValidationError`):

```json
{
  "code": "VALIDATION_ERROR",
  "message": "La solicitud contiene datos inválidos",
  "details": ["sku: debe cumplir el formato AAA-9999"]
}
```

## Esquemas

| Esquema | Campos | Reglas |
| :--- | :--- | :--- |
| `RegisterProductRequest` | `sku`\*, `name`\*, `description`, `price`, `category` | `sku` con patrón `^[A-Z]{3}-\d{4}$`; `name` de 3 a 100; `description` de máximo 500; `price` > 0 y `multipleOf: 0.01`; `category` del catálogo |
| `UpdateProductRequest` | `name`, `description`, `price`, `category` | Mismas reglas que el registro; `minProperties: 1`; sin null |
| `DiscontinueProductRequest` | `reason`\* | De 10 a 500 caracteres |
| `ProductResponse` | `id`\*, `sku`\*, `name`\*, `description`, `price`, `category`, `status`\*, `discontinuationReason`, `createdAt`\*, `updatedAt`\* | `description`, `price`, `category` y `discontinuationReason` pueden ser `null` |
| `ProductPageResponse` | `items`\*, `page`\*, `size`\*, `totalElements`\*, `totalPages`\* | — |
| `ErrorResponse` | `code`\*, `message`\*, `details`\* | `code` del enum `ErrorCode` |

\* obligatorio. Enums: `ProductStatus` = `DRAFT`, `ACTIVE`, `DISCONTINUED`; `ProductCategory` =
`ELECTRONICS`, `HOME`, `FOOD`, `CLOTHING`, `OFFICE`; `ErrorCode` = `VALIDATION_ERROR`, `PRODUCT_NOT_FOUND`,
`DUPLICATE_SKU`, `INVALID_STATE_TRANSITION`, `PRODUCT_INCOMPLETE`, `INTERNAL_ERROR`.

---

## registerProduct

`POST /api/v1/products`: registra un producto en estado `DRAFT` (RF-01, US01).
Trazabilidad completa en [operaciones/registerProduct.md](operaciones/registerProduct.md).

- **Request:** `RegisterProductRequest`. Precio y categoría son opcionales (US01 CA-06).
- **201:** `ProductResponse`, con el header `Location` apuntando a `/api/v1/products/{id}`.
- **400** `VALIDATION_ERROR` · **409** `DUPLICATE_SKU` · **500** `INTERNAL_ERROR`.

Petición (ejemplo `completo` del contrato):

```json
{
  "sku": "ELE-0001",
  "name": "Mouse inalámbrico",
  "description": "Mouse óptico de 3 botones",
  "price": 49.90,
  "category": "ELECTRONICS"
}
```

Petición mínima (ejemplo `minimo`, US01 CA-06): `{ "sku": "OFF-0010", "name": "Engrapador" }`.

Respuesta 201 (ejemplo `creado`):

```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "sku": "ELE-0001",
  "name": "Mouse inalámbrico",
  "description": "Mouse óptico de 3 botones",
  "price": 49.90,
  "category": "ELECTRONICS",
  "status": "DRAFT",
  "discontinuationReason": null,
  "createdAt": "2026-09-26T15:00:00Z",
  "updatedAt": "2026-09-26T15:00:00Z"
}
```

Respuesta 409:

```json
{ "code": "DUPLICATE_SKU", "message": "Ya existe un producto con SKU ELE-0001", "details": [] }
```

## searchProducts

`GET /api/v1/products`: lista productos con filtros opcionales y paginación (RF-05, US05).
Trazabilidad completa en [operaciones/searchProducts.md](operaciones/searchProducts.md).

| Parámetro (query) | Tipo | Por defecto | Restricción |
| :--- | :--- | :---: | :--- |
| `status` | `ProductStatus` | — | Opcional |
| `category` | `ProductCategory` | — | Opcional |
| `page` | integer | 0 | ≥ 0 |
| `size` | integer | 20 | 1 a 100 |

- **200:** `ProductPageResponse`, ordenado por fecha de creación ascendente y, a igual fecha, por
  identificador (RF-05).
- **400** `VALIDATION_ERROR` (parámetro fuera de rango o valor fuera de catálogo) · **500** `INTERNAL_ERROR`.

Petición: `GET /api/v1/products?status=DRAFT&category=ELECTRONICS&page=0&size=20`

El contrato no trae un ejemplo de `ProductPageResponse`. La forma es esta, con `items` del tipo
`ProductResponse` (ver el ejemplo de [registerProduct](#registerproduct)):

```json
{ "items": [ /* ProductResponse */ ], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
```

## getProduct

`GET /api/v1/products/{productId}`: consulta un producto por su identificador (RF-05, US05).
Trazabilidad completa en [operaciones/getProduct.md](operaciones/getProduct.md).

- **200:** `ProductResponse` con todos los datos y el estado (US05 CA-01). Los productos descontinuados se
  siguen consultando (INV-10, US04 CA-04).
- **400** `VALIDATION_ERROR` (`productId` no es UUID) · **404** `PRODUCT_NOT_FOUND` · **500** `INTERNAL_ERROR`.

Respuesta 404 (`components/responses/NotFound`):

```json
{ "code": "PRODUCT_NOT_FOUND", "message": "No existe el producto solicitado", "details": [] }
```

## updateProduct

`PATCH /api/v1/products/{productId}`: modifica nombre, descripción, precio o categoría (RF-02, US02).
Trazabilidad completa en [operaciones/updateProduct.md](operaciones/updateProduct.md).

- **Request:** `UpdateProductRequest`. Solo cambian los campos enviados; precio y categoría se pueden
  reemplazar pero no quitar (INV-11). El SKU y el estado no forman parte del request (INV-03, ADR-0002).
- **200:** `ProductResponse`; el estado no cambia.
- **400** `VALIDATION_ERROR` (datos inválidos, null explícito o cuerpo sin campos) · **404** `PRODUCT_NOT_FOUND` ·
  **409** `INVALID_STATE_TRANSITION` (producto `DISCONTINUED`, INV-08) · **500** `INTERNAL_ERROR`.

Petición (ejemplo `nuevoPrecio` del contrato): `{ "price": 59.90 }`

Petición rechazada con 400 (ADR-0009): `{ "price": null }` y `{}`.

## activateProduct

`POST /api/v1/products/{productId}/activate`: activa un producto en `DRAFT` con precio y categoría
(RF-03, US03). Sin cuerpo.
Trazabilidad completa en [operaciones/activateProduct.md](operaciones/activateProduct.md).

- **200:** `ProductResponse` con `status: ACTIVE`.
- **400** `VALIDATION_ERROR` · **404** `PRODUCT_NOT_FOUND` · **409** `INVALID_STATE_TRANSITION` (no está en
  `DRAFT`, INV-06) · **422** `PRODUCT_INCOMPLETE` (falta precio o categoría, INV-07) · **500** `INTERNAL_ERROR`.

Respuesta 422 (ejemplo del contrato):

```json
{
  "code": "PRODUCT_INCOMPLETE",
  "message": "El producto no puede activarse porque le faltan datos",
  "details": ["price", "category"]
}
```

Respuesta 409 (`components/responses/InvalidStateTransition`):

```json
{ "code": "INVALID_STATE_TRANSITION", "message": "No se puede activar un producto en estado DISCONTINUED", "details": [] }
```

## discontinueProduct

`POST /api/v1/products/{productId}/discontinue`: descontinúa un producto en `DRAFT` o `ACTIVE`, con motivo
obligatorio. `DISCONTINUED` es un estado final (RF-04, US04).
Trazabilidad completa en [operaciones/discontinueProduct.md](operaciones/discontinueProduct.md).

- **Request:** `DiscontinueProductRequest`.
- **200:** `ProductResponse` con `status: DISCONTINUED` y `discontinuationReason` registrado.
- **400** `VALIDATION_ERROR` (motivo vacío, corto o largo, INV-09) · **404** `PRODUCT_NOT_FOUND` ·
  **409** `INVALID_STATE_TRANSITION` (ya descontinuado, INV-06) · **500** `INTERNAL_ERROR`.

Petición (ejemplo `motivo` del contrato):

```json
{ "reason": "El proveedor dejó de fabricarlo" }
```
