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
    public static NumberPadController numberPadController = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader numberPadLoader = new FXMLLoader();
        Parent root = numberPadLoader.load(getClass().getResource("fxml/number_pad.fxml").openStream());

        root.getStylesheets().add(getClass().getResource("css/globally.css").toExternalForm());
        numberPadController = numberPadLoader.getController();

        primaryStage.setTitle("Javafx Calculator");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 676, 550));
        primaryStage.show();

        // ========================================================================================

        FXMLLoader saveAsVarLoader = new FXMLLoader();
        Parent saveAsVarRoot = saveAsVarLoader.load(getClass().getResource("fxml/save_as_var.fxml").openStream());

        saveAsVarRoot.getStylesheets().add(getClass().getResource("css/globally.css").toExternalForm());
        SaveAsVarController saveAsVarController = saveAsVarLoader.getController();

        saveAsVarStage = new Stage();
        saveAsVarStage.setTitle("Save as variable");
        saveAsVarStage.setScene(new Scene(saveAsVarRoot, 350, 200));
        saveAsVarStage.setResizable(false);
        saveAsVarStage.initStyle(StageStyle.UTILITY);
        saveAsVarStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                numberPadController.root_pane.setDisable(false);
                if(SaveAsVarController.varName != null)
                    numberPadController.addVariable(SaveAsVarController.varName);

                SaveAsVarController.varName = null;
                saveAsVarController.btn_confirm_name.setDisable(true);
                saveAsVarController.txf_var_name.setText("");
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
