import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public void inserir(Produto p) throws SQLException {
        String sql = "INSERT INTO produtos (nome, descricao, preco, quantidade) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setDouble(3, p.getPreco());
            ps.setInt(4, p.getQuantidade());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    p.setId(chaves.getInt(1));
                }
            }
        }
    }

    public List<Produto> listar() throws SQLException {
        String sql = "SELECT id, nome, descricao, preco, quantidade FROM produtos ORDER BY id";
        List<Produto> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, descricao, preco, quantidade FROM produtos WHERE id = ?";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public boolean atualizar(Produto p) throws SQLException {
        String sql = "UPDATE produtos SET nome = ?, descricao = ?, preco = ?, quantidade = ? WHERE id = ?";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setDouble(3, p.getPreco());
            ps.setInt(4, p.getQuantidade());
            ps.setInt(5, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM produtos WHERE id = ?";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getDouble("preco"),
                rs.getInt("quantidade"));
    }
}