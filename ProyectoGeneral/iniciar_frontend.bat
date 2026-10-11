@echo off
setlocal
echo ========================================================
echo        INICIANDO SERVIDOR FRONTEND (ANGULAR)
echo ========================================================
cd /d "%~dp0client-front"
echo Servidor frontend disponible en http://localhost:4200...
call npm start
pause

