@echo off
chcp 65001 >nul
REM Gift Ledger API 启动脚本

echo ========================================
echo Gift Ledger API 启动
echo ========================================
echo.

REM 读取 .env 文件（存在则覆盖默认值）
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=gift_ledger
set DB_USER=root
set DB_PASSWORD=
set SERVER_PORT=8080

if exist .env (
  for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
    if not "%%a"=="" if not "%%a:~0,1%"=="#" (
      set "%%a=%%b"
    )
  )
)

echo 数据库配置:
echo   主机: %DB_HOST%
echo   端口: %DB_PORT%
echo   数据库: %DB_NAME%
echo   用户: %DB_USER%
echo.
echo 服务器端口: %SERVER_PORT%
echo.
echo ========================================
echo 正在启动服务器...
echo ========================================
echo.

REM 启动应用
call gradlew.bat :app:run

echo.
echo ========================================
echo 程序已退出（退出码: %errorlevel%）
echo ========================================
echo.
echo 如果看到错误，请截图或复制给我
echo.
pause
