# Guía de Diseño de API

## Rutas y operaciones
- Prefijo con versión: `/api/v1/...`. Recursos en plural y kebab-case: `/products`.
- Identificador en la ruta: UUID (`{productId}`), ver ADR-0003.
- Crear: `POST` a la colección → `201` + header `Location`.
- Consultar/listar: `GET` → `200`.
- Actualizar datos: `PATCH` con semántica parcial (solo los campos enviados cambian; no se admite null).
- Transiciones de estado: `POST` a sub-recurso con verbo de negocio (`/activate`, `/discontinue`), ver ADR-0002.
- `operationId` en camelCase igual al nombre del caso de uso (`activateProduct` ↔ `ActivateProductUseCase`).

## Trazabilidad
- Toda operación declara `x-spec-refs` con los RF y US que la originan.
- Toda operación que cambia el estado declara `x-transition` con estados de `domain/product-lifecycle.md`.

## Modelos
- Todo esquema reutilizable va en `components/schemas`.
- Sufijos: `*Request` (entrada), `*Response` (salida).
- Enums idénticos a `specs/domain/` (mismos códigos, mismo orden).
- Nunca poner otras claves junto a `$ref` (OpenAPI 3.0 las ignora): usar `allOf` + `nullable`.
- Campos que decide el servidor (`id`, `status`, fechas) nunca aparecen en un `*Request`.

## Errores
- Siempre `ErrorResponse { code, message, details }` con los códigos y HTTP de ADR-0004.

## Seguridad
- Fuera de alcance en este release (no figura en `vision.md`).
