package br.edu.ifsul.venancio.tenisshop.model.util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe utilitária para enviar e-mails via SMTP (usada pela recuperação
 * de senha). As credenciais SMTP nunca ficam no código-fonte: são lidas em
 * tempo de execução de "email.properties", um arquivo fora do controle de
 * versão (ver email.properties.example e o .gitignore do projeto) — a
 * mesma prática de nunca commitar credenciais já discutida na Aula 02.
 *
 * @author Geovane Griesang
 */
public class EmailUtil {

    private static final String ARQUIVO_CONFIGURACAO = "/email.properties";

    /**
     * Envia um e-mail em texto puro pelo servidor SMTP configurado em
     * email.properties.
     * @param destinatario e-mail de quem vai receber a mensagem
     * @param assunto assunto do e-mail
     * @param corpo corpo do e-mail, em texto puro
     * @throws MessagingException caso o envio falhe (SMTP indisponível, autenticação recusada etc.)
     * @throws IOException caso email.properties não seja encontrado ou esteja ilegível
     */
    public static void enviar(String destinatario, String assunto, String corpo) throws MessagingException, IOException {
        Properties configuracoes = carregarConfiguracoes();

        String host = configuracoes.getProperty("smtp.host");
        String porta = configuracoes.getProperty("smtp.port");
        String usuario = configuracoes.getProperty("smtp.usuario");
        String senha = configuracoes.getProperty("smtp.senha");
        String remetente = configuracoes.getProperty("smtp.from");
        String usarStarttls = configuracoes.getProperty("smtp.starttls", "true");

        Properties propriedadesSmtp = new Properties();
        propriedadesSmtp.put("mail.smtp.host", host);
        propriedadesSmtp.put("mail.smtp.port", porta);
        propriedadesSmtp.put("mail.smtp.auth", "true");
        propriedadesSmtp.put("mail.smtp.starttls.enable", usarStarttls);

        Session sessao = Session.getInstance(propriedadesSmtp, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, senha);
            }
        });

        MimeMessage mensagem = new MimeMessage(sessao);
        mensagem.setFrom(new InternetAddress(remetente));
        mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        mensagem.setSubject(assunto);
        mensagem.setText(corpo);

        Transport.send(mensagem);
    }

    private static Properties carregarConfiguracoes() throws IOException {
        Properties configuracoes = new Properties();
        try (InputStream entrada = EmailUtil.class.getResourceAsStream(ARQUIVO_CONFIGURACAO)) {
            if (entrada == null) {
                throw new IOException(
                    "[ERRO] Arquivo email.properties não encontrado em src/main/resources. "
                    + "Copie email.properties.example, renomeie para email.properties e preencha suas credenciais SMTP."
                );
            }
            configuracoes.load(entrada);
        }
        return configuracoes;
    }
}
