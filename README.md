# EVion Backend (Express.js)

This repository is an Express.js backend preserving the existing API routes and core business logic.

## Stack

- Node.js + Express
- PostgreSQL
- Sequelize ORM
- JWT authentication

## Setup

1. Copy environment template:

```bash
cp .env.example .env
```

2. Install dependencies:

```bash
npm install
```

3. Run API in dev mode:

```bash
npm run dev
```

4. Run API in normal mode:

```bash
npm start
```

## API Base

- `http://localhost:8080`

## API Documentation (Swagger)

- `http://localhost:8080/api-docs`

The OpenAPI source file is located at `docs/openapi.yaml`.

## Main Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/user/:email`
- `GET /api/auth/users`
- `PUT /api/auth/user/:id`
- `DELETE /api/auth/user/:id`
- `POST /api/auth/user/:id/change-password`
- `POST /api/auth/forgot-password`

- `POST /api/vehicles`
- `GET /api/vehicles/:id`
- `GET /api/vehicles/user/:userId`
- `PUT /api/vehicles/:id`
- `DELETE /api/vehicles/:id`

- `POST /api/trips/log`
- `GET /api/trips?userId=...`

- `GET /api/stations`
- `GET /api/stations/:id`
- `POST /api/stations` (ADMIN)
- `PUT /api/stations/:id` (ADMIN)
- `DELETE /api/stations/:id` (ADMIN)

- `GET /api/user/:userId/profile`
- `PUT /api/user/:userId/profile`

- `POST /api/route/plan`
- `POST /api/battery/status`

## Notes

- JWT roles are `USER` and `ADMIN`.
- `SEQUELIZE_SYNC=true` auto-creates/updates tables from models at startup.
- Existing Java files are still present in the repo, but runtime now uses the Express app (`src/server.js`).
