# 🚨 FORÇAR LOGS DEBUG - DESCOBRIR ERRO REAL

Write-Host "🚨 PROBLEMA: Logs DEBUG não estão aparecendo!" -ForegroundColor Red
Write-Host "🎯 Vamos forçar os logs de uma forma diferente..." -ForegroundColor Yellow

# 1. Verificar se a configuração foi realmente aplicada
Write-Host "`n📋 1. Verificando se configuração DEBUG foi aplicada:" -ForegroundColor Cyan
Get-Content "src/main/resources/application-docker.yml" | Select-String -Pattern "DEBUG|TRACE" -Context 1

# 2. Tentar método alternativo - logs no nível do container
Write-Host "`n📋 2. Capturando TODOS os logs sem filtro:" -ForegroundColor Cyan
docker-compose logs app --tail=200 > temp_logs.txt
$logs = Get-Content temp_logs.txt
Write-Host "Total de linhas de log: $($logs.Count)"

# 3. Procurar por qualquer erro nos logs
Write-Host "`n📋 3. Procurando por padrões de erro nos logs:" -ForegroundColor Cyan
$errorPatterns = @(
    "Exception", "ERROR", "Failed", "Error", "Caused by", 
    "NullPointerException", "IllegalArgumentException", "RuntimeException",
    "AuthService", "register", "jwt", "JWT", "at com.example"
)

foreach ($pattern in $errorPatterns) {
    $matches = $logs | Select-String -Pattern $pattern
    if ($matches) {
        Write-Host "🔍 Encontrado '$pattern':" -ForegroundColor Yellow
        $matches | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
    }
}

# 4. Método direto: executar comando dentro do container
Write-Host "`n📋 4. Verificando logs diretamente no container:" -ForegroundColor Cyan
docker exec azure-sql-demo-app tail -n 100 /proc/1/fd/1 | Select-String -Pattern "ERROR|Exception"

# 5. Verificar se aplicação está realmente usando application-docker.yml
Write-Host "`n📋 5. Verificando profile ativo:" -ForegroundColor Cyan
docker exec azure-sql-demo-app env | Select-String -Pattern "SPRING_PROFILES_ACTIVE"

# 6. Tentar capturar erro com curl (mais verboso)
Write-Host "`n📋 6. Fazendo requisição com curl para logs mais detalhados:" -ForegroundColor Cyan

$userData = @{
    username = "curltest$(Get-Random -Maximum 9999)"
    email = "curltest$(Get-Random -Maximum 9999)@test.com"
    password = "test12345"
    confirmPassword = "test12345"
    firstName = "Curl"
    lastName = "Test"
    phoneNumber = "11999887766"
    passwordMatching = $true
} | ConvertTo-Json

$curlCommand = "curl -v -X POST http://localhost:8080/api/auth/register -H 'Content-Type: application/json' -d '$($userData.Replace('"', '\"').Replace("`n", " ").Replace("  ", ""))'"

Write-Host "📤 Executando: $curlCommand" -ForegroundColor Gray

# Executar curl e capturar saída
try {
    $curlResult = Invoke-Expression $curlCommand 2>&1
    Write-Host "📤 Resultado do curl:" -ForegroundColor Gray
    $curlResult | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
} catch {
    Write-Host "❌ Erro no curl: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Imediatamente após, verificar logs
Write-Host "`n📋 7. Logs IMEDIATAMENTE após curl:" -ForegroundColor Cyan
Start-Sleep -Seconds 3
docker-compose logs app --tail=50 | ForEach-Object { 
    if ($_ -match "ERROR|Exception|register|AuthService|DEBUG.*register") {
        Write-Host $_ -ForegroundColor Red
    }
}

# 8. Último recurso: verificar se há exception no GlobalExceptionHandler
Write-Host "`n📋 8. Verificando se GlobalExceptionHandler está mascarando erro:" -ForegroundColor Cyan
$globalHandler = Get-Content "src/main/java/com/example/azure_sql_demo/exception/GlobalExceptionHandler.java" | Select-String -Pattern "Exception.class" -Context 3
Write-Host "GlobalExceptionHandler genérico:" -ForegroundColor Yellow
$globalHandler | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }

# 9. SOLUÇÃO TEMPORÁRIA: Desabilitar GlobalExceptionHandler
Write-Host "`n💡 SOLUÇÃO TEMPORÁRIA: Vamos desabilitar o GlobalExceptionHandler:" -ForegroundColor Yellow
Write-Host "Isso vai mostrar o erro real sem mascaramento!" -ForegroundColor Yellow

$tempFix = @"

# TEMPORÁRIO: Comentar @RestControllerAdvice para ver erro real
# @RestControllerAdvice
# @Slf4j
# public class GlobalExceptionHandler {
"@

Write-Host "`n🎯 PRÓXIMOS PASSOS:" -ForegroundColor Green
Write-Host "1. Se não encontrarmos erro, vamos desabilitar GlobalExceptionHandler temporariamente" -ForegroundColor White
Write-Host "2. Isso vai expor o stack trace real do erro" -ForegroundColor White
Write-Host "3. Depois reabilitamos o handler" -ForegroundColor White

# Limpar arquivo temporário
Remove-Item temp_logs.txt -ErrorAction SilentlyContinue

Write-Host "`n🔍 Análise concluída!" -ForegroundColor Green