package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.AuditoriaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.UsuarioDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.AcaoAuditoria;
import br.edu.ifsul.venancio.tenisshop.model.util.FotoUsuarioUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Controller da tela de autosserviço "Meu Perfil" (Aula 09): permite à
 * pessoa logada escolher, trocar ou remover sua própria foto de perfil.
 * Diferente de EditarUsuarioController (tela administrativa, que edita o
 * cadastro de qualquer usuário e usa controle de concorrência otimista,
 * Aula 10), aqui só o próprio usuário mexe no próprio registro, e só no
 * campo foto, então UsuarioDAO.atualizarFoto() não precisa checar versão.
 *
 * O botão Escolher Foto só troca a pré-visualização (imgFoto) e guarda o
 * arquivo escolhido em arquivoFotoSelecionada; nada é copiado para a
 * pasta fotos_usuarios/ nem gravado no banco até o clique em Salvar. Isso
 * evita deixar arquivos "perdidos" na pasta caso o usuário desista e
 * clique em Voltar.
 *
 * @author Geovane Griesang
 */
public class MeuPerfilController {

    private static final ExtensionFilter FILTRO_IMAGENS =
            new ExtensionFilter("Imagens (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");

    @FXML
    private ImageView imgFoto;

    private String fotoAtual;
    private File arquivoFotoSelecionada;
    private boolean removerFotoPendente;

    @FXML
    private void initialize() {
        fotoAtual = TenisShop.usuarioLogado.getFoto();
        exibirFoto(fotoAtual);
    }

    @FXML
    private void btnEscolherFotoAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolher Foto de Perfil");
        fileChooser.getExtensionFilters().add(FILTRO_IMAGENS);

        File arquivoEscolhido = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (arquivoEscolhido == null) {
            return;
        }

        arquivoFotoSelecionada = arquivoEscolhido;
        removerFotoPendente = false;
        imgFoto.setImage(new Image(arquivoEscolhido.toURI().toString()));
    }

    @FXML
    private void btnRemoverFotoAction(ActionEvent event) {
        arquivoFotoSelecionada = null;
        removerFotoPendente = true;
        imgFoto.setImage(null);
    }

    @FXML
    private void btnSalvarAction(ActionEvent event) throws IOException {
        if (!removerFotoPendente && arquivoFotoSelecionada == null) {
            TenisShop.setRoot("principal");
            return;
        }

        int usuarioId = TenisShop.usuarioLogado.getId();

        try {
            String fotoFinal;
            String detalhe;

            if (removerFotoPendente) {
                FotoUsuarioUtil.excluir(fotoAtual);
                fotoFinal = null;
                detalhe = "Foto de perfil removida.";
            } else {
                fotoFinal = FotoUsuarioUtil.salvar(usuarioId, arquivoFotoSelecionada);
                detalhe = "Foto de perfil atualizada.";
            }

            new UsuarioDAO().atualizarFoto(usuarioId, fotoFinal);
            new AuditoriaDAO().registrar(usuarioId, AcaoAuditoria.FOTO_ALTERADA, "USUARIO", usuarioId, detalhe);
            TenisShop.usuarioLogado.setFoto(fotoFinal);

            showAlert("Foto de perfil atualizada com sucesso!", AlertType.INFORMATION);
            TenisShop.setRoot("principal");
        } catch (SQLException e) {
            showAlert("Não foi possível salvar a foto. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void btnVoltarAction(ActionEvent event) throws IOException {
        TenisShop.setRoot("principal");
    }

    private void exibirFoto(String nomeArquivo) {
        if (nomeArquivo == null) {
            imgFoto.setImage(null);
            return;
        }

        Path caminho = FotoUsuarioUtil.caminhoCompleto(nomeArquivo);
        if (!Files.exists(caminho)) {
            imgFoto.setImage(null);
            return;
        }

        imgFoto.setImage(new Image(caminho.toUri().toString()));
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Meu Perfil");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
