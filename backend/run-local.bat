@echo off
echo Configurando variaveis de ambiente do Finly Backend...

set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/finly
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=9519
set JWT_SECRET=ZmlubHlfc3VwZXJfc2VjcmV0X2tleV9jaGFuZ2VfaW5fcHJvZHVjdGlvbl8xMjM0NTY3ODk=
set JWT_EXPIRATION=86400000

echo Iniciando Finly Backend...
mvn spring-boot:run
