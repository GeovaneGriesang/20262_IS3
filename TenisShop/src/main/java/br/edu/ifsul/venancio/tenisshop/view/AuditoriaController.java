package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.AuditoriaDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.Auditoria;
import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller da tela administrativa de auditoria: somente leitura, mostra
 * o que AuditoriaDAO.registrar() já vem acumulando desde cada ponto de
 * escrita instrumentado do sistema (cadastro/edição de usuário, troca de
 * senha, login com sucesso ou falha, autocadastro, alteração de
 * configuração). Não existe filtro ou busca nesta primeira versão — só a
 * listagem completa, mais recente primeiro.
 *
 * @author Geovane Griesang
 */
public class AuditoriaController {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    private TableView<Auditoria> tblAuditoria;

    @FXML
    private TableColumn<Auditoria, String> colDataHora;

    @FXML
    private TableColumn<Auditoria, String> colUsuario;

    @FXML
    private TableColumn<Auditoria, String> colAcao;

    @FXML
    private TableColumn<Auditoria, String> colEntidade;

    @FXML
    private TableColumn<Auditoria, String> colDetalhe;

    @FXML
    private void initialize() throws IOException {
        if (TenisShop.usuarioLogado == null || TenisShop.usuarioLogado.getPerfil() != Perfil.ADMINISTRADOR) {
            TenisShop.setRoot("principal");
            return;
        }

        colDataHora.setCellValueFactory(dados ->
                new SimpleStringProperty(dados.getValue().getDataHora().format(FORMATO_DATA_HORA)));
        colUsuario.setCellValueFactory(dados ->
                new SimpleStringProperty(dados.getValue().getNomeUsuario() != null ? dados.getValue().getNomeUsuario() : "—"));
        colAcao.setCellValueFactory(dados ->
                new SimpleStringProperty(dados.getValue().getAcao().name()));
        colEntidade.setCellValueFactory(dados -> {
            Auditoria evento = dados.getValue();
            if (evento.getEntidade() == null) {
                return new SimpleStringProperty("—");
            }
            String texto = evento.getEntidadeId() != null
                    ? evento.getEntidade() + " #" + evento.getEntidadeId()
                    : evento.getEntidade();
            return new SimpleStringProperty(texto);
        });
        colDetalhe.setCellValueFactory(dados ->
                new SimpleStringProperty(dados.getValue().getDetalhe() != null ? dados.getValue().getDetalhe() : "—"));

        try {
            tblAuditoria.setItems(FXCollections.observableArrayList(new AuditoriaDAO().listarTodos()));
        } catch (SQLException e) {
            showAlert("Não foi possível carregar a auditoria. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void irParaPrincipal(ActionEvent event) throws IOException {
        TenisShop.setRoot("principal");
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Auditoria do Sistema");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
