$ErrorActionPreference = 'Stop'
$login = Invoke-RestMethod -Uri 'http://localhost:8080/administration/login' -Method Post -Body (ConvertTo-Json @{email='dev_schemusic@schemusic.com'; senha='dev123'}) -ContentType 'application/json'
$token = $login.token
if (-not $token) { $token = $login.token_jwt }
Write-Host "Token: $token"
$headers = @{ Authorization = "Bearer $token" }
$ids = @(33,34,35,36)
Write-Host "Deleting permission ids: $ids"
Invoke-RestMethod -Uri 'http://localhost:8080/administration/access-control/catalog/screens/delete' -Headers $headers -Method Post -Body (ConvertTo-Json @{ ids = $ids }) -ContentType 'application/json' | ConvertTo-Json -Depth 5 | Write-Output
Write-Host "Fetching updated catalog..."
Invoke-RestMethod -Uri 'http://localhost:8080/administration/access-control/catalog' -Headers $headers -Method Get | ConvertTo-Json -Depth 5 | Write-Output
