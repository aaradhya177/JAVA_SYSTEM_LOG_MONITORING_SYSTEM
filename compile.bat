@echo off
REM Compile Java sources for Windows

echo Compiling Java sources...
javac -d bin -cp "lib\*" src\com\logmonitor\model\*.java src\com\logmonitor\util\*.java src\com\logmonitor\dao\*.java src\com\logmonitor\service\*.java src\com\logmonitor\server\*.java src\com\logmonitor\ui\*.java src\com\logmonitor\*.java

if %ERRORLEVEL% equ 0 (
    echo Compilation successful!
    echo Compiled classes are in bin directory
) else (
    echo Compilation failed!
    exit /b 1
)
