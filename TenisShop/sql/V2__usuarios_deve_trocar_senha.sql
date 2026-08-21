-- V2: sinalizador de troca de senha obrigatória no próximo login
-- Usado tanto para usuários criados por um administrador (com senha
-- temporária) quanto para o usuário administrador semente do sistema.
-- Aplicado automaticamente por SchemaInitializer.java (com verificação
-- de existência da coluna antes de alterar, para nunca falhar num banco
-- que já está em dia).

USE tenisshop_db;

ALTER TABLE usuarios ADD COLUMN deve_trocar_senha BOOLEAN NOT NULL DEFAULT FALSE;
