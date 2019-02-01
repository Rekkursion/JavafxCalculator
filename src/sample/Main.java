package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application {
    public static Stage saveAsVarStage = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Javafx Calculator");
        primaryStage.setScene(new Scene(root, 600, 550));
        primaryStage.show();

        Parent saveAsVarRoot = FXMLLoader.load(getClass().getResource("save_as_var.fxml"));
        saveAsVarStage = new Stage();
        saveAsVarStage.setTitle("Save as variable");
        saveAsVarStage.setScene(new Scene(saveAsVarRoot, 350, 200));
        saveAsVarStage.setResizable(false);
        saveAsVarStage.initStyle(StageStyle.UTILITY);
        saveAsVarStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                System.out.println("var name stage closing");
                ((GridPane)root.lookup("#gpn_number_pad")).setDisable(false);
                //((TextField)root.lookup("#txf_show")).setText(SaveAsVarController.varName);
                if(SaveAsVarController.varName != null) {
                    // TODO
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
