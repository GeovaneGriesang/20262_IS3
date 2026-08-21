package br.edu.ifsul.venancio.tenisshop.view;

import br.edu.ifsul.venancio.tenisshop.TenisShop;
import br.edu.ifsul.venancio.tenisshop.model.dao.AuditoriaDAO;
import br.edu.ifsul.venancio.tenisshop.model.dao.UsuarioDAO;
import br.edu.ifsul.venancio.tenisshop.model.domain.AcaoAuditoria;
import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import br.edu.ifsul.venancio.tenisshop.model.util.AuditoriaUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * Controller da tela administrativa de edição de um usuário existente.
 * Nunca mexe na senha (isso é exclusivo de AlterarSenhaController) e é
 * onde o controle de concorrência otimista é demonstrado na prática: o
 * usuário é recarregado do banco (nunca a partir da linha da tabela de
 * ListarUsuariosController, que pode já estar desatualizada) e a versão
 * lida nesse momento é conferida na hora de salvar, em UsuarioDAO.atualizar.
 * Se outra pessoa salvou uma mudança nesse meio-tempo, atualizar()
 * retorna false, e esta tela recarrega os dados em vez de sobrescrever
 * silenciosamente o que a outra pessoa salvou.
 *
 * O campo usuarioEditado nunca é modificado depois de carregado; ele
 * funciona como o "retrato" (memento) do estado anterior, usado só para
 * comparação em AuditoriaUtil.descreverAlteracoesUsuario(). Os dados
 * novos do formulário vão para um objeto Usuario separado, montado só
 * na hora de salvar.
 *
 * @author Geovane Griesang
 */
public class EditarUsuarioController {

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtEmail;

    @FXML
    private ComboBox<Perfil> cmbPerfil;

    @FXML
    private CheckBox chkAtivo;

    private Usuario usuarioEditado;
    private String emailOriginal;

    @FXML
    private void initialize() throws IOException {
        if (TenisShop.usuarioLogado == null || TenisShop.usuarioLogado.getPerfil() != Perfil.ADMINISTRADOR) {
            TenisShop.setRoot("principal");
            return;
        }

        if (TenisShop.usuarioEmEdicao == null) {
            TenisShop.setRoot("listar-usuarios");
            return;
        }

        cmbPerfil.setItems(FXCollections.observableArrayList(Perfil.values()));

        try {
            // Sempre busca de novo no banco, nunca confia na linha que veio
            // da tabela de ListarUsuariosController; é assim que a versão
            // conferida no Salvar reflete o estado mais recente possível.
            usuarioEditado = new UsuarioDAO().buscarPorId(TenisShop.usuarioEmEdicao.getId());
            if (usuarioEditado == null) {
                showAlert("Este usuário não existe mais.", AlertType.ERROR);
                TenisShop.setRoot("listar-usuarios");
                return;
            }

            emailOriginal = usuarioEditado.getEmail();
            txtNome.setText(usuarioEditado.getNome());
            txtEmail.setText(usuarioEditado.getEmail());
            cmbPerfil.getSelectionModel().select(usuarioEditado.getPerfil());
            chkAtivo.setSelected(Boolean.TRUE.equals(usuarioEditado.getAtivo()));
        } catch (SQLException e) {
            showAlert("Não foi possível carregar o usuário. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    @FXML
    private void btnSalvarAction(ActionEvent event) throws IOException {
        if (!validaForm()) {
            return;
        }

        String email = txtEmail.getText().trim();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        try {
            boolean emailMudou = !email.equalsIgnoreCase(emailOriginal);
            if (emailMudou && usuarioDAO.existeEmail(email)) {
                showAlert("Já existe outro usuário cadastrado com este e-mail.", AlertType.ERROR);
                return;
            }

            // usuarioEditado (carregado em initialize()) nunca é tocado
            // daqui pra frente; é o "antes" que AuditoriaUtil vai comparar
            // contra o "depois" montado abaixo. id, senha, versao e
            // deveTrocarSenha vêm copiados dele porque esta tela não edita
            // nenhum desses campos.
            Usuario usuarioComDadosNovos = new Usuario();
            usuarioComDadosNovos.setId(usuarioEditado.getId());
            usuarioComDadosNovos.setSenha(usuarioEditado.getSenha());
            usuarioComDadosNovos.setVersao(usuarioEditado.getVersao());
            usuarioComDadosNovos.setDeveTrocarSenha(usuarioEditado.getDeveTrocarSenha());
            usuarioComDadosNovos.setNome(txtNome.getText().trim());
            usuarioComDadosNovos.setEmail(email);
            usuarioComDadosNovos.setPerfil(cmbPerfil.getValue());
            usuarioComDadosNovos.setAtivo(chkAtivo.isSelected());

            boolean atualizado = usuarioDAO.atualizar(usuarioComDadosNovos);

            if (!atualizado) {
                showAlert("Este usuário foi alterado por outra pessoa desde que esta tela foi aberta.\n\n"
                        + "Os dados serão recarregados, refaça as alterações necessárias.", AlertType.ERROR);
                TenisShop.setRoot("editar-usuario");
                return;
            }

            registrarAuditoria(usuarioComDadosNovos);

            showAlert("Usuário atualizado com sucesso!", AlertType.INFORMATION);
            TenisShop.setRoot("listar-usuarios");
        } catch (SQLException e) {
            showAlert("Não foi possível salvar as alterações. Verifique se o MySQL está ativo.", AlertType.ERROR);
        }
    }

    /**
     * Registra a auditoria da edição, escolhendo a ação certa conforme o
     * que mudou: uma mudança de ativo=true para ativo=false é o mais
     * próximo que este sistema tem de "remover" um usuário (não existe
     * exclusão definitiva (ver o porquê na Aula 06), então ganha uma
     * ação própria e mais visível (USUARIO_DESATIVADO) em vez de cair no
     * genérico USUARIO_ATUALIZADO. O detalhe, nos três casos, é sempre a
     * descrição completa de tudo que mudou, não só do campo ativo.
     * @param usuarioComDadosNovos usuário já salvo, com os dados novos
     * @throws SQLException caso ocorra falha ao gravar a auditoria
     */
    private void registrarAuditoria(Usuario usuarioComDadosNovos) throws SQLException {
        String detalhe = AuditoriaUtil.descreverAlteracoesUsuario(usuarioEditado, usuarioComDadosNovos);

        boolean estavaAtivo = Boolean.TRUE.equals(usuarioEditado.getAtivo());
        boolean continuaAtivo = Boolean.TRUE.equals(usuarioComDadosNovos.getAtivo());

        AcaoAuditoria acao;
        if (estavaAtivo && !continuaAtivo) {
            acao = AcaoAuditoria.USUARIO_DESATIVADO;
        } else if (!estavaAtivo && continuaAtivo) {
            acao = AcaoAuditoria.USUARIO_REATIVADO;
        } else {
            acao = AcaoAuditoria.USUARIO_ATUALIZADO;
        }

        new AuditoriaDAO().registrar(TenisShop.usuarioLogado.getId(), acao,
                "USUARIO", usuarioComDadosNovos.getId(), detalhe);
    }

    @FXML
    private void btnCancelarAction(ActionEvent event) throws IOException {
        TenisShop.setRoot("listar-usuarios");
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

        return true;
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Editar Usuário");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
