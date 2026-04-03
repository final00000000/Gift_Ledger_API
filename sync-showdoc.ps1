# Gift Ledger API 文档同步到 ShowDoc
# PowerShell 脚本

$ErrorActionPreference = "Stop"

# 读取 .env 文件
function Load-EnvFile {
    param([string]$Path = ".env")
    
    if (-not (Test-Path $Path)) {
        Write-Error "未找到 .env 文件"
        exit 1
    }
    
    $envVars = @{}
    Get-Content $Path | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            $envVars[$key] = $value
        }
    }
    return $envVars
}

# 读取 OpenAPI JSON 文件
function Read-OpenApiDoc {
    param([string]$Path)
    
    if (-not (Test-Path $Path)) {
        Write-Error "未找到 OpenAPI 文档: $Path"
        exit 1
    }
    
    return Get-Content $Path -Raw | ConvertFrom-Json
}

# 转换 OpenAPI 到 ShowDoc 格式
function Convert-ToShowDocFormat {
    param($OpenApiDoc)
    
    $showdocItems = @()
    
    foreach ($path in $OpenApiDoc.paths.PSObject.Properties) {
        $pathName = $path.Name
        $pathItem = $path.Value
        
        foreach ($method in $pathItem.PSObject.Properties) {
            $methodName = $method.Name.ToUpper()
            $operation = $method.Value
            
            if ($methodName -in @('GET', 'POST', 'PUT', 'DELETE', 'PATCH')) {
                $item = @{
                    cat_name = if ($operation.tags) { $operation.tags[0] } else { "默认分类" }
                    page_title = "$methodName $pathName"
                    page_content = @"
# $($operation.summary)

**接口地址**: ``$methodName $pathName``

**接口描述**: $($operation.description)

## 请求参数

$(if ($operation.parameters) {
    $operation.parameters | ForEach-Object {
        "- **$($_.name)** ($($_.in)): $($_.description) - $($_.schema.type)"
    }
} else {
    "无"
})

## 请求体

$(if ($operation.requestBody) {
    $schema = $operation.requestBody.content.'application/json'.schema
    "``````json`n$(ConvertTo-Json $schema -Depth 10)`n```````"
} else {
    "无"
})

## 响应示例

$(if ($operation.responses.'200') {
    $response = $operation.responses.'200'
    "**成功响应 (200)**:`n`n$($response.description)`n`n``````json`n$(ConvertTo-Json $response.content.'application/json'.example -Depth 10)`n```````"
} else {
    "无"
})

## 错误码

| 状态码 | 说明 |
|--------|------|
$(foreach ($code in $operation.responses.PSObject.Properties.Name) {
    if ($code -ne '200' -and $code -ne '201' -and $code -ne '204') {
        "| $code | $($operation.responses.$code.description) |"
    }
})

"@
                }
                $showdocItems += $item
            }
        }
    }
    
    return $showdocItems
}

# 同步到 ShowDoc
function Sync-ToShowDoc {
    param(
        [string]$ApiUrl,
        [string]$ApiKey,
        [string]$ApiToken,
        [array]$Items
    )
    
    $successCount = 0
    $failCount = 0
    
    foreach ($item in $Items) {
        try {
            $body = @{
                api_key = $ApiKey
                api_token = $ApiToken
                cat_name = $item.cat_name
                page_title = $item.page_title
                page_content = $item.page_content
            }
            
            Write-Host "正在同步: $($item.page_title)..." -NoNewline
            
            $response = Invoke-RestMethod -Uri $ApiUrl -Method Post -Body $body -ContentType "application/x-www-form-urlencoded"
            
            if ($response.error_code -eq 0) {
                Write-Host " [成功]" -ForegroundColor Green
                $successCount++
            } else {
                Write-Host " [失败] $($response.error_message)" -ForegroundColor Red
                $failCount++
            }
        }
        catch {
            Write-Host " [异常] $($_.Exception.Message)" -ForegroundColor Red
            $failCount++
        }
        
        Start-Sleep -Milliseconds 500  # 避免请求过快
    }
    
    Write-Host "`n同步完成: 成功 $successCount 个, 失败 $failCount 个" -ForegroundColor Cyan
}

# 主流程
try {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Gift Ledger API 文档同步到 ShowDoc" -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
    
    # 加载环境变量
    Write-Host "[1/4] 加载配置..." -ForegroundColor Yellow
    $env = Load-EnvFile
    
    $apiUrl = $env['SHOWDOC_API_URL']
    $apiKey = $env['SHOWDOC_API_KEY']
    $apiToken = $env['SHOWDOC_API_TOKEN']
    
    if (-not $apiUrl -or -not $apiKey -or -not $apiToken) {
        Write-Error "ShowDoc 配置不完整，请检查 .env 文件"
        exit 1
    }
    
    # 读取 OpenAPI 文档
    Write-Host "[2/4] 读取 OpenAPI 文档..." -ForegroundColor Yellow
    $openApiPath = "adapters\http\src\main\resources\static\openapi.json"
    $openApiDoc = Read-OpenApiDoc -Path $openApiPath
    
    # 转换格式
    Write-Host "[3/4] 转换文档格式..." -ForegroundColor Yellow
    $showdocItems = Convert-ToShowDocFormat -OpenApiDoc $openApiDoc
    Write-Host "共 $($showdocItems.Count) 个接口待同步`n" -ForegroundColor Cyan
    
    # 同步到 ShowDoc
    Write-Host "[4/4] 同步到 ShowDoc..." -ForegroundColor Yellow
    Sync-ToShowDoc -ApiUrl $apiUrl -ApiKey $apiKey -ApiToken $apiToken -Items $showdocItems
    
    Write-Host "`n✓ 同步完成！" -ForegroundColor Green
}
catch {
    Write-Host "`n✗ 同步失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
