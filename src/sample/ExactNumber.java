package sample;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExactNumber {
    public String numerator;
    public String denominator;
    public String fractionStyle;
    public boolean isNeg;

    // default constructor
    public ExactNumber() { this("1", "1", false); }

    // constructor: 2 strings as numerator and denominator respectively
    public ExactNumber(String nume, String deno) {
        boolean numeIsNeg = (nume.length() > 0 && nume.charAt(0) == '-');
        boolean denoIsNeg = (deno.length() > 0 && deno.charAt(0) == '-');

        numerator = PositiveIntegerOperation.removePreZero(numeIsNeg ? nume.substring(1) : nume);
        denominator = PositiveIntegerOperation.removePreZero(denoIsNeg ? deno.substring(1) : deno);
        isNeg = (numeIsNeg ^ denoIsNeg) && !PositiveIntegerOperation.isZero(numerator);
        fractionStyle = ExactNumber.toFractionStyle(numerator, denominator);
    }

    // constructor: 2 strings as numerator and denominator respectively, and 1 boolean as if it's negative
    public ExactNumber(String nume, String deno, boolean isNegative) {
        this(nume, deno);
        isNeg = isNegative;
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
        fractionStyle = ExactNumber.toFractionStyle(numerator, denominator);

        //System.err.println((isNeg ? "-" : "") + numerator + "/" + denominator);
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
        return new ExactNumber();
    }

    // reduct (yue fen)
    private void reduct_update() throws ArithmeticException {
        try {
            String gcd = PositiveIntegerOperation.gcd(numerator, denominator);
            numerator = PositiveIntegerOperation.divide(numerator, gcd);
            denominator = PositiveIntegerOperation.divide(denominator, gcd);
        } catch (ArithmeticException e) {
            throw e;
        }
    }

    // convert numerator and denominator to fraction style
    private static String toFractionStyle(String a, String b) {
        return "9";
    }

    @Override
    public String toString() {
        return (isNeg ? "-(" : "(") + numerator + "/" + denominator + ")";
    }
}
