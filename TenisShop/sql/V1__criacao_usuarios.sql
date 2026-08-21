-- V1: criação do banco de dados e da tabela usuarios
-- Referência legível do schema; quem aplica isto automaticamente é
-- SchemaInitializer.java a cada início da aplicação (ver Aula 04, Seção
-- "Evolução do Banco de Dados"). Rodar este arquivo à mão só é necessário
-- como plano B, num banco onde a aplicação nunca chegou a rodar.

CREATE DATABASE IF NOT EXISTS tenisshop_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE tenisshop_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL DEFAULT 'OPERADOR',
    ativo BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
