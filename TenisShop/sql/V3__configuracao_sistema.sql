-- V3: tabela de configuração global do sistema (singleton: sempre 1 linha)
-- Aplicado automaticamente por SchemaInitializer.java.

USE tenisshop_db;

CREATE TABLE IF NOT EXISTS configuracao_sistema (
    id TINYINT NOT NULL PRIMARY KEY DEFAULT 1,
    permitir_autocadastro BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_configuracao_singleton CHECK (id = 1)
);

INSERT IGNORE INTO configuracao_sistema (id, permitir_autocadastro) VALUES (1, FALSE);
