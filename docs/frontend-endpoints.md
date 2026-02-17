# Frontend endpoints (gateway-api)

Este documento resume los endpoints expuestos por el API Gateway y como se usan desde la UI. El gateway enruta a microservicios internos y aplica seguridad cuando el profile `auth` esta activo.

## Base URL y autenticacion

- Base URL dev: `http://localhost:8080`
- Cuando el profile `auth` esta activo, todas las rutas (excepto health) requieren JWT.
- Header esperado:
  - `Authorization: Bearer <token>`
- Respuesta de error de autenticacion (401) incluye:
  - `error`, `message`, `details`, `timestamp`, `status`

## Rutas principales (gateway -> microservicios)

Todas estas rutas usan el prefijo `/api/<service>/...`. El gateway hace `StripPrefix=2`, por lo que el microservicio recibe el path sin `/api/<service>`.

| Ruta gateway | Servicio destino | Uso UI (referencial) |
| --- | --- | --- |
| `/api/portfolio/**` | portfolio-service | Pantallas de portfolio, holdings, movimientos, resumen de cuenta. |
| `/api/prices/**` | prices-service | Consultas de precios, cotizaciones, historicos para charts. |
| `/api/alert/**` | alert-engine | Crear/editar alertas y ver estado de disparo. |
| `/api/notification/**` | notification-service | Listado de notificaciones y detalles de mensajes. |
| `/api/recommendation/**` | recommendation-service | Recomendaciones para el usuario. Timeout de respuesta mas alto. |

Notas:
- Las rutas exactas dentro de cada microservicio se definen en cada repo. El frontend solo debe usar el prefijo del gateway.
- El gateway aplica circuit breaker. Si un servicio esta caido, responde 503 via endpoints de fallback.

## Endpoints de soporte

### Health
- Publicos aun con `auth`:
  - `/actuator/health`
  - `/actuator/info`
  - `/api/*/actuator/health`
  - `/api/*/actuator/info`

### Test (debug de auth)
- Requieren JWT:
  - `GET /api/test/hello`
  - `GET /api/test/claims`
  - `GET /api/test/roles`

Uso UI: solo para debug local y verificacion de token.

### Fallback (no consumir desde UI)
Cuando un servicio falla, el gateway responde 503 con el body siguiente (ejemplo):

```json
{
  "error": "service_unavailable",
  "message": "Portfolio service is temporarily unavailable",
  "service": "portfolio-service",
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 503
}
```

Endpoints de fallback:
- `/fallback/portfolio`
- `/fallback/prices`
- `/fallback/recommendation`

## Recomendaciones para la UI

- Cliente HTTP centralizado: base URL `http://localhost:8080` (o env), y manejo de `Authorization` en un interceptor.
- Manejo de errores:
  - 401: pedir login/refresh de token.
  - 403: mostrar falta de permisos.
  - 503: mostrar estado degradado y permitir reintento.
- Timeouts y loading states claros, especialmente para recomendaciones (tiene timeout mayor en gateway).
- No enviar `X-User-*` desde frontend. El gateway los agrega a los microservicios cuando `auth` esta activo.
- Para debug rapido de autenticacion, usar `/api/test/hello` y `/api/test/roles`.
- CORS: si el frontend corre en otro origen, revisar `FRONTEND_ORIGINS` en el gateway.
