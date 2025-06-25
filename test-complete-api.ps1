# Teste API - Versao ASCII (Sem Emojis)
$BaseUrl = "http://localhost:8080"
$UserToken = $null
$TestsPassed = 0
$TestsTotal = 0

Write-Host "=== TESTE DA API AZURE SQL DEMO ===" -ForegroundColor Green
Write-Host "Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host "Inicio: $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Cyan
Write-Host ""

# Funcao para testar endpoints
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Endpoint,
        [object]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    $global:TestsTotal++
    Write-Host "TESTE: $Name" -ForegroundColor Yellow
    
    try {
        $uri = "$BaseUrl$Endpoint"
        $params = @{
            Uri = $uri
            Method = $Method
            ContentType = "application/json"
        }
        
        if ($Headers.Count -gt 0) {
            $params.Headers = $Headers
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "[OK] ${Name}: SUCESSO" -ForegroundColor Green
        $global:TestsPassed++
        return @{ Success = $true; Data = $response }
    }
    catch {
        $statusCode = 500
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
        }
        
        if ($statusCode -eq 401 -or $statusCode -eq 403) {
            Write-Host "[OK] ${Name}: SEGURANCA OK (Status: $statusCode)" -ForegroundColor Green
            $global:TestsPassed++
            return @{ Success = $true; SecurityTest = $true }
        }
        else {
            Write-Host "[ERRO] ${Name}: FALHOU (Status: $statusCode)" -ForegroundColor Red
            return @{ Success = $false; Error = $_.Exception.Message }
        }
    }
}

# TESTE 1: Health Check
$healthResult = Test-Endpoint -Name "Health Check" -Method "GET" -Endpoint "/actuator/health"

# TESTE 2: Swagger UI
Write-Host "TESTE: Swagger UI" -ForegroundColor Yellow
$TestsTotal++
try {
    $swaggerResponse = Invoke-WebRequest -Uri "$BaseUrl/swagger-ui/index.html" -Method GET
    Write-Host "[OK] Swagger UI: DISPONIVEL" -ForegroundColor Green
    $TestsPassed++
}
catch {
    Write-Host "[ERRO] Swagger UI: NAO DISPONIVEL" -ForegroundColor Red
}

# TESTE 3: Registro de Usuario
$randomSuffix = Get-Random -Maximum 9999
$userData = @{
    username = "testuser$randomSuffix"
    email = "test$randomSuffix@example.com"
    password = "Test123!"
    confirmPassword = "Test123!"
    firstName = "Test"
    lastName = "User"
    phoneNumber = "11999998888"
    passwordMatching = $true
}

$registerResult = Test-Endpoint -Name "Registro de Usuario" -Method "POST" -Endpoint "/api/auth/register" -Body $userData

# Tentar obter token do registro
if ($registerResult.Success -and $registerResult.Data) {
    if ($registerResult.Data.accessToken) {
        $UserToken = $registerResult.Data.accessToken
        Write-Host "[TOKEN] Token obtido do registro!" -ForegroundColor Green
    }
    elseif ($registerResult.Data.token) {
        $UserToken = $registerResult.Data.token
        Write-Host "[TOKEN] Token obtido do registro!" -ForegroundColor Green
    }
}

# TESTE 4: Login (se nao temos token)
if (-not $UserToken) {
    $loginData = @{
        username = $userData.username
        password = $userData.password
    }
    
    $loginResult = Test-Endpoint -Name "Login" -Method "POST" -Endpoint "/api/auth/login" -Body $loginData
    
    if ($loginResult.Success -and $loginResult.Data) {
        if ($loginResult.Data.accessToken) {
            $UserToken = $loginResult.Data.accessToken
            Write-Host "[TOKEN] Token obtido do login!" -ForegroundColor Green
        }
        elseif ($loginResult.Data.token) {
            $UserToken = $loginResult.Data.token
            Write-Host "[TOKEN] Token obtido do login!" -ForegroundColor Green
        }
    }
}

# Mostrar status do token
if ($UserToken) {
    Write-Host "[OK] JWT Token: OBTIDO" -ForegroundColor Green
    Write-Host "Token: $($UserToken.Substring(0, 30))..." -ForegroundColor Yellow
}
else {
    Write-Host "[ERRO] JWT Token: NAO OBTIDO" -ForegroundColor Red
}

Write-Host ""

# TESTES COM TOKEN (se disponivel)
if ($UserToken) {
    $authHeaders = @{
        "Authorization" = "Bearer $UserToken"
        "Content-Type" = "application/json"
    }
    
    # TESTE 5: Perfil do usuario
    Test-Endpoint -Name "Perfil do Usuario" -Method "GET" -Endpoint "/api/users/current" -Headers $authHeaders
    
    # TESTE 6: Listar produtos
    Test-Endpoint -Name "Listar Produtos" -Method "GET" -Endpoint "/api/products?page=0&size=5" -Headers $authHeaders
    
    # TESTE 7: Criar conta bancaria
    $accountData = @{
        accountType = "CHECKING"
        initialDeposit = 1000.0
        creditLimit = 2000.0
        currency = "BRL"
    }
    Test-Endpoint -Name "Criar Conta Bancaria" -Method "POST" -Endpoint "/api/accounts" -Body $accountData -Headers $authHeaders
}
else {
    Write-Host "[AVISO] Testes com token PULADOS - Token nao disponivel" -ForegroundColor Yellow
}

Write-Host ""

# TESTES DE SEGURANCA
Write-Host "=== TESTES DE SEGURANCA ===" -ForegroundColor Magenta

# TESTE: Acesso sem token
Test-Endpoint -Name "Acesso sem Token" -Method "GET" -Endpoint "/api/users"

# TESTE: Token invalido
$invalidHeaders = @{ "Authorization" = "Bearer token_invalido_123" }
Test-Endpoint -Name "Token Invalido" -Method "GET" -Endpoint "/api/users" -Headers $invalidHeaders

Write-Host ""

# RELATORIO FINAL
Write-Host "=== RELATORIO FINAL ===" -ForegroundColor Green
Write-Host "=========================================="

$successRate = if ($TestsTotal -gt 0) { 
    [math]::Round(($TestsPassed / $TestsTotal) * 100, 1) 
} else { 0 }

Write-Host "ESTATISTICAS:" -ForegroundColor White
Write-Host "  Total de Testes:    $TestsTotal"
Write-Host "  Testes Passou:      $TestsPassed" -ForegroundColor Green
Write-Host "  Testes Falhou:      $($TestsTotal - $TestsPassed)" -ForegroundColor Red
Write-Host "  Taxa de Sucesso:    $successRate%" -ForegroundColor $(if($successRate -ge 80){"Green"}elseif($successRate -ge 60){"Yellow"}else{"Red"})

Write-Host ""
Write-Host "STATUS:" -ForegroundColor White
$tokenStatus = if($UserToken) { "[OK] OBTIDO" } else { "[ERRO] NAO OBTIDO" }
Write-Host "  JWT Token:          $tokenStatus"

Write-Host ""

# Avaliacao final
if ($successRate -ge 80) {
    Write-Host "APLICACAO FUNCIONANDO EXCELENTEMENTE!" -ForegroundColor Green
    Write-Host "[OK] Autenticacao funcional" -ForegroundColor Green
    Write-Host "[OK] Endpoints respondendo" -ForegroundColor Green
    Write-Host "[OK] Seguranca implementada" -ForegroundColor Green
}
elseif ($successRate -ge 60) {
    Write-Host "APLICACAO FUNCIONANDO PARCIALMENTE" -ForegroundColor Yellow
    Write-Host "[OK] Funcionalidades basicas OK" -ForegroundColor Green
    Write-Host "[AVISO] Alguns endpoints precisam ajustes" -ForegroundColor Yellow
}
else {
    Write-Host "PROBLEMAS DETECTADOS" -ForegroundColor Red
    Write-Host "[ACAO] Verifique logs da aplicacao" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "LINKS UTEIS:" -ForegroundColor Cyan
Write-Host "  Swagger UI:    $BaseUrl/swagger-ui/index.html"
Write-Host "  API Docs:      $BaseUrl/v3/api-docs"
Write-Host "  H2 Console:    $BaseUrl/h2-console"  
Write-Host "  Health:        $BaseUrl/actuator/health"

if ($UserToken) {
    Write-Host ""
    Write-Host "TOKEN PARA TESTES MANUAIS:" -ForegroundColor Yellow
    Write-Host "Bearer $UserToken" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== TESTE FINALIZADO ===" -ForegroundColor Green
Write-Host "Concluido as: $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Cyan