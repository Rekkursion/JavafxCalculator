package sample;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PositiveIntegerOperation {
    // integer add
    public static String add(String a, String b) {
        int carry = 0;
        StringBuilder resultBuf = new StringBuilder();

        a = removePreZero(a);
        b = removePreZero(b);

        for(int k = a.length() - 1, j = b.length() - 1; k >= 0 || j >= 0 || carry > 0; --k, --j) {
            int val_a = k < 0 ? 0 : a.charAt(k) - '0';
            int val_b = j < 0 ? 0 : b.charAt(j) - '0';
            resultBuf.append((val_a + val_b + carry) % 10);
            carry = (val_a + val_b + carry) / 10;
        }
        resultBuf.reverse();

        if(resultBuf.toString().equals(""))
            resultBuf.append(0);
        return resultBuf.toString();
    }

    // integer minus
    public static String minus(String a, String b) {
        boolean isNegative = false;
        StringBuilder resultBuf = new StringBuilder();
        String result;
        int borrow = 0;

        a = removePreZero(a);
        b = removePreZero(b);

        if(compareTwoPositiveIntegers(a, b) < 0) {
            isNegative = true;
            String tmp = a; a = b; b = tmp;
        }

        for(int k = a.length() - 1, j = b.length() - 1; k >= 0 || j >= 0; --k, --j) {
            int val_a = (k < 0 ? 0 : a.charAt(k) - '0') - borrow;
            int val_b = j < 0 ? 0 : b.charAt(j) - '0';
            resultBuf.append((val_a < val_b) ? 10 + val_a - val_b : val_a - val_b);
            borrow = (val_a < val_b) ? 1 : 0;
        }

        result = PositiveIntegerOperation.removePreZero(resultBuf.reverse().toString());
        return isNegative ? "-" + result : result;
    }

    // integer multiple
    public static String multiple(String a, String b) {
        a = removePreZero(a);
        b = removePreZero(b);
        if(isZero(a) || isZero(b))
            return "0";

        int[] reversedResult = new int[a.length() + b.length()];
        StringBuilder resultBuf = new StringBuilder();

        int i = 0;
        for(int k = a.length() - 1, offset = 0; k >= 0; --k, ++offset) {
            i = offset;
            for(int j = b.length() - 1; j >= 0; --j, ++i) {
                int ret = (a.charAt(k) - '0') * (b.charAt(j) - '0');
                reversedResult[i] += ret % 10;
                reversedResult[i + 1] += ret / 10;

                if(reversedResult[i] >= 10) {
                    reversedResult[i + 1] += reversedResult[i] / 10;
                    reversedResult[i] %= 10;
                }
            }
        }

        for(int k = reversedResult[i] == 0 ? i - 1 : i; k >= 0; --k)
            resultBuf.append(reversedResult[k]);

        return resultBuf.toString();
    }

    // integer divide and mod
    public static String[] divideAndMod(String a, String b) throws ArithmeticException {
        if(isZero(b))
            throw new ArithmeticException();

        a = removePreZero(a);
        b = removePreZero(b);
        if(compareTwoPositiveIntegers(a, b) < 0) {
            String[] ret = {"0", a};
            return ret;
        }

        int cursor = b.length();
        StringBuilder resultBuf = new StringBuilder();

        while(cursor <= a.length()) {
            String minuend = a.substring(0, cursor);
            String subtrahend = b;
            int quot = 0;

            while(compareTwoPositiveIntegers(minuend, subtrahend) >= 0) {
                ++quot;
                minuend = minus(minuend, subtrahend);
            }
            a = new StringBuilder(a).replace(0, cursor, Stream.iterate("0", ch -> "0").limit(cursor - minuend.length()).collect(Collectors.joining("")) + minuend).toString();

            resultBuf.append(quot);
            ++cursor;

            //System.out.println("quot = " + quot + " | a = " + a);
        }

        String[] ret = {removePreZero(resultBuf.toString()), removePreZero(a)};
        return ret;
    }

    // integer divide
    public static String divide(String a, String b) throws ArithmeticException {
        return divideAndMod(a, b)[0];
    }

    // real divide
    public static String divide(String a, String b, int precision, boolean showRedundantDecimal) throws ArithmeticException {
        String[] div_mod = divideAndMod(a, b);
        String intDivResult = div_mod[0];
        String frcDivResult;
        String minuend = div_mod[1];
        StringBuilder frcDivResultBuf = new StringBuilder();
        int cursor = 0;

        while(cursor < precision + 1) {
            minuend += "0";

            int quot = 0;
            while(compareTwoPositiveIntegers(minuend, b) >= 0) {
                ++quot;
                minuend = minus(minuend, b);
            }

            frcDivResultBuf.append(quot);
            ++cursor;
        }

        frcDivResult = frcDivResultBuf.toString();
        // si she wu ru
        if(frcDivResult.charAt(frcDivResult.length() - 1) >= '5') {
            // only a digit in fraction part: integer part plus 1 and set fraction part to zero
            if(frcDivResult.length() == 1) {
                intDivResult = add(intDivResult, "1");
                frcDivResult = "0";
            }
            // many digits (>= 2) in fraction part
            else {
                int origLen = frcDivResult.length() - 1;
                frcDivResult = add(frcDivResult.substring(0, frcDivResult.length() - 1), "1");
                if(frcDivResult.length() < origLen)
                    frcDivResult = Stream.iterate("0", ch -> "0").limit(origLen - frcDivResult.length()).collect(Collectors.joining("")) + frcDivResult;

                // carry to integer part
                if(frcDivResult.matches("^10+")) {
                    intDivResult = add(intDivResult, "1");
                    frcDivResult = frcDivResult.substring(1);
                }
            }
        }
        // discard the last digit (else if (frcDivResult.charAt(frcDivResult.length() - 1) < '5') )
        else
            frcDivResult = frcDivResult.substring(0, frcDivResult.length() - 1);

        frcDivResult = removePostZero(frcDivResult);
        // put redundant zeroes to the tail if user want to show redundant zeroes
        if(frcDivResult.length() < precision && showRedundantDecimal)
            frcDivResult += Stream.iterate("0", ch -> "0").limit(precision - frcDivResult.length()).collect(Collectors.joining(""));

        if(showRedundantDecimal)
            return precision == 0 ? intDivResult : intDivResult + "." + frcDivResult;
        else
            return isZero(frcDivResult) || precision == 0 ? intDivResult : intDivResult + "." + frcDivResult;
    }

    // integer mod
    public static String mod(String a, String b) throws ArithmeticException {
        return divideAndMod(a, b)[1];
    }

    // get GCD
    public static String gcd(String a, String b) throws ArithmeticException {
        if(isZero(b))
            throw new ArithmeticException();

        a = removePreZero(a);
        b = removePreZero(b);

        while(compareTwoPositiveIntegers(b, "0") > 0) {
            String remainder = mod(a, b);
            a = b;
            b = remainder;
        }

        return a;
    }

    // get LCM
    public static String lcm(String a, String b) throws ArithmeticException {
        return divide(multiple(a, b), gcd(a, b));
    }

    // compare two positive integers
    public static int compareTwoPositiveIntegers(String a, String b) {
        a = removePreZero(a);
        b = removePreZero(b);

        int lenA = a.length();
        int lenB = b.length();

        if(lenA < lenB)
            return -1;
        if(lenA > lenB)
            return 1;
        for(int k = 0; k < lenA; ++k) {
            if(a.charAt(k) < b.charAt(k))
                return -1;
            if(a.charAt(k) > b.charAt(k))
                return 1;
        }

        return 0;
    }

    // remove zeroes from the head
    public static String removePreZero(String str) {
        if(!str.startsWith("0"))
            return str;
        str = str.replaceAll("^0*", "");
        if(str.length() == 0)
            str += "0";
        return str;
    }

    // remove zeroes from the tail
    public static String removePostZero(String str) {
        if(!str.endsWith("0"))
            return str;
        str = str.replaceAll("0*$", "");
        if(str.length() == 0)
            str += "0";
        return str;
    }

    // check if the value is zero
    public static boolean isZero(String str) {
        return str.matches("0+");
    }
}