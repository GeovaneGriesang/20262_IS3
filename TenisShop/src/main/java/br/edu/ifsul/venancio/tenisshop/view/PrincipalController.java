package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Controller da tela inicial exibida após o login no TenisShop.
 * Tela provisória: as telas reais de cadastro chegam a partir de 23/10/2026,
 * conforme o cronograma da Aula 01; por enquanto ela confirma que o login
 * está funcionando e dá o primeiro exemplo real de nível de acesso, um
 * elemento da tela que só aparece para o perfil ADMINISTRADOR.
 *
 * @author Geovane Griesang
 */
public class PrincipalController {

    @FXML
    private Label lblBoasVindas;

    @FXML
    private Label lblPerfil;

    @FXML
    private VBox boxAreaAdmin;

    @FXML
    private void initialize() {
        if (TenisShop.usuarioLogado != null) {
            lblBoasVindas.setText("Bem-vindo, " + TenisShop.usuarioLogado.getNome() + "!");
            lblPerfil.setText("Perfil: " + TenisShop.usuarioLogado.getPerfil());

            // Nível de acesso em ação: o Enum decide o que aparece na tela.
            // As telas administrativas (CadastrarUsuarioController,
            // ConfiguracoesSistemaController) repetem essa mesma checagem
            // por conta própria, para não confiar só em um botão escondido.
            boolean ehAdministrador = TenisShop.usuarioLogado.getPerfil() == Perfil.ADMINISTRADOR;
            boxAreaAdmin.setVisible(ehAdministrador);
            boxAreaAdmin.setManaged(ehAdministrador);
        }
    }

    @FXML
    private void irParaAlterarSenha(ActionEvent event) throws IOException {
        TenisShop.setRoot("alterar-senha");
    }

    @FXML
    private void irParaCadastrarUsuario(ActionEvent event) throws IOException {
        TenisShop.setRoot("cadastrar-usuario");
    }

    @FXML
    private void irParaListarUsuarios(ActionEvent event) throws IOException {
        TenisShop.setRoot("listar-usuarios");
    }

    @FXML
    private void irParaConfiguracoesSistema(ActionEvent event) throws IOException {
        TenisShop.setRoot("configuracoes-sistema");
    }

    @FXML
    private void irParaAuditoria(ActionEvent event) throws IOException {
        TenisShop.setRoot("auditoria");
    }

    @FXML
    private void sair(ActionEvent event) throws IOException {
        TenisShop.usuarioLogado = null;
        TenisShop.setRoot("login");
    }
}
