# ShowDoc 集成指南

本文档说明如何将 Gift Ledger API 文档集成到 ShowDoc。

## 方式一：手动导入 OpenAPI 文档

### 步骤

1. **启动 Gift Ledger API 服务**
   ```bash
   .\gradlew :app:run
   ```

2. **获取 OpenAPI JSON 文件**
   - 访问：http://localhost:8081/openapi.json
   - 或直接使用项目中的文件：`adapters/http/src/main/resources/static/openapi.json`

3. **登录 ShowDoc**
   - 访问你的 ShowDoc 地址（如：https://www.showdoc.com.cn/）
   - 登录你的账号

4. **创建或选择项目**
   - 在 ShowDoc 中创建新项目或选择现有项目

5. **导入 OpenAPI 文档**
   - 点击项目设置 → 导入文档
   - 选择 "OpenAPI/Swagger" 格式
   - 上传 `openapi.json` 文件或粘贴 JSON 内容
   - 点击导入

6. **验证导入结果**
   - 检查所有 API 端点是否正确导入
   - 验证请求/响应示例是否完整

---

## 方式二：使用 ShowDoc API 自动同步

ShowDoc 提供了 API 接口，可以通过脚本自动同步文档。

### 前置准备

1. **获取 ShowDoc API Token**
   - 登录 ShowDoc
   - 进入项目设置 → API 设置
   - 复制 API Token 和 API Key

2. **配置环境变量**
   
   在 `.env` 文件中添加：
   ```properties
   # ShowDoc 配置
   SHOWDOC_API_URL=https://www.showdoc.com.cn/server/api/item/updateByApi
   SHOWDOC_API_KEY=your_api_key_here
   SHOWDOC_API_TOKEN=your_api_token_here
   ```

### 使用同步脚本

运行同步脚本：
```bash
# Windows
.\sync-to-showdoc.bat

# Linux/Mac
./sync-to-showdoc.sh
```

---

## 方式三：通过 ShowDoc 开放 API 批量导入

### 使用 PowerShell 脚本

```powershell
# 见 sync-showdoc.ps1 文件
.\sync-showdoc.ps1
```

### 使用 Python 脚本

```bash
# 见 sync_showdoc.py 文件
python sync_showdoc.py
```

---

## 文档更新流程

当 API 发生变化时：

1. **更新 OpenAPI 文档**
   - 修改 `openapi.json` 文件
   - 或通过代码注解自动生成

2. **重新导入到 ShowDoc**
   - 手动：重复方式一的步骤
   - 自动：运行同步脚本

3. **验证更新**
   - 检查 ShowDoc 中的文档是否已更新
   - 测试 API 调用示例

---

## 注意事项

1. **API Token 安全**
   - 不要将 API Token 提交到版本控制
   - 使用环境变量或配置文件管理

2. **文档版本管理**
   - 建议在 ShowDoc 中为不同版本创建不同项目
   - 或使用文档版本标签

3. **自动化建议**
   - 可以将同步脚本集成到 CI/CD 流程
   - 在 API 部署后自动更新文档

---

## 常见问题

### Q: ShowDoc 不支持某些 OpenAPI 特性怎么办？
A: 可以手动在 ShowDoc 中补充说明，或使用 ShowDoc 的 Markdown 编辑器。

### Q: 如何保持文档和代码同步？
A: 建议使用代码注解生成 OpenAPI 文档，或在 CI/CD 中自动同步。

### Q: 可以同时使用 Swagger UI 和 ShowDoc 吗？
A: 可以。Swagger UI 用于开发调试，ShowDoc 用于团队协作和对外展示。

---

## 相关链接

- [ShowDoc 官方文档](https://www.showdoc.com.cn/help)
- [OpenAPI 规范](https://swagger.io/specification/)
- [Gift Ledger API 文档](http://localhost:8081/)
