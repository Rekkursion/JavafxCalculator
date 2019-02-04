package sample;

public class NumberTooBigException extends Exception {
    public NumberTooBigException() {
        super("Number too big");
    }

    public NumberTooBigException(String message) {
        super(message);
    }

    public NumberTooBigException(String message, Throwable throwable) {
        super(message, throwable);
    }
}