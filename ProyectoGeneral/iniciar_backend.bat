@echo off
setlocal
echo ========================================================
echo        INICIANDO SERVIDOR BACKEND (SPRING BOOT)
echo ========================================================
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0api"
echo Compilando y ejecutando en puerto 8080 con perfil local...
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
pause

