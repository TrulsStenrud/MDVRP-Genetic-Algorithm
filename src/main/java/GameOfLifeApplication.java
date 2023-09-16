import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameOfLifeApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(GameOfLifeApplication.class.getResource("sample.fxml"));

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(fxmlLoader.load(), 1400, 1000));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
