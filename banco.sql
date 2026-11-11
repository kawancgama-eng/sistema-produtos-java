CREATE DATABASE IF NOT EXISTS cadastro_produtos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cadastro_produtos;

CREATE TABLE IF NOT EXISTS produtos (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(100)   NOT NULL,
    descricao  VARCHAR(255),
    preco      DECIMAL(10, 2) NOT NULL,
    quantidade INT            NOT NULL DEFAULT 0
);

INSERT INTO produtos (nome, descricao, preco, quantidade) VALUES
    ('Caneta Azul', 'Caneta esferográfica azul', 2.50, 100),
    ('Caderno 100 folhas', 'Caderno brochura capa dura', 18.90, 40);

SELECT * FROM produtos;