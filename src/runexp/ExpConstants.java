package runexp;

public class ExpConstants {
    public static final int ERR_INVALID_IDENTIFIER = 1;
    public static final int ERR_INVALID_NUMBER = 2;
    public static final int ERR_UNKNOWN_CONSTANT = 3;
    public static final int ERR_UNKNOWN_NAME = 4;
    public static final int ERR_INVALID_BRACKET = 5;
    public static final int ERR_EMPTY_EXPRESSION = 6;

    public static final int C_CONST = 1;
    public static final int C_X = 2;
    public static final int C_NEG = 3;

    public static final int C_MUL = 4;
    public static final int C_DIV = 8;
    public static final int C_ADD = 12;
    public static final int C_SUB = 16;
    public static final int C_MOD = 20;

    public static final String BINARY_OPERATORS = "+-*^/%";
    public static final String POW_OP = "^";

    public static int unaryOp(String text) {
        switch (text){
            case "-":
                return C_NEG;
        }
        throw new IllegalStateException(text);
    }

    public static int binaryOp(String text) {
        switch (text){
            case "*": return C_MUL;
            case "/": return C_DIV;
            case "+": return C_ADD;
            case "-": return C_SUB;
            case "%": return C_MOD;
        }
        throw new IllegalStateException(text);
    }
}
