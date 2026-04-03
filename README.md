# Gift Ledger 礼金记账系统

一个用于管理人情往来的应用，帮助用户记录收送礼金、管理宾客关系、追踪活动账本。

## 🚀 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Gradle 8.x

### 配置数据库
编辑 `.env` 文件配置 MySQL 连接：
```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=gift_ledger
DB_USER=root
DB_PASSWORD=your_password
SERVER_PORT=8081
JWT_SECRET=please-change-me
```

### 启动服务
```bash
./gradlew :app:run
```
Windows:
```bat
gradlew.bat :app:run
```

## 📖 API 文档

启动服务后，访问以下地址查看在线 API 文档：

| 文档类型 | 地址 | 说明 |
|---------|------|------|
| **Swagger UI** | http://localhost:8081/ | 交互式文档，支持在线调试 |
| **ReDoc** | http://localhost:8081/redoc.html | 美观的只读文档 |
| **OpenAPI JSON** | http://localhost:8081/openapi.json | OpenAPI 3.0 规范文件 |
| **健康检查** | http://localhost:8081/health | 服务状态检查 |
| **ShowDoc** | https://www.showdoc.com.cn/3276114 | 在线云端文档 |

### ShowDoc 集成

支持将 API 文档同步到 ShowDoc 平台：

1. **手动导入**：将 `openapi.json` 文件导入到 ShowDoc
2. **自动同步**：配置 `.env` 文件后运行同步脚本
   ```bash
   # Windows
   .\sync-to-showdoc.bat
   
   # Python
   python sync_showdoc.py
   ```

详细说明请查看 [SHOWDOC_INTEGRATION.md](SHOWDOC_INTEGRATION.md)

## 🔧 Gradle 命令

* `./gradlew :app:run` - 构建并运行应用
* `./gradlew build` - 仅构建应用
* `./gradlew check` - 运行所有检查和测试
* `./gradlew clean` - 清理构建输出

## 📁 项目结构

```
Gift_Ledger/
├── domain/              # 领域模型
├── application/         # 应用服务和端口接口
├── adapters/
│   ├── http/           # HTTP 适配器 (Ktor 路由)
│   └── persistence/    # 持久化适配器 (MySQL/Exposed)
├── infrastructure/      # 基础设施 (数据库、JWT、密码加密)
├── app/                # 应用入口
└── utils/              # 工具类
```

## 🔐 认证方式

API 使用 JWT 令牌认证。登录后获取 `accessToken`，在请求头中携带：

```
Authorization: Bearer <your_access_token>
```

---

This project uses [Gradle](https://gradle.org/) with the Gradle Wrapper.
