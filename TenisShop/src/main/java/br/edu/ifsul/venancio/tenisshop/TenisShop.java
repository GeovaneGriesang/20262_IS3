package br.edu.ifsul.venancio.tenisshop;

import br.edu.ifsul.venancio.tenisshop.model.domain.Usuario;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"));
        stage.setTitle("TenisShop");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Troca a tela atual sem abrir uma janela nova: os Controllers chamam
     * este método (ex.: TenisShop.setRoot("principal")) para navegar.
     * @param fxml nome do arquivo FXML, sem a extensão (ex.: "login")
     * @throws IOException se o FXML não existir ou tiver erro de carga
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TenisShop.class.getResource("view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
