package sample;

public class NoFunctionException extends Exception {
    public NoFunctionException(String message) {
        super(message);
    }

    public NoFunctionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}