# Render Deployment

## What changed

This project now runs correctly on Render as a web service because it:

- binds to the Render-provided `PORT`
- exposes `GET /health` for health checks
- accepts Render-style PostgreSQL URLs like `postgresql://...`
- auto-creates the database schema on startup

The Swing frontend is not used on Render. Render runs the headless HTTP mode automatically.

## 1. Create the Render PostgreSQL instance

Create a PostgreSQL database with:

- Database name: `logmonitor`
- User: `postgres` or the Render-generated default user

Copy the external database URL after creation.

## 2. Create the web service

Create a new Render Web Service from this repository with:

- Environment: `Docker`
- Dockerfile: root `Dockerfile`
- Health Check Path: `/health`

## 3. Set environment variables

Add these variables in Render:

```text
DATABASE_URL=postgresql://postgres:YOUR_PASSWORD@YOUR_HOST:5432/logmonitor
DB_USER=postgres
DB_PASSWORD=YOUR_PASSWORD
```

Notes:

- `DATABASE_URL` can stay in Render's native `postgresql://...` form.
- If your `DATABASE_URL` already contains the username and password, this app can read them from the URL.
- Keeping `DB_USER` and `DB_PASSWORD` set explicitly is still fine.

## 4. Deploy

Deploy the service. On startup it will:

1. connect to PostgreSQL
2. create the `logs` table and indexes if missing
3. start the HTTP server on Render's `PORT`

## 5. Verify

After deploy:

- open `https://your-service.onrender.com/health`
- expect a `200` response with database status
- optionally call `POST /demo` once to insert sample data

Useful endpoints:

- `GET /health`
- `GET /logs`
- `GET /stats`
- `POST /demo`

## Troubleshooting

`Application startup failed`

- Check `DATABASE_URL`, `DB_USER`, and `DB_PASSWORD`
- Confirm the PostgreSQL instance is reachable from Render

`/health` returns 503

- The app is running, but PostgreSQL credentials or connectivity are failing

Build fails

- Make sure `lib/postgresql-42.7.1.jar` is present in the repo
- Make sure the repo includes the updated `src/com/logmonitor/server` package
