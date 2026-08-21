-- V7: aumenta o tamanho de auditoria.detalhe, que passa a guardar o
-- histórico completo de campo a campo alterado (ex.: "nome: \"Carlos\" ->
-- \"Carlos Silva\"; perfil: OPERADOR -> ADMINISTRADOR"), não só uma frase
-- curta genérica. VARCHAR(255) era pouco para vários campos alterados de
-- uma vez. Aplicado automaticamente por SchemaInitializer.java.

USE tenisshop_db;

ALTER TABLE auditoria MODIFY COLUMN detalhe VARCHAR(500) NULL;
