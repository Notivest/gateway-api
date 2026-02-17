# Guia local - gateway-api

## Que es
API Gateway de entrada. Enruta `/api/<service>/**` a microservicios internos.

## Prerrequisitos
- Docker + Docker Compose
- Java 21 (si corres por Gradle)

## Opcion recomendada: stack completo
Este compose levanta gateway + microservicios + bases.

1. Desde la raiz del workspace, construir imagenes de servicios:

```bash
docker build -t price-fetcher ./price-fetcher
docker build -t portfolio-service ./portfolio-service
docker build -t alert-engine ./alert-engine
docker build -t notification-service ./notification-service
docker build -t recommendation-service ./recommendation-service
```

2. Levantar stack:

```bash
cd gateway-api
docker compose up -d
```

3. Verificar:

```bash
curl http://localhost:8080/actuator/health
```

4. Apagar:

```bash
docker compose down
```

## Correr solo gateway (Gradle)

```bash
set -a
source .env-gateway
set +a
./gradlew bootRun --args='--spring.profiles.active=auth'
```

## Uso
Base URL local:
- `http://localhost:8080`

Rutas principales:
- `/api/portfolio/**`
- `/api/prices/**`
- `/api/alert/**`
- `/api/notification/**`
- `/api/recommendation/**`

Health:

```bash
curl http://localhost:8080/actuator/health
```

## Referencias
- `docker-compose.yml`
- `.env-gateway`
- `docs/frontend-endpoints.md`
