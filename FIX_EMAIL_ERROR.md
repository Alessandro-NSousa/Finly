# CONFIGURAÇÃO RÁPIDA - FIX ERRO 500 NO CADASTRO

## Problema
O erro 500 ao cadastrar ocorre porque o servidor de email não está configurado.

## Solução Rápida

### Para ambiente de desenvolvimento (SEM envio real de email)

#### Opção 1: Usar Servidor SMTP de Teste (Mailtrap ou similar)

1. Acesse https://mailtrap.io (ou outro serviço de teste)
2. Crie uma conta gratuita
3. Copie as credenciais SMTP
4. Configure as variáveis abaixo

#### Opção 2: Usar Gmail (Requer configuração adicional)

Para usar Gmail, você precisa de uma **Senha de App** (não sua senha normal):

1. Acesse: https://myaccount.google.com/security
2. Ative "Verificação em duas etapas" se ainda não ativou
3. Acesse "Senhas de app" em Segurança
4. Gere uma nova senha de app para "Email"
5. Copie a senha de 16 caracteres (sem espaços)

### Configurar Variáveis de Ambiente

Escolha UMA das opções abaixo:

#### Opção A: Usando arquivo .env (Recomendado)

Crie ou edite o arquivo `backend/.env`:

```env
# Database (já deve estar configurado)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/finly
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=sua_senha_postgres

# JWT (já deve estar configurado)
JWT_SECRET=ZmlubHlfc3VwZXJfc2VjcmV0X2tleV9jaGFuZ2VfaW5fcHJvZHVjdGlvbl8xMjM0NTY3ODk=
JWT_EXPIRATION=86400000

# Email (CONFIGURE AQUI)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=sua_senha_de_app_de_16_caracteres

# Application URL
APP_BASE_URL=http://localhost:4200
```

**Para Mailtrap:**
```env
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=seu_username_mailtrap
MAIL_PASSWORD=sua_senha_mailtrap
```

#### Opção B: Usando application-local.properties

Crie ou edite `backend/src/main/resources/application-local.properties`:

```properties
# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seu_email@gmail.com
spring.mail.password=sua_senha_de_app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# App URL
app.base.url=http://localhost:4200
```

E execute o backend com o profile local:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

#### Opção C: Variáveis de Ambiente do Sistema (Windows)

No PowerShell (como administrador):
```powershell
[System.Environment]::SetEnvironmentVariable('MAIL_USERNAME', 'seu_email@gmail.com', 'User')
[System.Environment]::SetEnvironmentVariable('MAIL_PASSWORD', 'sua_senha_app', 'User')
```

Depois reinicie o terminal e o backend.

### Testar Configuração

1. Reinicie o backend
2. Tente cadastrar novamente
3. Se configurado corretamente, você verá nos logs:

```
INFO  c.f.service.AuthService - Iniciando registro para email: teste@example.com
INFO  c.f.service.AuthService - Usuário salvo no banco com ID: 1
INFO  c.f.service.AuthService - Token de verificação criado para usuário ID: 1
INFO  c.f.service.EmailService - Enviando email para: teste@example.com
INFO  c.f.service.EmailService - Email enviado com sucesso para: teste@example.com
```

### Erros Comuns e Soluções

#### "Servidor de email não configurado"
- As variáveis MAIL_USERNAME e MAIL_PASSWORD não foram configuradas
- Solução: Configure as variáveis conforme acima

#### "Authentication failed" com Gmail
- Você está usando sua senha normal do Gmail (não funciona)
- Solução: Use uma **Senha de App** conforme instruções acima

#### "Connection timeout"
- Firewall bloqueando porta 587
- Solução: Verifique firewall ou use porta 465 com SSL

#### Porta 465 (SSL) vs 587 (TLS)

**Porta 587 (TLS - Recomendado):**
```properties
spring.mail.port=587
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Porta 465 (SSL):**
```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.starttls.enable=false
```

### Alternativa: Desabilitar Email Temporariamente (Apenas DEV)

Se você quer testar sem configurar email, pode modificar temporariamente:

Em `AuthService.java`, comente o bloco de envio de email e ative a conta automaticamente:

```java
// APENAS PARA DESENVOLVIMENTO - REMOVER EM PRODUÇÃO
user.setEnabled(true);  // Ativar automaticamente
user.setEmailVerified(true);
user = userRepository.save(user);

// Comentar envio de email
// emailService.sendVerificationEmail(user.getEmail(), token);

return new RegisterResponse(
    "Cadastro realizado com sucesso! [MODO DEV - Email desabilitado]",
    user.getEmail()
);
```

⚠️ **ATENÇÃO**: Esta alternativa é APENAS para desenvolvimento. Não use em produção!

## Checklist de Verificação

- [ ] Variáveis de ambiente configuradas (MAIL_USERNAME e MAIL_PASSWORD)
- [ ] Porta SMTP correta (587 para TLS)
- [ ] Senha de App gerada (se usar Gmail)
- [ ] Backend reiniciado após configurar variáveis
- [ ] Verificar logs para mensagens de erro detalhadas

## Suporte

Se o problema persistir:
1. Verifique os logs do backend em tempo real
2. Confirme que as variáveis estão sendo carregadas (não aparecem como vazias nos logs)
3. Teste a conexão SMTP usando ferramenta externa (telnet, openssl)
