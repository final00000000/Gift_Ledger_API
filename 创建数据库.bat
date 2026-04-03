@echo off
chcp 65001 >nul
echo ========================================
echo 创建 Gift Ledger 数据库
echo ========================================
echo.

set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe

echo 请输入你的 MySQL root 密码：
set /p DB_PASSWORD=密码:

echo.
echo 正在连接 MySQL 并创建数据库...
echo.

"%MYSQL_PATH%" -u root -p%DB_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS gift_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>error.txt

if %errorlevel% equ 0 (
    echo ✓ 数据库创建成功！
    echo.
    echo 数据库信息：
    echo   名称: gift_ledger
    echo   字符集: utf8mb4
    echo.
    echo 验证数据库是否存在：
    "%MYSQL_PATH%" -u root -p%DB_PASSWORD% -e "SHOW DATABASES LIKE 'gift_ledger';"
    echo.
    echo ========================================
    echo 成功！现在可以启动应用了
    echo ========================================
    if exist error.txt del error.txt
) else (
    echo ✗ 创建失败！
    echo.
    if exist error.txt (
        echo 错误信息：
        type error.txt
        del error.txt
    )
    echo.
    echo 可能的原因：
    echo 1. 密码错误 - 请重新运行并输入正确密码
    echo 2. MySQL 服务未启动 - 请检查服务
    echo 3. 权限不足 - 请以管理员身份运行
    echo.
)

echo.
echo 按任意键关闭窗口...
pause >nul

