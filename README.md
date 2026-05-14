# Server Log Monitor - PostgreSQL Version

A Java application for monitoring and analyzing server logs using PostgreSQL database.

## Project Structure

```
ServerLogMonitor/
├── src/                          # Java source code
│   └── com/logmonitor/
│       ├── Main.java            # Entry point
│       ├── model/
│       │   └── LogEntry.java    # Log data model
│       ├── dao/
│       │   └── LogDAO.java      # Database access layer
│       └── util/
│           └── DatabaseConnection.java  # DB connection manager
├── lib/                          # External dependencies
├── bin/                          # Compiled class files
├── db/
│   └── schema.sql               # Database schema
├── Dockerfile                    # Docker configuration
├── compile.bat                   # Windows compilation script
├── compile.sh                    # Unix/Linux compilation script
└── README.md
```

## Requirements

- Java 11 or higher
- PostgreSQL 12 or higher
- PostgreSQL JDBC Driver (included in lib/)

## Building

### On Windows
```bash
compile.bat
```

### On Linux/Mac
```bash
chmod +x compile.sh
./compile.sh
```

## Running Locally

### 1. Setup PostgreSQL Database

```sql
psql -U postgres -d postgres -c "CREATE DATABASE logmonitor;"
psql -U postgres -d logmonitor -f db/schema.sql
```

### 2. Set Environment Variables

**Windows (PowerShell):**
```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/logmonitor"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "your_password"
```

**Linux/Mac:**
```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/logmonitor"
export DB_USER="postgres"
export DB_PASSWORD="your_password"
```

### 3. Run the Application

```bash
java -cp "bin:lib/*" com.logmonitor.Main
```

### 4. Insert Demo Data

```bash
java -cp "bin:lib/*" com.logmonitor.Main --demo
```

## Deployment on Render

### Step 1: Prepare Your Repository

1. Initialize git repository:
```bash
git init
git add .
git commit -m "Initial commit"
```

2. Push to GitHub:
```bash
git remote add origin https://github.com/YOUR_USERNAME/ServerLogMonitor.git
git branch -M main
git push -u origin main
```

### Step 2: Create PostgreSQL Database on Render

1. Go to https://render.com
2. Click **"New"** → **"PostgreSQL"**
3. Fill in details:
   - **Name:** logmonitor-db
   - **Database:** logmonitor
   - **User:** postgres
   - **Region:** Choose closest to you
   - **PostgreSQL Version:** 12 or higher
4. Click **"Create Database"**
5. Copy the **External Database URL** (starts with `postgresql://`)

### Step 3: Initialize Database Schema

1. On Render dashboard, find your PostgreSQL instance
2. Click **"Connect"** → **"External Connection"**
3. Use `psql` to connect and run:
```bash
psql postgresql://user:password@host:5432/logmonitor < db/schema.sql
```

### Step 4: Create Web Service on Render

1. Go to https://render.com
2. Click **"New"** → **"Web Service"**
3. Select your GitHub repository
4. Fill in details:
   - **Name:** logmonitor-app
   - **Environment:** Docker
   - **Plan:** Free (or Starter)
5. Add **Environment Variables:**
   - `DATABASE_URL`: Paste the PostgreSQL URL from Step 2
   - `DB_USER`: postgres (or your username)
   - `DB_PASSWORD`: (paste password from Step 2)
6. Click **"Create Web Service"**

### Step 5: Verify Deployment

1. Wait for the build to complete (5-10 minutes)
2. Click on your service name to view logs
3. Your app URL will be shown at the top

### Step 6: Insert Demo Data (Optional)

Connect to your Render PostgreSQL and insert sample logs, or run:
```bash
curl -X POST https://your-app.onrender.com/demo
```

## Database Schema

The `logs` table contains:
- `id` (Primary Key)
- `timestamp` (VARCHAR)
- `level` (VARCHAR) - ERROR, WARNING, INFO
- `message` (TEXT)
- `source` (VARCHAR) - Service name
- `created_at` (TIMESTAMP)

## Features

- View all logs from database
- Filter by log level (ERROR, WARNING, INFO)
- Search logs by keyword
- Filter by source service
- View statistics
- Add new logs
- PostgreSQL-backed persistent storage

## Environment Variables

- `DATABASE_URL`: PostgreSQL connection URL
- `DB_USER`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: postgres)

## Troubleshooting

### Connection Error
- Ensure PostgreSQL is running
- Check DATABASE_URL environment variable
- Verify credentials are correct

### Compilation Error
- Ensure Java 11+ is installed
- Check that lib/ directory has all dependencies
- Verify source files are in correct paths

### Docker Build Error
- Ensure Dockerfile is in root directory
- Run `compile.bat` or `compile.sh` before building Docker image
- Check that lib/ directory has all JAR files

## Dependencies

- PostgreSQL JDBC Driver 42.x
- Java 11 Runtime

## License

MIT
