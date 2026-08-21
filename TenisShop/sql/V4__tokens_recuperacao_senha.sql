-- V4: tokens de recuperação de senha (fluxo "esqueci minha senha")
-- Só o hash SHA-256 do token é guardado (token_hash), nunca o token em
-- texto puro enviado por e-mail. Aplicado automaticamente por
-- SchemaInitializer.java.

USE tenisshop_db;

CREATE TABLE IF NOT EXISTS tokens_recuperacao_senha (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    data_expiracao DATETIME NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_token_hash (token_hash)
);
