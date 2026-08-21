package br.edu.ifsul.venancio.tenisshop.model.domain;

import java.time.LocalDateTime;

/**
 * Representa um evento registrado na trilha de auditoria do sistema:
 * quem fez, o quê, em qual registro e quando. nomeUsuario não é uma
 * coluna própria da tabela auditoria — vem de um LEFT JOIN com usuarios
 * em AuditoriaDAO.listarTodos(), só para a tela não precisar de uma
 * segunda consulta para mostrar o nome de quem agiu.
 *
 * @author Geovane Griesang
 */
public class Auditoria {
    private Integer id;
    private Integer usuarioId;
    private String nomeUsuario;
    private AcaoAuditoria acao;
    private String entidade;
    private Integer entidadeId;
    private String detalhe;
    private LocalDateTime dataHora;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public AcaoAuditoria getAcao() {
        return acao;
    }

    public void setAcao(AcaoAuditoria acao) {
        this.acao = acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public void setEntidade(String entidade) {
        this.entidade = entidade;
    }

    public Integer getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(Integer entidadeId) {
        this.entidadeId = entidadeId;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public void setDetalhe(String detalhe) {
        this.detalhe = detalhe;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
