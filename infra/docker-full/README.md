# Bithave - Full Stack Docker Setup

This folder contains everything needed to run the complete Bithave application.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)

## Quick Start

1. Open a terminal in this folder:

   ```bash
   cd infra/docker-full
   ```

2. Copy the environment template and configure if needed:

   ```bash
   cp .env.example .env
   ```

3. Start the application:

   ```bash
   docker compose up --build
   ```

4. Wait for all services to start (first build takes a few minutes)

5. Open the application in your browser:
   ```
   http://localhost:4200
   ```

## Services

| Service  | Description             | Internal Port | External Port |
| -------- | ----------------------- | ------------- | ------------- |
| postgres | PostgreSQL Database     | 5432          | -             |
| redis    | Redis Cache             | 6379          | -             |
| backend  | Spring Boot API         | 8080          | -             |
| frontend | Angular App (via nginx) | 80            | 4200          |

## Commands

### Start the application

```bash
docker compose up --build
```

### Start in background (detached mode)

```bash
docker compose up --build -d
```
