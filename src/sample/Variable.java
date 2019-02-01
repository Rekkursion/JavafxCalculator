package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

public class Variable {
    private final StringProperty identity;
    private final StringProperty  value;
    private final Button btnDelete;
    private static final NumberPadController numberPadController = Main.numberPadController;

    public Variable(String identity, String value) {
        this.identity = new SimpleStringProperty(identity);
        this.value = new SimpleStringProperty(value);
        this.btnDelete = new Button("Delete");

        this.btnDelete.setPrefWidth(100);
        this.btnDelete.setMinWidth(10);
        this.btnDelete.setAlignment(Pos.CENTER);
        this.btnDelete.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Deletion confirm");
            alert.setHeaderText("");
            alert.setContentText("Are you sure to delete this variable?");
            alert.showAndWait();

            ButtonType result = alert.getResult();
            if(result == ButtonType.OK) {
                numberPadController.tbv_vars.getSelectionModel().select(this);
                numberPadController.tbv_vars.getItems().remove(numberPadController.tbv_vars.getSelectionModel().getSelectedItem());
                numberPadController.btn_add_left.setDisable(true);
                numberPadController.btn_add_cursor.setDisable(true);
                numberPadController.btn_add_right.setDisable(true);
            }
        });
    }

    public StringProperty getIdentityProperty() {
        return this.identity;
    }

    public String getIdentity() {
        return this.identity.get();
    }

    public StringProperty getValueProperty() {
        return this.value;
    }

    public String getValue() {
        return this.value.get();
    }

    public void setValue(String newValue) {
        this.value.setValue(newValue);
    }

    public Button getBtnDelete() { return this.btnDelete; }

    @Override
    public String toString() {
        return "|" + this.identity.get() + "|: " + this.value.get();
    }
}
