import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class TelaProdutos extends JFrame {

    private final ProdutoDAO dao = new ProdutoDAO();
    private List<Produto> produtos = new ArrayList<>();

    private final JTextField txtId = new JTextField();
    private final JTextField txtNome = new JTextField();
    private final JTextField txtDescricao = new JTextField();
    private final JTextField txtPreco = new JTextField();
    private final JTextField txtQuantidade = new JTextField();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nome", "Descrição", "Preço (R$)", "Qtd"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modelo);

    public TelaProdutos() {
        super("Cadastro de Produtos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 540);
        setLocationRelativeTo(null);

        JPanel conteudo = new JPanel(new BorderLayout(10, 10));
        conteudo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        conteudo.add(criarFormulario(), BorderLayout.NORTH);
        conteudo.add(new JScrollPane(tabela), BorderLayout.CENTER);
        setContentPane(conteudo);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(24);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormulario();
            }
        });

        carregarTabela();
    }

    private JPanel criarFormulario() {
        JPanel campos = new JPanel(new GridLayout(5, 2, 8, 8));
        txtId.setEditable(false);
        campos.add(new JLabel("ID:"));
        campos.add(txtId);
        campos.add(new JLabel("Nome:"));
        campos.add(txtNome);
        campos.add(new JLabel("Descrição:"));
        campos.add(txtDescricao);
        campos.add(new JLabel("Preço (ex.: 12.50):"));
        campos.add(txtPreco);
        campos.add(new JLabel("Quantidade:"));
        campos.add(txtQuantidade);

        JButton btnCadastrar = new JButton("Cadastrar");
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnLimpar = new JButton("Limpar");

        btnCadastrar.addActionListener(e -> cadastrar());
        btnAtualizar.addActionListener(e -> atualizar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limparCampos());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botoes.add(btnCadastrar);
        botoes.add(btnAtualizar);
        botoes.add(btnExcluir);
        botoes.add(btnLimpar);

        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createTitledBorder("Dados do produto"));
        painel.add(campos, BorderLayout.CENTER);
        painel.add(botoes, BorderLayout.SOUTH);
        return painel;
    }

    private void cadastrar() {
        Produto p = lerFormulario();
        if (p == null) {
            return;
        }
        try {
            dao.inserir(p);
            carregarTabela();
            limparCampos();
            informar("Produto cadastrado com sucesso! ID gerado: " + p.getId());
        } catch (SQLException e) {
            erroBanco(e);
        }
    }

    private void atualizar() {
        if (txtId.getText().isBlank()) {
            avisar("Selecione um produto na tabela para atualizar.");
            return;
        }
        Produto p = lerFormulario();
        if (p == null) {
            return;
        }
        p.setId(Integer.parseInt(txtId.getText()));
        try {
            if (dao.atualizar(p)) {
                carregarTabela();
                limparCampos();
                informar("Produto atualizado com sucesso!");
            } else {
                avisar("Nenhuma alteração realizada.");
            }
        } catch (SQLException e) {
            erroBanco(e);
        }
    }

    private void excluir() {
        if (txtId.getText().isBlank()) {
            avisar("Selecione um produto na tabela para excluir.");
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir o produto \"" + txtNome.getText() + "\"?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.excluir(Integer.parseInt(txtId.getText()));
            carregarTabela();
            limparCampos();
            informar("Produto excluído com sucesso!");
        } catch (SQLException e) {
            erroBanco(e);
        }
    }

    private void carregarTabela() {
        try {
            produtos = dao.listar();
            modelo.setRowCount(0);
            for (Produto p : produtos) {
                modelo.addRow(new Object[]{
                        p.getId(),
                        p.getNome(),
                        p.getDescricao(),
                        String.format("%.2f", p.getPreco()),
                        p.getQuantidade()
                });
            }
        } catch (SQLException e) {
            erroBanco(e);
        }
    }

    private void preencherFormulario() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= produtos.size()) {
            return;
        }
        Produto p = produtos.get(linha);
        txtId.setText(String.valueOf(p.getId()));
        txtNome.setText(p.getNome());
        txtDescricao.setText(p.getDescricao() == null ? "" : p.getDescricao());
        txtPreco.setText(String.valueOf(p.getPreco()));
        txtQuantidade.setText(String.valueOf(p.getQuantidade()));
    }

    private void limparCampos() {
        txtId.setText("");
        txtNome.setText("");
        txtDescricao.setText("");
        txtPreco.setText("");
        txtQuantidade.setText("");
        tabela.clearSelection();
        txtNome.requestFocus();
    }

    private Produto lerFormulario() {
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            avisar("O nome é obrigatório.");
            return null;
        }

        double preco;
        try {
            preco = Double.parseDouble(txtPreco.getText().trim().replace(",", "."));
            if (preco < 0) {
                avisar("O preço não pode ser negativo.");
                return null;
            }
        } catch (NumberFormatException e) {
            avisar("Preço inválido. Use números, por exemplo 12.50.");
            return null;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(txtQuantidade.getText().trim());
            if (quantidade < 0) {
                avisar("A quantidade não pode ser negativa.");
                return null;
            }
        } catch (NumberFormatException e) {
            avisar("Quantidade inválida. Use um número inteiro.");
            return null;
        }

        return new Produto(nome, txtDescricao.getText().trim(), preco, quantidade);
    }

    private void informar(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void avisar(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void erroBanco(SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro de banco de dados:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
        }
        SwingUtilities.invokeLater(() -> new TelaProdutos().setVisible(true));
    }
}