@echo off
cd /d "%~dp0client"
if "%CARDGAME_HOST%"=="" set CARDGAME_HOST=127.0.0.1
if "%CARDGAME_PORT%"=="" set CARDGAME_PORT=5000
echo ========================================================
echo        STARTING MEMORY DUEL CLIENT
echo   Server Address: %CARDGAME_HOST%:%CARDGAME_PORT%
echo ========================================================
if exist target\cardgame-client-1.0.0.jar (
    java -Dcardgame.host=%CARDGAME_HOST% -Dcardgame.port=%CARDGAME_PORT% -jar target\cardgame-client-1.0.0.jar
) else (
    call mvn -q -Dcardgame.host=%CARDGAME_HOST% -Dcardgame.port=%CARDGAME_PORT% compile exec:java -Dexec.mainClass=com.cardgame.client.MainClient
)
if errorlevel 1 pause
