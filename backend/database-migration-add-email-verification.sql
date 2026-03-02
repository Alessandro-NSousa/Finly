-- =====================================================
-- Migration: Adicionar colunas de verificação de email
-- Data: 01/03/2026
-- Descrição: Adiciona campos enabled e email_verified
-- =====================================================

-- Passo 1: Adicionar colunas SEM constraint NOT NULL primeiro (permite NULL temporariamente)
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN;

-- Passo 2: Atualizar valores NULL para false (usuários existentes)
UPDATE users SET enabled = false WHERE enabled IS NULL;
UPDATE users SET email_verified = false WHERE email_verified IS NULL;

-- Passo 3: Ativar usuários que foram criados antes desta funcionalidade
-- (Usuários criados há mais de 1 minuto são considerados antigos)
UPDATE users 
SET enabled = true, email_verified = true 
WHERE created_at < (CURRENT_TIMESTAMP - INTERVAL '1 minute');

-- Passo 4: Agora adicionar constraint NOT NULL com valor padrão
ALTER TABLE users ALTER COLUMN enabled SET DEFAULT false;
ALTER TABLE users ALTER COLUMN email_verified SET DEFAULT false;
ALTER TABLE users ALTER COLUMN enabled SET NOT NULL;
ALTER TABLE users ALTER COLUMN email_verified SET NOT NULL;

-- Criar tabela de tokens de verificação
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

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_verification_tokens_token ON verification_tokens(token);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_user_id ON verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Verificar resultado
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default 
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name IN ('enabled', 'email_verified')
ORDER BY column_name;

-- Verificar se há usuários
SELECT 
    id, 
    name, 
    email, 
    enabled, 
    email_verified,
    created_at
FROM users 
ORDER BY id;
