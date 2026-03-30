# MySQL 数据库配置指南

## 📋 前提条件

确保你的 MySQL 已经安装并运行。

## 🗄️ 创建数据库

连接到 MySQL 并创建数据库：

```sql
-- 连接到 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE gift_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选，如果不想用 root）
CREATE USER 'giftledger'@'localhost' IDENTIFIED BY '你的密码';
GRANT ALL PRIVILEGES ON gift_ledger.* TO 'giftledger'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

## ⚙️ 配置环境变量

### 方式一：创建 .env 文件（推荐）

在项目根目录创建 `.env` 文件：

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=gift_ledger
DB_USER=root
DB_PASSWORD=你的MySQL密码

# 如果创建了专用用户
# DB_USER=giftledger
# DB_PASSWORD=你设置的密码

# 服务器配置
SERVER_PORT=8080

# 数据库连接池配置（可选）
DB_POOL_MAX_SIZE=10
DB_POOL_MIN_IDLE=2

# SSL 配置（可选）
DB_USE_SSL=false
```

### 方式二：设置系统环境变量

**Windows (PowerShell):**
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="gift_ledger"
$env:DB_USER="root"
$env:DB_PASSWORD="你的密码"
$env:SERVER_PORT="8080"
```

**Windows (CMD):**
```cmd
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=gift_ledger
set DB_USER=root
set DB_PASSWORD=你的密码
set SERVER_PORT=8080
```

**Linux/Mac:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=gift_ledger
export DB_USER=root
export DB_PASSWORD=你的密码
export SERVER_PORT=8080
```

## 🚀 启动应用

```bash
cd D:\Desktop\MyProject\server\Gift_Ledger\Gift_Ledger
./gradlew build
./gradlew :app:run
```

## 📊 数据库迁移

应用启动时会自动执行 Flyway 数据库迁移，创建所需的表结构。

迁移文件位置：`infrastructure/src/main/resources/db/migration/`

## 🔍 验证连接

启动应用后，你应该看到：

```
数据库连接成功: jdbc:mysql://localhost:3306/gift_ledger?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
```

然后测试 API：

```bash
curl http://localhost:8080/health
```

## ⚠️ 常见问题

### 问题 1：连接被拒绝

**错误信息：** `Connection refused`

**解决方案：**
1. 确认 MySQL 服务正在运行
   ```bash
   # Windows
   net start MySQL80

   # Linux
   sudo systemctl status mysql
   ```

2. 检查端口是否正确（默认 3306）

### 问题 2：认证失败

**错误信息：** `Access denied for user`

**解决方案：**
1. 检查用户名和密码是否正确
2. 确认用户有访问数据库的权限
   ```sql
   SHOW GRANTS FOR 'root'@'localhost';
   ```

### 问题 3：时区问题

**错误信息：** `The server time zone value 'XXX' is unrecognized`

**解决方案：**
已在 JDBC URL 中添加 `serverTimezone=UTC`，如果仍有问题，可以设置 MySQL 时区：
```sql
SET GLOBAL time_zone = '+8:00';
```

### 问题 4：字符集问题

**解决方案：**
确保数据库使用 UTF-8：
```sql
ALTER DATABASE gift_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 🔧 高级配置

### 自定义 JDBC URL

如果需要完全自定义连接字符串：

```bash
DB_JDBC_URL=jdbc:mysql://localhost:3306/gift_ledger?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
```

### 连接池配置

```bash
# 最大连接数
DB_POOL_MAX_SIZE=20

# 最小空闲连接数
DB_POOL_MIN_IDLE=5

# 连接超时（毫秒）
DB_POOL_CONNECTION_TIMEOUT_MS=30000

# 空闲超时（毫秒）
DB_POOL_IDLE_TIMEOUT_MS=600000

# 最大生命周期（毫秒）
DB_POOL_MAX_LIFETIME_MS=1800000
```

## 📝 数据库表结构

应用启动后会自动创建以下表：

- `users` - 用户表
- `refresh_tokens` - 刷新令牌表
- `guests` - 宾客表
- `event_books` - 事件簿表
- `gifts` - 礼物记录表
- `reminder_settings` - 提醒设置表

## 🔄 重置数据库

如果需要重置数据库：

```sql
DROP DATABASE gift_ledger;
CREATE DATABASE gift_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后重新启动应用，Flyway 会重新创建所有表。

## 📞 支持

如有问题，请检查：
1. MySQL 服务是否运行
2. 数据库是否已创建
3. 用户权限是否正确
4. 环境变量是否设置
5. 应用日志输出
