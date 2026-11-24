@echo off
cd /d "%~dp0server"
if not exist src\main\resources\db.properties (
    if exist src\main\resources\db.properties.example (
        copy src\main\resources\db.properties.example src\main\resources\db.properties >nul
    )
)
echo ========================================================
echo        STARTING MEMORY DUEL SERVER (CARDGAME)
echo ========================================================
if exist target\cardgame-server-1.0.0.jar (
    java -jar target\cardgame-server-1.0.0.jar
) else (
    call mvn -q compile exec:java -Dexec.mainClass=com.cardgame.server.MainServer
)
if errorlevel 1 pause
