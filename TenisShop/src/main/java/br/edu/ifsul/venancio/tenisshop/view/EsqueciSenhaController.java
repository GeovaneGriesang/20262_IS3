package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.TokenRecuperacaoSenhaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.UsuarioDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import br.edu.ifsul.venancio.tenisshop.model.util.EmailUtil;
import br.edu.ifsul.venancio.tenisshop.model.util.TokenUtil;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

/**
 * Controller do primeiro passo da recuperação de senha: pedir o e-mail.
 * Ponto central de segurança desta tela: ela SEMPRE mostra a mesma
 * mensagem e SEMPRE navega para a tela seguinte, não importa se o e-mail
 * existe, se o usuário está inativo, ou se o envio do e-mail falhou;
 * exatamente a mesma lógica de "não revelar quais e-mails têm conta" já
 * usada no login (LoginController), só que aplicada também ao fluxo, não
 * só ao texto da mensagem. Ela também tenta não revelar isso pelo
 * <em>tempo</em> de resposta: sem usuário, o processamento retorna quase
 * na hora; com usuário, grava um token e manda e-mail, o que demora bem
 * mais (ver DURACAO_MINIMA_MS).
 *
 * @author Geovane Griesang
 */
public class EsqueciSenhaController {

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int VALIDADE_TOKEN_MINUTOS = 30;

    // Tempo mínimo (em milissegundos) que a tela espera antes de
    // responder, contado a partir do clique. Sem isso, um e-mail sem
    // conta responderia visivelmente mais rápido que um e-mail com conta
    // (que grava um token no banco e manda um e-mail de verdade); outro
    // jeito de descobrir, cronometrando, quais e-mails têm cadastro,
    // mesmo com a mensagem sendo idêntica nos dois casos.
    private static final long DURACAO_MINIMA_MS = 400;

    @FXML
    private TextField txtEmail;

    @FXML
    private void btnEnviarAction(ActionEvent event) throws IOException {
        String email = txtEmail.getText().trim();

        if (email.isBlank() || !FORMATO_EMAIL.matcher(email).matches()) {
            showAlert("Digite um e-mail em um formato válido (ex.: nome@dominio.com).", AlertType.ERROR);
            return;
        }

        long inicio = System.currentTimeMillis();

        // Todo o processamento fica dentro deste try isolado: qualquer
        // exceção é só registrada no console, nunca aparece pro usuário
        // nem muda a navegação (ver o porquê no Javadoc da classe).
        try {
            processarPedidoDeRecuperacao(email);
        } catch (Exception e) {
            System.err.println("[ERRO] Falha ao processar pedido de recuperação de senha: " + e.getMessage());
        }

        aguardarTempoMinimo(inicio);

        showAlert("Se este e-mail estiver cadastrado, você receberá as instruções para redefinir sua senha.", AlertType.INFORMATION);
        TenisShop.setRoot("redefinir-senha");
    }

    private void aguardarTempoMinimo(long inicioMs) {
        long decorridoMs = System.currentTimeMillis() - inicioMs;
        long faltanteMs = DURACAO_MINIMA_MS - decorridoMs;
        if (faltanteMs > 0) {
            try {
                // Trava a tela por até DURACAO_MINIMA_MS: uma simplificação
                // aceitável aqui (o clique já é uma ação que espera uma
                // pequena espera); uma versão para produção moveria
                // processarPedidoDeRecuperacao para uma Task em segundo
                // plano, para nunca travar a interface.
                Thread.sleep(faltanteMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processarPedidoDeRecuperacao(String email) throws Exception {
        Usuario usuario = new UsuarioDAO().buscarPorEmail(email);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            return;
        }

        String token = TokenUtil.gerarToken();
        String tokenHash = TokenUtil.hash(token);
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(VALIDADE_TOKEN_MINUTOS);

        new TokenRecuperacaoSenhaDAO().criar(usuario.getId(), tokenHash, dataExpiracao);

        String corpo = "Olá, " + usuario.getNome() + "!\n\n"
                + "Recebemos um pedido de redefinição de senha para sua conta no TenisShop.\n\n"
                + "Use o código abaixo na tela \"Redefinir Senha\" do sistema:\n\n"
                + token + "\n\n"
                + "Este código é válido por " + VALIDADE_TOKEN_MINUTOS + " minutos e só pode ser usado uma vez.\n"
                + "Se você não pediu essa redefinição, ignore este e-mail: sua senha continua a mesma.";

        EmailUtil.enviar(email, "TenisShop - Redefinição de senha", corpo);
    }

    @FXML
    private void irParaLogin(ActionEvent event) throws IOException {
        TenisShop.setRoot("login");
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Esqueci Minha Senha");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
