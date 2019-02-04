package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
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
import java.util.stream.Collectors;

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
    private ArrayDeque<String> undoHistory = null;
    private static final int MAX_HISTORY_SIZE = 200002;
    private boolean undoing = false;
    private boolean redoing = false;
    private ObservableMap<String, Function> funcMap = null;
    private static final int FUNCTION_OPERATOR = 11;

    @FXML BorderPane root_pane;
    @FXML Button btn_add_left, btn_add_cursor, btn_add_right;
    @FXML GridPane gpn_number_pad;
    @FXML private TextField txf_show;
    @FXML private Button btn_one, btn_two, btn_three, btn_four, btn_five, btn_six, btn_seven, btn_eight, btn_nine, btn_zero;
    @FXML private Button btn_dot, btn_plus, btn_div, btn_multi, btn_minus, btn_real_power, btn_calc, btn_factorial, btn_parens;
    @FXML private Button btn_backspace, btn_clear, btn_save_as_var, btn_undo, btn_redo;
    @FXML private VBox vbx_vars;
    @FXML private Slider sld_precision;
    @FXML private Label lbl_precision_value, lbl_precision_name;
    @FXML private CheckBox chk_show_redundant_zero, chk_use_scientific_notation;

    @FXML TableView<Variable> tbv_vars;
    @FXML private TableColumn<Variable, String> tbc_identity;
    @FXML private TableColumn<Variable, String> tbc_value;
    @FXML private TableColumn tbc_action;

    @FXML
    public void initialize() {
        funcMap = FXCollections.observableHashMap();
        funcMap.put("sin", new Function("sin", 1, true));
        funcMap.put("cos", new Function("cos", 1, true));

        // scientific notation checkbox
        chk_use_scientific_notation.setSelected(false);
        chk_use_scientific_notation.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            // disable or enable decimal precision control functions
            if(newValue)
                chk_show_redundant_zero.setSelected(false);
            chk_show_redundant_zero.setDisable(newValue);
        }));

        // show redundant zero checkbox
        chk_show_redundant_zero.setSelected(false);
        chk_show_redundant_zero.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            ExactNumber.setShowRedundantDecimal(newValue);
        }));

        // precision setting
        ExactNumber.setDecimalPrecision((int)sld_precision.getValue());
        lbl_precision_value.setText(String.format("%4d", (int)sld_precision.getValue()));
        sld_precision.setBlockIncrement(1);
        sld_precision.valueProperty().addListener(((observable, oldValue, newValue) -> {
            lbl_precision_value.setText(String.format("%4d", (int)newValue.doubleValue()));
            ExactNumber.setDecimalPrecision((int)newValue.doubleValue());
        }));

        // undo and redo feature
        history = new ArrayDeque<>();
        txf_show.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!undoing) {
                if(history.size() == MAX_HISTORY_SIZE)
                    history.pollFirst();
                history.offerLast(oldValue);
                btn_undo.setDisable(history.isEmpty());

                if(!redoing) {
                    undoHistory.clear();
                    btn_redo.setDisable(true);
                }
            }
        });
        undoHistory = new ArrayDeque<>();
        btn_undo.setDisable(true);
        btn_redo.setDisable(true);

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
                boolean isValid = ExactNumber.checkIsValidNumber(newValue);

                if(!isValid) {
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
        else if(clickedBtn == btn_real_power)
            insertValue = "^";
        else if(clickedBtn == btn_factorial)
            insertValue = "!";
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
        boolean hasError = false;

        if(clickedBtn == btn_save_as_var && Main.saveAsVarStage != null && !errorHappened && !txf_show.getText().equals("")) {
            String result;
            try {
                result = calc(expression.toString());
            } catch (ArithmeticException e) {
                hasError = true;
                result = ARITHM_ERR_MSG;
            } catch (NumberFormatException e) {
                hasError = true;
                result = FORMAT_ERR_MSG;
            } catch (NoVariableException e) {
                hasError = true;
                result = e.getMessage();
            } catch (NoFunctionException e) {
                hasError = true;
                result = e.getMessage();
            } catch (NumberTooBigException e) {
                hasError = true;
                result = e.getMessage();
            }

            if(hasError) {
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

    public void btnUndoRedoClick(ActionEvent actionEvent) {
        Object clickedBtn = actionEvent.getSource();

        if(clickedBtn == btn_undo) {
            if(!history.isEmpty()) {
                if(undoHistory.size() == MAX_HISTORY_SIZE)
                    undoHistory.pollFirst();
                undoHistory.offerLast(expression.toString());

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
        else if(clickedBtn == btn_redo) {
            if(!undoHistory.isEmpty()) {
                expression.delete(0, expression.length());
                expression.append(undoHistory.pollLast());
                redoing = true;
                txf_show.setText(expression.toString());
                redoing = false;
                txf_show.requestFocus();
                txf_show.positionCaret(expression.length());
                mouseCaret.set(txf_show.getCaretPosition());
                mouseAnchor.set(txf_show.getCaretPosition());
            }
        }

        btn_undo.setDisable(history.isEmpty());
        btn_redo.setDisable(undoHistory.isEmpty());
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
        if(errorHappened) {
            Character tmpChar = null;
            if(txf_show.getText().length() > 0 && (keyEvent.getCode().isKeypadKey() || keyEvent.getCode().isDigitKey() || keyEvent.getCode().isLetterKey()))
                tmpChar = txf_show.getText().charAt(txf_show.getText().length() - 1);

            expression.delete(0, expression.length());
            txf_show.setText("");
            mouseCaret.set(0);
            mouseAnchor.set(0);

            if(tmpChar != null) {
                expression.append(tmpChar);
                txf_show.setText(tmpChar.toString());
                txf_show.positionCaret(1);
                mouseCaret.set(1);
                mouseAnchor.set(1);
            }

            errorHappened = false;
        }

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
        } catch (NoFunctionException e) {
            errorHappened = true;
            result = e.getMessage();
        } catch (NumberTooBigException e) {
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
    private String calc(String exp) throws NumberFormatException, ArithmeticException, NoVariableException, NoFunctionException, NumberTooBigException {
        ArrayList<Object> postfix;
        ArrayDeque<ExactNumber> numStk = new ArrayDeque<>();

        postfix = toPostfix(exp);

        for(Object ele: postfix) {
            // operators or functions
            if(ele instanceof String) {
                try {
                    // functions
                    if(((String)ele).charAt(0) == '#') {
                        String funcString = ((String)ele).substring(1);
                        Function func = funcMap.getOrDefault(funcString, null);
                        if(func == null)
                            throw new NoFunctionException("No such function");
                        int paramNum = func.getParamNum();
                        ArrayList<ExactNumber> numberList = new ArrayList<>();

                        while(paramNum > 0) {
                            --paramNum;
                            numberList.add(numStk.pop());
                        }

                        // FIXME: may have exception
                        ExactNumber result = func.call(numberList);
                        numStk.push(result);
                    }

                    // operators
                    else {
                        String op = (String)ele;
                        ExactNumber num2 = numStk.pop();
                        ExactNumber num1 = null;
                        if(op.charAt(0) != 'p' && op.charAt(0) != 'n' && op.charAt(0) != '!')
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
                            case '^':
                                result = ExactNumber.realPower(num1, num2);
                                break;
                            case '!':
                                result = ExactNumber.factorial(num2);
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
                    } // end of operators else
                } catch (ArithmeticException e) {
                    throw new ArithmeticException();
                } catch (NoFunctionException e) {
                    throw e;
                } catch (NumberTooBigException e) {
                    throw e;
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
        return chk_use_scientific_notation.isSelected() ? ret.scientificNotationStyle : ret.fractionStyle;
    }

    // convert the expression to the postfix format
    private ArrayList<Object> toPostfix(String exp) throws NumberFormatException, NoVariableException, NoFunctionException {
        ArrayList<Object> postfix = new ArrayList<>();
        // opStk: if the operator is begin with a '#', then it's a function
        ArrayDeque<String> opStk = new ArrayDeque<>();
        // calculating priority of operators
        Map<Character, Integer> opPriority = new HashMap<>(){{
            put('!', 9);
            put('p', 6); // unary operator: positive
            put('n', 6); // unary operator: negative
            put('^', 3); // cifang
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
                // '0'-'9' or '.' or 'e' (or '-')
                while(k < exp.length() && ((exp.charAt(k) >= '0' && exp.charAt(k) <= '9') || exp.charAt(k) == '.' || exp.charAt(k) == 'e')) {
                    if(exp.charAt(k) == 'e' && k + 1 < exp.length() && exp.charAt(k + 1) == '-') {
                        numBuf.append(exp.charAt(k));
                        ++k;
                    }
                    numBuf.append(exp.charAt(k));
                    ++k;
                }
                --k;
                num = numBuf.toString();

                // duplicate '.'s in a number, invalid
                if(num.indexOf('.') != num.lastIndexOf('.'))
                    throw new NumberFormatException();
                // invalid number
                if(!ExactNumber.checkIsValidNumber(num))
                    throw new NumberFormatException();

                // num is a scientific notation style string
                if(num.indexOf('e') != -1) {
                    try {
                        num = ExactNumber.convertScientificNotationStyle2FractionStyle(num);
                        if(num.length() >= 20000)
                            throw new NumberFormatException();
                    } catch(NumberFormatException e) {
                        throw e;
                    }
                }

                postfix.add(new ExactNumber(num));
            }

            // left parenthesis
            else if(c == '(')
                opStk.push(String.valueOf(c));

            // right parenthesis
            else if(c == ')') {
                try {
                    while(!opStk.isEmpty() && opStk.peek().charAt(0) != '#' && !opStk.peek().equals("("))
                        postfix.add(opStk.pop());
                    if(opStk.peek().charAt(0) == '#')
                        postfix.add(opStk.pop());
                    else
                        opStk.pop();
                } catch(Exception e) {
                    throw new NumberFormatException();
                }
            }

            // operators
            else if(c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '!') {
                try {
                    // the left side of factorial must not be an operator except for factorial
                    if(c == '!' && (k == 0 || (exp.charAt(k - 1) == '+' || exp.charAt(k - 1) == '-' || exp.charAt(k - 1) == '*' || exp.charAt(k - 1) == '/' || exp.charAt(k - 1) == '^')))
                        throw new NumberFormatException();

                    // a '+' or a '-' is a unary operator if the character before it is an operator or an opened parenthesis
                    // or it's the first character in the expression
                    if((c == '+' || c == '-') && (k == 0 || exp.charAt(k - 1) == '(' || exp.charAt(k - 1) == '+' || exp.charAt(k - 1) == '-' || exp.charAt(k - 1) == '*' || exp.charAt(k - 1) == '/' || exp.charAt(k - 1) == '^'))
                        c = c == '+' ? 'p' : 'n'; // p: positive, n: negative

                    while(!opStk.isEmpty() && opStk.peek().charAt(0) !=  '#' && opPriority.get(opStk.peek().charAt(0)) >= opPriority.get(c)) {
                        if(opPriority.get(opStk.peek().charAt(0)) == 6 && opPriority.get(c) == 6)
                            break;
                        postfix.add(opStk.pop());
                    }
                    opStk.push(String.valueOf(c));
                } catch (Exception e) {
                    throw new NumberFormatException();
                }
            }

            // variable or function
            else if(c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                StringBuilder nameBuf = new StringBuilder();

                while(k < exp.length() && (exp.charAt(k) == '_' || (exp.charAt(k) >= 'a' && exp.charAt(k) <= 'z') || (exp.charAt(k) >= 'A' && exp.charAt(k) <= 'Z'))) {
                    nameBuf.append(exp.charAt(k));
                    ++k;
                }

                // variable
                if(k == exp.length() || exp.charAt(k) != '(') {
                    --k;
                    final String varName = nameBuf.toString();
                    try {
                        ExactNumber newNum = new ExactNumber(tbv_vars.getItems().stream().filter(obj -> obj.getIdentity().equals(varName)).collect(Collectors.toList()).get(0).getValue().trim());
                        postfix.add(newNum);
                    } catch(Exception e) {
                        throw new NoVariableException("No such variable");
                    }
                }

                // function
                else {
                    final String funName = nameBuf.toString();
                    Function func = funcMap.getOrDefault(funName, null);
                    // no such function
                    if(func == null)
                        throw new NoFunctionException("No such function");

                    // add function to operator stack
                    opStk.push("#" + funName);
                }
            }

            // invalid characters
            else
                throw new NumberFormatException();
        }

        while(!opStk.isEmpty()) {
            if(opStk.peek().equals("("))
                throw new NumberFormatException();
            postfix.add(opStk.pop());
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
