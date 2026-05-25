@echo off
cd /d "%~dp0"

echo ========================================
echo   suxai Server Start
echo ========================================
echo.

echo Checking port 8080...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080 "') do (
    echo Killing PID %%a on port 8080
    taskkill /PID %%a /F > nul 2>&1
)
ping -n 2 127.0.0.1 > nul

echo Spring Boot starting... (port 8080)
start "suxai-backend" cmd /k "cd /d %~dp0 && mvn spring-boot:run"

ping -n 4 127.0.0.1 > nul
start "" http://localhost:8080

echo.
echo Server : http://localhost:8080
echo.
pause
