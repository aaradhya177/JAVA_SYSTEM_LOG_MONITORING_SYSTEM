# Deploying Server Log Monitor on Render - Complete Guide

## Prerequisites

- GitHub account with repository pushed
- Render account (free tier available)
- PostgreSQL installed locally (for initial testing)

## Complete Deployment Steps

### Phase 1: Prepare Your Application (Local Machine)

#### Step 1.1: Compile Your Application

**Windows:**
```bash
cd C:\Users\aarad\Desktop\java_project\ServerLogMonitor
compile.bat
```

**Linux/Mac:**
```bash
cd ~/Desktop/java_project/ServerLogMonitor
chmod +x compile.sh
./compile.sh
```

Verify `bin/` directory has compiled `.class` files.

#### Step 1.2: Download PostgreSQL JDBC Driver

Download from: https://jdbc.postgresql.org/download.html
- Download: postgresql-42.x.x.jar
- Place in: `lib/` folder

Or use these commands:

**Windows (PowerShell):**
```powershell
$url = "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar"
$output = "lib\postgresql-42.7.1.jar"
Invoke-WebRequest -Uri $url -OutFile $output
```

**Linux/Mac:**
```bash
curl -o lib/postgresql-42.7.1.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar
```

#### Step 1.3: Test Locally

Set environment variables and test:

**Windows (PowerShell):**
```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/logmonitor"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "postgres"
java -cp "bin;lib/*" com.logmonitor.Main --demo
```

**Linux/Mac:**
```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/logmonitor"
export DB_USER="postgres"
export DB_PASSWORD="postgres"
java -cp "bin:lib/*" com.logmonitor.Main --demo
```

#### Step 1.4: Push to GitHub

```bash
git add .
git commit -m "Add PostgreSQL support and Render deployment files"
git push origin main
```

---

### Phase 2: Create PostgreSQL Database on Render

#### Step 2.1: Create Database

1. Go to https://render.com
2. Sign in with GitHub account
3. Click **"New +"** button in top-right
4. Select **"PostgreSQL"**
5. Fill in form:
   - **Name:** `logmonitor-db`
   - **Database:** `logmonitor`
   - **User:** `postgres`
   - **Region:** Select closest to your users (e.g., US East)
   - **PostgreSQL Version:** 12 or 13
6. Click **"Create Database"**

Wait for creation (2-3 minutes).

#### Step 2.2: Get Database Credentials

1. Find your PostgreSQL service in Render dashboard
2. Click on it
3. Copy these values:
   - **External Database URL** (format: `postgresql://user:password@host:port/database`)
   - **Hostname**
   - **Port**
   - **Database**
   - **Username**
   - **Password**

Save these values in a secure location.

#### Step 2.3: Initialize Database Schema

1. In Render, click your PostgreSQL service
2. Click **"Connect"** → **"External Connection"**
3. Open terminal and run:

```bash
# Replace with your External Database URL
psql "postgresql://postgres:YOUR_PASSWORD@YOUR_HOST:5432/logmonitor" < db/schema.sql
```

Verify connection was successful and tables created.

---

### Phase 3: Create Docker Image (Optional but Recommended)

Docker makes deployment simpler. If you don't want Docker, skip to Phase 4.

#### Step 3.1: Verify Dockerfile

Check `Dockerfile` exists in root:
```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY lib/ /app/lib/
COPY bin/ /app/bin/
CMD ["java", "-cp", "/app/bin:/app/lib/*", "com.logmonitor.Main"]
```

#### Step 3.2: Test Docker Build Locally (Optional)

```bash
docker build -t logmonitor:latest .
docker run -e DATABASE_URL="jdbc:postgresql://localhost:5432/logmonitor" logmonitor:latest
```

#### Step 3.3: Ensure .gitignore is Correct

Verify `.gitignore` excludes unnecessary files:
```
bin/
*.class
target/
.idea/
.vscode/
```

---

### Phase 4: Deploy to Render (Docker Deployment)

#### Step 4.1: Create Web Service

1. Go to https://render.com
2. Click **"New +"** → **"Web Service"**
3. Select your GitHub repository:
   - Click **"Connect"** next to your ServerLogMonitor repo
4. Fill in settings:
   - **Name:** `logmonitor`
   - **Environment:** `Docker`
   - **Region:** Same as database (e.g., US East)
   - **Branch:** `main`
   - **Build Filter:** Leave empty
   - **Plan:** Free (or Starter for persistent service)

#### Step 4.2: Add Environment Variables

Click **"Advanced"** then **"Add Environment Variable"**:

| Key | Value |
|-----|-------|
| `DATABASE_URL` | `postgresql://postgres:YOUR_PASSWORD@YOUR_HOST:5432/logmonitor` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `YOUR_PASSWORD` |

Get these values from your PostgreSQL service in Render.

#### Step 4.3: Configure Build Command (Optional)

- **Build Command:** Leave empty (Docker handles it)
- **Start Command:** Leave empty (Dockerfile defines it)

#### Step 4.4: Deploy

Click **"Create Web Service"**

Watch the logs during build:
- Should show: `Building docker image...`
- Then: `Running: ./compile.sh` (or `compile.bat` equivalent)
- Then: `Pushing image to registry...`
- Finally: Service will start

Build takes 5-10 minutes.

#### Step 4.5: Verify Deployment

1. Wait for status to turn **green** (✓ Live)
2. Click the service URL at top
3. Application should be running

---

### Phase 5: Alternative - Deploy Without Docker

If you prefer not to use Docker:

#### Step 5.1: Create Web Service

1. Go to https://render.com → **"New +"** → **"Web Service"**
2. Connect your repository
3. Settings:
   - **Environment:** `Java`
   - **Build Command:** `chmod +x compile.sh && ./compile.sh`
   - **Start Command:** `java -cp "bin:lib/*" com.logmonitor.Main`

#### Step 5.2: Add Environment Variables (same as Phase 4.2)

#### Step 5.3: Deploy

Render will compile and run your application.

---

## Post-Deployment

### Verify Application is Running

1. Check service logs in Render dashboard
2. Look for: `Connected to database successfully`
3. Insert test data:

```bash
# SSH into container (if available)
# Or use direct connection if exposed
```

### Insert Demo Data

```bash
java -cp "bin:lib/*" com.logmonitor.Main --demo
```

### Monitor Performance

In Render dashboard:
- View real-time logs
- Check resource usage
- Monitor database connections

---

## Troubleshooting

### Build Fails with "Compilation Error"

**Fix:**
- Ensure all `.java` files are in correct package paths
- Verify `lib/postgresql-42.7.1.jar` is in repository
- Check `compile.sh` has correct paths

```bash
# Rebuild locally first
./compile.sh
git add bin/
git commit -m "Add compiled classes"
git push
```

### "Connection refused" Error

**Fix:**
1. Verify `DATABASE_URL` environment variable is correct
2. Check PostgreSQL service is running on Render
3. Ensure credentials are correct:
   ```bash
   psql "postgresql://postgres:PASSWORD@HOST:5432/logmonitor" -c "\dt"
   ```

### "JDBC Driver not found" Error

**Fix:**
- Ensure `lib/postgresql-42.7.1.jar` exists in repository
- Check .gitignore doesn't exclude lib folder
- Verify JAR is not in bin/.gitkeep

```bash
git ls-files lib/  # Verify JAR is tracked
```

### Container Exits Immediately

**Fix:**
- Check logs in Render dashboard
- Ensure `Dockerfile` is correct
- Verify `bin/` contains compiled `.class` files

### Cannot Connect to Database

**Fix:**
1. Get PostgreSQL External URL from Render
2. Test connection locally:
   ```bash
   psql "postgresql://postgres:PASSWORD@HOST:5432/logmonitor" -c "SELECT 1;"
   ```
3. Update environment variables in web service
4. Redeploy

---

## Environment Variables Reference

Set in Render Web Service **Settings** → **Environment**:

```
DATABASE_URL=postgresql://postgres:PASSWORD@HOST:5432/logmonitor
DB_USER=postgres
DB_PASSWORD=PASSWORD
```

For PostgreSQL connection string format:
```
postgresql://[user[:password]@][netloc][:port][/dbname]
```

---

## Database URL Format Examples

**Local:**
```
jdbc:postgresql://localhost:5432/logmonitor
```

**Render PostgreSQL:**
```
jdbc:postgresql://YOUR_HOST:5432/logmonitor
```

**From Render External URL (convert):**
- Render gives: `postgresql://user:pass@host:port/database`
- Convert to: `jdbc:postgresql://host:port/database`

---

## Monitoring & Maintenance

### View Application Logs

1. Go to service on Render
2. Click **"Logs"** tab
3. See real-time output

### Database Backups

Render automatically backs up PostgreSQL. To restore:
1. Go to PostgreSQL service
2. Click **"Backups"**
3. Select restore point

### Scaling

Free tier restarts daily. For 24/7 uptime, upgrade to:
- **Starter:** $12/month (persistent)
- **Standard:** $19+/month (more resources)

---

## Going Live - Production Checklist

- [ ] Database created and schema initialized
- [ ] Environment variables set correctly
- [ ] Application tested locally
- [ ] Docker image builds successfully
- [ ] Logs show "Connected to database successfully"
- [ ] Demo data inserted successfully
- [ ] Application accessible via Render URL
- [ ] Database backups enabled
- [ ] SSL/TLS enabled (Render default)
- [ ] Error monitoring configured

---

## Contact & Support

For Render issues: https://render.com/docs
For PostgreSQL issues: https://www.postgresql.org/docs/
For Java/JDBC issues: https://jdbc.postgresql.org/documentation/

