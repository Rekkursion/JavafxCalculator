package sample;

import javafx.geometry.Pos;

import java.util.FormatterClosedException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.PI;

public class ExactNumber {
    private static int decimalPrecision = 5;
    private static boolean showRedundantDecimal = false;
    public String numerator;
    public String denominator;
    public String fractionStyle = "";
    public String scientificNotationStyle;
    public boolean isNeg;

    // default constructor
    public ExactNumber() { this("1", "1", false); }

    // copy constructor
    public ExactNumber(ExactNumber old) {
        numerator = old.numerator;
        denominator = old.denominator;
        isNeg = old.isNeg;
        fractionStyle = old.fractionStyle;
        scientificNotationStyle = old.scientificNotationStyle;
    }

    // constructor: 2 strings as numerator and denominator respectively
    public ExactNumber(String nume, String deno) {
        boolean numeIsNeg = (nume.length() > 0 && nume.charAt(0) == '-');
        boolean denoIsNeg = (deno.length() > 0 && deno.charAt(0) == '-');

        numerator = PositiveIntegerOperation.removePreZero(numeIsNeg ? nume.substring(1) : nume);
        denominator = PositiveIntegerOperation.removePreZero(denoIsNeg ? deno.substring(1) : deno);
        isNeg = (numeIsNeg ^ denoIsNeg) && !PositiveIntegerOperation.isZero(numerator);
        toFractionStyle_update(numerator, denominator);
        toScientificNotationStyle_update(numerator, denominator);
    }

    // constructor: 2 strings as numerator and denominator respectively, and 1 boolean as if it's negative
    public ExactNumber(String nume, String deno, boolean isNegative) {
        this(nume, deno);
        isNeg = isNegative;
        toFractionStyle_update(numerator, denominator);
        toScientificNotationStyle_update(numerator, denominator);
    }

    // constructor: a string as a fraction number
    public ExactNumber(String num) {
        if(num.length() > 0 && num.charAt(0) == '-') {
            isNeg = true;
            num = num.substring(1);
        }
        else if(num.length() > 0 && num.charAt(0) == '+') {
            isNeg = false;
            num = num.substring(1);
        }
        else
            isNeg = false;

        int dotIdx = num.indexOf(".");

        // doesn't have decimal point
        if(dotIdx == -1) {
            numerator = num;
            denominator = "1";
        }
        // has decimal point
        else {
            String intPart = num.substring(0, dotIdx);
            String frcPart = num.substring(dotIdx + 1);
            if(intPart.length() == 0)
                intPart += "0";
            if(frcPart.length() == 0)
                frcPart += "0";
            frcPart = PositiveIntegerOperation.removePostZero(frcPart);

            // fraction is zero
            if(PositiveIntegerOperation.isZero(frcPart)) {
                numerator = PositiveIntegerOperation.removePreZero(intPart);
                denominator = "1";
            }
            // fraction is not zero
            else {
                numerator = PositiveIntegerOperation.removePreZero(intPart + frcPart);
                denominator = Stream.iterate("1", ch -> "0").limit(frcPart.length() + 1).collect(Collectors.joining(""));
            }

            if(PositiveIntegerOperation.isZero(numerator))
                isNeg = false;
        }
        toFractionStyle_update(numerator, denominator);
        toScientificNotationStyle_update(numerator, denominator);
    }

    // ===================================================================================

    // static method: set show redundant decimal
    public static void setShowRedundantDecimal(boolean _showRedundantDecimal) {
        showRedundantDecimal = _showRedundantDecimal;
    }

    // static method: set new decimal precision
    public static int getDecimalPrecision() { return decimalPrecision; }

    // static method: set new decimal precision
    public static void setDecimalPrecision(int newDecimalPrecision) {
        decimalPrecision = newDecimalPrecision;
    }


    // static method: check if the string is a valid number
    public static boolean checkIsValidNumber(String s) {
        final int INTEGER_MODE = 0;
        final int FRACTION_MODE = 1;
        final int EXPONENTIAL_MODE = 2;

        if(s == null)
            return false;

        // trim the white space at head and tail
        s = s.trim();
        if(s.length() == 0)
            return false;

        int startIdx = 0;
        int mode = INTEGER_MODE;
        boolean hasValidExp = true, hasValidBase = false;

        // deal with the first character
        if(s.charAt(0) >= '0' && s.charAt(0) <= '9')
            hasValidBase = true;
        else if(s.charAt(0) == '+' || s.charAt(0) == '-')
            startIdx = 1;
        else if(s.charAt(0) == '.') {
            startIdx = 1;
            mode = FRACTION_MODE;
        }
        else
            return false;

        for(int k = startIdx; k < s.length(); ++k) {
            if(s.charAt(k) >= '0' && s.charAt(k) <= '9') {
                if(mode == EXPONENTIAL_MODE)
                    hasValidExp = true;
                else
                    hasValidBase = true;
            }
            else if(s.charAt(k) == '.') {
                if(mode == INTEGER_MODE)
                    mode = FRACTION_MODE;
                else
                    return false;
            }
            else if(s.charAt(k) == 'e') {
                if(mode == EXPONENTIAL_MODE)
                    return false;
                else
                    mode = EXPONENTIAL_MODE;

                if(k + 1 < s.length() && (s.charAt(k + 1) == '+' || s.charAt(k + 1) == '-'))
                    ++k;
                hasValidExp = false;
            }
            else
                return false;
        }

        return (hasValidExp && hasValidBase);
    }

    // static method: convert a scientific notation style number into a fraction style number
    public static String convertScientificNotationStyle2FractionStyle(String s) throws NumberFormatException {
        if(!checkIsValidNumber(s))
            throw new NumberFormatException();

        boolean isNegative = false;
        StringBuilder intPartBuf = new StringBuilder();
        StringBuilder frcPartBuf = new StringBuilder();
        int exponential = 0;

        if(s.charAt(0) == '+') {
            s = s.substring(1);
        }
        else if(s.charAt(0) == '-') {
            isNegative = true;
            s = s.substring(1);
        }

        int curIdx;
        // search for int part
        for(curIdx = 0; curIdx < s.length() && s.charAt(curIdx) >= '0' && s.charAt(curIdx) <= '9'; ++curIdx)
            intPartBuf.append(s.charAt(curIdx));
        if(intPartBuf.length() == 0)
            intPartBuf.append(0);

        // reach the end
        if(curIdx >= s.length())
            frcPartBuf.append(0);
        // still not reach the end
        else {
            boolean hasExponential = false;

            // current character is decimal point
            if(s.charAt(curIdx) == '.') {
                ++curIdx;
                for(; curIdx < s.length() && s.charAt(curIdx) >= '0' && s.charAt(curIdx) <= '9'; ++curIdx)
                    frcPartBuf.append(s.charAt(curIdx));
                // meet the character 'e'
                if(curIdx < s.length())
                    hasExponential = true;
            }
            // current character is 'e' ( else if(s.charAt(curIdx) == 'e') )
            else
                hasExponential = true;

            // deal with 'e'
            if(hasExponential) {
                boolean isExponentialPositive = true;

                ++curIdx;
                if(s.charAt(curIdx) == '+')
                    ++curIdx;
                else if(s.charAt(curIdx) == '-') {
                    ++curIdx;
                    isExponentialPositive = false;
                }

                for(; curIdx < s.length(); ++curIdx)
                    exponential = (exponential * 10) + (s.charAt(curIdx) - '0');
                if(!isExponentialPositive)
                    exponential = -exponential;
            }

            if(frcPartBuf.length() == 0)
                frcPartBuf.append(0);
        } // end of still not reach the end

        // move decimal point to right
        if(exponential > 0) {
            while(exponential > 0) {
                if(frcPartBuf.length() > 0) {
                    intPartBuf.append(frcPartBuf.charAt(0));
                    frcPartBuf.deleteCharAt(0);
                }
                else
                    intPartBuf.append(0);
                --exponential;
            }
            if(frcPartBuf.length() == 0)
                frcPartBuf.append(0);
        }
        // move decimal point to left
        else {
            while(exponential < 0) {
                if(intPartBuf.length() > 0) {
                    frcPartBuf.insert(0, intPartBuf.charAt(intPartBuf.length() - 1));
                    intPartBuf.deleteCharAt(intPartBuf.length() - 1);
                }
                else
                    frcPartBuf.insert(0, "0");

                ++exponential;
            }
            if(intPartBuf.length() == 0)
                intPartBuf.append(0);
        }

        String intPart = PositiveIntegerOperation.removePreZero(intPartBuf.toString());
        String frcPart = PositiveIntegerOperation.removePostZero(frcPartBuf.toString());

        if(PositiveIntegerOperation.isZero(intPart) && PositiveIntegerOperation.isZero(frcPart))
            return "0";

        if(PositiveIntegerOperation.isZero(frcPart))
            return (isNegative ? "-" : "") + intPart;
        return (isNegative ? "-" : "") + intPart + "." + frcPart;
    }

    // ===================================================================================

    // add
    public static ExactNumber add(ExactNumber a, ExactNumber b) throws ArithmeticException {
        String newDeno = PositiveIntegerOperation.lcm(a.denominator, b.denominator);
        String nume_a = PositiveIntegerOperation.multiple(a.numerator, PositiveIntegerOperation.divide(newDeno, a.denominator));
        String nume_b = PositiveIntegerOperation.multiple(b.numerator, PositiveIntegerOperation.divide(newDeno, b.denominator));
        String newNume;
        boolean isNegative = false;

        // neg + neg
        if(a.isNeg && b.isNeg) {
            newNume = PositiveIntegerOperation.add(nume_a, nume_b);
            isNegative = true;
        }
        // neg + pos
        else if(a.isNeg) {
            newNume = PositiveIntegerOperation.minus(nume_b, nume_a);
            if(newNume.charAt(0) == '-') {
                isNegative = true;
                newNume = newNume.substring(1);
            }
        }
        // pos + neg
        else if(b.isNeg) {
            newNume = PositiveIntegerOperation.minus(nume_a, nume_b);
            if(newNume.charAt(0) == '-') {
                isNegative = true;
                newNume = newNume.substring(1);
            }
        }
        // pos + pos
        else
            newNume = PositiveIntegerOperation.add(nume_a, nume_b);

        ExactNumber ret = new ExactNumber(newNume, newDeno, isNegative && !PositiveIntegerOperation.isZero(newNume));
        ret.reduct_update();

        return ret;
    }

    // minus
    public static ExactNumber minus(ExactNumber a, ExactNumber b) throws ArithmeticException {
        String newDeno = PositiveIntegerOperation.lcm(a.denominator, b.denominator);
        String nume_a = PositiveIntegerOperation.multiple(a.numerator, PositiveIntegerOperation.divide(newDeno, a.denominator));
        String nume_b = PositiveIntegerOperation.multiple(b.numerator, PositiveIntegerOperation.divide(newDeno, b.denominator));
        String newNume;
        boolean isNegative = false;

        // neg - neg
        if(a.isNeg && b.isNeg) {
            newNume = PositiveIntegerOperation.minus(nume_b, nume_a);
            if(newNume.charAt(0) == '-') {
                isNegative = true;
                newNume = newNume.substring(1);
            }
        }
        // neg - pos
        else if(a.isNeg) {
            newNume = PositiveIntegerOperation.add(nume_a, nume_b);
            isNegative = true;
        }
        // pos - neg
        else if(b.isNeg)
            newNume = PositiveIntegerOperation.add(nume_a, nume_b);
        // pos - pos
        else {
            newNume = PositiveIntegerOperation.minus(nume_a, nume_b);
            if(newNume.charAt(0) == '-') {
                isNegative = true;
                newNume = newNume.substring(1);
            }
        }

        ExactNumber ret = new ExactNumber(newNume, newDeno, isNegative && !PositiveIntegerOperation.isZero(newNume));
        ret.reduct_update();

        return ret;
    }

    // multiple
    public static ExactNumber multiple(ExactNumber a, ExactNumber b) throws ArithmeticException {
        boolean isNegative = (a.isNeg ^ b.isNeg);
        String newNume = PositiveIntegerOperation.multiple(a.numerator, b.numerator);
        String newDeno = PositiveIntegerOperation.multiple(a.denominator, b.denominator);

        ExactNumber ret = new ExactNumber(newNume, newDeno, isNegative && !PositiveIntegerOperation.isZero(newNume));
        try {
            ret.reduct_update();
        } catch(ArithmeticException e) {
            throw e;
        }

        return ret;
    }

    // divide
    public static ExactNumber divide(ExactNumber a, ExactNumber b) throws ArithmeticException {
        boolean isNegative = (a.isNeg ^ b.isNeg);
        String newNume = PositiveIntegerOperation.multiple(a.numerator, b.denominator);
        String newDeno = PositiveIntegerOperation.multiple(a.denominator, b.numerator);

        ExactNumber ret = new ExactNumber(newNume, newDeno, isNegative && !PositiveIntegerOperation.isZero(newNume));
        try {
            ret.reduct_update();
        } catch(ArithmeticException e) {
            throw e;
        }

        return ret;
    }

    // real power (ex: 3.1^-2.6)
    // 9^0.95 = 8.63626
    public static ExactNumber realPower(ExactNumber a, ExactNumber b) throws ArithmeticException {
        a.reduct_update();
        b.reduct_update();

        // 0^any = 0
        if(ExactNumber.isZero(a))
            return new ExactNumber("0");
        // any^0 = 1
        if(ExactNumber.isZero(b))
            return new ExactNumber("1");
        // any^-x = (1/any)^x
        if(b.isNeg) {
            String tmp = a.numerator;
            a.numerator = a.denominator;
            a.denominator = tmp;
            b.isNeg = false;
        }

        boolean isNegative = (a.isNeg && !PositiveIntegerOperation.isEven(b.numerator));
        a.isNeg = false;
        // need to kaifang but is negative, error
        if(isNegative && !ExactNumber.isInteger(b))
            throw new ArithmeticException();

        /*
        // a^b.numerator part (cifang)
        ExactNumber onlyCifangPart = power(a, new ExactNumber(b.numerator));

        // a^b.denominator part (kaifang)
        String numeRootResult = PositiveIntegerOperation.root(onlyCifangPart.numerator, b.denominator);
        String denoRootResult = PositiveIntegerOperation.root(onlyCifangPart.denominator, b.denominator);
        ExactNumber resultNumePart = new ExactNumber(numeRootResult);
        ExactNumber resultDenoPart = new ExactNumber(denoRootResult);
        ExactNumber result = divide(resultNumePart, resultDenoPart);
        */

        // a^b.denominator part (kaifang)
        String numeRootResult = PositiveIntegerOperation.root(a.numerator, b.denominator);
        String denoRootResult = PositiveIntegerOperation.root(a.denominator, b.denominator);
        ExactNumber resultNumePart = new ExactNumber(numeRootResult);
        ExactNumber resultDenoPart = new ExactNumber(denoRootResult);
        ExactNumber onlyKaifangResult = divide(resultNumePart, resultDenoPart);

        // a^b.numerator part (cifang)
        ExactNumber result = power(onlyKaifangResult, new ExactNumber(b.numerator));

        return new ExactNumber(result.numerator, result.denominator, isNegative);
    }

    // power (ex: 3.1^-2)
    public static ExactNumber power(ExactNumber a, ExactNumber b) throws ArithmeticException {
        if(!ExactNumber.isInteger(b))
            throw new ArithmeticException();

        // 0^any = 0
        if(ExactNumber.isZero(a))
            return new ExactNumber("0");
        // any^0 = 1
        if(ExactNumber.isZero(b))
            return new ExactNumber("1");
        // 1^any or (-1)^any
        if(a.numerator.equals("1") && a.denominator.equals("1")) {
            // (-1)^any = any or -any
            if(a.isNeg)
                return new ExactNumber("1", "1", !PositiveIntegerOperation.isEven(b.numerator));
            // 1^any = 1
            else
                return new ExactNumber("1");
        }
        // any^1 = any
        if(b.numerator.equals("1"))
            return a;
        // any^-x = (1/any)^x
        if(b.isNeg) {
            String tmp = a.numerator;
            a.numerator = a.denominator;
            a.denominator = tmp;
            b.isNeg = false;
        }

        boolean isNegative = (a.isNeg && !PositiveIntegerOperation.isEven(b.numerator));
        a.isNeg = false;

        String newNume = "1";
        String newDeno = "1";

        /*
        if(PositiveIntegerOperation.compareTwoPositiveIntegers(b.numerator, "2147483647") <= 0) {
            int cif = Integer.parseInt(b.numerator);
            String numeMultisor = a.numerator;
            String denoMultisor = a.denominator;

            // 3^10 = 3^2 * 3^8
            for(int k = 0; k < 32; ++k) {
                if((cif & (1 << k)) != 0) {
                    newNume = PositiveIntegerOperation.multiple(newNume, numeMultisor);
                    //newDeno = PositiveIntegerOperation.multiple(newDeno, denoMultisor);
                }

                numeMultisor = PositiveIntegerOperation.multiple(numeMultisor, numeMultisor);
                //denoMultisor = PositiveIntegerOperation.multiple(denoMultisor, denoMultisor);
                System.out.println("=> " + numeMultisor.length() + "\n");
            }
        }
        else {
            // will be extremely slow
            // 2^20000 will be slow obviously
            String counter = "1";
            newNume = a.numerator;
            newDeno = a.denominator;
            while(PositiveIntegerOperation.compareTwoPositiveIntegers(counter, b.numerator) < 0) {
                newNume = PositiveIntegerOperation.multiple(newNume, a.numerator);
                newDeno = PositiveIntegerOperation.multiple(newDeno, a.denominator);
                counter = PositiveIntegerOperation.add(counter, "1");
            }
        }
        */

        // 2^20000 will be slow obviously
        if(PositiveIntegerOperation.compareTwoPositiveIntegers(b.numerator, "2147483647") <= 0) {
            int counter = 1, b_nume = Integer.parseInt(b.numerator);
            newNume = a.numerator;
            newDeno = a.denominator;

            while(counter < b_nume) {
                newNume = PositiveIntegerOperation.multiple(newNume, a.numerator);
                newDeno = PositiveIntegerOperation.multiple(newDeno, a.denominator);
                ++counter;
            }
        }
        else {
            String counter = "1";
            newNume = a.numerator;
            newDeno = a.denominator;

            while(PositiveIntegerOperation.compareTwoPositiveIntegers(counter, b.numerator) < 0) {
                newNume = PositiveIntegerOperation.multiple(newNume, a.numerator);
                newDeno = PositiveIntegerOperation.multiple(newDeno, a.denominator);
                counter = PositiveIntegerOperation.add(counter, "1");
            }
        }

        return new ExactNumber(newNume, newDeno, isNegative);
    }

    // factorial
    public static ExactNumber factorial(ExactNumber x) throws ArithmeticException, NumberFormatException, NumberTooBigException {
        if(!ExactNumber.isInteger(x) || x.isNeg)
            throw new ArithmeticException();

        String retString = PositiveIntegerOperation.factorial(x.numerator);
        return new ExactNumber(retString);
    }

    // sin
    public static ExactNumber sin(ExactNumber x) throws ArithmeticException {
        // restrict x to the range of [0, 2*pi]
        ExactNumber ex_2pi = new ExactNumber(String.valueOf(2.0 * Math.PI));
        ExactNumber times = toInteger(divide(x, ex_2pi));
        ExactNumber substrahend = multiple(ex_2pi, times);
        x = minus(x, substrahend);

        final int taylorJingdu = 50;
        boolean isNegative = true;
        ExactNumber ret = new ExactNumber(x);
        ExactNumber x_pow = new ExactNumber(x);

        for(int jd = 3; jd < taylorJingdu; jd += 2) {
            x_pow = multiple(multiple(x_pow, x), x);
            String nume = x_pow.numerator;
            String deno = PositiveIntegerOperation.multiple(x_pow.denominator, FactorialTable.factorialTable[jd]);
            ExactNumber next = new ExactNumber(PositiveIntegerOperation.divide(nume, deno, ExactNumber.getDecimalPrecision(), true));

            ret = isNegative ? minus(ret, next) : add(ret, next);
            isNegative = !isNegative;
        }

        return ret;
    }

    // is zero
    public static boolean isZero(ExactNumber x) throws ArithmeticException {
        if(PositiveIntegerOperation.isZero(x.denominator))
            throw new ArithmeticException();
        return PositiveIntegerOperation.isZero(x.numerator);
    }

    // is integer
    public static boolean isInteger(ExactNumber x) throws ArithmeticException {
        if(PositiveIntegerOperation.isZero(x.denominator))
            throw new ArithmeticException();
        x.reduct_update();
        return x.denominator.equals("1");
    }

    // to integer
    public static ExactNumber toInteger(ExactNumber x) throws ArithmeticException {
        String frc = x.fractionStyle;
        if(frc.length() == 0)
            throw new ArithmeticException();

        boolean isNegative = false;
        if(frc.charAt(0) == '-') {
            isNegative = true;
            frc = frc.substring(1);
        }

        int dotIdx = frc.indexOf('.');
        // originally is an integer
        if(dotIdx == -1)
            return new ExactNumber(frc, "1", isNegative);
        // originally has the decimal point
        return new ExactNumber(frc.substring(0, dotIdx), "1", isNegative);
    }

    // compare two exact number
    public static int compareTwo(ExactNumber a, ExactNumber b) throws ArithmeticException {
        // a = b = 0
        if(ExactNumber.isZero(a) && ExactNumber.isZero(b))
            return 0;
        // a < 0, b >= 0 -> a < b
        if(a.isNeg && !b.isNeg)
            return -1;
        // a >= 0, b < 0 -> a > b
        if(!a.isNeg && b.isNeg)
            return 1;

        String frc_a = a.fractionStyle;
        String frc_b = b.fractionStyle;

        try {
            // if(a.isNeg && b.isNeg)
            if(a.isNeg) {
                frc_a = frc_a.substring(1);
                frc_b = frc_b.substring(1);
            }

            int dotIdx_a = frc_a.indexOf('.');
            int dotIdx_b = frc_b.indexOf('.');
            String intPart_a = "";
            String intPart_b = "";
            String frcPart_a = "";
            String frcPart_b = "";

            if(dotIdx_a == -1) {
                intPart_a = frc_a;
                frcPart_a = "0";
            } else {
                intPart_a = frc_a.substring(0, dotIdx_a);
                frcPart_a = frc_a.substring(dotIdx_a + 1);
            }
            if(dotIdx_b == -1) {
                intPart_b = frc_b;
                frcPart_b = "0";
            } else {
                intPart_b = frc_b.substring(0, dotIdx_b);
                frcPart_b = frc_b.substring(dotIdx_b + 1);
            }
            intPart_a = PositiveIntegerOperation.removePreZero(intPart_a);
            intPart_b = PositiveIntegerOperation.removePreZero(intPart_b);
            frcPart_a = PositiveIntegerOperation.removePostZero(frcPart_a);
            frcPart_b = PositiveIntegerOperation.removePostZero(frcPart_b);

            // len of int part: a < b
            if(intPart_a.length() < intPart_b.length())
                return !a.isNeg ? -1 : 1;
            // len of int part: a > b
            else if(intPart_a.length() > intPart_b.length())
                return !a.isNeg ? 1 : -1;
            else {
                for(int k = 0; k < intPart_a.length(); ++k) {
                    if(intPart_a.charAt(k) < intPart_b.charAt(k))
                        return !a.isNeg ? -1 : 1;
                    else if(intPart_a.charAt(k) > intPart_a.charAt(k))
                        return !a.isNeg ? 1 : -1;
                }

                // int parts are the same
                int idx = 0;
                for(; idx < frcPart_a.length() && idx < frcPart_b.length(); ++idx) {
                    if(frcPart_a.charAt(idx) < frcPart_b.charAt(idx))
                        return !a.isNeg ? -1 : 1;
                    else if(frcPart_a.charAt(idx) > frcPart_b.charAt(idx))
                        return !a.isNeg ? 1 : -1;
                }

                if(idx == frcPart_a.length() && idx == frcPart_b.length())
                    return 0;
                else if(idx == frcPart_a.length())
                    return !a.isNeg ? -1 : 1;
                else
                    return !a.isNeg ? 1 : -1;
            }
        } catch (Exception e) {
            throw new ArithmeticException();
        }
    }

    // ===================================================================================

    // reduct (yue fen)
    private void reduct_update() throws ArithmeticException {
        try {
            String gcd = PositiveIntegerOperation.gcd(numerator, denominator);
            numerator = PositiveIntegerOperation.divide(numerator, gcd);
            denominator = PositiveIntegerOperation.divide(denominator, gcd);
            numerator = PositiveIntegerOperation.removePreZero(numerator);
            denominator = PositiveIntegerOperation.removePreZero(denominator);
            toFractionStyle_update(numerator, denominator);
        } catch (ArithmeticException e) {
            throw e;
        }
    }

    // convert numerator and denominator to fraction style without signed
    private void toFractionStyleWithoutSigned_update(String a, String b) throws ArithmeticException {
        fractionStyle = PositiveIntegerOperation.divide(a, b, decimalPrecision, showRedundantDecimal);
    }

    // convert numerator and denominator to fraction style
    private void toFractionStyle_update(String a, String b) throws ArithmeticException {
        toFractionStyleWithoutSigned_update(a, b);
        if(PositiveIntegerOperation.isZero(a))
            isNeg = false;
        fractionStyle = (isNeg ? "-" : "") + fractionStyle;
    }

    // convert numerator and denominator to scientific notation style
    private void toScientificNotationStyle_update(String a, String b) throws ArithmeticException {
        // ensure that fraction style is built
        if(fractionStyle == null || fractionStyle.equals(""))
            toFractionStyle_update(a, b);

        boolean isNegative = false;
        int dotIdx;
        String fraction = fractionStyle;
        StringBuilder intPartBuf = new StringBuilder();
        StringBuilder frcPartBuf = new StringBuilder();
        int exponential = 0;

        if(fraction.charAt(0) == '-') {
            isNegative = true;
            fraction = fraction.substring(1);
        }

        dotIdx = fraction.indexOf(".");
        // no decimal point, it is an integer
        if(dotIdx == -1) {
            intPartBuf.append(fraction);
            frcPartBuf.append(0);
        }
        // has decimal point
        else {
            intPartBuf.append(fraction.substring(0, dotIdx));
            frcPartBuf.append(fraction.substring(dotIdx + 1));
        }

        // intPart is 0, frcPart is 0 as well
        if(PositiveIntegerOperation.isZero(intPartBuf.toString()) && PositiveIntegerOperation.isZero(frcPartBuf.toString())) {
            isNegative = false;
        }
        // intPart is 0
        else if(PositiveIntegerOperation.isZero(intPartBuf.toString())) {
            do {
                --exponential;
                intPartBuf.append(frcPartBuf.toString().charAt(0));
                frcPartBuf.deleteCharAt(0);
            } while(frcPartBuf.length() > 0 && intPartBuf.charAt(intPartBuf.length() - 1) == '0');
            if(frcPartBuf.length() == 0)
                frcPartBuf.append(0);
        }
        // intPart is not 0
        else {
            while(intPartBuf.length() > 1) {
                ++exponential;
                frcPartBuf.insert(0, intPartBuf.charAt(intPartBuf.length() - 1));
                intPartBuf.deleteCharAt(intPartBuf.length() - 1);
            }
        }

        String intPart = PositiveIntegerOperation.removePreZero(intPartBuf.toString());
        String frcPart = PositiveIntegerOperation.removePostZero(frcPartBuf.toString());

        if(PositiveIntegerOperation.isZero(frcPart)) {
            if(exponential == 0)
                scientificNotationStyle = (isNegative ? "-" : "") + intPart;
            else
                scientificNotationStyle = (isNegative ? "-" : "") + intPart + "e" + String.valueOf(exponential);
        }
        else {
            if(exponential == 0)
                scientificNotationStyle = (isNegative ? "-" : "") + intPart + "." + frcPart;
            else
                scientificNotationStyle = (isNegative ? "-" : "") + intPart + "." + frcPart + "e" + String.valueOf(exponential);
        }
    }

    @Override
    public String toString() {
        return (isNeg ? "-(" : "(") + numerator + "/" + denominator + ")";
    }
}
