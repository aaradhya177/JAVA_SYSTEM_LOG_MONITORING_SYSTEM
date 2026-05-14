@echo off
REM Quick start script for Windows

echo ===== Server Log Monitor - Quick Start =====
echo.

echo Step 1: Compiling Java sources...
call compile.bat

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Setting up environment...
echo Make sure PostgreSQL JDBC driver is in lib\ folder

echo.
echo Step 3: Choose how to launch...
echo Ensure these environment variables are set:
echo   DATABASE_URL=jdbc:postgresql://localhost:5432/logmonitor
echo   DB_USER=postgres
echo   DB_PASSWORD=postgres

echo.
echo To launch the premium Swing desktop UI, use:
echo   java -cp "bin;lib/*" com.logmonitor.Main

echo.
echo To force CLI mode, use:
echo   java -cp "bin;lib/*" com.logmonitor.Main --cli

echo.
echo To run the deployable HTTP service locally, use:
echo   set PORT=8080 ^&^& java -cp "bin;lib/*" com.logmonitor.Main

echo.
echo To insert demo data, use:
echo   java -cp "bin;lib/*" com.logmonitor.Main --demo

pause
