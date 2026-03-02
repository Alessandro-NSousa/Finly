# Guia de Ativação de Conta por Email

## Visão Geral

O sistema Finly agora implementa ativação de conta obrigatória por email. Após o cadastro, o usuário recebe um email com um link de ativação que expira em 24 horas.

## Configuração

### 1. Configurar Servidor SMTP

#### Opção A: Usando Gmail

1. Acesse sua conta Google em https://myaccount.google.com/security
2. Ative a verificação em duas etapas
3. Vá em "Senhas de app" e gere uma nova senha para o aplicativo
4. Use essa senha de 16 caracteres no arquivo de configuração

#### Opção B: Usando outro provedor SMTP

Configure as variáveis de ambiente com os dados do seu provedor:
- `MAIL_HOST`: endereço do servidor SMTP
- `MAIL_PORT`: porta (geralmente 587 para TLS ou 465 para SSL)
- `MAIL_USERNAME`: seu email
- `MAIL_PASSWORD`: sua senha

### 2. Configurar Variáveis de Ambiente

#### Backend (.env)

Crie um arquivo `.env` na pasta `backend/` com:

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/finly_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=sua_senha

# JWT
JWT_SECRET=sua_chave_secreta_base64
JWT_EXPIRATION=86400000

# Email (SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=sua_senha_de_aplicativo_de_16_caracteres

# Application URL
APP_BASE_URL=http://localhost:4200
```

#### Ou usando application-local.properties

Copie o arquivo `application-local.properties.example` para `application-local.properties` e preencha os valores.

## Fluxo de Ativação

### 1. Cadastro

1. Usuário acessa `/register`
2. Preenche nome, email, senha e renda mensal
3. Sistema valida os dados:
   - Email deve ser válido e único
   - Senha deve ter no mínimo 6 caracteres
   - Renda mensal deve ser maior que zero
4. Sistema cria usuário com `enabled = false`
5. Sistema gera token único de ativação
6. Sistema envia email com link de ativação
7. Usuário vê mensagem de sucesso

### 2. Ativação

1. Usuário clica no link recebido por email
2. Sistema valida o token:
   - Token existe
   - Token não foi usado
   - Token não expirou (24 horas)
3. Sistema ativa a conta (`enabled = true`, `emailVerified = true`)
4. Sistema marca o token como usado
5. Usuário é redirecionado para a página de login

### 3. Login

1. Usuário tenta fazer login
2. Sistema valida credenciais
3. Sistema verifica se conta está ativada
4. Se não ativada, retorna erro: "Conta não ativada. Verifique seu email."
5. Se ativada, gera token JWT e permite acesso

### 4. Reenvio de Email

Se o usuário não recebeu o email ou o link expirou:

1. Na tela de cadastro, após mensagem de sucesso, clique em "Reenviar"
2. Ou faça requisição POST para `/api/auth/resend-verification` com `{"email": "seu_email@example.com"}`
3. Sistema invalida tokens antigos
4. Sistema gera novo token
5. Sistema envia novo email

## API Endpoints

### POST /api/auth/register

Registra novo usuário e envia email de ativação.

**Request:**
```json
{
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "senha123",
  "monthlyIncome": 5000.00
}
```

**Response (200):**
```json
{
  "message": "Cadastro realizado com sucesso! Verifique seu email para ativar sua conta.",
  "email": "joao@example.com"
}
```

### GET /api/auth/activate?token={token}

Ativa a conta do usuário.

**Response (200):**
```json
{
  "message": "Conta ativada com sucesso! Você já pode fazer login."
}
```

**Response (400):**
```json
{
  "message": "Token inválido"
}
```

### POST /api/auth/resend-verification

Reenvia o email de verificação.

**Request:**
```json
{
  "email": "joao@example.com"
}
```

**Response (200):**
```json
{
  "message": "Email de verificação reenviado com sucesso!"
}
```

### POST /api/auth/login

Faz login (requer conta ativada).

**Request:**
```json
{
  "email": "joao@example.com",
  "password": "senha123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "João Silva",
  "email": "joao@example.com"
}
```

**Response (400):**
```json
{
  "message": "Conta não ativada. Verifique seu email."
}
```

## Modelo de Dados

### User

```java
@Entity
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private BigDecimal monthlyIncome;
    private Boolean enabled = false;          // Novo campo
    private Boolean emailVerified = false;    // Novo campo
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### VerificationToken

```java
@Entity
public class VerificationToken {
    private Long id;
    private String token;                     // UUID único
    private User user;                        // Relacionamento 1-1
    private LocalDateTime expiryDate;         // createdAt + 24 horas
    private Boolean used = false;
    private LocalDateTime createdAt;
}
```

## Email Template

O email enviado segue o seguinte formato:

**Assunto:** Finly - Ative sua conta

**Corpo:**
```
Olá!

Obrigado por se cadastrar no Finly.

Para ativar sua conta, clique no link abaixo:
http://localhost:4200/activate?token=abc123-def456-ghi789

Este link expira em 24 horas.

Se você não se cadastrou no Finly, ignore este email.

Atenciosamente,
Equipe Finly
```

## Troubleshooting

### Email não está sendo enviado

1. Verifique as credenciais SMTP nas variáveis de ambiente
2. Para Gmail, certifique-se de usar uma senha de aplicativo, não sua senha normal
3. Verifique os logs do backend para mensagens de erro
4. Certifique-se de que a porta SMTP não está bloqueada pelo firewall

### Link de ativação expirado

1. Solicite um novo email através do botão "Reenviar"
2. Ou faça uma requisição para `/api/auth/resend-verification`

### Conta não ativa após clicar no link

1. Verifique se o token na URL está completo
2. Verifique se você não clicou no link mais de uma vez
3. Verifique os logs do backend para erros

### Problemas com localhost em produção

Atualize a variável `APP_BASE_URL` para o domínio real da aplicação:
```env
APP_BASE_URL=https://seudominio.com
```

## Segurança

### Proteções Implementadas

1. **Token único por usuário**: Cada usuário tem apenas um token válido por vez
2. **Expiração de 24 horas**: Tokens expiram automaticamente
3. **Uso único**: Tokens não podem ser reutilizados após ativação
4. **UUID aleatório**: Tokens são difíceis de adivinhar
5. **Validação de email**: Apenas emails válidos são aceitos
6. **Email único**: Não permite duplicação de emails

### Limpeza de Tokens Expirados

Para manter o banco de dados limpo, você pode criar uma tarefa agendada para deletar tokens expirados:

```java
@Scheduled(cron = "0 0 2 * * ?") // Executa diariamente às 2h
public void cleanExpiredTokens() {
    verificationTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
}
```

## Próximos Passos

- [ ] Implementar templates HTML para emails mais bonitos
- [ ] Adicionar página personalizada de sucesso/erro após ativação
- [ ] Implementar recuperação de senha por email
- [ ] Adicionar notificações de login suspeito
- [ ] Implementar alteração de email com re-verificação
