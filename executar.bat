@echo off
setlocal
chcp 65001 >nul
pushd "%~dp0"
if errorlevel 1 (
    echo Nao foi possivel acessar a pasta do projeto.
    pause
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo Java nao encontrado. Configure um JDK 17 ou superior no PATH.
    popd
    pause
    exit /b 1
)

set "MAVEN_EXECUTAR=%~dp0FERRAMENTAS\apache-maven-3.10.0\bin\mvn.cmd"
if not exist "%MAVEN_EXECUTAR%" (
    where mvn.cmd >nul 2>&1
    if errorlevel 1 (
        echo Maven nao encontrado na pasta FERRAMENTAS nem no PATH.
        popd
        pause
        exit /b 1
    )
    set "MAVEN_EXECUTAR=mvn.cmd"
)

echo Compilando e iniciando o sistema...
echo Na primeira execucao, o Maven pode precisar baixar dependencias.
echo.
call "%MAVEN_EXECUTAR%" -q compile exec:java "-Dexec.mainClass=Main"
set "RESULTADO_EXECUTAR=%ERRORLEVEL%"
echo.
if not "%RESULTADO_EXECUTAR%"=="0" (
    echo Nao foi possivel concluir a execucao. Confira as mensagens acima.
)
popd
pause
exit /b %RESULTADO_EXECUTAR%
