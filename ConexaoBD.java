import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/cadastro_produtos?useSSL=false&serverTimezone=America/Fortaleza&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String SENHA = "sua_senha_aqui";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}