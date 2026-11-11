# Sistema de Cadastro de Produtos (Java + MySQL)

Trabalho de Banco de Dados. CRUD de produtos (cadastrar, listar, atualizar e excluir) em Java com JDBC e MySQL, com três formas de uso: menu no console, janela (Swing) e página web.

## Como executar

1. No MySQL Workbench, execute o arquivo `banco.sql`.
2. Baixe o Connector/J (Platform Independent) em https://dev.mysql.com/downloads/connector/j/ e coloque o `.jar` nesta pasta.
3. Em `ConexaoBD.java`, troque `sua_senha_aqui` pela senha do seu MySQL.
4. Compile e execute (troque a versão do `.jar` se necessário):

    javac -cp ".;mysql-connector-j-26.10.0.jar" *.java

    java -cp ".;mysql-connector-j-26.10.0.jar" Main            (console)
    java -cp ".;mysql-connector-j-26.10.0.jar" TelaProdutos    (janela)
    java -cp ".;mysql-connector-j-26.10.0.jar" ServidorWeb     (navegador: http://localhost:8080)

## Arquivos

- `banco.sql`: cria o banco e a tabela
- `Produto.java`: modelo
- `ConexaoBD.java`: conexão com o MySQL
- `ProdutoDAO.java`: operações SQL
- `Main.java`, `TelaProdutos.java`, `ServidorWeb.java`: as três interfaces