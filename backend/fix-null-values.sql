-- SCRIPT DE CORREÇÃO RÁPIDA
-- Execute este script MANUALMENTE no banco de dados

-- Atualizar valores NULL para false
UPDATE users SET enabled = false WHERE enabled IS NULL;
UPDATE users SET email_verified = false WHERE email_verified IS NULL;

-- Ativar usuários existentes (criados antes de agora)
UPDATE users SET enabled = true, email_verified = true WHERE id IS NOT NULL;

-- Adicionar valor padrão
ALTER TABLE users ALTER COLUMN enabled SET DEFAULT false;
ALTER TABLE users ALTER COLUMN email_verified SET DEFAULT false;

-- Adicionar constraint NOT NULL
ALTER TABLE users ALTER COLUMN enabled SET NOT NULL;
ALTER TABLE users ALTER COLUMN email_verified SET NOT NULL;

-- Verificar
SELECT id, name, email, enabled, email_verified FROM users;
