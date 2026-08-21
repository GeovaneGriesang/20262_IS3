package br.edu.ifsul.venancio.tenisshop.model.util;

import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;

/**
 * Classe utilitária que compara o estado de um registro antes e depois
 * de uma edição, e descreve em texto exatamente o que mudou: campo a
 * campo, com o valor antigo e o novo. É o que permite à trilha de
 * auditoria (Aula 06) responder não só "o que foi alterado", mas
 * "para que valor mudou".
 *
 * A ideia é inspirada no padrão de projeto <strong>Memento</strong>
 * (GoF, categoria comportamental): guardar o estado de um objeto num
 * momento específico, sem que esse "retrato" seja alterado enquanto o
 * objeto original continua mudando. Aqui, o parâmetro "antes" é
 * exatamente esse retrato: o estado do usuário no instante em que a
 * tela de edição foi aberta (ver EditarUsuarioController), guardado
 * intacto enquanto o formulário monta um objeto "depois" separado com
 * os dados novos. O Memento clássico do GoF também sabe <em>restaurar</em>
 * esse estado antigo automaticamente (um "desfazer"); esta versão é
 * simplificada, só para exibição (ninguém aqui clica em "desfazer" e
 * volta o banco ao estado anterior), mas a ideia central (capturar uma
 * fotografia do estado para consultar depois, sem misturar com o estado
 * atual) é a mesma.
 *
 * A senha nunca entra em nenhuma comparação feita por esta classe, de
 * propósito: nem o hash antigo nem o novo devem aparecer em auditoria.
 *
 * @author Geovane Griesang
 */
public class AuditoriaUtil {

    /**
     * Compara dois estados de um mesmo usuário e descreve, campo a
     * campo, o que mudou entre eles (nome, e-mail, perfil, ativo, mas
     * nunca a senha).
     * @param antes estado do usuário antes da edição (o "retrato" tirado ao abrir a tela)
     * @param depois estado do usuário com os dados novos do formulário, ainda não salvos
     * @return String descrição legível de cada campo alterado, ou uma mensagem indicando que nada mudou
     */
    public static String descreverAlteracoesUsuario(Usuario antes, Usuario depois) {
        StringBuilder descricao = new StringBuilder();

        compararCampo(descricao, "nome", antes.getNome(), depois.getNome());
        compararCampo(descricao, "email", antes.getEmail(), depois.getEmail());
        compararCampo(descricao, "perfil", antes.getPerfil(), depois.getPerfil());
        compararCampo(descricao, "ativo", formatarAtivo(antes.getAtivo()), formatarAtivo(depois.getAtivo()));

        return descricao.length() > 0 ? descricao.toString() : "Nenhum campo alterado.";
    }

    /**
     * Descreve a alteração de um único valor booleano nomeado (ex.: uma
     * configuração do sistema), no mesmo formato usado para os campos de
     * descreverAlteracoesUsuario.
     * @param nomeCampo nome do campo alterado, para aparecer na descrição
     * @param valorAntigo valor antes da alteração
     * @param valorNovo valor depois da alteração
     * @return String descrição legível da alteração, ou uma mensagem indicando que nada mudou
     */
    public static String descreverAlteracaoBooleana(String nomeCampo, boolean valorAntigo, boolean valorNovo) {
        if (valorAntigo == valorNovo) {
            return "Nenhuma alteração.";
        }
        return nomeCampo + ": " + formatarAtivo(valorAntigo) + " -> " + formatarAtivo(valorNovo);
    }

    private static void compararCampo(StringBuilder descricao, String nomeCampo, Object valorAntigo, Object valorNovo) {
        boolean mudou = valorAntigo == null ? valorNovo != null : !valorAntigo.equals(valorNovo);
        if (!mudou) {
            return;
        }
        if (descricao.length() > 0) {
            descricao.append("; ");
        }
        descricao.append(nomeCampo).append(": \"").append(valorAntigo).append("\" -> \"").append(valorNovo).append("\"");
    }

    private static String formatarAtivo(boolean valor) {
        return valor ? "Sim" : "Não";
    }
}
