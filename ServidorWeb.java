import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Interface web do cadastro de produtos.
 * Usa o servidor HTTP que já vem no JDK (sem instalar nada) e reutiliza o ProdutoDAO.
 * Depois de iniciar, abra no navegador: http://localhost:8080
 */
public class ServidorWeb {

    private static final int PORTA = 8080;
    private static final ProdutoDAO dao = new ProdutoDAO();

    public static void main(String[] args) throws IOException {
        // 127.0.0.1 = só este computador consegue acessar
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", PORTA), 0);
        servidor.createContext("/", ServidorWeb::tratar);
        servidor.start();
        System.out.println("Servidor no ar! Abra no navegador: http://localhost:" + PORTA);
        System.out.println("Para encerrar, pressione Ctrl+C neste terminal.");
    }

    // ---------- Roteamento ----------

    private static void tratar(HttpExchange ex) throws IOException {
        String caminho = ex.getRequestURI().getPath();
        String metodo = ex.getRequestMethod();
        try {
            if (metodo.equals("GET") && caminho.equals("/")) {
                paginaInicial(ex);
            } else if (metodo.equals("POST") && caminho.equals("/cadastrar")) {
                cadastrar(ex);
            } else if (metodo.equals("POST") && caminho.equals("/atualizar")) {
                atualizar(ex);
            } else if (metodo.equals("POST") && caminho.equals("/excluir")) {
                excluir(ex);
            } else {
                responder(ex, 404, "<h1>Página não encontrada</h1><a href=\"/\">Voltar</a>");
            }
        } catch (IllegalArgumentException e) {
            // dados inválidos digitados pelo usuário
            redirecionar(ex, "/?erro=" + codificar(e.getMessage()));
        } catch (SQLException e) {
            redirecionar(ex, "/?erro=" + codificar("Erro de banco de dados: " + e.getMessage()));
        } catch (Exception e) {
            responder(ex, 500, "<h1>Erro interno</h1><p>" + esc(String.valueOf(e)) + "</p>");
        }
    }

    private static void paginaInicial(HttpExchange ex) throws IOException {
        Map<String, String> consulta = lerParametros(ex.getRequestURI().getRawQuery());
        String erro = consulta.get("erro");
        List<Produto> lista = new ArrayList<>();
        Produto editando = null;
        try {
            lista = dao.listar();
            if (consulta.containsKey("editar")) {
                editando = dao.buscarPorId(lerId(consulta.get("editar")));
            }
        } catch (IllegalArgumentException e) {
            erro = e.getMessage();
        } catch (SQLException e) {
            erro = "Erro de banco de dados: " + e.getMessage();
        }
        responder(ex, 200, montarPagina(lista, editando, erro, consulta.get("ok")));
    }

    private static void cadastrar(HttpExchange ex) throws IOException, SQLException {
        Produto p = produtoDoFormulario(lerParametros(lerCorpo(ex)));
        dao.inserir(p);
        redirecionar(ex, "/?ok=" + codificar("Produto cadastrado com sucesso!"));
    }

    private static void atualizar(HttpExchange ex) throws IOException, SQLException {
        Map<String, String> formulario = lerParametros(lerCorpo(ex));
        Produto p = produtoDoFormulario(formulario);
        p.setId(lerId(formulario.get("id")));
        if (dao.atualizar(p)) {
            redirecionar(ex, "/?ok=" + codificar("Produto atualizado com sucesso!"));
        } else {
            redirecionar(ex, "/?erro=" + codificar("Produto não encontrado."));
        }
    }

    private static void excluir(HttpExchange ex) throws IOException, SQLException {
        int id = lerId(lerParametros(lerCorpo(ex)).get("id"));
        if (dao.excluir(id)) {
            redirecionar(ex, "/?ok=" + codificar("Produto excluído com sucesso!"));
        } else {
            redirecionar(ex, "/?erro=" + codificar("Produto não encontrado."));
        }
    }

    // ---------- Validação ----------

    private static Produto produtoDoFormulario(Map<String, String> f) {
        String nome = f.getOrDefault("nome", "").trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        double preco;
        try {
            preco = Double.parseDouble(f.getOrDefault("preco", "").trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preço inválido. Use números, por exemplo 12.50.");
        }
        if (Double.isNaN(preco) || Double.isInfinite(preco) || preco < 0) {
            throw new IllegalArgumentException("O preço deve ser um número maior ou igual a zero.");
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(f.getOrDefault("quantidade", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Quantidade inválida. Use um número inteiro.");
        }
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa.");
        }

        return new Produto(nome, f.getOrDefault("descricao", "").trim(), preco, quantidade);
    }

    private static int lerId(String texto) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID inválido.");
        }
    }

    // ---------- Montagem da página HTML ----------

    private static String montarPagina(List<Produto> produtos, Produto editando, String erro, String ok) {
        boolean editar = editando != null;
        StringBuilder h = new StringBuilder();

        h.append("""
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Cadastro de Produtos</title>
                <style>
                  * { box-sizing: border-box; }
                  body { font-family: Arial, sans-serif; background: #f2f4f8; margin: 0; color: #222; }
                  main { max-width: 900px; margin: 0 auto; padding: 24px 16px; }
                  h1 { margin-top: 0; }
                  .cartao { background: #fff; border-radius: 10px; padding: 20px; margin-bottom: 24px;
                            box-shadow: 0 1px 4px rgba(0,0,0,.12); }
                  label { display: block; font-weight: bold; margin: 12px 0 4px; }
                  input[type=text], input[type=number] { width: 100%; padding: 9px; font-size: 15px;
                            border: 1px solid #bbb; border-radius: 6px; }
                  .botoes { margin-top: 16px; display: flex; gap: 10px; flex-wrap: wrap; }
                  button, .botao-sec { padding: 9px 18px; font-size: 14px; border: none; border-radius: 6px;
                            cursor: pointer; background: #2563eb; color: #fff; text-decoration: none; }
                  .botao-sec { background: #6b7280; }
                  .perigo { background: #dc2626; padding: 6px 12px; }
                  .msg { padding: 12px 16px; border-radius: 8px; margin-bottom: 16px; }
                  .msg.ok { background: #dcfce7; color: #166534; }
                  .msg.erro { background: #fee2e2; color: #991b1b; }
                  .rolagem { overflow-x: auto; }
                  table { width: 100%; border-collapse: collapse; }
                  th, td { text-align: left; padding: 10px 8px; border-bottom: 1px solid #e5e7eb; }
                  th { background: #f9fafb; }
                  .acoes { white-space: nowrap; }
                  .acoes a { color: #2563eb; margin-right: 10px; }
                  form.inline { display: inline; }
                </style>
                </head>
                <body>
                <main>
                <h1>Cadastro de Produtos</h1>
                """);

        if (erro != null && !erro.isBlank()) {
            h.append("<p class=\"msg erro\">").append(esc(erro)).append("</p>\n");
        }
        if (ok != null && !ok.isBlank()) {
            h.append("<p class=\"msg ok\">").append(esc(ok)).append("</p>\n");
        }

        // Formulário (serve para cadastrar e para editar)
        h.append("<section class=\"cartao\">\n");
        h.append("<h2>").append(editar ? "Editar produto #" + editando.getId() : "Novo produto").append("</h2>\n");
        h.append("<form method=\"post\" action=\"").append(editar ? "/atualizar" : "/cadastrar").append("\">\n");
        if (editar) {
            h.append("<input type=\"hidden\" name=\"id\" value=\"").append(editando.getId()).append("\">\n");
        }
        campo(h, "Nome", "nome", "text", editar ? editando.getNome() : "", "required maxlength=\"100\"");
        campo(h, "Descrição", "descricao", "text",
                editar && editando.getDescricao() != null ? editando.getDescricao() : "", "maxlength=\"255\"");
        campo(h, "Preço (R$)", "preco", "number",
                editar ? String.valueOf(editando.getPreco()) : "", "required step=\"0.01\" min=\"0\"");
        campo(h, "Quantidade", "quantidade", "number",
                editar ? String.valueOf(editando.getQuantidade()) : "", "required step=\"1\" min=\"0\"");
        h.append("<div class=\"botoes\">");
        h.append("<button type=\"submit\">").append(editar ? "Atualizar" : "Cadastrar").append("</button>");
        if (editar) {
            h.append("<a class=\"botao-sec\" href=\"/\">Cancelar</a>");
        }
        h.append("</div>\n</form>\n</section>\n");

        // Tabela de produtos
        h.append("<section class=\"cartao\">\n<h2>Produtos cadastrados</h2>\n");
        if (produtos.isEmpty()) {
            h.append("<p>Nenhum produto cadastrado.</p>\n");
        } else {
            h.append("<div class=\"rolagem\"><table>\n");
            h.append("<tr><th>ID</th><th>Nome</th><th>Descrição</th><th>Preço</th><th>Qtd</th><th>Ações</th></tr>\n");
            for (Produto p : produtos) {
                h.append("<tr>");
                h.append("<td>").append(p.getId()).append("</td>");
                h.append("<td>").append(esc(p.getNome())).append("</td>");
                h.append("<td>").append(esc(p.getDescricao())).append("</td>");
                h.append("<td>R$ ").append(String.format(Locale.of("pt", "BR"), "%.2f", p.getPreco())).append("</td>");
                h.append("<td>").append(p.getQuantidade()).append("</td>");
                h.append("<td class=\"acoes\">");
                h.append("<a href=\"/?editar=").append(p.getId()).append("\">Editar</a>");
                h.append("<form class=\"inline\" method=\"post\" action=\"/excluir\" ")
                        .append("onsubmit=\"return confirm('Deseja realmente excluir este produto?');\">");
                h.append("<input type=\"hidden\" name=\"id\" value=\"").append(p.getId()).append("\">");
                h.append("<button class=\"perigo\" type=\"submit\">Excluir</button>");
                h.append("</form></td>");
                h.append("</tr>\n");
            }
            h.append("</table></div>\n");
        }
        h.append("</section>\n</main>\n</body>\n</html>\n");
        return h.toString();
    }

    private static void campo(StringBuilder h, String rotulo, String nome, String tipo, String valor, String extra) {
        h.append("<label for=\"").append(nome).append("\">").append(rotulo).append("</label>\n");
        h.append("<input id=\"").append(nome).append("\" name=\"").append(nome).append("\" type=\"").append(tipo)
                .append("\" value=\"").append(esc(valor)).append("\" ").append(extra).append(">\n");
    }

    // ---------- Utilitários HTTP ----------

    private static String lerCorpo(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> lerParametros(String texto) {
        Map<String, String> mapa = new HashMap<>();
        if (texto == null || texto.isBlank()) {
            return mapa;
        }
        for (String par : texto.split("&")) {
            String[] partes = par.split("=", 2);
            String chave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length > 1 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            mapa.put(chave, valor);
        }
        return mapa;
    }

    private static void responder(HttpExchange ex, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = ex.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private static void redirecionar(HttpExchange ex, String destino) throws IOException {
        ex.getResponseHeaders().set("Location", destino);
        ex.sendResponseHeaders(303, -1);
        ex.close();
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto == null ? "" : texto, StandardCharsets.UTF_8);
    }

    /** Escapa caracteres especiais para que o texto digitado não vire código HTML. */
    private static String esc(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}