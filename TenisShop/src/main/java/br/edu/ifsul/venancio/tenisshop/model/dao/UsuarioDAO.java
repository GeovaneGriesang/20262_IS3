package br.edu.ifsul.venancio.tenisshop.model.dao;

import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável pelo acesso a dados da entidade Usuario.
 * Aplica o padrão de projeto DAO (Data Access Object).
 *
 * @author Geovane Griesang
 */
public class UsuarioDAO {

    /**
     * Lista todos os usuários cadastrados no banco de dados.
     * @return List<Usuario> lista de usuários encontrados
     * @throws SQLException caso ocorra falha na consulta
     */
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nome, email, senha, perfil, ativo FROM usuarios ORDER BY nome";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    /**
     * Busca um usuário pelo e-mail cadastrado.
     * @param email e-mail do usuário buscado
     * @return Usuario encontrado, ou null se não existir
     * @throws SQLException caso ocorra falha na consulta
     */
    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT id, nome, email, senha, perfil, ativo FROM usuarios WHERE email = ?";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setPerfil(rs.getString("perfil"));
        usuario.setAtivo(rs.getBoolean("ativo"));
        return usuario;
    }
}
