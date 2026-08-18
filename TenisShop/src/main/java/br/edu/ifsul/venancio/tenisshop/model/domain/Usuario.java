package br.edu.ifsul.venancio.tenisshop.model.domain;

import java.time.LocalDateTime;

/**
 * Representa a entidade Usuário no sistema TenisShop.
 * Contém os atributos, construtores, getters e setters.
 *
 * @author Geovane Griesang
 */
public class Usuario {
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private Perfil perfil;
    private Boolean ativo;
    private LocalDateTime dataCadastro;

    public Usuario() {
    }

    public Usuario(Integer id, String nome, String email, String senha, Perfil perfil, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @Override
    public String toString() {
        return this.nome + " (" + this.perfil + ")";
    }
}
