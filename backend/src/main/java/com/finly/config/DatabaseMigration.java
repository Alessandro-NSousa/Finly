package com.finly.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Executa migrations de banco de dados no startup da aplicação
 * Esta é uma solução temporária até implementar Flyway/Liquibase
 */
@Component
public class DatabaseMigration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void migrate() {
        logger.info("Verificando e aplicando migrations do banco de dados...");
        
        try {
            // Adicionar coluna 'enabled' se não existir
            addColumnIfNotExists("users", "enabled", "BOOLEAN DEFAULT false NOT NULL");
            
            // Adicionar coluna 'email_verified' se não existir
            addColumnIfNotExists("users", "email_verified", "BOOLEAN DEFAULT false NOT NULL");
            
            // Criar tabela verification_tokens se não existir
            createVerificationTokensTable();
            
            // Ativar usuários existentes (criados antes da implementação de verificação)
            activateExistingUsers();
            
            logger.info("Migrations aplicadas com sucesso!");
            
        } catch (Exception e) {
            logger.error("Erro ao aplicar migrations: {}", e.getMessage(), e);
            // Não lança exceção para não impedir o startup da aplicação
        }
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        try {
            // Verifica se a coluna já existe
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_name = ? AND column_name = ?",
                Integer.class,
                tableName, columnName
            );
            
            if (count == null || count == 0) {
                // Adiciona coluna SEM NOT NULL primeiro
                String alterSql = String.format(
                    "ALTER TABLE %s ADD COLUMN %s BOOLEAN",
                    tableName, columnName
                );
                
                logger.info("Adicionando coluna {}.{}", tableName, columnName);
                jdbcTemplate.execute(alterSql);
                
                // Atualiza valores NULL para false
                String updateSql = String.format(
                    "UPDATE %s SET %s = false WHERE %s IS NULL",
                    tableName, columnName, columnName
                );
                jdbcTemplate.execute(updateSql);
                
                // Adiciona valor padrão
                String defaultSql = String.format(
                    "ALTER TABLE %s ALTER COLUMN %s SET DEFAULT false",
                    tableName, columnName
                );
                jdbcTemplate.execute(defaultSql);
                
                // Agora adiciona constraint NOT NULL
                String notNullSql = String.format(
                    "ALTER TABLE %s ALTER COLUMN %s SET NOT NULL",
                    tableName, columnName
                );
                jdbcTemplate.execute(notNullSql);
                
                logger.info("Coluna {}.{} adicionada com sucesso", tableName, columnName);
            } else {
                logger.debug("Coluna {}.{} já existe, verificando valores NULL...", tableName, columnName);
                
                // Se a coluna existe, garante que não há valores NULL
                String updateSql = String.format(
                    "UPDATE %s SET %s = false WHERE %s IS NULL",
                    tableName, columnName, columnName
                );
                int updated = jdbcTemplate.update(updateSql);
                
                if (updated > 0) {
                    logger.info("Atualizados {} registros NULL em {}.{}", updated, tableName, columnName);
                    
                    // Adiciona valor padrão se não existir
                    try {
                        String defaultSql = String.format(
                            "ALTER TABLE %s ALTER COLUMN %s SET DEFAULT false",
                            tableName, columnName
                        );
                        jdbcTemplate.execute(defaultSql);
                    } catch (Exception e) {
                        logger.debug("Valor padrão já existe ou erro ao adicionar: {}", e.getMessage());
                    }
                    
                    // Adiciona constraint NOT NULL se não existir
                    try {
                        String notNullSql = String.format(
                            "ALTER TABLE %s ALTER COLUMN %s SET NOT NULL",
                            tableName, columnName
                        );
                        jdbcTemplate.execute(notNullSql);
                        logger.info("Constraint NOT NULL adicionada em {}.{}", tableName, columnName);
                    } catch (Exception e) {
                        logger.debug("Constraint NOT NULL já existe ou erro: {}", e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Erro ao adicionar/atualizar coluna {}.{}: {}", tableName, columnName, e.getMessage());
        }
    }

    private void createVerificationTokensTable() {
        try {
            // Verifica se a tabela já existe
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_name = 'verification_tokens'",
                Integer.class
            );
            
            if (count == null || count == 0) {
                String createTableSql = 
                    "CREATE TABLE verification_tokens (" +
                    "    id BIGSERIAL PRIMARY KEY," +
                    "    token VARCHAR(255) NOT NULL UNIQUE," +
                    "    user_id BIGINT NOT NULL," +
                    "    expiry_date TIMESTAMP NOT NULL," +
                    "    used BOOLEAN NOT NULL DEFAULT false," +
                    "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "    CONSTRAINT fk_verification_token_user " +
                    "        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
                
                logger.info("Criando tabela verification_tokens");
                jdbcTemplate.execute(createTableSql);
                
                // Criar índices
                jdbcTemplate.execute("CREATE INDEX idx_verification_tokens_token ON verification_tokens(token)");
                jdbcTemplate.execute("CREATE INDEX idx_verification_tokens_user_id ON verification_tokens(user_id)");
                
                logger.info("Tabela verification_tokens criada com sucesso");
            } else {
                logger.debug("Tabela verification_tokens já existe, pulando...");
            }
            
        } catch (Exception e) {
            logger.warn("Erro ao criar tabela verification_tokens: {}", e.getMessage());
        }
    }

    private void activateExistingUsers() {
        try {
            // Ativa usuários que foram criados antes da implementação de verificação
            int updated = jdbcTemplate.update(
                "UPDATE users SET enabled = true, email_verified = true " +
                "WHERE (enabled IS NULL OR enabled = false) " +
                "AND created_at < (CURRENT_TIMESTAMP - INTERVAL '1 hour')"
            );
            
            if (updated > 0) {
                logger.info("Ativados {} usuários existentes", updated);
            }
            
        } catch (Exception e) {
            logger.warn("Erro ao ativar usuários existentes: {}", e.getMessage());
        }
    }
}
