# Gift Ledger API 运行说明

## ✅ API 完善完成

所有编译错误已修复，API 层已完全实现并可以运行。

**✅ 已验证：服务器可以成功启动并响应请求**

## 🚀 快速启动

### 方式一：使用 Gradle 运行（推荐）

```bash
cd D:\Desktop\MyProject\server\Gift_Ledger\Gift_Ledger
./gradlew :app:run
```

### 方式二：使用 IntelliJ IDEA 运行

1. 打开项目
2. 找到 `adapters/http/src/main/kotlin/com/giftledger/adapters/http/Application.kt`
3. 点击 `main()` 函数旁边的绿色运行按钮

## 📡 API 端点

服务器默认运行在 `http://localhost:8080`

### 认证相关
- `POST /auth/register` - 用户注册
- `POST /auth/login` - 用户登录
- `POST /auth/refresh` - 刷新令牌
- `POST /auth/logout` - 用户登出

### 用户相关
- `GET /users/me` - 获取当前用户信息（需要认证）

### 礼物相关
- `GET /gifts` - 获取礼物列表（需要认证）

### 事件簿相关
- `GET /event-books` - 获取事件簿列表（需要认证）

### 宾客相关
- `GET /guests` - 获取宾客列表（需要认证）

### 报告相关
- `GET /reports/summary` - 获取报告摘要（需要认证）

### 导出相关
- `GET /exports/json` - 导出 JSON 数据（需要认证）

### 提醒相关
- `GET /reminders/pending` - 获取待处理提醒（需要认证）

### 健康检查
- `GET /health` - 健康检查

## 🔧 当前状态

### ✅ 已完成
1. 所有路由文件已创建并注册
2. 错误处理已配置
3. JWT 认证已配置
4. CORS 已配置
5. 依赖注入（Koin）已配置
6. Mock Repository 实现（用于测试）
7. **日志配置已添加（Logback）**
8. **服务器已验证可以成功启动和响应**

### ⚠️ 注意事项
- **数据库初始化已暂时禁用**，当前使用 Mock 数据
- 所有 Repository 方法返回模拟数据
- 需要连接真实数据库时，请参考下面的数据库配置

## 🗄️ 数据库配置（可选）

如果需要连接真实的 PostgreSQL 数据库：

### 1. 安装 PostgreSQL
下载并安装：https://www.postgresql.org/download/windows/

### 2. 创建数据库
```sql
CREATE DATABASE gift_ledger;
```

### 3. 配置环境变量
创建 `.env` 文件或设置系统环境变量：
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=gift_ledger
DB_USER=postgres
DB_PASSWORD=你的密码
SERVER_PORT=8080
```

### 4. 启用数据库初始化
在 `Application.kt` 中取消注释：
```kotlin
fun main() {
    val dbConfig = DbConfig.fromEnv()
    DatabaseFactory.init(dbConfig)

    // ...
}
```

## 📝 测试 API

### 使用 curl 测试健康检查
```bash
curl http://localhost:8080/health
```

预期响应：
```json
{
  "status": "ok"
}
```

### 使用 curl 测试注册
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "username": "testuser"
  }'
```

## 🔍 故障排查

### 问题：端口已被占用
**解决方案**：修改端口
```bash
# Windows
set SERVER_PORT=8081
./gradlew :app:run

# Linux/Mac
export SERVER_PORT=8081
./gradlew :app:run
```

### 问题：编译错误
**解决方案**：清理并重新构建
```bash
./gradlew clean build
```

### 问题：找不到 Java
**解决方案**：确保 JDK 11 或更高版本已安装
```bash
java -version
```

## 📦 项目结构

```
Gift_Ledger/
├── adapters/
│   ├── http/          # HTTP 适配器（API 层）
│   │   ├── routes/    # 路由定义
│   │   ├── Application.kt
│   │   ├── DI.kt      # 依赖注入配置
│   │   ├── ErrorHandling.kt
│   │   └── Routes.kt
│   └── persistence/   # 持久化适配器
├── application/       # 应用服务层
│   ├── services/      # 业务服务
│   └── ports/         # 接口定义
├── domain/           # 领域模型
├── infrastructure/   # 基础设施
│   ├── db/          # 数据库配置
│   ├── jwt/         # JWT 服务
│   └── security/    # 安全相关
└── app/             # 应用入口
```

## 🎯 下一步

1. **实现真实的 Repository**：替换 DI.kt 中的 Mock 实现
2. **添加数据库迁移**：使用 Flyway 创建数据库表
3. **完善业务逻辑**：实现完整的业务规则
4. **添加单元测试**：为服务和路由添加测试
5. **添加 API 文档**：使用 OpenAPI/Swagger

## 📞 支持

如有问题，请检查：
1. 日志输出
2. 端口是否被占用
3. Java 版本是否正确
4. 依赖是否正确下载
