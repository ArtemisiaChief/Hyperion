import teacher.network.client.DedicatedServer;
import teacher.network.server.LocalServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("teacher/component/View.fxml"));
        primaryStage.setTitle("Hyperion - Teacher");
        primaryStage.setScene(new Scene(root, 590, 390));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            LocalServer.getInstance().close();
            DedicatedServer.getInstance().close();
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
