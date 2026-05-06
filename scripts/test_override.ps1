$ErrorActionPreference = 'Stop'
$login = Invoke-RestMethod -Uri 'http://localhost:8080/administration/login' -Method Post -Body (ConvertTo-Json @{email='dev_schemusic@schemusic.com'; senha='dev123'}) -ContentType 'application/json'
$token = $login.token
if (-not $token) { $token = $login.token_jwt }
Write-Host "Token: $token"
$headers = @{ Authorization = "Bearer $token" }
$catalog = Invoke-RestMethod -Uri 'http://localhost:8080/administration/access-control/catalog' -Headers $headers -Method Get
$perm = $null
foreach ($t in $catalog.telas) {
  foreach ($p in $t.permissoes) {
    if ($p.acaoCodigo -eq 'CONSULTAR') { $perm = $p; break }
  }
  if ($perm) { break }
}
Write-Host "perm id: $($perm.idPermissao) tela: $($perm.telaCodigo) acao: $($perm.acaoCodigo)"
$users = Invoke-RestMethod -Uri 'http://localhost:8080/administration/users' -Headers $headers -Method Get
Write-Host "Users count: $($users.Count)"
$target = $users | Where-Object { $_.idUsuario -ne $login.idUsuario } | Select-Object -First 1
if (-not $target) { $target = $users | Select-Object -First 1 }
Write-Host "Target user id: $($target.idUsuario) name: $($target.nome)"
$overrides = @(@{ idPermissao = $perm.idPermissao; permitido = $false })
Write-Host "Applying override deny..."
Invoke-RestMethod -Uri ("http://localhost:8080/administration/access-control/users/{0}" -f $target.idUsuario) -Headers $headers -Method Put -Body (ConvertTo-Json @{ overrides = $overrides }) -ContentType 'application/json' | ConvertTo-Json -Depth 5 | Write-Output
Write-Host "Fetching permissions after override..."
Invoke-RestMethod -Uri ("http://localhost:8080/administration/access-control/users/{0}" -f $target.idUsuario) -Headers $headers -Method Get | ConvertTo-Json -Depth 5 | Write-Output
