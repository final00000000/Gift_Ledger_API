@echo off
chcp 65001 >nul
echo ========================================
echo Gift Ledger API 文档同步到 ShowDoc
echo ========================================
echo.

REM 检查 .env 文件是否存在
if not exist .env (
    echo [错误] 未找到 .env 文件
    echo 请先配置 ShowDoc API 信息
    pause
    exit /b 1
)

REM 加载环境变量
for /f "tokens=1,2 delims==" %%a in (.env) do (
    if "%%a"=="SHOWDOC_API_URL" set SHOWDOC_API_URL=%%b
    if "%%a"=="SHOWDOC_API_KEY" set SHOWDOC_API_KEY=%%b
    if "%%a"=="SHOWDOC_API_TOKEN" set SHOWDOC_API_TOKEN=%%b
)

REM 检查必要的环境变量
if "%SHOWDOC_API_URL%"=="" (
    echo [错误] 未配置 SHOWDOC_API_URL
    echo 请在 .env 文件中添加 ShowDoc API 配置
    pause
    exit /b 1
)

if "%SHOWDOC_API_KEY%"=="" (
    echo [错误] 未配置 SHOWDOC_API_KEY
    pause
    exit /b 1
)

if "%SHOWDOC_API_TOKEN%"=="" (
    echo [错误] 未配置 SHOWDOC_API_TOKEN
    pause
    exit /b 1
)

echo [信息] 正在读取 OpenAPI 文档...
set OPENAPI_FILE=adapters\http\src\main\resources\static\openapi.json

if not exist %OPENAPI_FILE% (
    echo [错误] 未找到 OpenAPI 文档文件: %OPENAPI_FILE%
    pause
    exit /b 1
)

echo [信息] 正在同步到 ShowDoc...
echo.

REM 调用 PowerShell 脚本进行同步
powershell -ExecutionPolicy Bypass -File sync-showdoc.ps1

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [成功] 文档已成功同步到 ShowDoc
) else (
    echo.
    echo [失败] 同步失败，请检查配置和网络连接
)

echo.
pause
