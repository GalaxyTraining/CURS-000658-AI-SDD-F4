# Visión - Catálogo de Productos

## 1. Propósito
Mantener un catálogo centralizado y confiable de los productos que la empresa ofrece,
asegurando que solo los productos completos y aprobados estén disponibles para la venta.

## 2. Problema a resolver
- Productos registrados con datos incompletos (sin precio o sin categoría) que aparecen como disponibles.
- Códigos de producto (SKU) duplicados o con formatos distintos.
- Productos que ya no se venden siguen apareciendo como vigentes.

## 3. Alcance
- Registrar productos en estado borrador.
- Activar un producto cuando cumple los requisitos mínimos.
- Descontinuar un producto que ya no se ofrece.
- Consultar y listar productos por estado y categoría.
- Actualizar los datos de un producto mientras no esté descontinuado.

## 4. Fuera de alcance (este release)
- Control de inventario / stock.
- Precios por cliente, descuentos o múltiples monedas.
- Imágenes del producto.
- Eliminación física de productos (un producto nunca se borra, se descontinúa).

## 5. Partes interesadas
| Rol | Interés principal |
| :--- | :--- |
| Administrador de catálogo | Registrar, activar y descontinuar productos sin errores. |
| Área de ventas | Consultar solo productos activos y con datos confiables. |
