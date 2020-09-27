import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SapphireGUI extends Application
{

    public static void launchGUI(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Sapphire Extract");
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();

        try { root = loader.load(getClass().getResource("/FileBrowser.fxml")); }
        catch (IOException e) { e.printStackTrace(); }

        /*
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);*/

        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
}
