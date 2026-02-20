# Configurando variáveis de ambiente do Finly Backend
Write-Host "Configurando variaveis de ambiente do Finly Backend..." -ForegroundColor Green

$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/finly"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "9519"
$env:JWT_SECRET = "ZmlubHlfc3VwZXJfc2VjcmV0X2tleV9jaGFuZ2VfaW5fcHJvZHVjdGlvbl8xMjM0NTY3ODk="
$env:JWT_EXPIRATION = "86400000"

Write-Host "Iniciando Finly Backend..." -ForegroundColor Green
mvn spring-boot:run
