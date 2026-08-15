package br.edu.ifsul.venancio.tenisshop.model.domain;

/**
 * Representa a entidade Categoria no sistema TenisShop.
 * Contém os atributos, construtores, getters e setters.
 *
 * @author Geovane Griesang
 */
public class Categoria {
    private Integer id;
    private String nome;
    private Boolean ativo;

    public Categoria() {
    }

    public Categoria(Integer id, String nome, Boolean ativo) {
        this.id = id;
        this.nome = nome;
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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}
