import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MDVRPApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(MDVRPApplication.class.getResource("sample.fxml"));

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(fxmlLoader.load(), 1400, 1000));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
