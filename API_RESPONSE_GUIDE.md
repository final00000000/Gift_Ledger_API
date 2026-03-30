# Gift Ledger API 响应数据指南

本文档详细说明了所有 API 端点的成功响应格式和数据结构。

---

## 📋 目录

1. [认证接口](#认证接口)
2. [用户管理](#用户管理)
3. [礼物管理](#礼物管理)
4. [宾客管理](#宾客管理)
5. [活动账本](#活动账本)
6. [报告统计](#报告统计)
7. [提醒功能](#提醒功能)
8. [数据导出](#数据导出)

---

## 认证接口

### POST /auth/register - 用户注册

**请求示例：**
```json
{
  "email": "zhangsan@example.com",
  "password": "password123",
  "username": "zhangsan",
  "fullName": "张三"
}
```

**成功响应 (200)：**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "fullName": "张三",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| userId | UUID | 用户唯一标识 |
| username | string | 用户名 |
| email | string | 邮箱地址 |
| fullName | string | 真实姓名 |
| createdAt | datetime | 注册时间 |

---

### POST /auth/login - 用户登录

**请求示例：**
```json
{
  "email": "zhangsan@example.com",
  "password": "password123"
}
```

**成功响应 (200)：**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE3MDUzMjAwMDAsImV4cCI6MTcwNTMyMzYwMH0.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE3MDUzMjAwMDAsImV4cCI6MTcwNTkyNDAwMH0.signature",
  "expiresIn": 3600
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| accessToken | string | 访问令牌（有效期1小时） |
| refreshToken | string | 刷新令牌（有效期7天） |
| expiresIn | integer | 访问令牌有效期（秒） |

---

### POST /auth/refresh - 刷新令牌

**请求示例：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**成功响应 (200)：**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE3MDUzMjAwMDAsImV4cCI6MTcwNTMyMzYwMH0.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE3MDUzMjAwMDAsImV4cCI6MTcwNTkyNDAwMH0.signature",
  "expiresIn": 3600
}
```

---

### POST /auth/logout - 用户登出

**请求示例：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**成功响应 (200)：**
```json
{
  "message": "登出成功"
}
```

---

### POST /auth/change-password - 修改密码

**请求示例：**
```json
{
  "currentPassword": "password123",
  "newPassword": "newpassword456"
}
```

**成功响应 (200)：**
```json
{
  "message": "密码修改成功"
}
```

---

## 用户管理

### GET /users/me - 获取当前用户资料

**成功响应 (200)：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "fullName": "张三",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

### PUT /users/me - 更新用户资料

**请求示例：**
```json
{
  "username": "zhangsan_new",
  "email": "zhangsan_new@example.com",
  "fullName": "张三（新）"
}
```

**成功响应 (200)：**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "zhangsan_new",
  "email": "zhangsan_new@example.com",
  "fullName": "张三（新）",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

## 礼物管理

### POST /gifts - 创建礼物记录

**请求示例：**
```json
{
  "guestId": "660e8400-e29b-41d4-a716-446655440001",
  "isReceived": true,
  "amount": 500,
  "eventType": "婚礼",
  "eventBookId": "770e8400-e29b-41d4-a716-446655440002",
  "occurredAt": "2024-01-15",
  "note": "张三婚礼红包"
}
```

**成功响应 (201)：**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "guestId": "660e8400-e29b-41d4-a716-446655440001",
  "isReceived": true,
  "amount": 500,
  "eventType": "婚礼",
  "eventBookId": "770e8400-e29b-41d4-a716-446655440002",
  "occurredAt": "2024-01-15",
  "note": "张三婚礼红包",
  "relatedGiftId": null,
  "isReturned": false,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 礼物记录唯一标识 |
| guestId | UUID | 关联的宾客ID |
| isReceived | boolean | 是否为收礼（true=收礼，false=送礼） |
| amount | number | 金额（元） |
| eventType | string | 活动类型 |
| eventBookId | UUID | 关联的活动账本ID |
| occurredAt | date | 发生日期 |
| note | string | 备注信息 |
| relatedGiftId | UUID | 关联的礼物ID（用于标记已还礼） |
| isReturned | boolean | 是否已还礼 |
| createdAt | datetime | 创建时间 |
| updatedAt | datetime | 更新时间 |

---

### GET /gifts - 获取礼物列表

**请求示例：**
```
GET /gifts?page=1&pageSize=20&guestId=660e8400-e29b-41d4-a716-446655440001&isReceived=true
```

**成功响应 (200)：**
```json
{
  "gifts": [
    {
      "id": "880e8400-e29b-41d4-a716-446655440003",
      "guestId": "660e8400-e29b-41d4-a716-446655440001",
      "isReceived": true,
      "amount": 500,
      "eventType": "婚礼",
      "eventBookId": "770e8400-e29b-41d4-a716-446655440002",
      "occurredAt": "2024-01-15",
      "note": "张三婚礼红包",
      "relatedGiftId": null,
      "isReturned": false,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "990e8400-e29b-41d4-a716-446655440004",
      "guestId": "660e8400-e29b-41d4-a716-446655440001",
      "isReceived": true,
      "amount": 800,
      "eventType": "生日",
      "eventBookId": null,
      "occurredAt": "2024-01-20",
      "note": "生日礼金",
      "relatedGiftId": null,
      "isReturned": false,
      "createdAt": "2024-01-20T14:15:00Z",
      "updatedAt": "2024-01-20T14:15:00Z"
    }
  ],
  "total": 2,
  "page": 1,
  "pageSize": 20
}
```

---

### GET /gifts/{id} - 获取礼物详情

**成功响应 (200)：**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "guestId": "660e8400-e29b-41d4-a716-446655440001",
  "isReceived": true,
  "amount": 500,
  "eventType": "婚礼",
  "eventBookId": "770e8400-e29b-41d4-a716-446655440002",
  "occurredAt": "2024-01-15",
  "note": "张三婚礼红包",
  "relatedGiftId": null,
  "isReturned": false,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

### PUT /gifts/{id} - 更新礼物记录

**请求示例：**
```json
{
  "amount": 600,
  "note": "张三婚礼红包（已确认）"
}
```

**成功响应 (200)：**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "guestId": "660e8400-e29b-41d4-a716-446655440001",
  "isReceived": true,
  "amount": 600,
  "eventType": "婚礼",
  "eventBookId": "770e8400-e29b-41d4-a716-446655440002",
  "occurredAt": "2024-01-15",
  "note": "张三婚礼红包（已确认）",
  "relatedGiftId": null,
  "isReturned": false,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T11:45:00Z"
}
```

---

### DELETE /gifts/{id} - 删除礼物记录

**成功响应 (204)：**
```
无响应体
```

---

## 宾客管理

### POST /guests - 创建宾客

**请求示例：**
```json
{
  "name": "张三",
  "relationship": "同事",
  "phone": "13800138000",
  "note": "公司同事，经常一起出差"
}
```

**成功响应 (201)：**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "张三",
  "relationship": "同事",
  "phone": "13800138000",
  "note": "公司同事，经常一起出差",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

### GET /guests - 获取宾客列表

**请求示例：**
```
GET /guests?page=1&pageSize=20&search=张
```

**成功响应 (200)：**
```json
{
  "guests": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "name": "张三",
      "relationship": "同事",
      "phone": "13800138000",
      "note": "公司同事，经常一起出差",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "661e8400-e29b-41d4-a716-446655440002",
      "name": "张四",
      "relationship": "朋友",
      "phone": "13800138001",
      "note": "大学同学",
      "createdAt": "2024-01-16T09:15:00Z",
      "updatedAt": "2024-01-16T09:15:00Z"
    }
  ],
  "total": 2,
  "page": 1,
  "pageSize": 20
}
```

---

### GET /guests/{id} - 获取宾客详情

**成功响应 (200)：**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "张三",
  "relationship": "同事",
  "phone": "13800138000",
  "note": "公司同事，经常一起出差",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

### PUT /guests/{id} - 更新宾客

**请求示例：**
```json
{
  "phone": "13800138888",
  "note": "公司同事，已升职为经理"
}
```

**成功响应 (200)：**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "张三",
  "relationship": "同事",
  "phone": "13800138888",
  "note": "公司同事，已升职为经理",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T11:50:00Z"
}
```

---

### DELETE /guests/{id} - 删除宾客

**成功响应 (204)：**
```
无响应体
```

---

## 活动账本

### POST /event-books - 创建活动账本

**请求示例：**
```json
{
  "name": "张三婚礼",
  "type": "婚礼",
  "eventDate": "2024-05-01",
  "lunarDate": "三月廿三",
  "note": "在北京举办，预计200人"
}
```

**成功响应 (201)：**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "name": "张三婚礼",
  "type": "婚礼",
  "eventDate": "2024-05-01",
  "lunarDate": "三月廿三",
  "note": "在北京举办，预计200人",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

### GET /event-books - 获取活动账本列表

**成功响应 (200)：**
```json
{
  "eventBooks": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "name": "张三婚礼",
      "type": "婚礼",
      "eventDate": "2024-05-01",
      "lunarDate": "三月廿三",
      "note": "在北京举办，预计200人",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "771e8400-e29b-41d4-a716-446655440003",
      "name": "李四生日",
      "type": "生日",
      "eventDate": "2024-03-15",
      "lunarDate": null,
      "note": "在家举办小型聚会",
      "createdAt": "2024-01-20T14:20:00Z",
      "updatedAt": "2024-01-20T14:20:00Z"
    }
  ],
  "total": 2
}
```

---

### GET /event-books/{id} - 获取活动账本详情

**成功响应 (200)：**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "name": "张三婚礼",
  "type": "婚礼",
  "eventDate": "2024-05-01",
  "lunarDate": "三月廿三",
  "note": "在北京举办，预计200人",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

### PUT /event-books/{id} - 更新活动账本

**请求示例：**
```json
{
  "note": "在北京举办，实际参加180人"
}
```

**成功响应 (200)：**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "name": "张三婚礼",
  "type": "婚礼",
  "eventDate": "2024-05-01",
  "lunarDate": "三月廿三",
  "note": "在北京举办，实际参加180人",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T12:00:00Z"
}
```

---

### DELETE /event-books/{id} - 删除活动账本

**成功响应 (204)：**
```
无响应体
```

---

## 报告统计

### GET /reports/summary - 获取统计报告

**请求示例：**
```
GET /reports/summary?from=2024-01-01&to=2024-12-31
```

**成功响应 (200)：**
```json
{
  "totalReceived": 5000,
  "totalSent": 3200,
  "netAmount": 1800,
  "giftCount": 12,
  "guestCount": 8,
  "period": "2024-01-01 至 2024-12-31"
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| totalReceived | number | 总收礼金额（元） |
| totalSent | number | 总送礼金额（元） |
| netAmount | number | 净收支（收礼-送礼） |
| giftCount | integer | 礼物记录总数 |
| guestCount | integer | 涉及宾客数 |
| period | string | 统计时间范围 |

---

## 提醒功能

### GET /reminders/pending - 获取待还礼提醒

**成功响应 (200)：**
```json
{
  "reminders": [
    {
      "gift": {
        "id": "880e8400-e29b-41d4-a716-446655440003",
        "guestId": "660e8400-e29b-41d4-a716-446655440001",
        "isReceived": true,
        "amount": 500,
        "eventType": "婚礼",
        "eventBookId": "770e8400-e29b-41d4-a716-446655440002",
        "occurredAt": "2024-01-15",
        "note": "张三婚礼红包",
        "relatedGiftId": null,
        "isReturned": false,
        "createdAt": "2024-01-15T10:30:00Z",
        "updatedAt": "2024-01-15T10:30:00Z"
      },
      "guestName": "张三",
      "daysSinceReceived": 45,
      "suggestedAmount": 500
    }
  ],
  "total": 1
}
```

---

## 数据导出

### GET /exports/json - 导出JSON数据

**成功响应 (200)：**
```json
{
  "exportedAt": "2024-01-25T15:30:00Z",
  "guests": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "name": "张三",
      "relationship": "同事",
      "phone": "13800138000",
      "note": "公司同事，经常一起出差",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "eventBooks": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "name": "张三婚礼",
      "type": "婚礼",
      "eventDate": "2024-05-01",
      "lunarDate": "三月廿三",
      "note": "在北京举办，预计200人",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "gifts": [
    {
      "id": "880e8400-e29b-41d4-a716-446655440003",
      "guestId": "660e8400-e29b-41d4-a716-446655440001",
      "isReceived": true,
      "amount": 500,
      "eventType": "婚礼",
      "eventBookId": "770e8400-e29b-41d4-a716-446655440002",
      "occurredAt": "2024-01-15",
      "note": "张三婚礼红包",
      "relatedGiftId": null,
      "isReturned": false,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ]
}
```

---

### GET /exports/excel - 导出Excel数据

**成功响应 (200)：**
```
二进制 Excel 文件流
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="gift_ledger_export_2024-01-25.xlsx"
```

---

## 错误响应

### 400 Bad Request - 验证失败

```json
{
  "error": "验证失败",
  "details": {
    "amount": "金额必须大于0",
    "eventType": "活动类型不能为空"
  }
}
```

### 401 Unauthorized - 未授权

```json
{
  "error": "未授权"
}
```

### 404 Not Found - 资源不存在

```json
{
  "error": "礼物记录不存在"
}
```

### 500 Internal Server Error - 服务器错误

```json
{
  "error": "服务器内部错误"
}
```

---

## 通用说明

### 时间格式
- **日期格式**：`YYYY-MM-DD`（例：2024-01-15）
- **日期时间格式**：`YYYY-MM-DDTHH:mm:ssZ`（ISO 8601，UTC 时区）

### 分页参数
- **page**：页码，从 1 开始（默认值：1）
- **pageSize**：每页数量（默认值：20，最大值：100）

### 金额单位
- 所有金额字段单位为**元**，支持小数点后两位

### 认证方式
所有需要认证的接口都需要在请求头中携带 JWT 令牌：
```
Authorization: Bearer <accessToken>
```

---

**文档版本**：2.0.0  
**最后更新**：2024-01-25
