package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.AuditoriaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.ConfiguracaoSistemaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.UsuarioDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.AcaoAuditoria;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller da tela de login do TenisShop (padrão MVC: esta classe é o
 * Controller, o login.fxml é a View, e Usuario/UsuarioDAO são o Model).
 * Além de autenticar, aplica duas regras de validação segura de dados:
 * nunca revelar se foi o e-mail ou a senha que errou (evita que alguém
 * descubra, por tentativa e erro, quais e-mails têm conta no sistema), e
 * nunca comparar a senha em texto puro (isso é feito dentro de
 * UsuarioDAO.autenticar, usando o hash BCrypt).
 *
 * @author Geovane Griesang
 */
public class LoginController {

    // Verificação simples de formato "algo@algo.algo"; não confirma que o
    // domínio existe de verdade. Validar e-mail por completo com regex é
    // notoriamente difícil, então esta checagem só evita erros óbvios de
    // digitação antes de gastar uma consulta no banco.
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField pwdSenha;

    @FXML
    private Hyperlink lnkCadastrar;

    @FXML
    private void initialize() {
        // O link de autocadastro só aparece se a configuração do sistema
        // permitir; se a consulta falhar (banco fora do ar), o link fica
        // escondido por padrão: mais seguro esconder na dúvida.
        boolean permitido = false;
        try {
            permitido = Boolean.TRUE.equals(new ConfiguracaoSistemaDAO().buscar().getPermitirAutocadastro());
        } catch (SQLException e) {
            System.err.println("[ERRO] Não foi possível consultar a configuração de autocadastro: " + e.getMessage());
        }
        lnkCadastrar.setVisible(permitido);
        lnkCadastrar.setManaged(permitido);
    }

    @FXML
    private void btnLoginAction(ActionEvent event) throws IOException {
        if (!validaForm()) {
            return;
        }

        String email = txtEmail.getText().trim();
        String senhaDigitada = pwdSenha.getText();

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        try {
            Usuario usuarioAutenticado = usuarioDAO.autenticar(email, senhaDigitada);

            if (usuarioAutenticado != null) {
                TenisShop.usuarioLogado = usuarioAutenticado;
                new AuditoriaDAO().registrar(usuarioAutenticado.getId(), AcaoAuditoria.LOGIN_SUCESSO,
                        "USUARIO", usuarioAutenticado.getId(), null);

                // Senha definida por um admin (ou a semente do sistema)
                // ainda não foi trocada: bloqueia o acesso até a troca.
                if (Boolean.TRUE.equals(usuarioAutenticado.getDeveTrocarSenha())) {
                    TenisShop.setRoot("alterar-senha");
                } else {
                    TenisShop.setRoot("principal");
                }
            } else {
                // Mesmo registro de auditoria do sucesso, só com usuarioId
                // nulo e o e-mail tentado no detalhe: mantém as duas
                // respostas simétricas em custo, sem reabrir o mesmo
                // side-channel de tempo que UsuarioDAO.autenticar já fecha.
                new AuditoriaDAO().registrar(null, AcaoAuditoria.LOGIN_FALHA,
                        null, null, "Tentativa de login com e-mail: " + email);

                // Mensagem única de propósito: separar "e-mail não
                // encontrado" de "senha incorreta" permitiria descobrir,
                // por tentativa e erro, quais e-mails têm cadastro.
                showAlert("E-mail ou senha inválidos.", AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Não foi possível conectar ao banco de dados. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void irParaEsqueciSenha(ActionEvent event) throws IOException {
        TenisShop.setRoot("esqueci-senha");
    }

    @FXML
    private void irParaCadastro(ActionEvent event) throws IOException {
        TenisShop.setRoot("cadastro");
    }

    private boolean validaForm() {
        if (txtEmail.getText().isBlank()) {
            showAlert("Preencha o campo E-mail!", AlertType.ERROR);
            txtEmail.requestFocus();
            return false;
        }

        if (!FORMATO_EMAIL.matcher(txtEmail.getText().trim()).matches()) {
            showAlert("Digite um e-mail em um formato válido (ex.: nome@dominio.com).", AlertType.ERROR);
            txtEmail.requestFocus();
            return false;
        }

        if (pwdSenha.getText().isBlank()) {
            showAlert("Preencha o campo Senha!", AlertType.ERROR);
            pwdSenha.requestFocus();
            return false;
        }

        return true;
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Login");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
