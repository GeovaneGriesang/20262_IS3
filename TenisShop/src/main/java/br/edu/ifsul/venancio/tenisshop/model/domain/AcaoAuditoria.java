package br.edu.ifsul.venancio.tenisshop.model.domain;

/**
 * Ações do sistema que ficam registradas na trilha de auditoria
 * (tabela auditoria). Assim como Perfil, o valor é salvo no banco via
 * name(): um Enum garante, em tempo de compilação, que só estas ações
 * existem, em vez de strings soltas espalhadas pelos Controllers.
 *
 * @author Geovane Griesang
 */
public enum AcaoAuditoria {
    USUARIO_CRIADO_POR_ADMIN,
    AUTOCADASTRO_REALIZADO,
    USUARIO_ATUALIZADO,
    USUARIO_DESATIVADO,
    USUARIO_REATIVADO,
    SENHA_ALTERADA,
    LOGIN_SUCESSO,
    LOGIN_FALHA,
    CONFIGURACAO_SISTEMA_ALTERADA
}
