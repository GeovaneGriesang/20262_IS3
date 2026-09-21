package br.edu.ifsul.venancio.tenisshop.model.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Classe utilitária responsável por guardar, no sistema de arquivos, a
 * foto de perfil de cada usuário (ver MeuPerfilController). O banco de
 * dados nunca guarda a foto em si, só o nome do arquivo (coluna
 * usuarios.foto, Aula 09): a foto de verdade fica numa pasta própria,
 * fora do controle de versão (ver .gitignore), com o nome do arquivo
 * sendo o id do usuário mais a extensão original (ex.: "7.png").
 *
 * Guardar arquivos grandes direto no banco (numa coluna BLOB) funciona,
 * mas deixa o banco pesado e lento para fazer backup; guardar só o
 * "endereço" do arquivo e deixar o sistema de arquivos cuidar dos bytes
 * é a prática mais comum, inclusive fora de aplicações Java.
 *
 * @author Geovane Griesang
 */
public class FotoUsuarioUtil {

    private static final Path PASTA_FOTOS = Paths.get("fotos_usuarios");

    /**
     * Copia o arquivo escolhido pelo usuário para a pasta de fotos,
     * nomeando-o com o id do usuário e a extensão original. Se já existir
     * uma foto anterior desse usuário (mesma ou outra extensão), ela é
     * apagada antes, para não acumular arquivos órfãos.
     * @param usuarioId id do usuário dono da foto
     * @param arquivoOrigem arquivo escolhido no seletor de arquivos (FileChooser)
     * @return String nome do arquivo salvo (ex.: "7.png"), para gravar em usuarios.foto
     * @throws IOException caso ocorra falha ao criar a pasta ou copiar o arquivo
     */
    public static String salvar(int usuarioId, File arquivoOrigem) throws IOException {
        Files.createDirectories(PASTA_FOTOS);
        removerFotosExistentes(usuarioId);

        String nomeOriginal = arquivoOrigem.getName();
        String extensao = nomeOriginal.contains(".")
                ? nomeOriginal.substring(nomeOriginal.lastIndexOf('.'))
                : "";
        String nomeArquivo = usuarioId + extensao;

        Files.copy(arquivoOrigem.toPath(), PASTA_FOTOS.resolve(nomeArquivo), StandardCopyOption.REPLACE_EXISTING);
        return nomeArquivo;
    }

    /**
     * Remove o arquivo de foto informado, se ele existir. Não faz nada
     * (sem lançar erro) se o nome for null ou o arquivo já não existir.
     * @param nomeArquivo nome do arquivo salvo por salvar(), ou null
     * @throws IOException caso ocorra falha ao apagar o arquivo
     */
    public static void excluir(String nomeArquivo) throws IOException {
        if (nomeArquivo == null) {
            return;
        }
        Files.deleteIfExists(PASTA_FOTOS.resolve(nomeArquivo));
    }

    /**
     * Resolve o caminho completo de um arquivo de foto, para carregá-lo
     * na tela (ex.: new Image(caminhoCompleto(usuario.getFoto()).toUri().toString())).
     * @param nomeArquivo nome do arquivo salvo por salvar()
     * @return Path caminho completo do arquivo na pasta de fotos
     */
    public static Path caminhoCompleto(String nomeArquivo) {
        return PASTA_FOTOS.resolve(nomeArquivo);
    }

    private static void removerFotosExistentes(int usuarioId) throws IOException {
        if (!Files.isDirectory(PASTA_FOTOS)) {
            return;
        }
        String prefixo = usuarioId + ".";
        try (DirectoryStream<Path> arquivos = Files.newDirectoryStream(PASTA_FOTOS, prefixo + "*")) {
            for (Path arquivo : arquivos) {
                Files.deleteIfExists(arquivo);
            }
        }
    }
}
