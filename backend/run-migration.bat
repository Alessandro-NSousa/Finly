@echo off
REM =====================================================
REM Script de Migracao - Windows
REM Execute este arquivo para adicionar as colunas
REM =====================================================

echo Executando migracao do banco de dados...
echo.

REM Tente encontrar o psql no caminho padrao do PostgreSQL
set PSQL_PATH="C:\Program Files\PostgreSQL\16\bin\psql.exe"
if not exist %PSQL_PATH% set PSQL_PATH="C:\Program Files\PostgreSQL\15\bin\psql.exe"
if not exist %PSQL_PATH% set PSQL_PATH="C:\Program Files\PostgreSQL\14\bin\psql.exe"
if not exist %PSQL_PATH% set PSQL_PATH=psql

echo Usando psql em: %PSQL_PATH%
echo.
echo Entre com a senha do PostgreSQL quando solicitado
echo.

%PSQL_PATH% -U postgres -d finly -f database-migration-add-email-verification.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo MIGRACAO CONCLUIDA COM SUCESSO!
    echo ========================================
    echo.
    echo Agora voce pode:
    echo 1. Reiniciar o backend
    echo 2. Tentar cadastrar novamente
    echo.
) else (
    echo.
    echo ========================================
    echo ERRO NA MIGRACAO!
    echo ========================================
    echo.
    echo Verifique se:
    echo 1. PostgreSQL esta instalado
    echo 2. Banco 'finly' existe
    echo 3. Senha do usuario 'postgres' esta correta
    echo.
)

pause
