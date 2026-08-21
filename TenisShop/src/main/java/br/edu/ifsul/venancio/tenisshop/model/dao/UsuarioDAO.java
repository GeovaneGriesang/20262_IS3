package br.edu.ifsul.venancio.tenisshop.model.dao;

import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import br.edu.ifsul.venancio.tenisshop.model.util.SenhaUtil;
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

    // Hash "de mentira", calculado uma única vez, usado só para gastar o
    // mesmo tempo de um BCrypt.checkpw() de verdade quando o e-mail nem
    // existe (ver autenticar()); sem isso, uma resposta rápida demais já
    // denunciaria que o e-mail não tem conta, mesmo com a mensagem de
    // erro sendo idêntica nos dois casos.
    private static final String HASH_FICTICIO = SenhaUtil.gerarHash("senha-usada-so-para-igualar-o-tempo-de-resposta");

    /**
     * Lista todos os usuários cadastrados no banco de dados.
     * @return List<Usuario> lista de usuários encontrados
     * @throws SQLException caso ocorra falha na consulta
     */
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nome, email, senha, perfil, ativo, deve_trocar_senha, versao FROM usuarios ORDER BY nome";

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
        String sql = "SELECT id, nome, email, senha, perfil, ativo, deve_trocar_senha, versao FROM usuarios WHERE email = ?";

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

    /**
     * Autentica um usuário conferindo a senha digitada contra o hash BCrypt
     * armazenado no banco; a senha em si nunca é comparada diretamente, e
     * usuários inativos não conseguem autenticar mesmo com a senha correta.
     * Mesmo quando o e-mail não existe, um BCrypt.checkpw() é executado
     * assim mesmo (contra um hash fixo, sem relação com o e-mail
     * digitado): o BCrypt é lento de propósito, então pular essa conta
     * faria a resposta voltar visivelmente mais rápido para e-mails sem
     * conta, um jeito de descobrir quais e-mails têm cadastro só
     * cronometrando a resposta, mesmo com a mensagem de erro sendo
     * idêntica nos dois casos (ver LoginController).
     * @param email e-mail digitado no login
     * @param senhaDigitada senha em texto puro digitada no login
     * @return Usuario autenticado, ou null se e-mail, senha ou status não conferirem
     * @throws SQLException caso ocorra falha na consulta
     */
    public Usuario autenticar(String email, String senhaDigitada) throws SQLException {
        Usuario usuario = buscarPorEmail(email);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            SenhaUtil.verificar(senhaDigitada, HASH_FICTICIO);
            return null;
        }

        if (SenhaUtil.verificar(senhaDigitada, usuario.getSenha())) {
            return usuario;
        }
        return null;
    }

    /**
     * Busca um usuário pelo id.
     * @param id id do usuário buscado
     * @return Usuario encontrado, ou null se não existir
     * @throws SQLException caso ocorra falha na consulta
     */
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, email, senha, perfil, ativo, deve_trocar_senha, versao FROM usuarios WHERE id = ?";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * Confere se já existe um usuário cadastrado com o e-mail informado.
     * Usado tanto pelo cadastro feito pelo administrador quanto pelo
     * autocadastro, para recusar e-mails duplicados antes de tentar o INSERT.
     * @param email e-mail a conferir
     * @return true se já existe um usuário com esse e-mail
     * @throws SQLException caso ocorra falha na consulta
     */
    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE email = ? LIMIT 1";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Insere um novo usuário no banco de dados. A senha já deve chegar
     * como hash BCrypt (gerado com SenhaUtil.gerarHash); este método
     * nunca hasheia nem valida a senha, só persiste o que recebeu.
     * @param usuario usuário a inserir; após a chamada, usuario.getId() é preenchido com o id gerado
     * @throws SQLException caso ocorra falha na inserção
     */
    public void inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, email, senha, perfil, ativo, deve_trocar_senha) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getPerfil().name());
            stmt.setBoolean(5, Boolean.TRUE.equals(usuario.getAtivo()));
            stmt.setBoolean(6, Boolean.TRUE.equals(usuario.getDeveTrocarSenha()));
            stmt.executeUpdate();

            try (ResultSet chavesGeradas = stmt.getGeneratedKeys()) {
                if (chavesGeradas.next()) {
                    usuario.setId(chavesGeradas.getInt(1));
                }
            }
        }
    }

    /**
     * Atualiza a senha (já como hash BCrypt) de um usuário existente, e
     * define se ele deverá trocá-la novamente no próximo login. Usado
     * tanto pela troca de senha comum quanto pela redefinição via token.
     * Qualquer token de recuperação ainda válido do usuário é invalidado
     * junto; sem isso, um token antigo (de um e-mail de recuperação
     * anterior) continuaria funcionando mesmo depois da senha já ter
     * mudado por outro caminho, dentro da janela de 30 minutos.
     * @param usuarioId id do usuário
     * @param novoHash novo hash BCrypt da senha (gerado com SenhaUtil.gerarHash)
     * @param deveTrocarSenha true para exigir nova troca no próximo login
     * @throws SQLException caso ocorra falha na atualização
     */
    public void alterarSenha(int usuarioId, String novoHash, boolean deveTrocarSenha) throws SQLException {
        String sql = "UPDATE usuarios SET senha = ?, deve_trocar_senha = ? WHERE id = ?";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, novoHash);
            stmt.setBoolean(2, deveTrocarSenha);
            stmt.setInt(3, usuarioId);
            stmt.executeUpdate();
        }

        new TokenRecuperacaoSenhaDAO().invalidarTokensDoUsuario(usuarioId);
    }

    /**
     * Atualiza os dados cadastrais (nome, e-mail, perfil, ativo, mas nunca
     * a senha, que só é trocada por alterarSenha) de um usuário existente,
     * usando controle de concorrência otimista: o UPDATE só é aplicado se
     * a versão informada em usuario.getVersao() ainda for a versão atual
     * no banco, e a versão é incrementada junto. Se outra atualização já
     * tiver acontecido entre a leitura e esta chamada (outra pessoa editou
     * o mesmo usuário nesse meio-tempo), nenhuma linha é afetada.
     * @param usuario usuário com os dados novos e a versão que foi lida antes de editar
     * @return true se a atualização foi aplicada; false se houve conflito de versão
     * @throws SQLException caso ocorra falha na atualização
     */
    public boolean atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nome = ?, email = ?, perfil = ?, ativo = ?, versao = versao + 1 "
                + "WHERE id = ? AND versao = ?";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getPerfil().name());
            stmt.setBoolean(4, Boolean.TRUE.equals(usuario.getAtivo()));
            stmt.setInt(5, usuario.getId());
            stmt.setInt(6, usuario.getVersao());
            return stmt.executeUpdate() > 0;
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setPerfil(Perfil.valueOf(rs.getString("perfil")));
        usuario.setAtivo(rs.getBoolean("ativo"));
        usuario.setDeveTrocarSenha(rs.getBoolean("deve_trocar_senha"));
        usuario.setVersao(rs.getInt("versao"));
        return usuario;
    }
}
