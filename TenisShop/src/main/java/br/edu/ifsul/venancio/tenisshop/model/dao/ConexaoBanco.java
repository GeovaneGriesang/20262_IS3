package br.edu.ifsul.venancio.tenisshop.model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsável por gerenciar e fornecer conexões com o banco de dados MySQL.
 * Aplica o padrão de projeto Singleton simples para reaproveitamento da conexão.
 *
 * @author Geovane Griesang
 */
public class ConexaoBanco {

    private static final String URL = "jdbc:mysql://localhost:3306/tenisshop_db?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String SENHA = ""; // Insira a senha do MySQL do laboratório, se houver

    private static Connection conexao = null;

    /**
     * Obtém uma conexão ativa com o banco de dados.
     * @return Connection objeto de conexão SQL
     * @throws SQLException caso ocorra falha na conexão
     */
    public static Connection getConexao() throws SQLException {
        try {
            if (conexao == null || conexao.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
                System.out.println("[INFO] Conexão com o banco de dados realizada com sucesso!");
            }
            return conexao;
        } catch (ClassNotFoundException e) {
            throw new SQLException("[ERRO] Driver JDBC do MySQL não foi encontrado.", e);
        }
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
