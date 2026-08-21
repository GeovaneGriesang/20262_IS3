-- V5: coluna de versão em usuarios, para controle de concorrência
-- otimista (evita que duas edições simultâneas do mesmo usuário se
-- sobrescrevam silenciosamente; ver Aula 06, "Bloqueio Otimista").
-- Aplicado automaticamente por SchemaInitializer.java.

USE tenisshop_db;

ALTER TABLE usuarios ADD COLUMN versao INT NOT NULL DEFAULT 1;
