package sample;

import javafx.geometry.Pos;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        isNeg = (num.length() > 0 && num.charAt(0) == '-');
        if(isNeg)
            num = num.substring(1);

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
    public static void setDecimalPrecision(int newDecimalPrecision) {
        decimalPrecision = newDecimalPrecision;
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

    // reduct (yue fen)
    private void reduct_update() throws ArithmeticException {
        try {
            String gcd = PositiveIntegerOperation.gcd(numerator, denominator);
            numerator = PositiveIntegerOperation.divide(numerator, gcd);
            denominator = PositiveIntegerOperation.divide(denominator, gcd);
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
            scientificNotationStyle = "0.0";
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
            // TODO: variable name strict cannot use e or E, convert scientific notation style to fraction style
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
