package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NumberPadController {
    private ObservableList<Variable> varList = FXCollections.observableArrayList();
    private String savedValue = "";
    private StringBuffer expression = new StringBuffer();
    private AtomicInteger mouseAnchor = new AtomicInteger(0);
    private AtomicInteger mouseCaret = new AtomicInteger(0);
    private static final String FORMAT_ERR_MSG = "Format error!";
    private static final String ARITHM_ERR_MSG = "Arithmetic error!";
    private boolean errorHappened = false;
    private Variable selectedVar = null;
    private ArrayDeque<String> history = null;
    private static final int MAX_HISTORY_SIZE = 200002;
    private boolean undoing = false;

    @FXML BorderPane root_pane;
    @FXML Button btn_add_left, btn_add_cursor, btn_add_right;
    @FXML GridPane gpn_number_pad;
    @FXML private TextField txf_show;
    @FXML private Button btn_one, btn_two, btn_three, btn_four, btn_five, btn_six, btn_seven, btn_eight, btn_nine, btn_zero;
    @FXML private Button btn_dot, btn_plus, btn_div, btn_multi, btn_minus, btn_calc, btn_parens, btn_backspace, btn_clear, btn_save_as_var, btn_undo;
    @FXML private VBox vbx_vars;
    @FXML private Slider sld_precision;
    @FXML private Label lbl_precision_value;
    @FXML private CheckBox chk_show_redundant_zero;

    @FXML TableView<Variable> tbv_vars;
    @FXML private TableColumn<Variable, String> tbc_identity;
    @FXML private TableColumn<Variable, String> tbc_value;
    @FXML private TableColumn tbc_action;

    @FXML
    public void initialize() {
        chk_show_redundant_zero.setSelected(false);
        chk_show_redundant_zero.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            ExactNumber.setShowRedundantDecimal(newValue);
        }));

        ExactNumber.setDecimalPrecision((int)sld_precision.getValue());
        lbl_precision_value.setText(String.format("%4d", (int)sld_precision.getValue()));
        sld_precision.setBlockIncrement(1);
        sld_precision.valueProperty().addListener(((observable, oldValue, newValue) -> {
            lbl_precision_value.setText(String.format("%4d", (int)newValue.doubleValue()));
            ExactNumber.setDecimalPrecision((int)newValue.doubleValue());
        }));

        history = new ArrayDeque<>();
        txf_show.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!undoing) {
                if(history.size() == MAX_HISTORY_SIZE)
                    history.pollFirst();
                history.offerLast(oldValue);
            }
        });

        tbv_vars.setEditable(true);

        // identity col
        tbc_identity = new TableColumn("Name");
        tbc_identity.setMinWidth(10);
        tbc_identity.setPrefWidth(98);
        tbc_identity.setCellValueFactory(new PropertyValueFactory("identity"));
        tbc_identity.setCellFactory(TextFieldTableCell.forTableColumn());
        tbc_identity.setEditable(false);
        tbc_identity.setSortable(false);

        // value col
        tbc_value = new TableColumn("Value");
        tbc_value.setMinWidth(10);
        tbc_value.setPrefWidth(98);
        tbc_value.setCellValueFactory(new PropertyValueFactory("value"));
        tbc_value.setCellFactory(TextFieldTableCell.forTableColumn());
        tbc_value.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Variable, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Variable, String> event) {
                String newValue = event.getNewValue();
                if(!newValue.matches("[+\\-]?[0-9]*.?[0-9]+") && !newValue.matches("[+\\-]?[0-9]+.?[0-9]*")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid value error");
                    alert.setHeaderText("");
                    alert.setContentText("It\'s an invalid value.");
                    alert.showAndWait();

                    newValue = event.getOldValue();
                }
                ((Variable) event.getTableView().getItems()
                        .get(event.getTablePosition().getRow()))
                        .setValue(newValue);
                tbv_vars.refresh();
            }
        });
        tbc_value.setSortable(false);

        // action col
        tbc_action = new TableColumn("Action");
        tbc_action.setMinWidth(10);
        tbc_action.setPrefWidth(98);
        tbc_action.setCellValueFactory(new PropertyValueFactory<Variable, String>("btnDelete"));
        tbc_action.setSortable(false);



        tbv_vars.setItems(varList);
        tbv_vars.getColumns().addAll(tbc_identity, tbc_value, tbc_action);

        tbv_vars.setRowFactory(tv -> {
            TableRow<Variable> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY) && (!row.isEmpty())) {
                    Variable rowData = row.getItem();
                    int index = row.getIndex();

                    if(event.getClickCount() == 1) {
                        if(tbv_vars.getSelectionModel().isSelected(index)) {
                            btn_add_left.setDisable(false);
                            btn_add_cursor.setDisable(false);
                            btn_add_right.setDisable(false);
                            selectedVar = rowData;
                        } else {
                            btn_add_left.setDisable(true);
                            btn_add_cursor.setDisable(true);
                            btn_add_right.setDisable(true);
                            selectedVar = null;
                        }
                    }
                }
            });
            return row;
        });

        VBox.setMargin(tbv_vars, new Insets(10, 10, 10, 0));
        VBox.setMargin(btn_add_left, new Insets(0, 10, 10, 0));
        VBox.setMargin(btn_add_cursor, new Insets(0, 10, 10, 0));
        VBox.setMargin(btn_add_right, new Insets(0, 10, 10, 0));
        btn_add_left.setDisable(true);
        btn_add_cursor.setDisable(true);
        btn_add_right.setDisable(true);

        root_pane.getStylesheets().add(getClass().getResource("css/number_pad.css").toExternalForm());
    }

    // number pad control
    public void numberPadButtonsClick(ActionEvent actionEvent) throws NumberFormatException, ArithmeticException, NoVariableException {
        Object clickedBtn = actionEvent.getSource();
        String insertValue = "";

        if(clickedBtn == btn_one)
            insertValue = "1";
        else if(clickedBtn == btn_two)
            insertValue = "2";
        else if(clickedBtn == btn_three)
            insertValue = "3";
        else if(clickedBtn == btn_four)
            insertValue = "4";
        else if(clickedBtn == btn_five)
            insertValue = "5";
        else if(clickedBtn == btn_six)
            insertValue = "6";
        else if(clickedBtn == btn_seven)
            insertValue = "7";
        else if(clickedBtn == btn_eight)
            insertValue = "8";
        else if(clickedBtn == btn_nine)
            insertValue = "9";
        else if(clickedBtn == btn_zero)
            insertValue = "0";
        else if(clickedBtn == btn_dot)
            insertValue = ".";
        else if(clickedBtn == btn_plus)
            insertValue = "+";
        else if(clickedBtn == btn_minus)
            insertValue = "-";
        else if(clickedBtn == btn_div)
            insertValue = "/";
        else if(clickedBtn == btn_multi)
            insertValue = "*";
        else if(clickedBtn == btn_parens)
            insertValue = "()";
        else if(clickedBtn == btn_backspace)
            insertValue = "<-";
        else if(clickedBtn == btn_clear)
            insertValue = "C";
        else if(clickedBtn == btn_calc) {
            startCalc();
        }

        // parentheses
        if(insertValue.equals("()")) {
            if(errorHappened) {
                expression.delete(0, expression.length());
                mouseCaret.set(0);
                mouseAnchor.set(0);

                errorHappened = false;
            }
            expression.insert(mouseAnchor.get(), "(");
            expression.insert(mouseCaret.get() + 1, ")");
            if(mouseAnchor.get() == mouseCaret.get()) {
                mouseAnchor.set(mouseCaret.get() + 1);
                mouseCaret.addAndGet(1);
            }
            else {
                mouseAnchor.set(mouseCaret.get() + 2);
                mouseCaret.addAndGet(2);
            }
        }
        // delete partial
        else if(insertValue.equals("<-")) {
            if(errorHappened) {
                expression.delete(0, expression.length());
                mouseCaret.set(0);
                mouseAnchor.set(0);

                errorHappened = false;
            }

            if(mouseAnchor.get() == mouseCaret.get()) {
                if(mouseAnchor.get() > 0) {
                    expression.delete(mouseAnchor.get() - 1, mouseCaret.get());
                    mouseAnchor.addAndGet(-1);
                    mouseCaret.set(mouseAnchor.get());
                }
            }
            else {
                expression.delete(mouseAnchor.get(), mouseCaret.get());
                mouseCaret.set(mouseAnchor.get());
            }
        }
        // delete all
        else if(insertValue.equals("C")) {
            expression.delete(0, expression.length());
            mouseCaret.set(0);
            mouseAnchor.set(0);
        }
        // others
        else if(!insertValue.equals("")) {
            if(errorHappened) {
                expression.delete(0, expression.length());
                mouseCaret.set(0);
                mouseAnchor.set(0);

                errorHappened = false;
            }

            expression.replace(mouseAnchor.get(), mouseCaret.get(), insertValue);
            if(mouseAnchor.get() == mouseCaret.get()) {
                mouseCaret.addAndGet(1);
                mouseAnchor.set(mouseCaret.get());
            }
            else {
                mouseCaret.set(mouseAnchor.get() + 1);
                mouseAnchor.addAndGet(1);
            }
        }

        txf_show.setText(expression.toString());
        txf_show.requestFocus();
        txf_show.positionCaret(mouseCaret.get());
    }

    // save the value as variable to the list, and show in the table view
    public void saveAsVarButtonClick(ActionEvent actionEvent) throws NumberFormatException, ArithmeticException, NoVariableException {
        Object clickedBtn = actionEvent.getSource();
        if(clickedBtn == btn_save_as_var && Main.saveAsVarStage != null && !errorHappened && !txf_show.getText().equals("")) {
            String result;
            try {
                result = calc(expression.toString());
            } catch (ArithmeticException e) {
                result = ARITHM_ERR_MSG;
            } catch (NumberFormatException e) {
                result = FORMAT_ERR_MSG;
            } catch (NoVariableException e) {
                result = e.getMessage();
            }

            if(result.equals(ARITHM_ERR_MSG) || result.equals(FORMAT_ERR_MSG) || result.equals("No such variable")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("");
                alert.setContentText(result);
                alert.showAndWait();
            }
            else {
                savedValue = result;
                root_pane.setDisable(true);
                Main.saveAsVarStage.setTitle("Save as variable: " + (expression.toString().equals(result) ? result : expression.toString() + " = " + result));
                Main.saveAsVarStage.show();
            }
        }
    }

    // add the selected variable into the expression
    public void btnAddVarClick(ActionEvent actionEvent) {
        if(selectedVar == null)
            return;
        Object clickedBtn = actionEvent.getSource();

        if(errorHappened) {
            expression.delete(0, expression.length());
            mouseCaret.set(0);
            mouseAnchor.set(0);

            errorHappened = false;
        }

        // add to the most left
        if(clickedBtn == btn_add_left) {
            expression.insert(0, selectedVar.getIdentity());
        }
        // add to the most right
        else if(clickedBtn == btn_add_right) {
            expression.insert(expression.length(), selectedVar.getIdentity());
        }
        // add to the cursor
        else if(clickedBtn == btn_add_cursor) {
            expression.replace(mouseAnchor.get(), mouseCaret.get(), selectedVar.getIdentity());

            if(mouseAnchor.get() == mouseCaret.get()) {
                mouseCaret.addAndGet(selectedVar.getIdentity().length());
                mouseAnchor.set(mouseCaret.get());
            }
            else {
                mouseCaret.set(mouseAnchor.get() + selectedVar.getIdentity().length());
                mouseAnchor.addAndGet(selectedVar.getIdentity().length());
            }
        }

        txf_show.setText(expression.toString());
    }

    // undo the input, takes from the history array deque
    public void btnUndoClick(ActionEvent actionEvent) {
        Object clickedBtn = actionEvent.getSource();
        if(clickedBtn == btn_undo) {
            if(!history.isEmpty()) {
                expression.delete(0, expression.length());
                expression.append(history.pollLast());
                undoing = true;
                txf_show.setText(expression.toString());
                undoing = false;
                txf_show.requestFocus();
                txf_show.positionCaret(expression.length());
                mouseCaret.set(txf_show.getCaretPosition());
                mouseAnchor.set(txf_show.getCaretPosition());
            }
        }
    }

    // click the expression text field
    public void txfClick(MouseEvent mouseEvent) {
        if(mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            mouseAnchor.set(Math.min(txf_show.getAnchor(), txf_show.getCaretPosition()));
            mouseCaret.set(Math.max(txf_show.getAnchor(), txf_show.getCaretPosition()));
        }
    }

    // key released when expression text field is focused
    public void txfKeyReleased(KeyEvent keyEvent) {
        // arrow keys
        if(keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.HOME || keyEvent.getCode() == KeyCode.END) {
            mouseAnchor.set(txf_show.getCaretPosition());
            mouseCaret.set(txf_show.getCaretPosition());
        }
        // enter
        else if(keyEvent.getCode() == KeyCode.ENTER) {
            startCalc();
            txf_show.setText(expression.toString());
            txf_show.requestFocus();
            txf_show.positionCaret(mouseCaret.get());
        }
        // others
        else {
            expression.delete(0, expression.length());
            expression.append(txf_show.getText());
            mouseAnchor.set(Math.min(txf_show.getAnchor(), txf_show.getCaretPosition()));
            mouseCaret.set(mouseAnchor.get());
        }
    }

    // start the calculation
    public void startCalc() {
        String result;
        errorHappened = false;
        try {
            result = calc(expression.toString());
        } catch (ArithmeticException e) {
            errorHappened = true;
            result = ARITHM_ERR_MSG;
        } catch (NumberFormatException e) {
            errorHappened = true;
            result = FORMAT_ERR_MSG;
        } catch (NoVariableException e) {
            errorHappened = true;
            result = e.getMessage();
        }

        expression.delete(0, expression.length());
        expression.append(result);
        mouseAnchor.set(result.length());
        mouseCaret.set(mouseAnchor.get());
    }

    // add new variable to varList
    public void addVariable(String identity) {
        boolean alreadyExists = false;
        Variable dup = null;
        for(Variable v: varList) {
            if(v.getIdentity().equals(identity)) {
                alreadyExists = true;
                dup = v;
                break;
            }
        }

        if(alreadyExists) {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Rewrite confirm");
            alert.setHeaderText("");
            alert.setContentText("There's already a variable with the same name. Do you want to rewrite the value?");
            final Optional<ButtonType> opt = alert.showAndWait();
            final ButtonType rtn = opt.get(); // 可以直接用「alert.getResult()」來取代

            if (rtn == ButtonType.OK) {
                dup.setValue(savedValue);
                ((Variable) tbv_vars.getItems()
                        .get(tbv_vars.getSelectionModel().getFocusedIndex()))
                        .setValue(savedValue);
                tbv_vars.refresh();
            }
        }
        else
            varList.add(new Variable(identity, savedValue));
    }

    // calculate the value of the expression
    private String calc(String exp) throws NumberFormatException, ArithmeticException, NoVariableException {
        ArrayList<Object> postfix;
        ArrayDeque<ExactNumber> numStk = new ArrayDeque<>();

        postfix = toPostfix(exp);

        for(Object ele: postfix) {
            // operators
            if(ele instanceof String) {
                try {
                    String op = (String)ele;
                    ExactNumber num2 = numStk.pop();
                    ExactNumber num1 = null;
                    if(op.charAt(0) != 'p' && op.charAt(0) != 'n')
                         num1 = numStk.pop();
                    ExactNumber result = null;

                    switch(op.charAt(0)) {
                        case '+':
                            result = ExactNumber.add(num1, num2);
                            break;
                        case '-':
                            result = ExactNumber.minus(num1, num2);
                            break;
                        case '*':
                            result = ExactNumber.multiple(num1, num2);
                            break;
                        case '/':
                            result = ExactNumber.divide(num1, num2);
                            break;
                        case 'p':
                            result = new ExactNumber(num2);
                            break;
                        case 'n':
                            result = new ExactNumber(num2.numerator, num2.denominator, !num2.isNeg);
                            break;
                    }

                    if(result == null)
                        throw new NumberFormatException();
                    numStk.push(result);
                } catch (ArithmeticException e) {
                    throw new ArithmeticException();
                } catch (Exception e) {
                    throw new NumberFormatException();
                }
            }

            // numbers
            else
                numStk.push((ExactNumber)ele);
        } // end of for

        if(numStk.size() != 1)
            throw new NumberFormatException();

        ExactNumber ret = numStk.pop();
        System.err.println(ret.fractionStyle);
        return ret.fractionStyle;
    }

    // convert the expression to the postfix format
    private ArrayList<Object> toPostfix(String exp) throws NumberFormatException, NoVariableException {
        ArrayList<Object> postfix = new ArrayList<>();
        ArrayDeque<Character> opStk = new ArrayDeque<>();
        // calculating priority of operators
        Map<Character, Integer> opPriority = new HashMap<>(){{
            put('p', 3); // unary operator: positive
            put('n', 3); // unary operator: negative
            put('*', 2);
            put('/', 2);
            put('+', 1);
            put('-', 1);
            put('(', -1);
            put(')', -1);
        }};

        // remove all white space in expression
        exp = exp.replaceAll(" *", "");

        for(int k = 0; k < exp.length(); ++k) {
            char c = exp.charAt(k);

            // a number
            if((c >= '0' && c <= '9') || c == '.') {
                // build the number
                StringBuilder numBuf = new StringBuilder();
                String num;
                while(k < exp.length() && ((exp.charAt(k) >= '0' && exp.charAt(k) <= '9') || exp.charAt(k) == '.')) {
                    numBuf.append(exp.charAt(k));
                    ++k;
                }
                --k;
                num = numBuf.toString();

                // duplicate '.'s in a number, invalid
                if(num.indexOf('.') != num.lastIndexOf('.'))
                    throw new NumberFormatException();

                postfix.add(new ExactNumber(num));
            }

            // left parenthesis
            else if(c == '(')
                opStk.push(c);

            // right parenthesis
            else if(c == ')') {
                try {
                    while(!opStk.isEmpty() && opStk.peek() != '(')
                        postfix.add(String.valueOf(opStk.pop()));
                    opStk.pop();
                } catch(Exception e) {
                    throw new NumberFormatException();
                }
            }

            // operators
            else if(c == '+' || c == '-' || c == '*' || c == '/') {
                try {
                    // a '+' or a '-' is a unary operator if the character before it is an operator or an opened parenthesis
                    // or it's the first character in the expression
                    if((c == '+' || c == '-') && (k == 0 || exp.charAt(k - 1) == '(' || exp.charAt(k - 1) == '+' || exp.charAt(k - 1) == '-' || exp.charAt(k - 1) == '*' || exp.charAt(k - 1) == '/'))
                        c = c == '+' ? 'p' : 'n'; // p: positive, n: negative

                    while(!opStk.isEmpty() && opPriority.get(opStk.peek()) >= opPriority.get(c)) {
                        if(opPriority.get(opStk.peek()) == 3 && opPriority.get(c) == 3)
                            break;
                        postfix.add(String.valueOf(opStk.pop()));
                    }
                    opStk.push(c);
                } catch (Exception e) {
                    throw new NumberFormatException();
                }
            }

            // variable
            else if(c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                StringBuilder varNameBuf = new StringBuilder();
                String varName = null;
                boolean found = false;

                while(k < exp.length() && (exp.charAt(k) == '_' || (exp.charAt(k) >= 'a' && exp.charAt(k) <= 'z') || (exp.charAt(k) >= 'A' && exp.charAt(k) <= 'Z'))) {
                    varNameBuf.append(exp.charAt(k));
                    ++k;
                }
                --k;

                varName = varNameBuf.toString();
                for(Variable v: varList) {
                    if(v.getIdentity().equals(varName)) {
                        found = true;
                        postfix.add(new ExactNumber(v.getValue()));
                        break;
                    }
                }
                if(!found)
                    throw new NoVariableException("No such variable");
            }

            // invalid characters
            else
                throw new NumberFormatException();
        }

        while(!opStk.isEmpty()) {
            if(opStk.peek() == '(')
                throw new NumberFormatException();
            postfix.add(String.valueOf(opStk.pop()));
        }

        /*
        for(Object p: postfix)
            System.out.print(p + " ");
        System.out.println();
        */

        return postfix;
    }

    /*
    // check if the key is number key or not (include decimal point)
    private boolean isNumberKey(KeyCode key) {
        //   DIGIT KEY: 24-33
        // NUM PAD KEY: 65-74
        return (key.ordinal() >= 24 && key.ordinal() <= 33) || (key.ordinal() >= 65 && key.ordinal() <= 74) || key == KeyCode.DECIMAL;
    }

    // check if the key is alphabet key or not
    private boolean isAlphabetKey(KeyCode key) {
        // ALPHABET KEY: 36-61
        return key.ordinal() >= 36 && key.ordinal() <= 61;
    }

    // check if the key is operator key (+, -, *, /) or not
    private boolean isOperatorKey(KeyEvent keyE) {
        KeyCombination keyComb = new KeyCharacterCombination("+", KeyCombination.SHIFT_DOWN);
        if(keyComb.match(keyE))
            return true;
        KeyCode key = keyE.getCode();
        return key == KeyCode.PLUS || key == KeyCode.MINUS || key == KeyCode.MULTIPLY || key == KeyCode.DIVIDE;
    }
    */
}
