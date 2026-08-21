package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.AuditoriaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.UsuarioDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.AcaoAuditoria;
import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import br.edu.ifsul.venancio.tenisshop.model.util.NivelForcaSenha;
import br.edu.ifsul.venancio.tenisshop.model.util.SenhaUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller da tela administrativa de cadastro de usuários. É a única
 * tela do sistema em que o perfil de acesso pode ser escolhido livremente
 * (inclusive ADMINISTRADOR); o autocadastro público (CadastroController)
 * nunca oferece essa opção, sempre cria OPERADOR.
 *
 * @author Geovane Griesang
 */
public class CadastrarUsuarioController {

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtEmail;

    @FXML
    private ComboBox<Perfil> cmbPerfil;

    @FXML
    private PasswordField pwdSenhaTemporaria;

    @FXML
    private Label lblForcaSenha;

    @FXML
    private CheckBox chkForcarTrocaSenha;

    @FXML
    private Button btnCadastrar;

    @FXML
    private void initialize() throws IOException {
        // Guarda dupla: o botão de acesso a esta tela já só aparece para
        // administradores em principal.fxml, mas o Controller confere de
        // novo antes de mostrar qualquer coisa; nunca confiar só na UI.
        if (TenisShop.usuarioLogado == null || TenisShop.usuarioLogado.getPerfil() != Perfil.ADMINISTRADOR) {
            TenisShop.setRoot("principal");
            return;
        }

        cmbPerfil.setItems(FXCollections.observableArrayList(Perfil.values()));
        cmbPerfil.getSelectionModel().select(Perfil.OPERADOR);

        pwdSenhaTemporaria.textProperty().addListener((obs, valorAntigo, senha) -> {
            atualizarMedidorForca(senha);
            btnCadastrar.setDisable(!SenhaUtil.atendeCriteriosMinimos(senha));
        });
    }

    private void atualizarMedidorForca(String senha) {
        NivelForcaSenha nivel = SenhaUtil.avaliarForca(senha);
        switch (nivel) {
            case FRACA -> {
                lblForcaSenha.setText("Força: fraca");
                lblForcaSenha.setStyle("-fx-font-size: 11px; -fx-text-fill: #cd191e;");
            }
            case MEDIA -> {
                lblForcaSenha.setText("Força: média");
                lblForcaSenha.setStyle("-fx-font-size: 11px; -fx-text-fill: #b36b00;");
            }
            case FORTE -> {
                lblForcaSenha.setText("Força: forte");
                lblForcaSenha.setStyle("-fx-font-size: 11px; -fx-text-fill: #1f6b2c;");
            }
        }
    }

    @FXML
    private void btnCadastrarAction(ActionEvent event) throws IOException {
        if (!validaForm()) {
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        String email = txtEmail.getText().trim();

        try {
            if (usuarioDAO.existeEmail(email)) {
                showAlert("Já existe um usuário cadastrado com este e-mail.", AlertType.ERROR);
                return;
            }

            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(txtNome.getText().trim());
            novoUsuario.setEmail(email);
            novoUsuario.setSenha(SenhaUtil.gerarHash(pwdSenhaTemporaria.getText()));
            novoUsuario.setPerfil(cmbPerfil.getValue());
            novoUsuario.setAtivo(true);
            novoUsuario.setDeveTrocarSenha(chkForcarTrocaSenha.isSelected());

            usuarioDAO.inserir(novoUsuario);

            new AuditoriaDAO().registrar(TenisShop.usuarioLogado.getId(), AcaoAuditoria.USUARIO_CRIADO_POR_ADMIN,
                    "USUARIO", novoUsuario.getId(), "Usuário \"" + novoUsuario.getNome() + "\" criado com perfil " + novoUsuario.getPerfil() + ".");

            showAlert("Usuário \"" + novoUsuario.getNome() + "\" cadastrado com sucesso!\n\n"
                    + "Anote a senha temporária agora, ela não pode ser recuperada depois:\n"
                    + pwdSenhaTemporaria.getText(), AlertType.INFORMATION);
            TenisShop.setRoot("principal");
        } catch (SQLException e) {
            showAlert("Não foi possível cadastrar o usuário. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void btnCancelarAction(ActionEvent event) throws IOException {
        TenisShop.setRoot("principal");
    }

    private boolean validaForm() {
        if (txtNome.getText().isBlank()) {
            showAlert("Preencha o campo Nome!", AlertType.ERROR);
            txtNome.requestFocus();
            return false;
        }

        if (txtEmail.getText().isBlank() || !FORMATO_EMAIL.matcher(txtEmail.getText().trim()).matches()) {
            showAlert("Digite um e-mail em um formato válido (ex.: nome@dominio.com).", AlertType.ERROR);
            txtEmail.requestFocus();
            return false;
        }

        if (cmbPerfil.getValue() == null) {
            showAlert("Selecione o perfil de acesso!", AlertType.ERROR);
            return false;
        }

        if (!SenhaUtil.atendeCriteriosMinimos(pwdSenhaTemporaria.getText())) {
            showAlert("A senha temporária precisa ter no mínimo 8 caracteres, com maiúscula, minúscula, número e símbolo.", AlertType.ERROR);
            pwdSenhaTemporaria.requestFocus();
            return false;
        }

        return true;
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Cadastrar Usuário");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
