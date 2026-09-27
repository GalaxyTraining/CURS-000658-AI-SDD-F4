# Anexo: Consideraciones de la primera operación

Se agrega al final del prompt `01-implementar-operacion.md` solo al implementar la primera
operación (registerProduct), porque crea la base que reutilizarán las demás.

```
CONSIDERACIONES DE PRIMERA OPERACIÓN (esta base la reutilizarán las demás):
- ProductPersistencePort: declara las 4 operaciones de specs/architecture/architecture.md §4
  (save, findById, existsBySku, search) e implementa las 4 en el adaptador de persistencia.
- Commands, Queries y Results como record en application.port.in; el controlador nunca recibe Product.
- GlobalExceptionHandler: mapea TODAS las excepciones de dominio a ErrorResponse según la tabla de
  ADR-0004, más los errores de validación de Spring (incluido un valor de enum inválido en el JSON,
  que corresponde a US01 CA-05) como 400 VALIDATION_ERROR.
- La hora actual se obtiene de un java.time.Clock inyectado (no uses Instant.now() en los servicios),
  para que las pruebas sean deterministas.
- El controlador implementa ProductsApi completa. Las otras 5 operaciones NO se implementan ahora:
  deja en cada una solo un TODO que cite su operationId y lance UnsupportedOperationException.
- El 201 debe incluir el header Location (/api/v1/products/{id}).
- Pruebas por capa: servicio con un doble del puerto de salida (US01 CA-02, SKU duplicado), controlador
  con prueba web (201, 400 y 409 con su code), y adaptador de persistencia con prueba de base de datos.
- Es Spring Boot 4: si una prueba necesita una dependencia de test que no está en el pom.xml,
  PROPÓNLA en el plan y espera aprobación; no la agregues por tu cuenta ni asumas APIs de Spring Boot 3.
```