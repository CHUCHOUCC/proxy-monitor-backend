# Proxy Monitor · Backend (Spring Boot)

Sistema de monitoreo de microservicios con **Proxy de auditoría y logging**.
Tres microservicios (Inventario, Pedidos, Pagos) quedan detrás de un `LoggingProxy` que intercepta
cada llamada y registra métricas de uso, tiempos de respuesta y errores.

## Patrón Proxy en el código

| Rol del patrón | Clase |
|---|---|
| Subject (interfaz común) | `proxy/MicroserviceProxy<T>` → `T execute(String operation, Object... params)` |
| RealSubject | `service/InventoryService`, `service/OrderService`, `service/PaymentService` (heredan de `AbstractMicroservice`) |
| Proxy | `proxy/LoggingProxy<T>` (envuelve **cualquier** `MicroserviceProxy<T>`) |
| Client | `web/ServiceController`, `metrics/LoadSimulator` (solo conocen la interfaz, vía `ServiceRegistry`) |

El `LoggingProxy`:
1. **Antes** de ejecutar: genera un `requestId` (UUID), guarda el `timestamp` y registra el inicio.
2. **Después**: registra `durationMs`, `status = SUCCESS` y la respuesta.
3. **Si falla**: registra `status = ERROR`, el mensaje y un **stack trace resumido** (5 frames) y relanza la excepción con el `requestId`.

Cada log (`logging/CallLog`) incluye: `serviceId`, `operation`, `durationMs`, `status`, `requestId`, `timestamp`, `params`, `response`, `errorType`, `errorMessage`, `stackTrace`.

`PaymentService` falla intencionalmente el **10%** de las llamadas (`FailureInjector`).

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/services/inventory/{operation}` | `listProducts`, `getStock`, `addStock`, `removeStock` |
| POST | `/api/services/orders/{operation}` | `createOrder`, `getOrder`, `listOrders`, `cancelOrder` |
| POST | `/api/services/payments/{operation}` | `processPayment`, `getPayment`, `refundPayment` |
| GET | `/api/metrics/summary` | Total de llamadas, tasa de éxito/error y tiempo promedio por servicio |
| GET | `/api/metrics/logs?service=&status=&from=&to=&page=0&size=20` | Logs filtrados y paginados (más recientes primero). `from`/`to` en ISO-8601, ej. `2026-10-30T15:00:00Z` |
| POST | `/api/metrics/simulate-load` | Genera 50 llamadas aleatorias a través de los proxies |

El body de las operaciones es un JSON con argumentos con nombre:

```bash
curl -X POST localhost:8080/api/services/inventory/getStock -H "Content-Type: application/json" -d '{"productId":"P-001"}'
curl -X POST localhost:8080/api/services/orders/createOrder -H "Content-Type: application/json" -d '{"customer":"Ana","productId":"P-002","quantity":2}'
curl -X POST localhost:8080/api/services/payments/processPayment -H "Content-Type: application/json" -d '{"orderId":"ORD-1","amount":160000,"method":"PSE"}'
curl -X POST localhost:8080/api/metrics/simulate-load
curl "localhost:8080/api/metrics/logs?service=payments&status=ERROR&page=0&size=10"
```

Productos precargados: `P-001` a `P-005`.

## Ejecutar en local

Requisitos: Java 21 y Maven 3.9+.

```bash
mvn spring-boot:run      # levanta en http://localhost:8080
mvn test                 # pruebas del LoggingProxy
```

## Despliegue en Railway

1. Sube este proyecto a su propio repositorio de GitHub.
2. En Railway: **New Project → Deploy from GitHub repo** y selecciona el repo.
   Railway detecta el `Dockerfile` y construye la imagen (Java 21).
3. En **Settings → Networking → Generate Domain** para obtener la URL pública.
4. En **Variables** agrega:
   - `CORS_ALLOWED_ORIGINS` = URL del frontend en Vercel (ej. `https://proxy-monitor.vercel.app`).
     Puedes poner varias separadas por coma. Si no la defines, se permite cualquier origen (`*`).
   - (Opcional) `PAYMENTS_FAILURE_RATE` = `0.10`.
5. `PORT` lo inyecta Railway automáticamente.

> Nota: los logs se guardan en memoria (máx. 10.000). Se reinician cuando el servicio se redepliega o se reinicia.
