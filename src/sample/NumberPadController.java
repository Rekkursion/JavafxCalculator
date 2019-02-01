package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

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

    @FXML TextField txf_show;
    @FXML Button btn_one, btn_two, btn_three, btn_four, btn_five, btn_six, btn_seven, btn_eight, btn_nine, btn_zero;
    @FXML Button btn_dot, btn_plus, btn_div, btn_multi, btn_minus, btn_calc, btn_parens, btn_backspace, btn_clear;
    @FXML Button btn_save_as_var;
    @FXML BorderPane root_pane;
    @FXML GridPane gpn_number_pad;

    @FXML TableView<Variable> tbv_vars;
    @FXML private TableColumn<Variable, String> tbc_identity;
    @FXML private TableColumn<Variable, String> tbc_value;

    @FXML
    public void initialize() {
        tbv_vars.setEditable(false);

        tbc_identity = new TableColumn("Identity");
        tbc_identity.setMinWidth(100);
        tbc_identity.setCellValueFactory(new PropertyValueFactory("identity"));
        tbc_identity.setCellFactory(TextFieldTableCell.forTableColumn());

        tbc_value = new TableColumn("Value");
        tbc_value.setMinWidth(100);
        tbc_value.setCellValueFactory(new PropertyValueFactory("value"));
        tbc_value.setCellFactory(TextFieldTableCell.forTableColumn());

        tbv_vars.setItems(varList);
        tbv_vars.getColumns().addAll(tbc_identity, tbc_value);
    }

    public void numberPadButtonsClick(ActionEvent actionEvent) {
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
            }

            expression.delete(0, expression.length());
            expression.append(result);
            mouseAnchor.set(result.length());
            mouseCaret.set(mouseAnchor.get());
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

    public void saveAsVarButtonClick(ActionEvent actionEvent) {
        Object clickedBtn = actionEvent.getSource();
        if(clickedBtn == btn_save_as_var && Main.saveAsVarStage != null && !errorHappened && !txf_show.getText().equals("")) {
            String result;
            try {
                result = calc(expression.toString());
            } catch (ArithmeticException e) {
                result = ARITHM_ERR_MSG;
            } catch (NumberFormatException e) {
                result = FORMAT_ERR_MSG;
            }

            if(result.equals(ARITHM_ERR_MSG) || result.equals(FORMAT_ERR_MSG)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("");
                alert.setContentText("Format error or arithmetic error happened.");
                alert.showAndWait();
            }
            else {
                savedValue = result;
                gpn_number_pad.setDisable(true);
                Main.saveAsVarStage.setTitle("Save as variable: " + (expression.toString().equals(result) ? result : expression.toString() + " = " + result));
                Main.saveAsVarStage.show();
            }
        }
    }

    public void txfClick(MouseEvent mouseEvent) {
        if(mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            mouseAnchor.set(Math.min(txf_show.getAnchor(), txf_show.getCaretPosition()));
            mouseCaret.set(Math.max(txf_show.getAnchor(), txf_show.getCaretPosition()));
        }
    }

    public void txfKeyReleased(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.BACK_SPACE || isNumberKey(keyEvent.getCode())) {
            expression.delete(0, expression.length());
            expression.append(txf_show.getText());
            mouseAnchor.set(Math.min(txf_show.getAnchor(), txf_show.getCaretPosition()));
            mouseCaret.set(mouseAnchor.get());
        }
        else if(keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.HOME || keyEvent.getCode() == KeyCode.END) {
            mouseAnchor.set(txf_show.getCaretPosition());
            mouseCaret.set(txf_show.getCaretPosition());
        }
        else {
            int tmpCaret = Math.min(mouseAnchor.get(), mouseCaret.get());
            txf_show.setText(expression.toString());
            mouseAnchor.set(tmpCaret);
            mouseCaret.set(tmpCaret);
            txf_show.positionCaret(mouseCaret.get());
        }
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
                varList.add(new Variable(identity, savedValue));
                varList.remove(varList.size() - 1);
            }
        }
        else
            varList.add(new Variable(identity, savedValue));
    }

    // calculate the value of the expression
    private String calc(String exp) throws NumberFormatException, ArithmeticException {
        ArrayList<Object> postfix;
        ArrayDeque<ExactNumber> numStk = new ArrayDeque<>();

        try {
            postfix = toPostfix(exp);
        } catch (NumberFormatException e) {
            throw e;
        }

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
    private ArrayList<Object> toPostfix(String exp) throws NumberFormatException {
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

    // check if the key is number key or not
    private boolean isNumberKey(KeyCode key) {
        return (key == KeyCode.DIGIT1 ||
                key == KeyCode.DIGIT2 ||
                key == KeyCode.DIGIT3 ||
                key == KeyCode.DIGIT4 ||
                key == KeyCode.DIGIT5 ||
                key == KeyCode.DIGIT6 ||
                key == KeyCode.DIGIT7 ||
                key == KeyCode.DIGIT8 ||
                key == KeyCode.DIGIT9 ||
                key == KeyCode.DIGIT0 ||
                key == KeyCode.NUMPAD1 ||
                key == KeyCode.NUMPAD2 ||
                key == KeyCode.NUMPAD3 ||
                key == KeyCode.NUMPAD4 ||
                key == KeyCode.NUMPAD5 ||
                key == KeyCode.NUMPAD6 ||
                key == KeyCode.NUMPAD7 ||
                key == KeyCode.NUMPAD8 ||
                key == KeyCode.NUMPAD9 ||
                key == KeyCode.NUMPAD0);
    }
}
