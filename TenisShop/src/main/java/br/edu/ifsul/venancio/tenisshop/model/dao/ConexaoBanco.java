package br.edu.ifsul.venancio.tenisshop.model.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Classe responsável por gerenciar e fornecer conexões com o banco de dados MySQL.
 * Aplica o padrão de projeto Singleton simples para reaproveitamento da conexão.
 * As credenciais nunca ficam no código-fonte: são lidas em tempo de execução
 * de "db.properties", um arquivo fora do controle de versão (ver
 * db.properties.example e o .gitignore do projeto); a mesma prática já usada
 * para as credenciais SMTP (ver EmailUtil, Aula 04).
 *
 * @author Geovane Griesang
 */
public class ConexaoBanco {

    private static final String ARQUIVO_CONFIGURACAO = "/db.properties";

    private static Connection conexao = null;

    /**
     * Obtém uma conexão ativa com o banco de dados.
     * @return Connection objeto de conexão SQL
     * @throws SQLException caso ocorra falha na conexão ou na leitura de db.properties
     */
    public static Connection getConexao() throws SQLException {
        try {
            if (conexao == null || conexao.isClosed()) {
                Properties configuracoes = carregarConfiguracoes();
                String url = configuracoes.getProperty("db.url");
                String usuario = configuracoes.getProperty("db.usuario");
                String senha = configuracoes.getProperty("db.senha", "");

                Class.forName("com.mysql.cj.jdbc.Driver");
                conexao = DriverManager.getConnection(url, usuario, senha);
                System.out.println("[INFO] Conexão com o banco de dados realizada com sucesso!");
            }
            return conexao;
        } catch (ClassNotFoundException e) {
            throw new SQLException("[ERRO] Driver JDBC do MySQL não foi encontrado.", e);
        } catch (IOException e) {
            throw new SQLException("[ERRO] Não foi possível ler db.properties.", e);
        }
    }

    private static Properties carregarConfiguracoes() throws IOException {
        Properties configuracoes = new Properties();
        try (InputStream entrada = ConexaoBanco.class.getResourceAsStream(ARQUIVO_CONFIGURACAO)) {
            if (entrada == null) {
                throw new IOException(
                    "[ERRO] Arquivo db.properties não encontrado em src/main/resources. "
                    + "Copie db.properties.example, renomeie para db.properties e preencha as credenciais do MySQL."
                );
            }
            configuracoes.load(entrada);
        }
        return configuracoes;
    }

    /**
     * Fecha a conexão com o banco de dados caso esteja aberta.
     */
    public static void fecharConexao() {
        if (conexao != null) {
            try {
                if (!conexao.isClosed()) {
                    conexao.close();
                    System.out.println("[INFO] Conexão com o banco de dados encerrada.");
                }
            } catch (SQLException e) {
                System.err.println("[ERRO] Falha ao fechar a conexão: " + e.getMessage());
            }
        }
    }
}
