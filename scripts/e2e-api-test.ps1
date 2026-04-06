# End-to-end API checks against http://localhost:8080
# Run: pwsh -File scripts/e2e-api-test.ps1   (or powershell -File ...)

$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$passed = 0
$failed = 0

function Wait-ServerReady {
    param([int]$MaxSeconds = 45)
    $deadline = (Get-Date).AddSeconds($MaxSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-WebRequest -Uri "$base/api/auth/login" -Method Post -ContentType "application/json" `
                -Body '{"email":"x","password":"x"}' -UseBasicParsing -TimeoutSec 3 | Out-Null
            return
        } catch {
            $resp = $_.Exception.Response
            if ($null -ne $resp) { return }
            Start-Sleep -Milliseconds 500
        }
    }
    throw "Server not reachable at $base within ${MaxSeconds}s. Start the app (mvn spring-boot:run) and retry."
}

function Invoke-JsonRetry {
    param([scriptblock]$Call, [int]$Attempts = 5)
    $last = $null
    for ($i = 0; $i -lt $Attempts; $i++) {
        try {
            return & $Call
        } catch {
            $last = $_
            if ($_.Exception.Message -match "Unable to connect|timed out|connection") {
                Start-Sleep -Seconds 1
                continue
            }
            throw
        }
    }
    throw $last.Exception
}

function Test-Step {
    param([string]$Name, [scriptblock]$Block)
    try {
        & $Block
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $script:passed++
    } catch {
        Write-Host "[FAIL] $Name - $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Uri,
        $Body = $null,
        [hashtable]$Headers = @{}
    )
    $params = @{
        Uri             = $Uri
        Method          = $Method
        ContentType     = "application/json"
        Headers         = $Headers
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Compress -Depth 5)
    }
    $resp = Invoke-WebRequest @params
    if ([string]::IsNullOrWhiteSpace($resp.Content)) { return $null }
    return $resp.Content | ConvertFrom-Json
}

function Invoke-Status {
    param(
        [string]$Method,
        [string]$Uri,
        $Body = $null,
        [hashtable]$Headers = @{},
        [int]$Expected,
        [int[]]$ExpectedAny = $null
    )
    try {
        $params = @{
            Uri             = $Uri
            Method          = $Method
            ContentType     = "application/json"
            Headers         = $Headers
            UseBasicParsing = $true
        }
        if ($null -ne $Body) {
            $params.Body = ($Body | ConvertTo-Json -Compress -Depth 5)
        }
        $r = Invoke-WebRequest @params
        $code = [int]$r.StatusCode
    } catch {
        $resp = $_.Exception.Response
        if ($resp) { $code = [int]$resp.StatusCode } else { throw }
    }
    if ($null -ne $ExpectedAny) {
        if ($ExpectedAny -notcontains $code) {
            throw "Expected one of $($ExpectedAny -join ','), got $code"
        }
    } elseif ($code -ne $Expected) {
        throw "Expected HTTP $Expected, got $code"
    }
}

Write-Host "`n=== Finance API E2E ===`n" -ForegroundColor Cyan

Write-Host "Waiting for $base ..." -ForegroundColor DarkGray
Wait-ServerReady

$script:admin = $null
$script:analyst = $null
$script:viewer = $null

Test-Step "POST /api/auth/login (admin)" {
    $script:admin = Invoke-JsonRetry {
        Invoke-Json Post "$base/api/auth/login" @{ email = "admin@finance.local"; password = "ChangeMe123!" }
    }
    if (-not $script:admin.token) { throw "No token" }
}

Test-Step "POST /api/auth/login (analyst)" {
    $script:analyst = Invoke-JsonRetry {
        Invoke-Json Post "$base/api/auth/login" @{ email = "analyst@finance.local"; password = "ChangeMe123!" }
    }
}

Test-Step "POST /api/auth/login (viewer)" {
    $script:viewer = Invoke-JsonRetry {
        Invoke-Json Post "$base/api/auth/login" @{ email = "viewer@finance.local"; password = "ChangeMe123!" }
    }
}

if (-not $script:admin.token -or -not $script:analyst.token -or -not $script:viewer.token) {
    Write-Host "`nFATAL: One or more logins did not return a token. Fix failures above and retry.`n" -ForegroundColor Red
    exit 1
}

$hdrAdmin = @{ Authorization = "Bearer $($script:admin.token)" }
$hdrAnalyst = @{ Authorization = "Bearer $($script:analyst.token)" }
$hdrViewer = @{ Authorization = "Bearer $($script:viewer.token)" }

Test-Step "GET /api/users (admin)" {
    $users = Invoke-Json Get "$base/api/users" -Headers $hdrAdmin
    if ($users.Count -lt 1) { throw "Expected users" }
}

Test-Step "GET /api/users (viewer) -> 403" {
    Invoke-Status Get "$base/api/users" -Headers $hdrViewer -Expected 403
}

Test-Step "GET /api/transactions (analyst)" {
    $txUri = "$base/api/transactions?page=0" + '&size=5'
    $page = Invoke-Json Get $txUri -Headers $hdrAnalyst
    if ($null -eq $page.content) { throw "No content" }
}

Test-Step "GET /api/transactions (viewer) -> 403" {
    Invoke-Status Get "$base/api/transactions" -Headers $hdrViewer -Expected 403
}

Test-Step "GET /api/transactions/mine (viewer)" {
    $mine = Invoke-Json Get "$base/api/transactions/mine" -Headers $hdrViewer
    if ($null -eq $mine.content) { throw "No content" }
}

$newTx = $null
Test-Step "POST /api/transactions (viewer creates own)" {
    $script:newTx = Invoke-Json Post "$base/api/transactions" @{
        amount   = 99.99
        type     = "EXPENSE"
        category = "E2E-Test"
        date     = (Get-Date -Format "yyyy-MM-dd")
        notes    = "automated test"
    } -Headers $hdrViewer
    if (-not $newTx.id) { throw "No id" }
}

Test-Step "PUT /api/transactions/{id} (viewer owns)" {
    $updated = Invoke-Json Put "$base/api/transactions/$($newTx.id)" @{
        amount   = 100.01
        type     = "EXPENSE"
        category = "E2E-Test"
        date     = $newTx.date
        notes    = "updated"
    } -Headers $hdrViewer
    if ([decimal]$updated.amount -ne 100.01) { throw "Amount not updated" }
}

Test-Step "PATCH /api/users/{id}/role (admin)" {
    # Idempotent: keep seeded viewer (id 3) as VIEWER
    $r = Invoke-Json Patch "$base/api/users/3/role" @{ role = "VIEWER" } -Headers $hdrAdmin
    if ($r.role -ne "VIEWER") { throw "Role patch failed" }
}

Test-Step "PATCH /api/users/{id}/status (admin)" {
    $r = Invoke-Json Patch "$base/api/users/3/status" @{ active = $true } -Headers $hdrAdmin
    if (-not $r.active) { throw "status" }
}

Test-Step "DELETE /api/transactions/{id} soft-delete (viewer)" {
    Invoke-WebRequest -Uri "$base/api/transactions/$($newTx.id)" -Method Delete -Headers $hdrViewer -UseBasicParsing | Out-Null
}

Test-Step "POST /graphql dashboardSummary (viewer token)" {
    $gq = @{
        query = "query { dashboardSummary { totalIncome totalExpense netBalance } }"
    }
    $r = Invoke-Json Post "$base/graphql" $gq -Headers $hdrViewer
    if ($null -eq $r.data.dashboardSummary) { throw "No data" }
}

Test-Step "POST /graphql (no token) -> 401 or 403" {
    Invoke-Status Post "$base/graphql" -Body @{ query = "query { dashboardSummary { totalIncome } }" } -ExpectedAny @(401, 403)
}

$regSuffix = [Guid]::NewGuid().ToString("N").Substring(0, 8)
$regEmail = "e2e_$regSuffix@test.local"
Test-Step "POST /api/auth/register (new VIEWER)" {
    $reg = Invoke-Json Post "$base/api/auth/register" @{
        email    = $regEmail
        password = "RegisterPass123"
    }
    if ($reg.role -ne "VIEWER") { throw "Should be VIEWER" }
}

Write-Host "`n=== Summary: $passed passed, $failed failed ===`n" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })
exit $(if ($failed -eq 0) { 0 } else { 1 })
