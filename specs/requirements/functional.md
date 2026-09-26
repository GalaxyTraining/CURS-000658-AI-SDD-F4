# Requerimientos Funcionales - Catálogo de Productos

### RF-01: Registrar producto
- Datos:
  - **SKU**: obligatorio, formato `AAA-9999` (3 letras mayúsculas, guion, 4 dígitos). Ej. `ELE-0001`.
    Único en todo el catálogo. No se puede modificar una vez registrado.
  - **Nombre**: obligatorio, entre 3 y 100 caracteres.
  - **Descripción**: opcional, máximo 500 caracteres.
  - **Precio**: opcional al registrar. Si se ingresa, debe ser mayor a 0, con máximo 2 decimales, en moneda única (PEN).
  - **Categoría**: opcional al registrar. Si se informa, debe pertenecer al catálogo cerrado de categorías.
- Todo producto nuevo inicia en estado **Borrador**.

### RF-02: Actualizar producto
- Permite modificar nombre, descripción, precio y categoría, con las mismas validaciones de RF-01.
- El SKU nunca se modifica.
- No se permite actualizar un producto **Descontinuado**.

### RF-03: Activar producto
- Solo un producto en **Borrador** puede activarse.
- Requisitos para activar: tener **precio** y **categoría** registrados.
- Si falta alguno, la activación se rechaza indicando qué dato falta.

### RF-04: Descontinuar producto
- Un producto en **Borrador** o **Activo** puede descontinuarse.
- Es obligatorio indicar el **motivo** (mínimo 10 caracteres).
- **Descontinuado** es un estado final: el producto no vuelve a ningún otro estado ni se modifica.
- Un producto nunca se elimina físicamente.

### RF-05: Consultar y listar productos
- Consultar un producto por su identificador.
- Listar productos con filtros opcionales por estado y categoría, con paginación.
