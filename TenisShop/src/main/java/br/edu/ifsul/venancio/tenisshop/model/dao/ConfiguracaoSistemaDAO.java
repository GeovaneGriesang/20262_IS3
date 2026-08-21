package br.edu.ifsul.venancio.tenisshop.model.dao;

import br.edu.ifsul.venancio.tenisshop.model.domain.ConfiguracaoSistema;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Classe responsável pelo acesso a dados da configuração global do sistema.
 * Sempre existe exatamente uma linha (id = 1) nesta tabela (ver
 * SchemaInitializer, que garante isso na inicialização da aplicação).
 *
 * @author Geovane Griesang
 */
public class ConfiguracaoSistemaDAO {

    /**
     * Busca a configuração atual do sistema.
     * @return ConfiguracaoSistema configuração vigente
     * @throws SQLException caso ocorra falha na consulta, ou a linha (id = 1) não exista
     */
    public ConfiguracaoSistema buscar() throws SQLException {
        String sql = "SELECT id, permitir_autocadastro FROM configuracao_sistema WHERE id = 1";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                ConfiguracaoSistema configuracao = new ConfiguracaoSistema();
                configuracao.setId(rs.getInt("id"));
                configuracao.setPermitirAutocadastro(rs.getBoolean("permitir_autocadastro"));
                return configuracao;
            }
        }
        throw new SQLException("[ERRO] Configuração do sistema (id = 1) não encontrada. Verifique se SchemaInitializer rodou corretamente.");
    }

    /**
     * Atualiza se o autocadastro público de novos usuários está permitido.
     * @param permitirAutocadastro true para permitir que qualquer pessoa se cadastre (sempre como OPERADOR)
     * @throws SQLException caso ocorra falha na atualização
     */
    public void atualizar(boolean permitirAutocadastro) throws SQLException {
        String sql = "UPDATE configuracao_sistema SET permitir_autocadastro = ? WHERE id = 1";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setBoolean(1, permitirAutocadastro);
            stmt.executeUpdate();
        }
    }
}
