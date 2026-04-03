@echo off
chcp 65001 >nul
echo ========================================
echo Gift Ledger API 启动
echo ========================================
echo.

REM 读取 .env 文件
for /f "tokens=1,2 delims==" %%a in (.env) do (
    if not "%%a"=="" if not "%%a:~0,1%"=="#" (
        set %%a=%%b
    )
)

echo 配置信息：
echo   数据库: %DB_HOST%:%DB_PORT%/%DB_NAME%
echo   用户: %DB_USER%
echo   服务器端口: %SERVER_PORT%
echo.
echo 正在启动服务器...
echo.

gradlew.bat :app:run

pause
