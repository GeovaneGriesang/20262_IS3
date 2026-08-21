package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.AuditoriaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.TokenRecuperacaoSenhaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.UsuarioDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.AcaoAuditoria;
import br.edu.ifsul.venancio.tenisshop.model.domain.TokenRecuperacaoSenha;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import br.edu.ifsul.venancio.tenisshop.model.util.NivelForcaSenha;
import br.edu.ifsul.venancio.tenisshop.model.util.SenhaUtil;
import br.edu.ifsul.venancio.tenisshop.model.util.TokenUtil;
import java.io.IOException;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller do segundo passo da recuperação de senha: conferir o código
 * recebido por e-mail e salvar a nova senha. Assim como no primeiro
 * passo, o erro é sempre a mesma mensagem genérica ("código inválido ou
 * expirado"), sem distinguir se o e-mail não existe, o código é de outra
 * conta, já foi usado ou já expirou; cada uma dessas distinções daria a
 * quem está tentando adivinhar uma pista a mais do que deveria ter.
 *
 * @author Geovane Griesang
 */
public class RedefinirSenhaController {

    private static final String MENSAGEM_ERRO_GENERICA = "Código inválido ou expirado. Solicite um novo em \"Esqueci minha senha\".";

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtToken;

    @FXML
    private PasswordField pwdNovaSenha;

    @FXML
    private PasswordField pwdConfirmarSenha;

    @FXML
    private Label lblForcaSenha;

    @FXML
    private Button btnRedefinir;

    @FXML
    private void initialize() {
        pwdNovaSenha.textProperty().addListener((obs, valorAntigo, senha) -> {
            atualizarMedidorForca(senha);
            atualizarEstadoBotaoRedefinir();
        });
        pwdConfirmarSenha.textProperty().addListener((obs, valorAntigo, valorNovo) -> atualizarEstadoBotaoRedefinir());
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

    private void atualizarEstadoBotaoRedefinir() {
        boolean senhaForte = SenhaUtil.atendeCriteriosMinimos(pwdNovaSenha.getText());
        boolean confirmacaoBate = pwdNovaSenha.getText().equals(pwdConfirmarSenha.getText());
        btnRedefinir.setDisable(!(senhaForte && confirmacaoBate));
    }

    @FXML
    private void btnRedefinirAction(ActionEvent event) throws IOException {
        if (txtEmail.getText().isBlank() || txtToken.getText().isBlank()) {
            showAlert("Preencha o e-mail e o código recebido.", AlertType.ERROR);
            return;
        }

        if (!SenhaUtil.atendeCriteriosMinimos(pwdNovaSenha.getText())) {
            showAlert("A nova senha precisa ter no mínimo 8 caracteres, com maiúscula, minúscula, número e símbolo.", AlertType.ERROR);
            return;
        }

        if (!pwdNovaSenha.getText().equals(pwdConfirmarSenha.getText())) {
            showAlert("A confirmação não confere com a nova senha.", AlertType.ERROR);
            return;
        }

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.buscarPorEmail(txtEmail.getText().trim());

            String tokenHash = TokenUtil.hash(txtToken.getText().trim());
            TokenRecuperacaoSenhaDAO tokenDAO = new TokenRecuperacaoSenhaDAO();
            TokenRecuperacaoSenha token = tokenDAO.buscarValidoPorHash(tokenHash);

            // Mesma mensagem genérica nos três casos: sem usuário, sem
            // token, ou token de outra conta; nunca dizer qual deles foi.
            if (usuario == null || token == null || !token.getUsuarioId().equals(usuario.getId())) {
                showAlert(MENSAGEM_ERRO_GENERICA, AlertType.ERROR);
                return;
            }

            String novoHash = SenhaUtil.gerarHash(pwdNovaSenha.getText());
            usuarioDAO.alterarSenha(usuario.getId(), novoHash, false);
            tokenDAO.marcarComoUsado(token.getId());

            new AuditoriaDAO().registrar(usuario.getId(), AcaoAuditoria.SENHA_ALTERADA, "USUARIO",
                    usuario.getId(), "Senha redefinida via link de recuperação por e-mail.");

            showAlert("Senha redefinida com sucesso! Faça login com sua nova senha.", AlertType.INFORMATION);
            TenisShop.setRoot("login");
        } catch (SQLException e) {
            showAlert("Não foi possível redefinir sua senha. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void irParaLogin(ActionEvent event) throws IOException {
        TenisShop.setRoot("login");
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Redefinir Senha");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
