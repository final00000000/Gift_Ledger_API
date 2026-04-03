@echo off
chcp 65001 >nul
echo ========================================
echo MySQL 环境检测工具
echo ========================================
echo.

echo [1/4] 检查 MySQL 服务...
sc query MySQL80 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ 找到 MySQL80 服务
    sc query MySQL80 | findstr "STATE"
) else (
    sc query MySQL >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✓ 找到 MySQL 服务
        sc query MySQL | findstr "STATE"
    ) else (
        echo ✗ 未找到 MySQL 服务
        echo   可能需要安装 MySQL
    )
)
echo.

echo [2/4] 检查 MySQL 命令行工具...
where mysql >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ MySQL 命令行工具在系统路径中
    mysql --version
) else (
    echo ✗ MySQL 命令行工具不在系统路径中
    echo   正在搜索常见安装位置...

    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        echo ✓ 找到: C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
    )
    if exist "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe" (
        echo ✓ 找到: C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe
    )
    if exist "C:\xampp\mysql\bin\mysql.exe" (
        echo ✓ 找到: C:\xampp\mysql\bin\mysql.exe
    )
    if exist "C:\wamp64\bin\mysql" (
        echo ✓ 找到 WAMP MySQL 安装
        dir /b "C:\wamp64\bin\mysql"
    )
)
echo.

echo [3/4] 检查 MySQL 端口...
netstat -an | findstr ":3306" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ MySQL 端口 3306 正在监听
    netstat -an | findstr ":3306"
) else (
    echo ✗ MySQL 端口 3306 未监听
    echo   MySQL 可能未启动
)
echo.

echo [4/4] 检查 Java 环境...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Java 已安装
    java -version 2>&1 | findstr "version"
) else (
    echo ✗ Java 未安装或不在系统路径中
)
echo.

echo ========================================
echo 检测完成
echo ========================================
echo.
echo 请将上面的结果截图或复制给我
echo.
pause
