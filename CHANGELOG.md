# Changelog - Sistema de Ativação de Conta por Email

## Data: Março 2026

### 🎉 Nova Funcionalidade: Ativação de Conta por Email

#### Backend

##### Novos Arquivos Criados

1. **`model/VerificationToken.java`**
   - Entidade para armazenar tokens de verificação
   - Campos: id, token (UUID), user (relacionamento 1-1), expiryDate, used, createdAt
   - Método `isExpired()` para verificar expiração

2. **`repository/VerificationTokenRepository.java`**
   - Interface JPA para gerenciar tokens
   - Métodos: findByToken, findByUser, deleteByExpiryDateBefore

3. **`service/EmailService.java`**
   - Serviço para envio de emails via SMTP
   - Método `sendVerificationEmail()` para envio de email de ativação
   - Configurável via variáveis de ambiente

4. **`dto/RegisterResponse.java`**
   - DTO para resposta de cadastro sem token JWT
   - Campos: message, email

##### Arquivos Modificados

1. **`model/User.java`**
   - Adicionado campo `enabled` (Boolean) - indica se conta está ativa
   - Adicionado campo `emailVerified` (Boolean) - indica se email foi verificado
   - Valores padrão: false para ambos

2. **`service/AuthService.java`**
   - **Método `register()`**: 
     - Agora retorna `RegisterResponse` ao invés de `AuthResponse`
     - Cria usuário com `enabled = false`
     - Gera token de verificação (UUID)
     - Envia email de ativação
     - Não faz login automático
   
   - **Método `login()`**:
     - Verifica se conta está ativada antes de permitir login
     - Lança exceção se conta não estiver ativa
   
   - **Novo método `activateAccount(String token)`**:
     - Valida token (existe, não usado, não expirado)
     - Ativa conta do usuário
     - Marca token como usado
   
   - **Novo método `resendVerificationEmail(String email)`**:
     - Invalida tokens antigos
     - Gera novo token
     - Reenvia email de verificação

3. **`controller/AuthController.java`**
   - **POST `/api/auth/register`**: 
     - Retorna `RegisterResponse` ao invés de `AuthResponse`
     - Descrição atualizada para mencionar envio de email
   
   - **Novo GET `/api/auth/activate`**:
     - Recebe token como query parameter
     - Ativa conta do usuário
   
   - **Novo POST `/api/auth/resend-verification`**:
     - Recebe email no body
     - Reenvia email de verificação

4. **`security/UserDetailsServiceImpl.java`**
   - Método `loadUserByUsername()` agora considera o campo `enabled`
   - UserDetails criado com status de enabled do usuário

5. **`pom.xml`**
   - Adicionada dependência `spring-boot-starter-mail`

6. **`application.properties`**
   - Adicionadas configurações de email:
     - spring.mail.host
     - spring.mail.port
     - spring.mail.username
     - spring.mail.password
     - spring.mail.properties (SMTP auth e TLS)
   - Adicionada configuração app.base.url

7. **Arquivos de exemplo**:
   - **`backend/.env.example`**: Adicionadas variáveis de email
   - **`application-local.properties.example`**: Adicionadas configurações de email

#### Frontend

##### Novos Arquivos Criados

1. **`components/activate/activate.component.ts`**
   - Componente para página de ativação de conta
   - Lê token da query string
   - Chama API de ativação
   - Redireciona para login após sucesso

2. **`components/activate/activate.component.html`**
   - Template da página de ativação
   - Mostra loading durante ativação
   - Mostra mensagem de sucesso ou erro
   - Botão para ir para login

3. **`components/activate/activate.component.css`**
   - Estilos para página de ativação
   - Spinner de loading
   - Alertas de sucesso/erro

##### Arquivos Modificados

1. **`models/user.model.ts`**
   - Nova interface `RegisterResponse` com campos: message, email

2. **`services/auth.service.ts`**
   - **Método `register()`**: 
     - Retorna `Observable<RegisterResponse>` ao invés de `AuthResponse`
     - Não armazena token no localStorage
   
   - **Novo método `activateAccount(token: string)`**:
     - Chama endpoint de ativação
     - Retorna observable com mensagem
   
   - **Novo método `resendVerificationEmail(email: string)`**:
     - Chama endpoint de reenvio
     - Retorna observable com mensagem

3. **`components/register/register.component.ts`**
   - Adicionadas propriedades: `successMessage`, `registeredEmail`
   - Método `onSubmit()` atualizado:
     - Mostra mensagem de sucesso ao invés de redirecionar
     - Armazena email registrado
     - Reseta formulário após sucesso
   - **Novo método `resendEmail()`**:
     - Reenvia email de verificação

4. **`components/register/register.component.html`**
   - Adicionado alert de sucesso
   - Adicionado botão "Reenviar" email
   - Formulário oculto após sucesso

5. **`components/register/register.component.css`**
   - Estilos para botão de link (reenviar)
   - Estilos para alertas (success/danger)

6. **`app.module.ts`**
   - Importado e declarado `ActivateComponent`

7. **`app-routing.module.ts`**
   - Nova rota: `/activate` -> `ActivateComponent`

#### Documentação

1. **`README.md`**
   - Atualizada seção de funcionalidades
   - Adicionados novos endpoints de autenticação
   - Atualizada seção de variáveis de ambiente com configurações de email
   - Adicionada seção de segurança com informações sobre ativação
   - Atualizado fluxo de uso para incluir ativação

2. **`ACTIVATION_GUIDE.md`** (Novo)
   - Guia completo sobre o sistema de ativação
   - Instruções de configuração SMTP
   - Fluxo detalhado de ativação
   - Documentação de API
   - Modelo de dados
   - Template de email
   - Troubleshooting

### 🔒 Melhorias de Segurança

- ✅ Validação obrigatória de email
- ✅ Emails únicos no sistema
- ✅ Conta desabilitada até verificação
- ✅ Tokens com expiração de 24 horas
- ✅ Tokens de uso único
- ✅ Proteção contra reuso de tokens

### 🎨 Melhorias de UX

- ✅ Mensagens claras sobre status de ativação
- ✅ Página dedicada para ativação
- ✅ Opção de reenvio de email
- ✅ Feedback visual durante todo o processo
- ✅ Redirecionamento automático após ativação

### 📝 Variáveis de Ambiente Necessárias

```env
# Configurações de Email (Backend)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=sua_senha_de_aplicativo
APP_BASE_URL=http://localhost:4200
```

### 🔄 Migração de Dados

Se você já tem usuários no banco de dados, execute o seguinte SQL para ativar contas existentes:

```sql
-- Ativar todas as contas existentes
UPDATE users SET enabled = true, email_verified = true WHERE enabled IS NULL;

-- Ou se preferir, apenas ativar (sem marcar email como verificado)
UPDATE users SET enabled = true WHERE enabled IS NULL;
```

### ⚠️ Breaking Changes

1. **API de Registro**: 
   - Endpoint `/api/auth/register` agora retorna `RegisterResponse` ao invés de `AuthResponse`
   - Não retorna mais token JWT imediatamente
   - Usuários devem ativar conta antes de fazer login

2. **Login**:
   - Login falhará se conta não estiver ativada
   - Retorna mensagem específica: "Conta não ativada. Verifique seu email."

3. **Modelo User**:
   - Adicionados campos obrigatórios: `enabled`, `emailVerified`
   - Spring JPA criará colunas automaticamente na próxima execução

### 📋 Checklist de Implantação

- [ ] Configurar servidor SMTP (Gmail ou outro)
- [ ] Gerar senha de aplicativo (se usar Gmail)
- [ ] Adicionar variáveis de ambiente no servidor
- [ ] Atualizar `APP_BASE_URL` para domínio de produção
- [ ] Executar SQL de migração para usuários existentes (se aplicável)
- [ ] Testar envio de email em ambiente de staging
- [ ] Monitorar logs de email após deploy

### 🐛 Problemas Corrigidos

- Contas podiam ser criadas com emails inválidos
- Não havia verificação de propriedade do email
- Qualquer pessoa podia registrar com qualquer email

### 🔮 Próximas Melhorias Sugeridas

- [ ] Templates HTML para emails
- [ ] Recuperação de senha por email
- [ ] Alteração de email com re-verificação
- [ ] Notificações de login
- [ ] Limpeza automática de tokens expirados
