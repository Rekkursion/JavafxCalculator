package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application {
    public static Stage saveAsVarStage = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader numberPadLoader = new FXMLLoader();
        Parent root = numberPadLoader.load(getClass().getResource("number_pad.fxml").openStream());
        NumberPadController numberPadController = numberPadLoader.getController();

        primaryStage.setTitle("Javafx Calculator");
        primaryStage.setScene(new Scene(root, 600, 550));
        primaryStage.show();

        // ========================================================================================

        Parent saveAsVarRoot = FXMLLoader.load(getClass().getResource("save_as_var.fxml"));
        saveAsVarStage = new Stage();
        saveAsVarStage.setTitle("Save as variable");
        saveAsVarStage.setScene(new Scene(saveAsVarRoot, 350, 200));
        saveAsVarStage.setResizable(false);
        saveAsVarStage.initStyle(StageStyle.UTILITY);
        saveAsVarStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                numberPadController.root_pane.setDisable(false);
                if(SaveAsVarController.varName != null) {
                    numberPadController.addVariable(SaveAsVarController.varName);
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
