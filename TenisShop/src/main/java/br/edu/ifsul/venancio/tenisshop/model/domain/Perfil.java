package br.edu.ifsul.venancio.tenisshop.model.domain;

/**
 * Representa os níveis de acesso disponíveis para um Usuario no sistema TenisShop.
 * Usar um Enum em vez de uma String solta garante, em tempo de compilação,
 * que só estes dois valores existem; ver a tabela de níveis de acesso na
 * Aula 04 para o que cada um pode fazer.
 *
 * @author Geovane Griesang
 */
public enum Perfil {

    /** Acesso completo: cadastra usuários, configura o sistema, vê relatórios gerenciais. */
    ADMINISTRADOR,

    /** Acesso operacional: usa as funcionalidades do dia a dia, sem tarefas administrativas. */
    OPERADOR
}
