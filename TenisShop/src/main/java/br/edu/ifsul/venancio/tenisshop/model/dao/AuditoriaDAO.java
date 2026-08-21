package br.edu.ifsul.venancio.tenisshop.model.dao;

import br.edu.ifsul.venancio.tenisshop.model.domain.AcaoAuditoria;
import br.edu.ifsul.venancio.tenisshop.model.domain.Auditoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável pelo acesso a dados da trilha de auditoria. Chamada
 * pelos Controllers logo depois de uma escrita já ter dado certo (nunca
 * pelos outros DAOs de escrita diretamente) — só o Controller sabe quem
 * é TenisShop.usuarioLogado, quem está realizando a ação.
 *
 * @author Geovane Griesang
 */
public class AuditoriaDAO {

    /**
     * Registra um evento na trilha de auditoria.
     * @param usuarioId id de quem realizou a ação, ou null (ex.: tentativa de login com e-mail sem conta)
     * @param acao ação realizada
     * @param entidade nome da entidade/tabela afetada, ou null se não houver uma
     * @param entidadeId id do registro afetado, ou null se não houver um
     * @param detalhe texto livre com contexto adicional, ou null
     * @throws SQLException caso ocorra falha na inserção
     */
    public void registrar(Integer usuarioId, AcaoAuditoria acao, String entidade, Integer entidadeId, String detalhe) throws SQLException {
        String sql = "INSERT INTO auditoria (usuario_id, acao, entidade, entidade_id, detalhe) VALUES (?, ?, ?, ?, ?)";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            if (usuarioId != null) {
                stmt.setInt(1, usuarioId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, acao.name());
            if (entidade != null) {
                stmt.setString(3, entidade);
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }
            if (entidadeId != null) {
                stmt.setInt(4, entidadeId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            if (detalhe != null) {
                stmt.setString(5, detalhe);
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Lista todos os eventos de auditoria, do mais recente para o mais
     * antigo. Sem paginação — mesmo padrão simples usado no restante do
     * sistema (ver listarTodos() de UsuarioDAO).
     * @return List<Auditoria> eventos registrados
     * @throws SQLException caso ocorra falha na consulta
     */
    public List<Auditoria> listarTodos() throws SQLException {
        List<Auditoria> eventos = new ArrayList<>();
        String sql = "SELECT a.id, a.usuario_id, u.nome AS nome_usuario, a.acao, a.entidade, a.entidade_id, a.detalhe, a.data_hora "
                + "FROM auditoria a LEFT JOIN usuarios u ON u.id = a.usuario_id "
                + "ORDER BY a.data_hora DESC";

        Connection conexao = ConexaoBanco.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Auditoria evento = new Auditoria();
                evento.setId(rs.getInt("id"));

                int usuarioId = rs.getInt("usuario_id");
                evento.setUsuarioId(rs.wasNull() ? null : usuarioId);

                evento.setNomeUsuario(rs.getString("nome_usuario"));
                evento.setAcao(AcaoAuditoria.valueOf(rs.getString("acao")));
                evento.setEntidade(rs.getString("entidade"));

                int entidadeId = rs.getInt("entidade_id");
                evento.setEntidadeId(rs.wasNull() ? null : entidadeId);

                evento.setDetalhe(rs.getString("detalhe"));
                evento.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
                eventos.add(evento);
            }
        }
        return eventos;
    }
}
