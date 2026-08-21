package br.edu.ifsul.venancio.tenisshop;

import br.edu.ifsul.venancio.tenisshop.model.dao.SchemaInitializer;
import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import br.edu.ifsul.venancio.tenisshop.model.util.EmailUtil;
import java.io.IOException;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Classe principal da aplicação TenisShop: inicializa a interface JavaFX
 * a partir da tela de login e comanda a navegação entre telas.
 * Não é o Model nem a View nem o Controller do padrão MVC; é a "casca" que
 * hospeda uma única Scene e troca só a raiz dela a cada tela, para nunca
 * abrir mais de uma janela ao mesmo tempo.
 *
 * @author Geovane Griesang
 */
public class TenisShop extends Application {

    // Guarda quem está logado para as telas seguintes consultarem; fica
    // static porque só existe uma sessão de usuário por vez nesta aplicação.
    public static Usuario usuarioLogado;

    // Guarda qual usuário está sendo editado, entre a tela de listagem e a
    // de edição: como setRoot() só recebe o nome do FXML, sem parâmetros,
    // esta é a mesma solução (campo static) já usada para usuarioLogado,
    // aplicada agora para passar dados entre duas telas administrativas.
    public static Usuario usuarioEmEdicao;

    private static Scene scene;
    private static Stage stage;

    @Override
    public void start(Stage stage) throws IOException {
        TenisShop.stage = stage;

        // Várias resoluções do mesmo ícone: o sistema operacional escolhe
        // sozinho a mais adequada para cada contexto (barra de tarefas,
        // Alt+Tab, cantinho da janela), sem esticar nem borrar a imagem.
        for (int tamanho : new int[] {16, 32, 48, 64, 128, 256}) {
            stage.getIcons().add(new Image(TenisShop.class.getResourceAsStream("icon-" + tamanho + ".png")));
        }

        try {
            // Roda antes de qualquer tela: cria tabelas/colunas que ainda
            // não existem e semeia o primeiro administrador, se o banco
            // estiver vazio. Ver SchemaInitializer para o motivo de isto
            // ser automático em vez de exigir que alguém rode SQL à mão.
            SchemaInitializer.inicializar();
        } catch (SQLException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("TenisShop");
            alert.setHeaderText(null);
            alert.setContentText("Não foi possível preparar o banco de dados. Verifique se o MySQL está ativo.\n\nDetalhes: " + e.getMessage());
            alert.showAndWait();
            return;
        }

        // Aviso único, no início: não bloqueia o uso do resto do sistema,
        // só avisa que a recuperação de senha por e-mail não vai
        // funcionar. É seguro mostrar isso aqui (mesmo aviso para
        // qualquer pessoa, antes de qualquer tela de login) porque não
        // depende de qual e-mail alguém digitou; ver o Javadoc de
        // EmailUtil.estaConfigurado() para o motivo dessa distinção.
        if (!EmailUtil.estaConfigurado()) {
            Alert aviso = new Alert(AlertType.WARNING);
            aviso.setTitle("TenisShop");
            aviso.setHeaderText(null);
            aviso.setContentText("O envio de e-mail não está configurado (arquivo email.properties ausente ou incompleto).\n\n"
                    + "A recuperação de senha por e-mail não vai funcionar até isso ser configurado; o restante do sistema funciona normalmente.\n\n"
                    + "Veja src/main/resources/email.properties.example para o modelo.");
            aviso.showAndWait();
        }

        scene = new Scene(loadFXML("login"));
        stage.setTitle("TenisShop");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    /**
     * Troca a tela atual sem abrir uma janela nova: os Controllers chamam
     * este método (ex.: TenisShop.setRoot("principal")) para navegar.
     * Cada tela tem seu próprio prefWidth/prefHeight no FXML, e o
     * sizeToScene() garante que a janela sempre reajusta para o tamanho
     * preferido da tela nova; sem ele, a janela ficaria travada no
     * tamanho da tela anterior, cortando botões de telas maiores.
     * @param fxml nome do arquivo FXML, sem a extensão (ex.: "login")
     * @throws IOException se o FXML não existir ou tiver erro de carga
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        stage.sizeToScene();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TenisShop.class.getResource("view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
