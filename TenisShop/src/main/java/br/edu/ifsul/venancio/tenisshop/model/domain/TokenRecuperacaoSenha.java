package br.edu.ifsul.venancio.tenisshop.model.domain;

import java.time.LocalDateTime;

/**
 * Representa um token de recuperação de senha emitido para um Usuario.
 * O token em si (a string enviada por e-mail) nunca é guardado aqui nem no
 * banco: apenas o seu hash SHA-256 (tokenHash), para que um vazamento do
 * banco de dados não permita a ninguém usar tokens ainda válidos.
 *
 * @author Geovane Griesang
 */
public class TokenRecuperacaoSenha {
    private Integer id;
    private Integer usuarioId;
    private String tokenHash;
    private LocalDateTime dataExpiracao;
    private Boolean usado;
    private LocalDateTime dataCriacao;

    public TokenRecuperacaoSenha() {
    }

    public TokenRecuperacaoSenha(Integer id, Integer usuarioId, String tokenHash, LocalDateTime dataExpiracao, Boolean usado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.dataExpiracao = dataExpiracao;
        this.usado = usado;
    }

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

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
