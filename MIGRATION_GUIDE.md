# Guia de Migração do Banco de Dados

## Problema

O erro ocorre porque a tabela `users` no banco de dados não possui as colunas `enabled` e `email_verified` que foram adicionadas ao código.

```
ERROR: column "email_verified" of relation "users" does not exist
```

## Solução: Executar Migration SQL

### Opção 1: Via psql (Linha de Comando)

1. Abra o terminal/PowerShell
2. Conecte ao banco de dados:

```bash
psql -U postgres -d finly
```

3. Execute o script SQL:

```bash
psql -U postgres -d finly -f backend/database-migration-add-email-verification.sql
```

Ou copie e cole manualmente:

```sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT false;

-- Ativar usuários existentes (opcional)
UPDATE users SET enabled = true, email_verified = true;

-- Criar tabela de tokens
CREATE TABLE IF NOT EXISTS verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_verification_token_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);
```

### Opção 2: Via pgAdmin

1. Abra pgAdmin
2. Conecte ao servidor PostgreSQL
3. Navegue até: Databases → finly → Schemas → public → Tables
4. Clique com botão direito na tabela `users`
5. Selecione "Query Tool"
6. Cole o SQL acima
7. Clique em "Execute" (F5)

### Opção 3: Via DBeaver ou Outras IDEs

1. Conecte ao banco `finly`
2. Abra um SQL Editor
3. Cole e execute o script `database-migration-add-email-verification.sql`

### Opção 4: Via Docker (se estiver usando)

```bash
docker exec -i finly-postgres psql -U postgres -d finly < backend/database-migration-add-email-verification.sql
```

## Verificação

Após executar a migration, verifique se as colunas foram criadas:

```sql
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users';
```

Você deve ver as colunas:
- `enabled` (boolean, default false)
- `email_verified` (boolean, default false)

E a tabela:
```sql
SELECT * FROM verification_tokens LIMIT 1;
```

## Após a Migration

1. **Reinicie o backend** (se já estava rodando)
2. **Tente cadastrar novamente**

Agora o cadastro deve funcionar corretamente!

## Para Usuários Existentes

Se você já tem usuários cadastrados no banco, eles foram automaticamente ativados pelo script (enabled=true, email_verified=true).

Se você NÃO quer ativar usuários antigos, remova esta linha do script antes de executar:

```sql
UPDATE users SET enabled = true, email_verified = true;
```

## Troubleshooting

### "relation users does not exist"
- A tabela users não existe. Primeiro execute o backend para criar as tabelas básicas

### "permission denied"
- Use o usuário correto (postgres ou finly_user conforme sua configuração)

### "database finly does not exist"
- Crie o banco primeiro:
```sql
CREATE DATABASE finly;
```

## Prevenção Futura

Para evitar este problema no futuro, considere usar:

1. **Flyway** ou **Liquibase** para migrations automáticas
2. **spring.jpa.hibernate.ddl-auto=update** funciona melhor em desenvolvimento, mas use migrations em produção
