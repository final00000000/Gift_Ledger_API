#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Gift Ledger API 文档同步到 ShowDoc
Python 脚本 - 增强版
"""

import json
import os
import time
import urllib.request
import urllib.parse
from typing import Dict, List, Any
from pathlib import Path


def load_env_file(path: str = ".env") -> Dict[str, str]:
    """加载 .env 文件"""
    env_vars = {}
    if not os.path.exists(path):
        raise FileNotFoundError(f"未找到 .env 文件: {path}")
    
    with open(path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith('#') and '=' in line:
                key, value = line.split('=', 1)
                env_vars[key.strip()] = value.strip()
    
    return env_vars


def read_openapi_doc(path: str) -> Dict[str, Any]:
    """读取 OpenAPI JSON 文档"""
    if not os.path.exists(path):
        raise FileNotFoundError(f"未找到 OpenAPI 文档: {path}")
    
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)


# 完整的API响应示例（统一格式：code, message, data）
API_EXAMPLES = {
    "/auth/register": {
        "request": {
            "email": "zhangsan@example.com",
            "password": "password123",
            "username": "zhangsan",
            "fullName": "张三"
        },
        "response": {
            "code": 201,
            "message": "注册成功",
            "data": {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "username": "zhangsan",
                "email": "zhangsan@example.com",
                "fullName": "张三",
                "createdAt": "2024-01-15T10:30:00Z"
            }
        }
    },
    "/auth/login": {
        "request": {
            "email": "zhangsan@example.com",
            "password": "password123"
        },
        "response": {
            "code": 200,
            "message": "登录成功",
            "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
                "expiresIn": 3600
            }
        }
    },
    "/auth/refresh": {
        "request": {
            "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
        },
        "response": {
            "code": 200,
            "message": "刷新成功",
            "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new...",
                "refreshToken": "bmV3IHJlZnJlc2ggdG9rZW4...",
                "expiresIn": 3600
            }
        }
    },
    "/auth/logout": {
        "request": {
            "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
        },
        "response": {
            "code": 200,
            "message": "登出成功",
            "data": None
        }
    },
    "/auth/change-password": {
        "request": {
            "currentPassword": "password123",
            "newPassword": "newpassword456"
        },
        "response": {
            "code": 200,
            "message": "密码修改成功",
            "data": None
        }
    },
    "/users/me GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "username": "zhangsan",
                "email": "zhangsan@example.com",
                "fullName": "张三",
                "createdAt": "2024-01-15T10:30:00Z"
            }
        }
    },
    "/users/me PUT": {
        "request": {
            "username": "zhangsan_new",
            "email": "zhangsan_new@example.com",
            "fullName": "张三丰"
        },
        "response": {
            "code": 200,
            "message": "更新成功",
            "data": {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "username": "zhangsan_new",
                "email": "zhangsan_new@example.com",
                "fullName": "张三丰",
                "createdAt": "2024-01-15T10:30:00Z"
            }
        }
    },
    "/gifts GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "items": [
                    {
                        "id": "660e8400-e29b-41d4-a716-446655440001",
                        "guestId": "770e8400-e29b-41d4-a716-446655440002",
                        "isReceived": True,
                        "amount": 500,
                        "eventType": "婚礼",
                        "eventBookId": "880e8400-e29b-41d4-a716-446655440003",
                        "occurredAt": "2024-01-15",
                        "note": "新婚快乐",
                        "relatedGiftId": None,
                        "isReturned": False,
                        "createdAt": "2024-01-15T10:30:00Z",
                        "updatedAt": "2024-01-15T10:30:00Z"
                    }
                ],
                "total": 1,
                "page": 1,
                "pageSize": 20,
                "totalPages": 1
            }
        }
    },
    "/gifts POST": {
        "request": {
            "guestId": "770e8400-e29b-41d4-a716-446655440002",
            "isReceived": True,
            "amount": 500,
            "eventType": "婚礼",
            "eventBookId": "880e8400-e29b-41d4-a716-446655440003",
            "occurredAt": "2024-01-15",
            "note": "新婚快乐"
        },
        "response": {
            "code": 201,
            "message": "创建成功",
            "data": {
                "id": "660e8400-e29b-41d4-a716-446655440001",
                "guestId": "770e8400-e29b-41d4-a716-446655440002",
                "isReceived": True,
                "amount": 500,
                "eventType": "婚礼",
                "eventBookId": "880e8400-e29b-41d4-a716-446655440003",
                "occurredAt": "2024-01-15",
                "note": "新婚快乐",
                "relatedGiftId": None,
                "isReturned": False,
                "createdAt": "2024-01-15T10:30:00Z",
                "updatedAt": "2024-01-15T10:30:00Z"
            }
        }
    },
    "/gifts/{id} GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "id": "660e8400-e29b-41d4-a716-446655440001",
                "guestId": "770e8400-e29b-41d4-a716-446655440002",
                "isReceived": True,
                "amount": 500,
                "eventType": "婚礼",
                "eventBookId": "880e8400-e29b-41d4-a716-446655440003",
                "occurredAt": "2024-01-15",
                "note": "新婚快乐",
                "relatedGiftId": None,
                "isReturned": False,
                "createdAt": "2024-01-15T10:30:00Z",
                "updatedAt": "2024-01-15T10:30:00Z"
            }
        }
    },
    "/gifts/{id} PUT": {
        "request": {
            "amount": 600,
            "note": "金额更正"
        },
        "response": {
            "code": 200,
            "message": "更新成功",
            "data": {
                "id": "660e8400-e29b-41d4-a716-446655440001",
                "guestId": "770e8400-e29b-41d4-a716-446655440002",
                "isReceived": True,
                "amount": 600,
                "eventType": "婚礼",
                "eventBookId": "880e8400-e29b-41d4-a716-446655440003",
                "occurredAt": "2024-01-15",
                "note": "金额更正",
                "relatedGiftId": None,
                "isReturned": False,
                "createdAt": "2024-01-15T10:30:00Z",
                "updatedAt": "2024-01-15T11:00:00Z"
            }
        }
    },
    "/gifts/{id} DELETE": {
        "response": {
            "code": 200,
            "message": "删除成功",
            "data": None
        }
    },
    "/guests GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "items": [
                    {
                        "id": "770e8400-e29b-41d4-a716-446655440002",
                        "name": "李四",
                        "relationship": "同事",
                        "phone": "13800138001",
                        "note": "技术部同事",
                        "createdAt": "2024-01-10T09:00:00Z",
                        "updatedAt": "2024-01-10T09:00:00Z"
                    }
                ],
                "total": 1,
                "page": 1,
                "pageSize": 20,
                "totalPages": 1
            }
        }
    },
    "/guests POST": {
        "request": {
            "name": "李四",
            "relationship": "同事",
            "phone": "13800138001",
            "note": "技术部同事"
        },
        "response": {
            "code": 201,
            "message": "创建成功",
            "data": {
                "id": "770e8400-e29b-41d4-a716-446655440002",
                "name": "李四",
                "relationship": "同事",
                "phone": "13800138001",
                "note": "技术部同事",
                "createdAt": "2024-01-10T09:00:00Z",
                "updatedAt": "2024-01-10T09:00:00Z"
            }
        }
    },
    "/guests/{id} GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "id": "770e8400-e29b-41d4-a716-446655440002",
                "name": "李四",
                "relationship": "同事",
                "phone": "13800138001",
                "note": "技术部同事",
                "createdAt": "2024-01-10T09:00:00Z",
                "updatedAt": "2024-01-10T09:00:00Z"
            }
        }
    },
    "/guests/{id} PUT": {
        "request": {
            "name": "李四",
            "relationship": "好友",
            "phone": "13900139001"
        },
        "response": {
            "code": 200,
            "message": "更新成功",
            "data": {
                "id": "770e8400-e29b-41d4-a716-446655440002",
                "name": "李四",
                "relationship": "好友",
                "phone": "13900139001",
                "note": "技术部同事",
                "createdAt": "2024-01-10T09:00:00Z",
                "updatedAt": "2024-01-15T10:00:00Z"
            }
        }
    },
    "/guests/{id} DELETE": {
        "response": {
            "code": 200,
            "message": "删除成功",
            "data": None
        }
    },
    "/event-books GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "eventBooks": [
                    {
                        "id": "880e8400-e29b-41d4-a716-446655440003",
                        "name": "张三婚礼",
                        "type": "婚礼",
                        "eventDate": "2024-05-01",
                        "lunarDate": "三月廿三",
                        "note": "在北京举办",
                        "createdAt": "2024-04-01T10:00:00Z",
                        "updatedAt": "2024-04-01T10:00:00Z"
                    }
                ],
                "total": 1
            }
        }
    },
    "/event-books POST": {
        "request": {
            "name": "张三婚礼",
            "type": "婚礼",
            "eventDate": "2024-05-01",
            "lunarDate": "三月廿三",
            "note": "在北京举办"
        },
        "response": {
            "code": 201,
            "message": "创建成功",
            "data": {
                "id": "880e8400-e29b-41d4-a716-446655440003",
                "name": "张三婚礼",
                "type": "婚礼",
                "eventDate": "2024-05-01",
                "lunarDate": "三月廿三",
                "note": "在北京举办",
                "createdAt": "2024-04-01T10:00:00Z",
                "updatedAt": "2024-04-01T10:00:00Z"
            }
        }
    },
    "/event-books/{id} GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "id": "880e8400-e29b-41d4-a716-446655440003",
                "name": "张三婚礼",
                "type": "婚礼",
                "eventDate": "2024-05-01",
                "lunarDate": "三月廿三",
                "note": "在北京举办",
                "createdAt": "2024-04-01T10:00:00Z",
                "updatedAt": "2024-04-01T10:00:00Z"
            }
        }
    },
    "/event-books/{id} PUT": {
        "request": {
            "name": "张三婚礼（更新）",
            "note": "改在上海举办"
        },
        "response": {
            "code": 200,
            "message": "更新成功",
            "data": {
                "id": "880e8400-e29b-41d4-a716-446655440003",
                "name": "张三婚礼（更新）",
                "type": "婚礼",
                "eventDate": "2024-05-01",
                "lunarDate": "三月廿三",
                "note": "改在上海举办",
                "createdAt": "2024-04-01T10:00:00Z",
                "updatedAt": "2024-04-10T15:00:00Z"
            }
        }
    },
    "/event-books/{id} DELETE": {
        "response": {
            "code": 200,
            "message": "删除成功",
            "data": None
        }
    },
    "/reports/summary GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "totalReceived": 15000,
                "totalSent": 8000,
                "netAmount": 7000,
                "giftCount": 25,
                "guestCount": 18
            }
        }
    },
    "/reminders/pending GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "reminders": [
                    {
                        "gift": {
                            "id": "660e8400-e29b-41d4-a716-446655440001",
                            "guestId": "770e8400-e29b-41d4-a716-446655440002",
                            "isReceived": True,
                            "amount": 500,
                            "eventType": "婚礼",
                            "occurredAt": "2024-01-15"
                        },
                        "guestName": "李四",
                        "daysSinceReceived": 180,
                        "suggestedAmount": 500
                    }
                ],
                "total": 1
            }
        }
    },
    "/exports/json GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "exportedAt": "2024-07-15T10:30:00Z",
                "guests": [],
                "eventBooks": [],
                "gifts": []
            }
        }
    },
    "/health GET": {
        "response": {
            "code": 200,
            "message": "success",
            "data": {
                "status": "ok"
            }
        }
    }
}


# 统一错误码文档
ERROR_CODES_DOC = """# API 响应格式说明

本文档说明 Gift Ledger API 的统一响应格式和错误码。

## 统一响应格式

所有 API 响应都遵循统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "errors": null
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | number | HTTP 状态码 |
| message | string | 响应消息（成功时为 "success" 或操作描述，失败时为错误信息） |
| data | object/array/null | 响应数据（成功时返回数据，失败时为 null） |
| errors | object/null | 字段级错误详情（仅验证失败时返回） |

### 成功响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "张三",
    "email": "zhangsan@example.com"
  }
}
```

### 错误响应示例

```json
{
  "code": 400,
  "message": "验证失败",
  "data": null,
  "errors": {
    "email": "邮箱格式无效",
    "password": "密码长度至少8位"
  }
}
```

## HTTP 状态码

| 状态码 | 名称 | 说明 |
|--------|------|------|
| 200 | OK | 请求成功 |
| 201 | Created | 资源创建成功 |
| 400 | Bad Request | 请求参数错误或验证失败 |
| 401 | Unauthorized | 未授权（未登录或令牌无效/过期） |
| 403 | Forbidden | 禁止访问（无权限操作该资源） |
| 404 | Not Found | 资源不存在 |
| 409 | Conflict | 资源冲突（如邮箱已被注册） |
| 500 | Internal Server Error | 服务器内部错误 |

## 常见错误类型

### 认证相关错误 (401)

| 错误信息 | 说明 | 解决方案 |
|----------|------|----------|
| 未授权 | 请求未携带有效的访问令牌 | 在请求头添加 `Authorization: Bearer <token>` |
| 令牌已过期 | 访问令牌已过期 | 使用刷新令牌获取新的访问令牌 |
| 无效的令牌 | 令牌格式错误或已被篡改 | 重新登录获取新令牌 |
| 刷新令牌无效 | 刷新令牌不存在或已被撤销 | 重新登录 |
| 登录失败 | 邮箱或密码错误 | 检查邮箱和密码 |

### 验证相关错误 (400)

| 错误信息 | 说明 | 解决方案 |
|----------|------|----------|
| 邮箱格式无效 | 邮箱地址格式无效 | 使用正确的邮箱格式 |
| 密码长度至少8位 | 密码长度不足 | 使用至少8位的密码 |
| 邮箱已注册 | 该邮箱已存在账号 | 使用其他邮箱或找回密码 |
| 姓名不能为空 | 必填字段为空 | 填写必填字段 |
| 金额必须大于0 | 金额值无效 | 输入正数金额 |
| 日期格式无效 | 日期格式无效 | 使用 YYYY-MM-DD 格式 |
| 宾客不存在 | 关联的宾客ID无效 | 先创建宾客或使用正确的ID |

### 资源相关错误 (404)

| 错误信息 | 说明 | 解决方案 |
|----------|------|----------|
| 礼物记录不存在 | 指定ID的礼物不存在 | 检查ID是否正确 |
| 宾客不存在 | 指定ID的宾客不存在 | 检查ID是否正确 |
| 活动账本不存在 | 指定ID的账本不存在 | 检查ID是否正确 |
| 用户不存在 | 指定的用户不存在 | 检查用户ID |

### 业务逻辑错误 (400)

| 错误信息 | 说明 | 解决方案 |
|----------|------|----------|
| 无法删除：该宾客有关联的礼物记录 | 宾客存在关联数据 | 先删除关联的礼物记录 |
| 无法删除：该活动账本有关联的礼物记录 | 账本存在关联数据 | 先删除关联的礼物记录 |
| 当前密码不正确 | 修改密码时旧密码不正确 | 输入正确的当前密码 |
| 新密码不能与当前密码相同 | 新旧密码相同 | 使用不同的新密码 |

## 分页响应格式

列表接口返回分页数据时，data 字段包含以下结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [...],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| items | array | 当前页数据列表 |
| total | number | 总记录数 |
| page | number | 当前页码 |
| pageSize | number | 每页数量 |
| totalPages | number | 总页数 |

## 错误处理建议

1. **检查 code 字段**：根据状态码判断请求是否成功（2xx 为成功）
2. **读取 message**：获取操作结果描述或错误信息
3. **处理 errors**：如果存在 errors 字段，显示具体字段的错误提示
4. **令牌刷新**：收到 401 错误时，尝试使用刷新令牌获取新的访问令牌
5. **友好提示**：将错误信息转换为用户友好的提示语

## 防刷机制

API 实现了多层防刷保护：

### 1. IP 限流
- 每个 IP 每分钟最多 60 次请求
- 超限返回 429 状态码

### 2. 设备指纹限流
- 根据 User-Agent 等信息生成设备指纹
- 每个设备每分钟最多 30 次请求

### 3. 用户限流
- 已登录用户每分钟最多 100 次请求
- 按 JWT Token 中的用户ID计数

### 4. 登录保护
- 同一邮箱连续登录失败 5 次后锁定账号
- 锁定时间 15 分钟
- 登录成功后自动清除失败记录

### 5. 全局限流
- 整个系统每秒最多处理 1000 次请求
- 防止 DDoS 攻击

### 限流响应示例

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后重试",
  "data": null
}
```

### 账号锁定响应示例

```json
{
  "code": 429,
  "message": "账号已被锁定，请900秒后重试",
  "data": null
}
```
"""


def get_example_key(path: str, method: str) -> str:
    """生成示例查找键"""
    # 尝试多种格式
    keys = [
        f"{path} {method}",
        path,
        f"{path} {method.upper()}"
    ]
    for key in keys:
        if key in API_EXAMPLES:
            return key
    return None


def convert_to_showdoc_format(openapi_doc: Dict[str, Any]) -> List[Dict[str, str]]:
    """转换 OpenAPI 到 ShowDoc 格式（增强版）"""
    showdoc_items = []
    
    paths = openapi_doc.get('paths', {})
    
    for path_name, path_item in paths.items():
        for method, operation in path_item.items():
            method_upper = method.upper()
            
            if method_upper not in ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']:
                continue
            
            # 构建文档内容
            tags = operation.get('tags', ['默认分类'])
            cat_name = tags[0] if tags else '默认分类'
            summary = operation.get('summary', f'{method_upper} {path_name}')
            page_title = summary
            
            # 请求参数
            parameters_md = "无"
            if 'parameters' in operation and operation['parameters']:
                params = []
                for param in operation['parameters']:
                    param_type = param.get('schema', {}).get('type', 'string')
                    required = '必填' if param.get('required', False) else '可选'
                    desc = param.get('description', '-')
                    example = param.get('schema', {}).get('example', '')
                    if example:
                        desc = f"{desc}，示例: `{example}`"
                    params.append(
                        f"| {param['name']} | {param.get('in', 'query')} | {param_type} | {required} | {desc} |"
                    )
                parameters_md = "| 参数名 | 位置 | 类型 | 必填 | 说明 |\n|--------|------|------|------|------|\n" + '\n'.join(params)
            
            # 查找示例
            example_key = get_example_key(path_name, method_upper)
            example_data = API_EXAMPLES.get(example_key, {}) if example_key else {}
            
            # 请求体
            request_body_md = "无"
            if 'requestBody' in operation:
                content = operation['requestBody'].get('content', {})
                if 'application/json' in content:
                    # 优先使用预定义示例
                    if 'request' in example_data:
                        example = example_data['request']
                    else:
                        schema = content['application/json'].get('schema', {})
                        example = content['application/json'].get('example', schema)
                    request_body_md = f"```json\n{json.dumps(example, indent=2, ensure_ascii=False)}\n```"
            
            # 响应示例
            response_md = ""
            responses = operation.get('responses', {})
            for code in ['200', '201', '204']:
                if code in responses:
                    response = responses[code]
                    desc = response.get('description', '')
                    
                    if code == '204':
                        response_md = f"**{code} {desc}**\n\n无返回内容"
                    elif 'response' in example_data:
                        if example_data['response'] is None:
                            response_md = f"**{code} {desc}**\n\n无返回内容"
                        else:
                            response_md = f"**{code} {desc}**\n\n```json\n{json.dumps(example_data['response'], indent=2, ensure_ascii=False)}\n```"
                    else:
                        content = response.get('content', {})
                        if 'application/json' in content:
                            example = content['application/json'].get('example', {})
                            if example:
                                response_md = f"**{code} {desc}**\n\n```json\n{json.dumps(example, indent=2, ensure_ascii=False)}\n```"
                        if not response_md:
                            response_md = f"**{code} {desc}**"
                    break
            
            if not response_md:
                response_md = "无"
            
            page_content = f"""**简要描述：** {operation.get('description', summary)}

**请求URL：** `{path_name}`

**请求方式：** {method_upper}

**请求参数：**

{parameters_md}

**请求体：**

{request_body_md}

**返回示例：**

{response_md}

**错误码：** 请参考 [错误码说明](错误码说明) 文档
"""
            
            showdoc_items.append({
                'cat_name': cat_name,
                'page_title': page_title,
                'page_content': page_content
            })
    
    return showdoc_items


def sync_to_showdoc(
    api_url: str,
    api_key: str,
    api_token: str,
    items: List[Dict[str, str]]
) -> tuple:
    """同步到 ShowDoc"""
    success_count = 0
    fail_count = 0
    
    for item in items:
        try:
            data = {
                'api_key': api_key,
                'api_token': api_token,
                'cat_name': item['cat_name'],
                'page_title': item['page_title'],
                'page_content': item['page_content']
            }
            
            print(f"正在同步: {item['page_title']}...", end=' ', flush=True)
            
            encoded_data = urllib.parse.urlencode(data).encode('utf-8')
            req = urllib.request.Request(api_url, data=encoded_data, method='POST')
            req.add_header('Content-Type', 'application/x-www-form-urlencoded')
            
            with urllib.request.urlopen(req, timeout=30) as response:
                result = json.loads(response.read().decode('utf-8'))
            
            if result.get('error_code') == 0:
                print("✓ 成功")
                success_count += 1
            else:
                print(f"✗ 失败: {result.get('error_message', '未知错误')}")
                fail_count += 1
        
        except Exception as e:
            print(f"✗ 异常: {str(e)}")
            fail_count += 1
        
        time.sleep(1.0)  # 增加延迟避免被限流
    
    return success_count, fail_count


def sync_error_codes_doc(api_url: str, api_key: str, api_token: str) -> bool:
    """同步错误码文档"""
    try:
        data = {
            'api_key': api_key,
            'api_token': api_token,
            'cat_name': '通用说明',
            'page_title': '错误码说明',
            'page_content': ERROR_CODES_DOC
        }
        
        print("正在同步: 错误码说明...", end=' ', flush=True)
        
        encoded_data = urllib.parse.urlencode(data).encode('utf-8')
        req = urllib.request.Request(api_url, data=encoded_data, method='POST')
        req.add_header('Content-Type', 'application/x-www-form-urlencoded')
        
        with urllib.request.urlopen(req, timeout=30) as response:
            result = json.loads(response.read().decode('utf-8'))
        
        if result.get('error_code') == 0:
            print("✓ 成功")
            return True
        else:
            print(f"✗ 失败: {result.get('error_message', '未知错误')}")
            return False
    
    except Exception as e:
        print(f"✗ 异常: {str(e)}")
        return False


def main():
    """主流程"""
    print("=" * 50)
    print("Gift Ledger API 文档同步到 ShowDoc")
    print("=" * 50)
    print()
    
    try:
        # 加载环境变量
        print("[1/5] 加载配置...")
        env = load_env_file()
        
        api_url = env.get('SHOWDOC_API_URL')
        api_key = env.get('SHOWDOC_API_KEY')
        api_token = env.get('SHOWDOC_API_TOKEN')
        
        if not all([api_url, api_key, api_token]):
            raise ValueError("ShowDoc 配置不完整，请检查 .env 文件")
        
        print(f"    API URL: {api_url}")
        print(f"    API Key: {api_key[:10]}...")
        
        # 读取 OpenAPI 文档
        print("\n[2/5] 读取 OpenAPI 文档...")
        openapi_path = "adapters/http/src/main/resources/static/openapi.json"
        openapi_doc = read_openapi_doc(openapi_path)
        print(f"    文档标题: {openapi_doc.get('info', {}).get('title', 'Unknown')}")
        print(f"    文档版本: {openapi_doc.get('info', {}).get('version', 'Unknown')}")
        
        # 转换格式
        print("\n[3/5] 转换文档格式...")
        showdoc_items = convert_to_showdoc_format(openapi_doc)
        print(f"    共 {len(showdoc_items)} 个接口待同步")
        
        # 同步错误码文档
        print("\n[4/5] 同步错误码文档...\n")
        error_doc_success = sync_error_codes_doc(api_url, api_key, api_token)
        
        # 同步 API 文档
        print("\n[5/5] 同步 API 文档...\n")
        success, fail = sync_to_showdoc(api_url, api_key, api_token, showdoc_items)
        
        # 统计
        total_success = success + (1 if error_doc_success else 0)
        total_fail = fail + (0 if error_doc_success else 1)
        
        print(f"\n{'=' * 50}")
        print(f"同步完成: 成功 {total_success} 个, 失败 {total_fail} 个")
        
        if total_fail == 0:
            print("\n✓ 全部同步成功！")
        else:
            print(f"\n⚠ 有 {total_fail} 个文档同步失败")
        
        return 0 if total_fail == 0 else 1
    
    except Exception as e:
        print(f"\n✗ 同步失败: {str(e)}")
        return 1


if __name__ == '__main__':
    exit(main())
