# Steam Lens Backend

A Spring Boot REST API that provides comprehensive insights into Steam gaming data, focusing on family library sharing analytics and game price information.

## Features

- **Family Library Management** - Retrieve and analyze Steam family library information
- **Shared Library Analytics** - View games shared across family groups with owner information and total value calculations
- **Player Profiles** - Get player summaries, owned games, and statistics
- **Game Database** - MongoDB-backed game information with pricing data
- **Caching** - Redis-based caching for optimized API performance
- **Database Seeding** - Automatic import of Steam game data on startup

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime |
| Spring Boot | 3.5.7 | Framework |
| MongoDB | Latest | Primary database |
| Redis | Alpine | Caching layer |
| Gradle | 8.12 | Build tool |
| Moneta | 1.4.5 | Monetary handling |

## Prerequisites

- Java 21
- Docker & Docker Compose
- Steam API Key ([Get one here](https://steamcommunity.com/dev/apikey))

## Quick Start

### Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/steam-lens-backend.git
cd steam-lens-backend

# Start all services
docker-compose up
```

The API will be available at `http://localhost:8080`

### Local Development

1. Start MongoDB:
```bash
docker run -d -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin \
  mongo:latest
```

2. Start Redis:
```bash
docker run -d -p 6379:6379 redis:alpine
```

3. Run the application:
```bash
./gradlew bootRun
```

### Kubernetes Deployment

```bash
# Build Docker image
docker build -t steamlens-backend:latest .

# Apply manifests
kubectl apply -f k8s/mongo.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/app.yaml
```

## API Endpoints

### Family Endpoints

| Method | Endpoint | Description | Headers |
|--------|----------|-------------|---------|
| GET | `/api/v1/family/my-group` | Get user's family group | `X-Steam-Access-Token` |
| GET | `/api/v1/family` | Get family group details | `X-Steam-Access-Token`, `X-Steam-Id`, `X-Family-Id` |
| GET | `/api/v1/family/{familyGroupId}/shared-library` | Get shared library with prices | `X-API-KEY`, `X-Steam-Access-Token` |

### Player Endpoints

| Method | Endpoint | Description | Headers |
|--------|----------|-------------|---------|
| GET | `/api/v1/players/{steamId}/games` | Get player's owned games | `X-API-KEY` |
| GET | `/api/v1/players/{steamId}/dashboard` | Get player dashboard stats | `X-API-KEY` |
| GET | `/api/v1/players/summaries` | Get player summaries | `X-API-KEY` |

### Game Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/games/{appId}` | Get game details (supports comma-separated IDs) |

## Configuration

### Required Headers

| Header | Description |
|--------|-------------|
| `X-API-KEY` | Your Steam API key |
| `X-Steam-Access-Token` | User's Steam access token |
| `X-Steam-Id` | User's Steam ID |
| `X-Family-Id` | Family group ID |

### Environment Variables

The application uses different profiles for different environments:

**Default (Local):**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
    mongodb:
      uri: mongodb://admin:admin@localhost:27017/steamlens?authSource=admin
```

**Docker:**
```yaml
spring:
  profiles:
    active: docker
```

### Cache TTL Configuration

| Cache | TTL |
|-------|-----|
| `steamGames` | 1 hour |
| `playerSummaries` | 5 minutes |
| `gameDetails` | 24 hours |
| Default | 10 minutes |

## Project Structure

```
src/main/java/com/example/steamlensbackend/
├── config/          # Configuration classes (Redis, MongoDB, CORS, etc.)
├── common/          # Shared DTOs, exceptions, and utilities
├── game/            # Game module (controller, service, repository)
├── family/          # Family library module
├── player/          # Player module (games, statistics)
└── SteamLensBackendApplication.java
```

## Building

```bash
# Build JAR
./gradlew bootJar

# Run JAR
java -Xmx512m -Xms256m -jar build/libs/steam-lens-backend-0.0.1-SNAPSHOT.jar
```

## Docker

The project uses a multi-stage Dockerfile:
- **Builder**: Gradle 8.12 with JDK 21
- **Runtime**: Eclipse Temurin JRE 21 Alpine
- **Port**: 8080
- **User**: Non-root (spring)

## License

This project is licensed under the MIT License.
