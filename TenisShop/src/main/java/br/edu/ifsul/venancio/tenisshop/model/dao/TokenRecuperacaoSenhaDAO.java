package br.edu.ifsul.venancio.tenisshop.model.dao;

import br.edu.ifsul.venancio.tenisshop.model.domain.TokenRecuperacaoSenha;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Classe responsável pelo acesso a dados dos tokens de recuperação de
 * senha. Só o hash SHA-256 do token (nunca o token em texto puro) passa
 * por esta classe — ver TokenUtil.
 *
 * @author Geovane Griesang
 */
public class TokenRecuperacaoSenhaDAO {

    /**
     * Cria um novo token de recuperação para um usuário, invalidando antes
     * qualquer token anterior ainda válido do mesmo usuário (garante que
     * só o link/token mais recente enviado por e-mail funciona).
     * @param usuarioId id do usuário para quem o token foi gerado
     * @param tokenHash hash SHA-256 do token (gerado com TokenUtil.hash)
     * @param dataExpiracao momento em que o token deixa de ser válido
     * @throws SQLException caso ocorra falha na operação
     */
    public void criar(int usuarioId, String tokenHash, LocalDateTime dataExpiracao) throws SQLException {
        invalidarTokensDoUsuario(usuarioId);

        String sqlInserir = "INSERT INTO tokens_recuperacao_senha (usuario_id, token_hash, data_expiracao) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = ConexaoBanco.getConexao().prepareStatement(sqlInserir)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, tokenHash);
            stmt.setTimestamp(3, Timestamp.valueOf(dataExpiracao));
            stmt.executeUpdate();
        }
    }

    /**
     * Invalida qualquer token de recuperação ainda válido de um usuário.
     * Chamado tanto ao criar um token novo (só o mais recente deve
     * funcionar) quanto sempre que a senha do usuário muda por qualquer
     * outro caminho (UsuarioDAO.alterarSenha) — sem isso, um token antigo
     * ainda dentro do prazo de validade continuaria funcionando mesmo
     * depois da pessoa já ter trocado a senha por conta própria.
     * @param usuarioId id do usuário cujos tokens serão invalidados
     * @throws SQLException caso ocorra falha na operação
     */
    public void invalidarTokensDoUsuario(int usuarioId) throws SQLException {
        String sql = "UPDATE tokens_recuperacao_senha SET usado = TRUE WHERE usuario_id = ? AND usado = FALSE";
        try (PreparedStatement stmt = ConexaoBanco.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        }
    }

    /**
     * Busca um token válido (ainda não usado e ainda não expirado) pelo
     * seu hash. Uma única consulta já garante as duas regras de segurança
     * (uso único e expiração) — se não achar nada, o token não pode ser
     * aceito, seja porque não existe, já foi usado, ou já expirou.
     * @param tokenHash hash SHA-256 do token digitado pelo usuário
     * @return TokenRecuperacaoSenha token válido encontrado, ou null
     * @throws SQLException caso ocorra falha na consulta
     */
    public TokenRecuperacaoSenha buscarValidoPorHash(String tokenHash) throws SQLException {
        String sql = "SELECT id, usuario_id, token_hash, data_expiracao, usado "
                + "FROM tokens_recuperacao_senha "
                + "WHERE token_hash = ? AND usado = FALSE AND data_expiracao > NOW()";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, tokenHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
                    token.setId(rs.getInt("id"));
                    token.setUsuarioId(rs.getInt("usuario_id"));
                    token.setTokenHash(rs.getString("token_hash"));
                    token.setDataExpiracao(rs.getTimestamp("data_expiracao").toLocalDateTime());
                    token.setUsado(rs.getBoolean("usado"));
                    return token;
                }
            }
        }
        return null;
    }

    /**
     * Marca um token como usado, tornando-o inválido para qualquer
     * tentativa futura de redefinição de senha.
     * @param tokenId id do token a marcar como usado
     * @throws SQLException caso ocorra falha na atualização
     */
    public void marcarComoUsado(int tokenId) throws SQLException {
        String sql = "UPDATE tokens_recuperacao_senha SET usado = TRUE WHERE id = ?";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, tokenId);
            stmt.executeUpdate();
        }
    }
}
