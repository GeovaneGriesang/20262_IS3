package br.edu.ifsul.venancio.tenisshop.model.util;

import org.mindrot.jbcrypt.BCrypt;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Classe utilitária para gerar e conferir o hash de senhas com BCrypt.
 * O BCrypt embute um "salt" aleatório em cada hash gerado, por isso hashes
 * diferentes para a mesma senha são normais e esperados; a conferência é
 * feita com verificar(), nunca comparando dois hashes com equals().
 *
 * @author Geovane Griesang
 */
public class SenhaUtil {

    // Cinco critérios de senha forte: comprimento mínimo e as quatro
    // classes de caractere (minúscula, maiúscula, dígito, símbolo). O
    // símbolo é "qualquer caractere que não seja letra ou dígito", em vez
    // de uma lista fechada de pontuação, para não recusar símbolos válidos
    // que a lista esqueceu.
    private static final int TAMANHO_MINIMO = 8;

    // O BCrypt ignora, em silêncio, qualquer byte além do 72º: duas senhas
    // diferentes que só divergem depois disso gerariam o mesmo hash. Uma
    // senha "forte" de verdade também precisa respeitar esse teto; cada
    // tela com campo de senha nova deve avisar esse limite, não só o
    // mínimo (ver Aula 04, Seção 09).
    private static final int TAMANHO_MAXIMO_BYTES = 72;
    private static final Pattern TEM_MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern TEM_MAIUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern TEM_DIGITO = Pattern.compile("[0-9]");
    private static final Pattern TEM_SIMBOLO = Pattern.compile("[^A-Za-z0-9]");

    /**
     * Avalia o quão forte é uma senha em texto puro, contando quantos dos
     * cinco critérios (tamanho dentro da faixa permitida, minúscula,
     * maiúscula, dígito, símbolo) ela atende. É a mesma conta usada tanto
     * pelo medidor visual nas telas quanto por atendeCriteriosMinimos(),
     * para as duas nunca discordarem.
     * @param senha senha em texto puro digitada pelo usuário
     * @return NivelForcaSenha classificação da força da senha
     */
    public static NivelForcaSenha avaliarForca(String senha) {
        if (senha == null) {
            return NivelForcaSenha.FRACA;
        }

        boolean tamanhoValido = senha.length() >= TAMANHO_MINIMO
                && senha.getBytes(StandardCharsets.UTF_8).length <= TAMANHO_MAXIMO_BYTES;

        int criteriosAtendidos = 0;
        if (tamanhoValido) criteriosAtendidos++;
        if (TEM_MINUSCULA.matcher(senha).find()) criteriosAtendidos++;
        if (TEM_MAIUSCULA.matcher(senha).find()) criteriosAtendidos++;
        if (TEM_DIGITO.matcher(senha).find()) criteriosAtendidos++;
        if (TEM_SIMBOLO.matcher(senha).find()) criteriosAtendidos++;

        if (criteriosAtendidos >= 5) {
            return NivelForcaSenha.FORTE;
        } else if (criteriosAtendidos >= 3) {
            return NivelForcaSenha.MEDIA;
        }
        return NivelForcaSenha.FRACA;
    }

    /**
     * Verifica se uma senha atende ao mínimo exigido pelo sistema (nível
     * FORTE: 8+ caracteres, com minúscula, maiúscula, dígito e símbolo).
     * Toda tela que cadastra ou troca senha deve chamar este método antes
     * de salvar, e não confiar só no medidor visual.
     * @param senha senha em texto puro a validar
     * @return true se a senha é forte o suficiente para ser aceita
     */
    public static boolean atendeCriteriosMinimos(String senha) {
        return avaliarForca(senha) == NivelForcaSenha.FORTE;
    }

    /**
     * Gera o hash BCrypt de uma senha em texto puro, com um salt novo.
     * @param senhaTextoPuro senha digitada pelo usuário
     * @return String hash BCrypt, já incluindo o salt, pronto para salvar no banco
     */
    public static String gerarHash(String senhaTextoPuro) {
        return BCrypt.hashpw(senhaTextoPuro, BCrypt.gensalt());
    }

    /**
     * Confere se uma senha em texto puro corresponde a um hash já gerado.
     * @param senhaTextoPuro senha digitada no login
     * @param hashArmazenado hash salvo no banco de dados
     * @return true se a senha confere com o hash
     */
    public static boolean verificar(String senhaTextoPuro, String hashArmazenado) {
        return BCrypt.checkpw(senhaTextoPuro, hashArmazenado);
    }

    /**
     * Gera e imprime no console um hash válido para a senha informada (ou
     * "admin123" se nenhuma for passada). Rode esta classe diretamente
     * (Run File, no NetBeans) para obter um hash pronto para usar num
     * INSERT ou UPDATE de teste no banco.
     * @param args primeiro argumento opcional: a senha a ser hasheada
     */
    public static void main(String[] args) {
        String senha = args.length > 0 ? args[0] : "admin123";
        System.out.println(gerarHash(senha));
    }
}
