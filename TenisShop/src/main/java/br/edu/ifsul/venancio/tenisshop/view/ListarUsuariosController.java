package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.UsuarioDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import java.io.IOException;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller da tela administrativa de listagem de usuários — a primeira
 * TableView do projeto. Fecha uma lacuna que existia desde a Aula 04:
 * até aqui, um administrador só conseguia CRIAR usuários
 * (CadastrarUsuarioController), nunca listar ou editar um já existente.
 * Selecionar uma linha e clicar em Editar leva à tela de edição
 * (EditarUsuarioController), que é onde o controle de concorrência
 * otimista (coluna "versao") entra em ação.
 *
 * @author Geovane Griesang
 */
public class ListarUsuariosController {

    @FXML
    private TableView<Usuario> tblUsuarios;

    @FXML
    private TableColumn<Usuario, String> colNome;

    @FXML
    private TableColumn<Usuario, String> colEmail;

    @FXML
    private TableColumn<Usuario, Perfil> colPerfil;

    @FXML
    private TableColumn<Usuario, Boolean> colAtivo;

    @FXML
    private Button btnEditar;

    @FXML
    private void initialize() throws IOException {
        if (TenisShop.usuarioLogado == null || TenisShop.usuarioLogado.getPerfil() != Perfil.ADMINISTRADOR) {
            TenisShop.setRoot("principal");
            return;
        }

        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPerfil.setCellValueFactory(new PropertyValueFactory<>("perfil"));
        colAtivo.setCellValueFactory(new PropertyValueFactory<>("ativo"));
        colAtivo.setCellFactory(coluna -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Boolean ativo, boolean vazio) {
                super.updateItem(ativo, vazio);
                setText(vazio || ativo == null ? null : (ativo ? "Sim" : "Não"));
            }
        });

        btnEditar.disableProperty().bind(tblUsuarios.getSelectionModel().selectedItemProperty().isNull());

        try {
            tblUsuarios.setItems(FXCollections.observableArrayList(new UsuarioDAO().listarTodos()));
        } catch (SQLException e) {
            showAlert("Não foi possível carregar a lista de usuários. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void btnEditarAction(ActionEvent event) throws IOException {
        Usuario selecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return;
        }
        TenisShop.usuarioEmEdicao = selecionado;
        TenisShop.setRoot("editar-usuario");
    }

    @FXML
    private void irParaPrincipal(ActionEvent event) throws IOException {
        TenisShop.setRoot("principal");
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Usuários Cadastrados");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
