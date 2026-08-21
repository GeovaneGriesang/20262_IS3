package br.edu.ifsul.venancio.tenisshop.model.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Classe utilitária para gerar e conferir tokens de recuperação de senha.
 * Diferente de SenhaUtil (BCrypt, feito para senhas escolhidas por
 * pessoas, de baixa entropia), aqui o token já nasce aleatório e de alta
 * entropia (gerado por SecureRandom), então não precisa de um hash lento
 * e salgado: um SHA-256 comum é suficiente e, por ser determinístico,
 * permite buscar o token no banco com "WHERE token_hash = ?" — algo que
 * um hash BCrypt (salgado, diferente a cada chamada) não permite.
 *
 * @author Geovane Griesang
 */
public class TokenUtil {

    private static final SecureRandom GERADOR_ALEATORIO = new SecureRandom();

    /**
     * Gera um token aleatório de recuperação de senha, pronto para ser
     * enviado por e-mail em texto puro (só o hash dele é salvo no banco).
     * @return String token com 256 bits de entropia, em Base64 URL-safe
     */
    public static String gerarToken() {
        byte[] bytesAleatorios = new byte[32];
        GERADOR_ALEATORIO.nextBytes(bytesAleatorios);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytesAleatorios);
    }

    /**
     * Calcula o hash SHA-256 de um token em texto puro, para salvar ou
     * conferir contra o valor guardado no banco de dados.
     * @param tokenTextoPuro token gerado por gerarToken() ou digitado pelo usuário
     * @return String hash SHA-256 em hexadecimal minúsculo (64 caracteres)
     */
    public static String hash(String tokenTextoPuro) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(tokenTextoPuro.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexadecimal = new StringBuilder();
            for (byte b : digest) {
                hexadecimal.append(String.format("%02x", b));
            }
            return hexadecimal.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 é obrigatório em toda implementação padrão da JVM;
            // isto nunca deveria acontecer de verdade.
            throw new IllegalStateException("[ERRO] Algoritmo SHA-256 indisponível na JVM.", e);
        }
    }
}
