package br.edu.ifsul.venancio.tenisshop.model.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe utilitária para gerar o hash de senhas com o algoritmo SHA-256.
 * Uso didático: sistemas em produção devem usar um algoritmo pensado para
 * senhas (BCrypt, Argon2, PBKDF2) combinado com "salt" por usuário.
 *
 * @author Geovane Griesang
 */
public class SenhaUtil {

    /**
     * Gera o hash SHA-256 de uma senha em texto puro, em hexadecimal.
     * @param senhaTextoPuro senha digitada pelo usuário
     * @return String hash em hexadecimal (64 caracteres)
     */
    public static String gerarHash(String senhaTextoPuro) {
        try {
            MessageDigest digestor = MessageDigest.getInstance("SHA-256");
            byte[] bytesHash = digestor.digest(senhaTextoPuro.getBytes());

            StringBuilder hexadecimal = new StringBuilder();
            for (byte b : bytesHash) {
                hexadecimal.append(String.format("%02x", b));
            }
            return hexadecimal.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[ERRO] Algoritmo SHA-256 indisponível.", e);
        }
    }
}
