package br.edu.ifsul.venancio.tenisshop;

import br.edu.ifsul.venancio.tenisshop.model.dao.ConexaoBanco;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe de inicialização e teste de infraestrutura da aplicação TenisShop.
 *
 * @author Geovane Griesang
 */
public class TenisShop {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   Iniciando Aplicação TenisShop         ");
        System.out.println("=========================================");

        try {
            Connection conn = ConexaoBanco.getConexao();
            if (conn != null && !conn.isClosed()) {
                System.out.println(">>> Teste de Conexão: SUCESSO!");
            }
        } catch (SQLException e) {
            System.err.println(">>> Teste de Conexão: FALHA!");
            System.err.println("Detalhes do erro: " + e.getMessage());
        } finally {
            ConexaoBanco.fecharConexao();
        }
    }
}
