# Quick Start

## Local

1. Create the database:

```sql
CREATE DATABASE logmonitor;
```

2. Set environment variables:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/logmonitor"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "postgres"
```

3. Compile:

```powershell
.\compile.bat
```

4. Seed demo data if you want:

```powershell
java -cp "bin;lib/*" com.logmonitor.Main --demo
```

5. Start the app:

Desktop GUI:

```powershell
java -cp "bin;lib/*" com.logmonitor.Main
```

CLI mode:

```powershell
java -cp "bin;lib/*" com.logmonitor.Main --cli
```

HTTP mode:

```powershell
$env:PORT = "8080"
java -cp "bin;lib/*" com.logmonitor.Main
```

## Deployment

1. Push the repo to GitHub.
2. Create a PostgreSQL database named `logmonitor`.
3. Deploy the repo with the included `Dockerfile`.
4. Set:

```text
DATABASE_URL=postgresql://postgres:YOUR_PASSWORD@YOUR_HOST:5432/logmonitor
DB_USER=postgres
DB_PASSWORD=YOUR_PASSWORD
```

5. Verify `GET /health` returns `200`.

The Swing frontend is for desktop use. Hosted deployments automatically run the HTTP backend mode.
