package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.AuditoriaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.UsuarioDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.AcaoAuditoria;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import br.edu.ifsul.venancio.tenisshop.model.util.NivelForcaSenha;
import br.edu.ifsul.venancio.tenisshop.model.util.SenhaUtil;
import java.io.IOException;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

/**
 * Controller da tela de troca de senha. Atende dois casos com a mesma
 * tela: a troca voluntária (usuário já logado decide trocar a senha) e a
 * troca obrigatória de primeiro acesso (usuarioLogado.getDeveTrocarSenha()
 * é true logo após o login); neste segundo caso, o campo de senha atual
 * some, porque o usuário já provou quem é ao autenticar nesta mesma
 * sessão, e o botão Cancelar some, porque a troca não pode ser adiada.
 *
 * @author Geovane Griesang
 */
public class AlterarSenhaController {

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblExplicacao;

    @FXML
    private VBox boxSenhaAtual;

    @FXML
    private PasswordField pwdSenhaAtual;

    @FXML
    private PasswordField pwdNovaSenha;

    @FXML
    private PasswordField pwdConfirmarSenha;

    @FXML
    private Label lblForcaSenha;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnCancelar;

    private boolean trocaObrigatoria;

    @FXML
    private void initialize() {
        trocaObrigatoria = TenisShop.usuarioLogado != null
                && Boolean.TRUE.equals(TenisShop.usuarioLogado.getDeveTrocarSenha());

        if (trocaObrigatoria) {
            lblTitulo.setText("Troca de Senha Obrigatória");
            lblExplicacao.setText("Sua senha foi definida por um administrador (ou é a senha padrão do sistema). Antes de continuar, defina uma nova senha só sua.");
            boxSenhaAtual.setVisible(false);
            boxSenhaAtual.setManaged(false);
            btnCancelar.setVisible(false);
            btnCancelar.setManaged(false);
        }

        pwdNovaSenha.textProperty().addListener((obs, valorAntigo, senha) -> {
            atualizarMedidorForca(senha);
            atualizarEstadoBotaoSalvar();
        });
        pwdConfirmarSenha.textProperty().addListener((obs, valorAntigo, valorNovo) -> atualizarEstadoBotaoSalvar());
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

    private void atualizarEstadoBotaoSalvar() {
        boolean senhaForte = SenhaUtil.atendeCriteriosMinimos(pwdNovaSenha.getText());
        boolean confirmacaoBate = pwdNovaSenha.getText().equals(pwdConfirmarSenha.getText());
        btnSalvar.setDisable(!(senhaForte && confirmacaoBate));
    }

    @FXML
    private void btnSalvarAction(ActionEvent event) throws IOException {
        Usuario usuarioLogado = TenisShop.usuarioLogado;
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        if (!trocaObrigatoria) {
            try {
                if (usuarioDAO.autenticar(usuarioLogado.getEmail(), pwdSenhaAtual.getText()) == null) {
                    showAlert("Senha atual incorreta.", AlertType.ERROR);
                    return;
                }
            } catch (SQLException e) {
                showAlert("Não foi possível conectar ao banco de dados. Verifique se o MySQL está ativo.", AlertType.ERROR);
                return;
            }
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
            String novoHash = SenhaUtil.gerarHash(pwdNovaSenha.getText());
            usuarioDAO.alterarSenha(usuarioLogado.getId(), novoHash, false);
            usuarioLogado.setDeveTrocarSenha(false);

            new AuditoriaDAO().registrar(usuarioLogado.getId(), AcaoAuditoria.SENHA_ALTERADA, "USUARIO",
                    usuarioLogado.getId(), trocaObrigatoria ? "Troca obrigatória (primeiro acesso)." : "Troca voluntária.");

            showAlert("Senha alterada com sucesso!", AlertType.INFORMATION);
            TenisShop.setRoot("principal");
        } catch (SQLException e) {
            showAlert("Não foi possível salvar a nova senha. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void btnCancelarAction(ActionEvent event) throws IOException {
        TenisShop.setRoot("principal");
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Alterar Senha");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
