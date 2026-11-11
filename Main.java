import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner entrada = new Scanner(System.in);
    private static final ProdutoDAO dao = new ProdutoDAO();

    public static void main(String[] args) {
        int opcao;
        do {
            exibirMenu();
            opcao = lerInt("Escolha uma opção: ");
            try {
                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> listar();
                    case 3 -> atualizar();
                    case 4 -> excluir();
                    case 0 -> System.out.println("Encerrando o sistema. Até logo!");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (SQLException e) {
                System.out.println("Erro de banco de dados: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static void exibirMenu() {
        System.out.println("\n===== CADASTRO DE PRODUTOS =====");
        System.out.println("1 - Cadastrar produto");
        System.out.println("2 - Listar produtos");
        System.out.println("3 - Atualizar produto");
        System.out.println("4 - Excluir produto");
        System.out.println("0 - Sair");
    }

    private static void cadastrar() throws SQLException {
        System.out.println("\n--- Novo produto ---");
        String nome = lerTexto("Nome: ");
        if (nome.isBlank()) {
            System.out.println("O nome é obrigatório.");
            return;
        }
        String descricao = lerTexto("Descrição: ");
        double preco = lerDouble("Preço: ");
        int quantidade = lerInt("Quantidade: ");

        Produto p = new Produto(nome, descricao, preco, quantidade);
        dao.inserir(p);
        System.out.println("Produto cadastrado com sucesso! ID gerado: " + p.getId());
    }

    private static void listar() throws SQLException {
        List<Produto> produtos = dao.listar();
        System.out.println("\n--- Produtos cadastrados ---");
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        for (Produto p : produtos) {
            System.out.println(p);
        }
    }

    private static void atualizar() throws SQLException {
        System.out.println("\n--- Atualizar produto ---");
        int id = lerInt("ID do produto: ");
        Produto p = dao.buscarPorId(id);
        if (p == null) {
            System.out.println("Produto não encontrado.");
            return;
        }
        System.out.println("Dados atuais: " + p);
        System.out.println("(Deixe em branco para manter o valor atual)");

        String nome = lerTexto("Novo nome [" + p.getNome() + "]: ");
        if (!nome.isBlank()) p.setNome(nome);

        String descricao = lerTexto("Nova descrição [" + p.getDescricao() + "]: ");
        if (!descricao.isBlank()) p.setDescricao(descricao);

        String preco = lerTexto("Novo preço [" + p.getPreco() + "]: ");
        if (!preco.isBlank()) {
            try {
                p.setPreco(Double.parseDouble(preco.replace(",", ".")));
            } catch (NumberFormatException e) {
                System.out.println("Preço inválido, mantendo o valor atual.");
            }
        }

        String qtd = lerTexto("Nova quantidade [" + p.getQuantidade() + "]: ");
        if (!qtd.isBlank()) {
            try {
                p.setQuantidade(Integer.parseInt(qtd));
            } catch (NumberFormatException e) {
                System.out.println("Quantidade inválida, mantendo o valor atual.");
            }
        }

        if (dao.atualizar(p)) {
            System.out.println("Produto atualizado com sucesso!");
        } else {
            System.out.println("Nenhuma alteração realizada.");
        }
    }

    private static void excluir() throws SQLException {
        System.out.println("\n--- Excluir produto ---");
        int id = lerInt("ID do produto: ");
        Produto p = dao.buscarPorId(id);
        if (p == null) {
            System.out.println("Produto não encontrado.");
            return;
        }
        System.out.println(p);
        String confirma = lerTexto("Confirma a exclusão? (s/n): ");
        if (confirma.equalsIgnoreCase("s")) {
            dao.excluir(id);
            System.out.println("Produto excluído com sucesso!");
        } else {
            System.out.println("Exclusão cancelada.");
        }
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return entrada.nextLine().trim();
    }

    private static int lerInt(String mensagem) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(mensagem));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private static double lerDouble(String mensagem) {
        while (true) {
            try {
                return Double.parseDouble(lerTexto(mensagem).replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido (ex.: 12.50).");
            }
        }
    }
}