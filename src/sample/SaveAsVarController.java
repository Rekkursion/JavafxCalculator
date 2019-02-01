package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SaveAsVarController {
    public static String varName = null;

    @FXML Label lbl_var_name_rule;
    @FXML TextField txf_var_name;
    @FXML Button btn_confirm_name, btn_cancel_name;

    @FXML
    public void initialize() {
        // set the text of rules
        String[] varNameRules = {"- Alphabets(a-zA-Z) and underline(_) only.",
                "- There must be at least one alphabet.",
                "- Minimum and maximum length is 1 and 100 respectively."};
        lbl_var_name_rule.setText(Arrays.stream(varNameRules).collect(Collectors.joining("\n")));
        btn_confirm_name.setDisable(true);
    }

    public void btnConfirmClick(ActionEvent actionEvent) {
        Main.saveAsVarStage.close();
    }

    public void btnCancelClick(ActionEvent actionEvent) {
        varName = null;
        Main.saveAsVarStage.close();
    }

    public void txfVarNameKeyReleased(KeyEvent keyEvent) {
        varName = txf_var_name.getText();
        //System.out.println("|" + varName + "|");
        btn_confirm_name.setDisable(!isValidName());
    }

    private boolean isValidName() {
        return isValidName(varName);
    }

    public static boolean isValidName(String name) {
        if(name == null)
            return false;
        return name.matches("([a-zA-Z_]*[a-zA-Z][a-zA-Z_]*){1,100}");
    }
}
