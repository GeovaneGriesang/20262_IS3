package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.AuditoriaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.ConfiguracaoSistemaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.UsuarioDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.AcaoAuditoria;
import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import br.edu.ifsul.venancio.tenisshop.model.util.NivelForcaSenha;
import br.edu.ifsul.venancio.tenisshop.model.util.SenhaUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller do autocadastro público. Deliberadamente NÃO existe nenhum
 * campo de perfil aqui: toda conta criada por esta tela nasce como
 * Perfil.OPERADOR, fixo no código; só a tela administrativa
 * (CadastrarUsuarioController) pode criar um Administrador. A tela é
 * bloqueada duas vezes contra a configuração "permitir autocadastro": o
 * link para chegar aqui já some no login quando está desligada, e o
 * initialize() confere de novo, caso alguém desligue a configuração
 * enquanto esta tela já estiver aberta em outra sessão.
 *
 * @author Geovane Griesang
 */
public class CadastroController {

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField pwdSenha;

    @FXML
    private PasswordField pwdConfirmarSenha;

    @FXML
    private Label lblForcaSenha;

    @FXML
    private Button btnCadastrar;

    @FXML
    private void initialize() throws IOException {
        try {
            boolean permitido = Boolean.TRUE.equals(new ConfiguracaoSistemaDAO().buscar().getPermitirAutocadastro());
            if (!permitido) {
                showAlert("O autocadastro está desativado no momento. Procure um administrador para criar sua conta.", AlertType.INFORMATION);
                TenisShop.setRoot("login");
                return;
            }
        } catch (SQLException e) {
            showAlert("Não foi possível conectar ao banco de dados. Verifique se o MySQL está ativo.", AlertType.ERROR);
            TenisShop.setRoot("login");
            return;
        }

        pwdSenha.textProperty().addListener((obs, valorAntigo, senha) -> {
            atualizarMedidorForca(senha);
            atualizarEstadoBotaoCadastrar();
        });
        pwdConfirmarSenha.textProperty().addListener((obs, valorAntigo, valorNovo) -> atualizarEstadoBotaoCadastrar());
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

    private void atualizarEstadoBotaoCadastrar() {
        boolean senhaForte = SenhaUtil.atendeCriteriosMinimos(pwdSenha.getText());
        boolean confirmacaoBate = pwdSenha.getText().equals(pwdConfirmarSenha.getText());
        btnCadastrar.setDisable(!(senhaForte && confirmacaoBate));
    }

    @FXML
    private void btnCadastrarAction(ActionEvent event) throws IOException {
        if (!validaForm()) {
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        String email = txtEmail.getText().trim();

        try {
            // Diferente do login (que nunca revela se um e-mail existe),
            // um formulário público de cadastro dizer "este e-mail já
            // está em uso" é o padrão esperado em qualquer site; a
            // alternativa (fingir sucesso) só confunde quem já tem conta.
            if (usuarioDAO.existeEmail(email)) {
                showAlert("Já existe uma conta cadastrada com este e-mail.", AlertType.ERROR);
                return;
            }

            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(txtNome.getText().trim());
            novoUsuario.setEmail(email);
            novoUsuario.setSenha(SenhaUtil.gerarHash(pwdSenha.getText()));
            novoUsuario.setPerfil(Perfil.OPERADOR);
            novoUsuario.setAtivo(true);
            novoUsuario.setDeveTrocarSenha(false);

            usuarioDAO.inserir(novoUsuario);

            new AuditoriaDAO().registrar(novoUsuario.getId(), AcaoAuditoria.AUTOCADASTRO_REALIZADO,
                    "USUARIO", novoUsuario.getId(), null);

            showAlert("Conta criada com sucesso! Faça login com seu e-mail e senha.", AlertType.INFORMATION);
            TenisShop.setRoot("login");
        } catch (SQLException e) {
            showAlert("Não foi possível criar sua conta. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void irParaLogin(ActionEvent event) throws IOException {
        TenisShop.setRoot("login");
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

        if (!SenhaUtil.atendeCriteriosMinimos(pwdSenha.getText())) {
            showAlert("A senha precisa ter no mínimo 8 caracteres, com maiúscula, minúscula, número e símbolo.", AlertType.ERROR);
            pwdSenha.requestFocus();
            return false;
        }

        if (!pwdSenha.getText().equals(pwdConfirmarSenha.getText())) {
            showAlert("A confirmação não confere com a senha.", AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Criar Conta");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
