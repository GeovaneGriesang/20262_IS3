-- V6: trilha de auditoria (quem fez o quê, quando, no sistema).
-- usuario_id é o ator (NULL em tentativa de login com e-mail sem conta);
-- entidade + entidade_id identificam o registro afetado, quando houver
-- um. Aplicado automaticamente por SchemaInitializer.java.

USE tenisshop_db;

CREATE TABLE IF NOT EXISTS auditoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NULL,
    acao VARCHAR(40) NOT NULL,
    entidade VARCHAR(40) NULL,
    entidade_id INT NULL,
    detalhe VARCHAR(255) NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_auditoria_data_hora (data_hora)
);
