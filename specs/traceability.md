# Matriz de Trazabilidad

| operationId | Caso de uso (port.in) | Servicio | RF / US | Invariantes | Pruebas mínimas |
| :--- | :--- | :--- | :--- | :--- | :--- |
| registerProduct | RegisterProductUseCase | RegisterProductService | RF-01 / US01 | INV-01, INV-02, INV-04, INV-05 | US01_CA01 a US01_CA06 |
| updateProduct | UpdateProductUseCase | UpdateProductService | RF-02 / US02 | INV-03, INV-04, INV-05, INV-08, INV-11 | US02_CA01 a US02_CA05 |
| activateProduct | ActivateProductUseCase | ActivateProductService | RF-03 / US03 | INV-06, INV-07 | US03_CA01 a US03_CA03 |
| discontinueProduct | DiscontinueProductUseCase | DiscontinueProductService | RF-04 / US04 | INV-06, INV-09, INV-10 | US04_CA01 a US04_CA04 |
| getProduct | GetProductUseCase | GetProductService | RF-05 / US05 | INV-10 | US05_CA01, US05_CA02 |
| searchProducts | SearchProductsUseCase | SearchProductsService | RF-05 / US05 | — | US05_CA03, US05_CA04 |