package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

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
    private Label lblAreaAdmin;

    @FXML
    private void initialize() {
        if (TenisShop.usuarioLogado != null) {
            lblBoasVindas.setText("Bem-vindo, " + TenisShop.usuarioLogado.getNome() + "!");
            lblPerfil.setText("Perfil: " + TenisShop.usuarioLogado.getPerfil());

            // Nível de acesso em ação: o Enum decide o que aparece na tela.
            // As telas reais (Controllers futuros) repetem essa mesma ideia
            // para esconder botões e menus inteiros por perfil.
            boolean ehAdministrador = TenisShop.usuarioLogado.getPerfil() == Perfil.ADMINISTRADOR;
            lblAreaAdmin.setVisible(ehAdministrador);
            lblAreaAdmin.setManaged(ehAdministrador);
        }
    }

    @FXML
    private void sair(ActionEvent event) throws IOException {
        TenisShop.usuarioLogado = null;
        TenisShop.setRoot("login");
    }
}
