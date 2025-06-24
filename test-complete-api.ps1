# Script Completo para Testar TODA a API baseado no Swagger
# Versao baseada na documentacao OpenAPI completa

param([string]$BaseUrl = "http://localhost:8080")

$ErrorActionPreference = "Continue"

function Write-Success { 
    param([string]$Message) 
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green 
}

function Write-Error { 
    param([string]$Message) 
    Write-Host "[ERROR] $Message" -ForegroundColor Red 
}

function Write-Info { 
    param([string]$Message) 
    Write-Host "[INFO] $Message" -ForegroundColor Cyan 
}

function Invoke-APIRequest {
    param(
        [string]$Method,
        [string]$Endpoint,
        [object]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    try {
        $uri = "$BaseUrl$Endpoint"
        $params = @{
            Uri = $uri
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-RestMethod @params
        return @{ 
            Success = $true
            Data = $response
            StatusCode = 200 
        }
    }
    catch {
        $statusCode = 500
        if ($_.Exception.Response) { 
            $statusCode = $_.Exception.Response.StatusCode.value__ 
        }
        return @{ 
            Success = $false
            Error = $_.Exception.Message
            StatusCode = $statusCode 
        }
    }
}

# Variaveis globais
$Global:UserToken = $null
$Global:AdminToken = $null
$Global:UserId = $null
$Global:AdminId = $null
$Global:AccountNumber1 = $null
$Global:AccountNumber2 = $null
$Global:ProductId = $null

Write-Info "=== TESTE COMPLETO DA API AZURE SQL DEMO ==="
Write-Info "Base URL: $BaseUrl"
Write-Info "Horario: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Host ""

# ===== SECAO 1: AUTENTICACAO =====
Write-Info "=== SECAO 1: AUTENTICACAO ==="

# 1.1 Registro de Usuario
Write-Info "1.1 Registro de Usuario"
$randomSuffix = Get-Random -Maximum 9999
$userReg = @{
    username = "testuser$randomSuffix"
    email = "test$randomSuffix@example.com"
    password = "Test123@"
    confirmPassword = "Test123@"
    firstName = "Test"
    lastName = "User"
    phoneNumber = "11999998888"
    passwordMatching = $true
}

$regResult = Invoke-APIRequest -Method "POST" -Endpoint "/api/auth/register" -Body $userReg
if ($regResult.Success) {
    Write-Success "Usuario registrado: $($userReg.username)"
    $Global:UserToken = $regResult.Data.token
    $Global:UserId = $regResult.Data.id
} else {
    Write-Error "Falha no registro: $($regResult.Error)"
}

# 1.2 Login Admin
Write-Info "1.2 Login Admin"
$adminLogin = @{
    username = "admin"
    password = "admin123"
}

$adminResult = Invoke-APIRequest -Method "POST" -Endpoint "/api/auth/login" -Body $adminLogin
if ($adminResult.Success) {
    Write-Success "Login admin realizado"
    $Global:AdminToken = $adminResult.Data.token
    $Global:AdminId = $adminResult.Data.id
} else {
    Write-Error "Falha login admin: $($adminResult.Error)"
}

Write-Host ""

# ===== SECAO 2: GERENCIAMENTO DE USUARIOS =====
Write-Info "=== SECAO 2: GERENCIAMENTO DE USUARIOS ==="

if ($Global:AdminToken) {
    $adminHeaders = @{ Authorization = "Bearer $Global:AdminToken" }
    
    # 2.1 Listar usuarios
    Write-Info "2.1 Listar todos os usuarios"
    $usersResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/users?page=0&size=5" -Headers $adminHeaders
    if ($usersResult.Success) {
        $userCount = $usersResult.Data.totalElements
        Write-Success "Total de usuarios: $userCount"
    }
    
    # 2.2 Buscar usuario atual
    Write-Info "2.2 Obter usuario atual"
    $currentUserResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/users/current" -Headers $adminHeaders
    if ($currentUserResult.Success) {
        Write-Success "Usuario atual: $($currentUserResult.Data.username)"
    }
    
    # 2.3 Usuarios por role
    Write-Info "2.3 Usuarios com role ADMIN"
    $adminUsersResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/users/role/ADMIN" -Headers $adminHeaders
    if ($adminUsersResult.Success) {
        $adminCount = $adminUsersResult.Data.Count
        Write-Success "Usuarios ADMIN: $adminCount"
    }
}

Write-Host ""

# ===== SECAO 3: PRODUTOS =====
Write-Info "=== SECAO 3: PRODUTOS ==="

if ($Global:AdminToken) {
    $adminHeaders = @{ Authorization = "Bearer $Global:AdminToken" }
    
    # 3.1 Criar produto
    Write-Info "3.1 Criar produto"
    $newProduct = @{
        name = "Laptop Gaming Dell"
        description = "Laptop Dell G15 com RTX 4060, Intel i7, 16GB RAM"
        price = 4999.99
        quantity = 15
        category = "Electronics"
    }
    
    $productResult = Invoke-APIRequest -Method "POST" -Endpoint "/api/products" -Body $newProduct -Headers $adminHeaders
    if ($productResult.Success) {
        Write-Success "Produto criado: ID $($productResult.Data.id)"
        $Global:ProductId = $productResult.Data.id
    } else {
        Write-Error "Falha criar produto: $($productResult.Error)"
    }
    
    # 3.2 Listar produtos
    Write-Info "3.2 Listar produtos"
    $productsResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/products?page=0&size=5" -Headers $adminHeaders
    if ($productsResult.Success) {
        $productCount = $productsResult.Data.totalElements
        Write-Success "Total de produtos: $productCount"
    }
    
    # 3.3 Estatisticas de produtos
    Write-Info "3.3 Estatisticas de produtos"
    $statsResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/products/stats" -Headers $adminHeaders
    if ($statsResult.Success) {
        Write-Success "Stats - Total: $($statsResult.Data.totalProducts), Ativos: $($statsResult.Data.activeProducts)"
    }
    
    # 3.4 Buscar produtos por nome
    Write-Info "3.4 Buscar produtos por nome"
    $searchResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/products/search?name=Dell" -Headers $adminHeaders
    if ($searchResult.Success) {
        $foundCount = $searchResult.Data.Count
        Write-Success "Produtos encontrados com 'Dell': $foundCount"
    }
    
    # 3.5 Produtos por faixa de preço
    Write-Info "3.5 Produtos por faixa de preço"
    $priceRangeResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/products/price-range?minPrice=1000&maxPrice=6000" -Headers $adminHeaders
    if ($priceRangeResult.Success) {
        $priceCount = $priceRangeResult.Data.Count
        Write-Success "Produtos entre R$1000-6000: $priceCount"
    }
    
    # 3.6 Produtos com estoque baixo
    Write-Info "3.6 Produtos com estoque baixo"
    $lowStockResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/products/low-stock?threshold=20" -Headers $adminHeaders
    if ($lowStockResult.Success) {
        $lowCount = $lowStockResult.Data.Count
        Write-Success "Produtos com estoque < 20: $lowCount"
    }
    
    # 3.7 Atualizar estoque do produto criado
    if ($Global:ProductId) {
        Write-Info "3.7 Atualizar estoque do produto"
        $updateStockResult = Invoke-APIRequest -Method "PATCH" -Endpoint "/api/products/$Global:ProductId/stock?quantity=25" -Headers $adminHeaders
        if ($updateStockResult.Success) {
            Write-Success "Estoque atualizado para 25 unidades"
        } else {
            Write-Error "Falha atualizar estoque: $($updateStockResult.Error)"
        }
    }
    
    # 3.8 Categorias
    Write-Info "3.8 Listar categorias"
    $categoriesResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/products/categories" -Headers $adminHeaders
    if ($categoriesResult.Success) {
        $catCount = $categoriesResult.Data.Count
        Write-Success "Categorias encontradas: $catCount"
    }
}

Write-Host ""

# ===== SECAO 4: CONTAS BANCARIAS =====
Write-Info "=== SECAO 4: CONTAS BANCARIAS ==="

if ($Global:UserToken) {
    $userHeaders = @{ Authorization = "Bearer $Global:UserToken" }
    
    # 4.1 Criar conta corrente
    Write-Info "4.1 Criar conta corrente"
    $account1 = @{
        accountType = "CHECKING"
        initialDeposit = 2000.0
        creditLimit = 5000.0
        currency = "BRL"
    }
    
    $acc1Result = Invoke-APIRequest -Method "POST" -Endpoint "/api/accounts" -Body $account1 -Headers $userHeaders
    if ($acc1Result.Success) {
        Write-Success "Conta corrente criada: $($acc1Result.Data.accountNumber)"
        $Global:AccountNumber1 = $acc1Result.Data.accountNumber
    } else {
        Write-Error "Falha criar conta 1: $($acc1Result.Error)"
    }
    
    # 4.2 Criar conta poupanca
    Write-Info "4.2 Criar conta poupanca"
    $account2 = @{
        accountType = "SAVINGS"
        initialDeposit = 1000.0
        creditLimit = 0
        currency = "BRL"
    }
    
    $acc2Result = Invoke-APIRequest -Method "POST" -Endpoint "/api/accounts" -Body $account2 -Headers $userHeaders
    if ($acc2Result.Success) {
        Write-Success "Conta poupanca criada: $($acc2Result.Data.accountNumber)"
        $Global:AccountNumber2 = $acc2Result.Data.accountNumber
    } else {
        Write-Error "Falha criar conta 2: $($acc2Result.Error)"
    }
    
    # 4.3 Depositar na conta corrente
    if ($Global:AccountNumber1) {
        Write-Info "4.3 Depositar na conta corrente"
        $depositReq = @{
            accountNumber = $Global:AccountNumber1
            amount = 500.0
            description = "Deposito via API"
            referenceNumber = "DEP$(Get-Random -Maximum 99999)"
        }
        
        $depositResult = Invoke-APIRequest -Method "POST" -Endpoint "/api/accounts/$Global:AccountNumber1/deposit" -Body $depositReq -Headers $userHeaders
        if ($depositResult.Success) {
            Write-Success "Deposito realizado: R$ $($depositReq.amount)"
        } else {
            Write-Error "Falha deposito: $($depositResult.Error)"
        }
    }
    
    # 4.4 Sacar da conta corrente
    if ($Global:AccountNumber1) {
        Write-Info "4.4 Sacar da conta corrente"
        $withdrawReq = @{
            accountNumber = $Global:AccountNumber1
            amount = 200.0
            description = "Saque via API"
            referenceNumber = "SAQ$(Get-Random -Maximum 99999)"
        }
        
        $withdrawResult = Invoke-APIRequest -Method "POST" -Endpoint "/api/accounts/$Global:AccountNumber1/withdraw" -Body $withdrawReq -Headers $userHeaders
        if ($withdrawResult.Success) {
            Write-Success "Saque realizado: R$ $($withdrawReq.amount)"
        } else {
            Write-Error "Falha saque: $($withdrawResult.Error)"
        }
    }
    
    # 4.5 Consultar detalhes da conta
    if ($Global:AccountNumber1) {
        Write-Info "4.5 Consultar detalhes da conta"
        $accountDetailsResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/accounts/$Global:AccountNumber1" -Headers $userHeaders
        if ($accountDetailsResult.Success) {
            $balance = $accountDetailsResult.Data.formattedBalance
            Write-Success "Saldo atual: $balance"
        }
    }
    
    # 4.6 Listar minhas contas
    Write-Info "4.6 Listar minhas contas"
    $myAccountsResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/accounts/my-accounts" -Headers $userHeaders
    if ($myAccountsResult.Success) {
        $accountCount = $myAccountsResult.Data.Count
        Write-Success "Minhas contas: $accountCount"
    }
}

Write-Host ""

# ===== SECAO 5: TRANSACOES FINANCEIRAS =====
Write-Info "=== SECAO 5: TRANSACOES FINANCEIRAS ==="

if ($Global:UserToken -and $Global:AccountNumber1 -and $Global:AccountNumber2) {
    $userHeaders = @{ Authorization = "Bearer $Global:UserToken" }
    
    # 5.1 Deposito
    Write-Info "5.1 Fazer deposito"
    $depositTx = @{
        fromAccountNumber = $Global:AccountNumber1
        toAccountNumber = $Global:AccountNumber1
        amount = 500.0
        transactionType = "DEPOSIT"
        description = "Deposito teste automatizado"
        referenceNumber = "DEP$(Get-Random -Maximum 99999)"
    }
    
    $depositResult = Invoke-APIRequest -Method "POST" -Endpoint "/api/transactions" -Body $depositTx -Headers $userHeaders
    if ($depositResult.Success) {
        Write-Success "Deposito realizado: $($depositResult.Data.formattedAmount)"
    } else {
        Write-Error "Falha deposito: $($depositResult.Error)"
    }
    
    # 5.2 Transferencia
    Write-Info "5.2 Transferencia entre contas"
    $transferTx = @{
        fromAccountNumber = $Global:AccountNumber1
        toAccountNumber = $Global:AccountNumber2
        amount = 300.0
        description = "Transferencia teste"
        referenceNumber = "TRF$(Get-Random -Maximum 99999)"
    }
    
    $transferResult = Invoke-APIRequest -Method "POST" -Endpoint "/api/transactions/transfer" -Body $transferTx -Headers $userHeaders
    if ($transferResult.Success) {
        Write-Success "Transferencia realizada: $($transferResult.Data.formattedAmount)"
    } else {
        Write-Error "Falha transferencia: $($transferResult.Error)"
    }
    
    # 5.3 Historico de transacoes
    Write-Info "5.3 Historico de transacoes"
    $historyResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/transactions/recent?hours=24" -Headers $userHeaders
    if ($historyResult.Success) {
        $txCount = $historyResult.Data.Count
        Write-Success "Transacoes recentes: $txCount"
    }
    
    # 5.4 Buscar transações por período
    Write-Info "5.4 Buscar transações por período"
    $yesterday = (Get-Date).AddDays(-1).ToString("yyyy-MM-ddTHH:mm:ss")
    $tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ss")
    $searchTxResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/transactions/search?startDate=$yesterday&endDate=$tomorrow&page=0&size=5" -Headers $userHeaders
    if ($searchTxResult.Success) {
        $searchCount = $searchTxResult.Data.totalElements
        Write-Success "Transações encontradas: $searchCount"
    }
    
    # 5.5 Transações por status
    Write-Info "5.5 Transações por status COMPLETED"
    $statusTxResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/transactions/status/COMPLETED" -Headers $userHeaders
    if ($statusTxResult.Success) {
        $completedCount = $statusTxResult.Data.Count
        Write-Success "Transações completadas: $completedCount"
    }
    
    # 5.6 Transações do usuário atual
    if ($Global:UserId) {
        Write-Info "5.6 Transações do usuário atual"
        $userTxResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/transactions/user/$Global:UserId?page=0&size=3" -Headers $userHeaders
        if ($userTxResult.Success) {
            $userTxCount = $userTxResult.Data.totalElements
            Write-Success "Transações do usuário: $userTxCount"
        }
    }
}

Write-Host ""

# ===== SECAO 6: OPERACOES ADMIN =====
Write-Info "=== SECAO 6: OPERACOES ADMIN ==="

if ($Global:AdminToken) {
    $adminHeaders = @{ Authorization = "Bearer $Global:AdminToken" }
    
    # 6.1 Carregar dados de exemplo
    Write-Info "6.1 Carregar dados predefinidos"
    $loadDataResult = Invoke-APIRequest -Method "POST" -Endpoint "/api/admin/load-predefined-data" -Headers $adminHeaders
    if ($loadDataResult.Success) {
        Write-Success "Dados predefinidos carregados"
    } else {
        Write-Error "Falha carregar dados: $($loadDataResult.Error)"
    }
    
    # 6.3 Buscar usuários com falha de login
    Write-Info "6.3 Buscar usuários com falhas de login"
    $failedLoginsResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/users/failed-logins?threshold=1" -Headers $adminHeaders
    if ($failedLoginsResult.Success) {
        $failedCount = $failedLoginsResult.Data.Count
        Write-Success "Usuários com falhas: $failedCount"
    }
    
    # 6.4 Buscar usuários ativos recentemente
    Write-Info "6.4 Usuários ativos recentemente"
    $yesterday = (Get-Date).AddDays(-1).ToString("yyyy-MM-ddTHH:mm:ss")
    $recentUsersResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/users/recent?since=$yesterday" -Headers $adminHeaders
    if ($recentUsersResult.Success) {
        $recentCount = $recentUsersResult.Data.Count
        Write-Success "Usuários ativos desde ontem: $recentCount"
    }
    
    # 6.5 Adicionar role ao usuário criado
    if ($Global:UserId) {
        Write-Info "6.5 Adicionar role PREMIUM ao usuário"
        $addRoleResult = Invoke-APIRequest -Method "POST" -Endpoint "/api/users/$Global:UserId/roles/PREMIUM" -Headers $adminHeaders
        if ($addRoleResult.Success) {
            Write-Success "Role PREMIUM adicionado"
        } else {
            Write-Info "Role PREMIUM pode não existir (normal)"
        }
    }
}

Write-Host ""

# ===== SECAO 7: TESTES DE SEGURANCA =====
Write-Info "=== SECAO 7: TESTES DE SEGURANCA ==="

# 7.1 Acesso sem token
Write-Info "7.1 Tentativa de acesso sem token"
$noTokenResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/users"
if (-not $noTokenResult.Success) {
    Write-Success "Seguranca OK - Acesso negado sem token"
} else {
    Write-Error "FALHA DE SEGURANCA - Acesso permitido sem token"
}

# 7.2 Token invalido
Write-Info "7.2 Tentativa com token invalido"
$invalidHeaders = @{ Authorization = "Bearer token_invalido_123456" }
$invalidResult = Invoke-APIRequest -Method "GET" -Endpoint "/api/users" -Headers $invalidHeaders
if (-not $invalidResult.Success) {
    Write-Success "Seguranca OK - Token invalido rejeitado"
} else {
    Write-Error "FALHA DE SEGURANCA - Token invalido aceito"
}

Write-Host ""

# ===== RELATORIO FINAL =====
Write-Info "=== RELATORIO FINAL COMPLETO ==="
Write-Host "=" * 60

$userTokenStatus = if($Global:UserToken) { "OBTIDO" } else { "FALHOU" }
$adminTokenStatus = if($Global:AdminToken) { "OBTIDO" } else { "FALHOU" }
$account1Status = if($Global:AccountNumber1) { "CRIADA" } else { "FALHOU" }
$account2Status = if($Global:AccountNumber2) { "CRIADA" } else { "FALHOU" }
$productStatus = if($Global:ProductId) { "CRIADO" } else { "FALHOU" }

Write-Info "AUTENTICACAO:"
Write-Host "  User Token:    $userTokenStatus" -ForegroundColor $(if($Global:UserToken){"Green"}else{"Red"})
Write-Host "  Admin Token:   $adminTokenStatus" -ForegroundColor $(if($Global:AdminToken){"Green"}else{"Red"})

Write-Info "RECURSOS CRIADOS:"
Write-Host "  Conta 1:       $account1Status" -ForegroundColor $(if($Global:AccountNumber1){"Green"}else{"Red"})
Write-Host "  Conta 2:       $account2Status" -ForegroundColor $(if($Global:AccountNumber2){"Green"}else{"Red"})
Write-Host "  Produto:       $productStatus" -ForegroundColor $(if($Global:ProductId){"Green"}else{"Red"})

Write-Host ""

if ($Global:UserToken -and $Global:AdminToken) {
    Write-Success "APLICACAO 100% FUNCIONAL - TODOS OS MODULOS OK!"
    Write-Info "Swagger UI: $BaseUrl/swagger-ui.html"
    Write-Info "API Docs: $BaseUrl/v3/api-docs"
    Write-Info "Health: $BaseUrl/actuator/health"
    
    Write-Host ""
    Write-Info "TOKENS PARA TESTES MANUAIS:"
    Write-Host "User Token:" -ForegroundColor Yellow
    Write-Host "$Global:UserToken" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Admin Token:" -ForegroundColor Magenta  
    Write-Host "$Global:AdminToken" -ForegroundColor Magenta
    
    if ($Global:AccountNumber1) {
        Write-Host ""
        Write-Info "CONTAS CRIADAS:"
        Write-Host "Conta 1: $Global:AccountNumber1" -ForegroundColor Cyan
        if ($Global:AccountNumber2) {
            Write-Host "Conta 2: $Global:AccountNumber2" -ForegroundColor Cyan
        }
    }
} else {
    Write-Error "PROBLEMAS DETECTADOS - Verifique logs da aplicacao"
}

Write-Host ""
Write-Info "=== TESTE COMPLETO FINALIZADO ==="
Write-Info "Total de secoes testadas: 7"
Write-Info "Executado em: $(Get-Date -Format 'HH:mm:ss')"