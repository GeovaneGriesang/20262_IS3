package br.edu.ifsul.venancio.tenisshop.model.domain;

/**
 * Representa as configurações globais do sistema TenisShop. Existe sempre
 * uma única linha desta entidade no banco (id = 1); não há como o sistema
 * ter "duas configurações" ao mesmo tempo.
 *
 * @author Geovane Griesang
 */
public class ConfiguracaoSistema {
    private Integer id;
    private Boolean permitirAutocadastro;

    public ConfiguracaoSistema() {
    }

    public ConfiguracaoSistema(Integer id, Boolean permitirAutocadastro) {
        this.id = id;
        this.permitirAutocadastro = permitirAutocadastro;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getPermitirAutocadastro() {
        return permitirAutocadastro;
    }

    public void setPermitirAutocadastro(Boolean permitirAutocadastro) {
        this.permitirAutocadastro = permitirAutocadastro;
    }

    @Override
    public String toString() {
        return "Autocadastro: " + (Boolean.TRUE.equals(permitirAutocadastro) ? "permitido" : "bloqueado");
    }
}
