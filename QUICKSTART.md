# Quick Start Guide

## ⚡ 5-Minute Setup

### 1. Download PostgreSQL JDBC Driver

**Windows (PowerShell):**
```powershell
$url = "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar"
$output = "lib\postgresql-42.7.1.jar"
Invoke-WebRequest -Uri $url -OutFile $output
```

**Linux/Mac (Terminal):**
```bash
curl -o lib/postgresql-42.7.1.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar
```

### 2. Compile Application

**Windows:**
```bash
compile.bat
```

**Linux/Mac:**
```bash
chmod +x compile.sh
./compile.sh
```

### 3. Set Environment Variables

**Windows (PowerShell):**
```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/logmonitor"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "postgres"
```

**Linux/Mac (Terminal):**
```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/logmonitor"
export DB_USER="postgres"
export DB_PASSWORD="postgres"
```

### 4. Create PostgreSQL Database

```bash
psql -U postgres
```

Inside psql:
```sql
CREATE DATABASE logmonitor;
\c logmonitor
-- Then paste contents of db/schema.sql
```

Or one command:
```bash
psql -U postgres -d postgres -f db/schema.sql
```

### 5. Run Application

**Insert Demo Data:**
```bash
java -cp "bin;lib/*" com.logmonitor.Main --demo
```

**Start Interactive Shell:**
```bash
java -cp "bin;lib/*" com.logmonitor.Main
```

---

## 🚀 Deploy to Render (3 Steps)

### Step 1: Push to GitHub

```bash
git add .
git commit -m "Deploy version"
git push origin main
```

### Step 2: Create PostgreSQL on Render

1. https://render.com
2. New → PostgreSQL
3. Save External Database URL

### Step 3: Create Web Service on Render

1. New → Web Service
2. Select GitHub repo
3. Environment: Docker
4. Add env vars:
   - `DATABASE_URL`: (from Step 2)
   - `DB_USER`: postgres
   - `DB_PASSWORD`: (from Step 2)
5. Deploy!

**Done!** Your app is live.

---

## 📋 Project Structure

```
ServerLogMonitor/
├── src/                    # Java source files
│   └── com/logmonitor/
│       ├── Main.java
│       ├── model/LogEntry.java
│       ├── dao/LogDAO.java
│       └── util/DatabaseConnection.java
├── lib/                    # Dependencies (add postgresql JAR here)
├── bin/                    # Compiled classes (auto-generated)
├── db/
│   └── schema.sql         # Database setup
├── Dockerfile             # Docker config
├── compile.bat/.sh        # Compilation scripts
├── run.bat/.sh            # Run scripts
├── README.md              # Full documentation
└── RENDER_DEPLOYMENT.md   # Detailed deployment guide
```

---

## 🐛 Common Issues

| Issue | Fix |
|-------|-----|
| `Class not found: org.postgresql.Driver` | Download JDBC JAR to lib/ |
| `Connection refused` | Check DATABASE_URL and PostgreSQL running |
| `Compilation failed` | Run `compile.bat` or `compile.sh` |
| `Table doesn't exist` | Run `psql -U postgres -d logmonitor -f db/schema.sql` |

---

## 📚 Full Docs

- Detailed deployment: [RENDER_DEPLOYMENT.md](RENDER_DEPLOYMENT.md)
- Project info: [README.md](README.md)
- Database schema: [db/schema.sql](db/schema.sql)

