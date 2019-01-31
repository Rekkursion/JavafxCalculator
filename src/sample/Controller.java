package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    private StringBuffer expression = new StringBuffer();
    private AtomicInteger mouseAnchor = new AtomicInteger(0);
    private AtomicInteger mouseCaret = new AtomicInteger(0);
    private static final String FORMAT_ERR_MSG = "Format error!";
    private static final String ARITHM_ERR_MSG = "Arithmetic error!";
    private boolean errorHappened = false;

    @FXML TextField txf_show;
    @FXML Button btn_one, btn_two, btn_three, btn_four, btn_five, btn_six, btn_seven, btn_eight, btn_nine, btn_zero;
    @FXML Button btn_dot, btn_plus, btn_div, btn_multi, btn_minus, btn_calc, btn_parens;

    public void btnClick(ActionEvent actionEvent) {
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

        if(insertValue.equals("()")) {
            if(errorHappened) {
                expression.delete(0, expression.length());
                mouseCaret.set(0);
                mouseAnchor.set(0);

                errorHappened = false;
            }

            expression.insert(mouseAnchor.get(), "(");
            expression.insert(mouseCaret.get() + 1, ")");
            mouseAnchor.set(mouseCaret.get() + 2);
            mouseCaret.addAndGet(2);
        }
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
    }

    public void txfClick(MouseEvent mouseEvent) {
        if(mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            mouseAnchor.set(Math.min(txf_show.getAnchor(), txf_show.getCaretPosition()));
            mouseCaret.set(Math.max(txf_show.getAnchor(), txf_show.getCaretPosition()));
        }
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
                    ExactNumber num2 = numStk.pop();
                    ExactNumber num1 = numStk.pop();
                    ExactNumber result = new ExactNumber();
                    String op = (String)ele;

                    switch(op.charAt(0)) {
                        case '+':
                            result = ExactNumber.add(num1, num2);
                            break;
                        case '-':
                            break;
                        case '*':
                            result = ExactNumber.multiple(num1, num2);
                            break;
                        case '/':
                            break;
                    }

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

        return numStk.pop().fractionStyle;
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

                    while(!opStk.isEmpty() && opPriority.get(opStk.peek()) >= opPriority.get(c))
                        postfix.add(String.valueOf(opStk.pop()));
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


        for(Object p: postfix) {
            System.out.print(p + " ");
        }
        System.out.println();


        return postfix;
    }
}
