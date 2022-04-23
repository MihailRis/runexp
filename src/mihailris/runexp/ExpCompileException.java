package mihailris.runexp;

public class ExpCompileException extends Exception {
    public final int pos;
    public final int errorCode;
    public final String message;

    public ExpCompileException(String message, int pos, int errorCode) {
        super(message);
        this.pos = pos;
        this.errorCode = errorCode;
        this.message = message;
    }
}