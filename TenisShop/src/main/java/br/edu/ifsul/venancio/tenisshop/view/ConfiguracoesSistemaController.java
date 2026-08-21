package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.AuditoriaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.ConfiguracaoSistemaDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.AcaoAuditoria;
import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import br.edu.ifsul.venancio.tenisshop.model.util.AuditoriaUtil;
import java.io.IOException;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;

/**
 * Controller da tela administrativa de configurações do sistema. Hoje só
 * tem uma configuração (permitir autocadastro), mas já existe como tela
 * separada para comportar novas configurações no futuro sem precisar
 * redesenhar nada.
 *
 * @author Geovane Griesang
 */
public class ConfiguracoesSistemaController {

    @FXML
    private CheckBox chkPermitirAutocadastro;

    private boolean permitirAutocadastroOriginal;

    @FXML
    private void initialize() throws IOException {
        if (TenisShop.usuarioLogado == null || TenisShop.usuarioLogado.getPerfil() != Perfil.ADMINISTRADOR) {
            TenisShop.setRoot("principal");
            return;
        }

        try {
            permitirAutocadastroOriginal = Boolean.TRUE.equals(new ConfiguracaoSistemaDAO().buscar().getPermitirAutocadastro());
            chkPermitirAutocadastro.setSelected(permitirAutocadastroOriginal);
        } catch (SQLException e) {
            showAlert("Não foi possível carregar as configurações. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void btnSalvarAction(ActionEvent event) throws IOException {
        try {
            boolean permitirAutocadastroNovo = chkPermitirAutocadastro.isSelected();
            new ConfiguracaoSistemaDAO().atualizar(permitirAutocadastroNovo);

            String detalhe = AuditoriaUtil.descreverAlteracaoBooleana("permitir_autocadastro",
                    permitirAutocadastroOriginal, permitirAutocadastroNovo);
            new AuditoriaDAO().registrar(TenisShop.usuarioLogado.getId(), AcaoAuditoria.CONFIGURACAO_SISTEMA_ALTERADA,
                    "CONFIGURACAO_SISTEMA", 1, detalhe);

            showAlert("Configurações salvas com sucesso!", AlertType.INFORMATION);
            TenisShop.setRoot("principal");
        } catch (SQLException e) {
            showAlert("Não foi possível salvar as configurações. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void btnVoltarAction(ActionEvent event) throws IOException {
        TenisShop.setRoot("principal");
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Configurações do Sistema");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
