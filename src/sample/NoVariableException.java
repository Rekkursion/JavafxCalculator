package sample;

public class NoVariableException extends Exception {
    public NoVariableException(String message) {
        super(message);
    }

    public NoVariableException(String message, Throwable throwable) {
        super(message, throwable);
    }
}