package br.edu.ifsul.venancio.tenisshop.model.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Classe utilitária para gerar e conferir o hash de senhas com BCrypt.
 * O BCrypt embute um "salt" aleatório em cada hash gerado, por isso hashes
 * diferentes para a mesma senha são normais e esperados; a conferência é
 * feita com verificar(), nunca comparando dois hashes com equals().
 *
 * @author Geovane Griesang
 */
public class SenhaUtil {

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
