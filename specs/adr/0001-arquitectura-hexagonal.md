# ADR-0001: Arquitectura hexagonal con puertos in/out y modelos por frontera

- **Estado:** Aceptado
- **Fecha:** 2026-09-26
- **Specs relacionadas:** `architecture/architecture.md`, `architecture/domain-model.md`

## Contexto
Las reglas del producto (INV-01 a INV-11) deben poder probarse sin levantar servidor ni base de datos,
y el contrato REST debe poder cambiar sin afectar al dominio.

## Decisión
Usaremos arquitectura hexagonal:
- `domain`: modelo y reglas en Java puro, sin puertos ni dependencias de frameworks.
- `application.port.in`: interfaces de casos de uso con Commands, Queries y Results.
- `application.port.out`: interfaces de lo que la aplicación necesita (`ProductPersistencePort`).
- `application.service`: un servicio por caso de uso.
- `adapter.in.web` y `adapter.out.persistence`: implementaciones técnicas.
Cada frontera tiene su propio modelo de datos; el dominio nunca se expone fuera de `application`.

## Alternativas consideradas
| Alternativa | A favor | En contra |
| :--- | :--- | :--- |
| Capas clásicas (controller-service-repository) | Menos clases, arranque rápido. | Entidades JPA como dominio; reglas dispersas en servicios; difícil probar sin Spring. |
| Hexagonal con repositorio en el dominio | Menos paquetes. | El dominio conoce la persistencia; mezcla responsabilidades. |

## Consecuencias
- Positivas: dominio probable con JUnit puro; cambios en contrato o base de datos quedan aislados en su adaptador.
- Negativas: más clases y mapeos (Request → Command → Product → Entity).
- Verificación: prueba de arquitectura (ArchUnit) con las reglas de la tabla de `architecture.md` §2.