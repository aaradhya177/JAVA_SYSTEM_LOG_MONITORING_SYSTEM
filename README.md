# Server Log Monitor

Java 11 + PostgreSQL log monitor with three runtime modes:

- Swing desktop mode for the interactive frontend
- CLI mode for terminal workflows
- HTTP mode for deployment platforms such as Render

The app now:

- uses your local defaults of `logmonitor` / `postgres`
- accepts both `jdbc:postgresql://...` and hosted `postgresql://...` URLs
- auto-creates the `logs` table and indexes on startup
- ships with a premium Swing dashboard for browsing, filtering, and curating logs
- starts an HTTP server automatically when `PORT` is set

## Requirements

- Java 11+
- PostgreSQL 12+
- PostgreSQL JDBC driver in `lib/`

## Local setup

Create the database:

```sql
CREATE DATABASE logmonitor;
```

Set environment variables.

Windows PowerShell:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/logmonitor"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "postgres"
```

Linux or macOS:

```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/logmonitor"
export DB_USER="postgres"
export DB_PASSWORD="postgres"
```

Compile:

```bash
compile.bat
```

or

```bash
chmod +x compile.sh
./compile.sh
```

## Run locally

Desktop GUI:

```bash
java -cp "bin;lib/*" com.logmonitor.Main
```

CLI mode:

```bash
java -cp "bin;lib/*" com.logmonitor.Main --cli
```

Seed demo data:

```bash
java -cp "bin;lib/*" com.logmonitor.Main --demo
```

Run HTTP mode locally:

```powershell
$env:PORT = "8080"
java -cp "bin;lib/*" com.logmonitor.Main
```

HTTP endpoints:

- `GET /health`
- `GET /logs`
- `GET /logs?level=ERROR`
- `GET /logs?source=AuthService`
- `GET /logs?search=database`
- `GET /stats`
- `POST /demo`

## Deployment

The included `Dockerfile` is ready for deployment. In hosted environments:

- set `DATABASE_URL`
- set `DB_USER` if the hosted URL does not already include the username
- set `DB_PASSWORD` if the hosted URL does not already include the password
- let the platform provide `PORT`

Desktop note:

- the Swing frontend is intended for local or packaged desktop use
- hosted deployments run the HTTP mode automatically in headless environments

Render-specific steps are in [RENDER_DEPLOYMENT.md](./RENDER_DEPLOYMENT.md).

## Notes

- If `PORT` is set, the app runs as a web service and responds on that port.
- If `PORT` is not set and graphics are available, the Swing desktop UI opens by default.
- If `PORT` is not set and graphics are unavailable, the app falls back to CLI mode.
- The schema is created automatically, so you do not need to run `db/schema.sql` manually for a fresh deploy unless you want manual control.
