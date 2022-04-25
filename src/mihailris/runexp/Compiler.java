package mihailris.runexp;


import mihailris.runexp.treeexps.ExpBinaryOperator;
import mihailris.runexp.treeexps.ExpCall;
import mihailris.runexp.treeexps.ExpUnaryOperator;
import mihailris.runexp.treeexps.ExpVariable;

public class Compiler {
    private final byte[] bytecode = new byte[256];
    private final RunExpFunction[] functions = new RunExpFunction[256];
    private final float[] constants = new float[256];

    private int bytecodeLength = 0;
    private int constantsCount = 0;
    private int functionsCount = 0;

    private int pushConstant(float constant){
        for (int i = 0; i < constantsCount; i++) {
            if (constants[i] == constant)
                return i;
        }
        constants[constantsCount] = constant;
        return constantsCount++;
    }

    private int pushFunction(RunExpFunction function) {
        for (int i = 0; i < functionsCount; i++) {
            if (functions[i] == function)
                return i;
        }
        functions[functionsCount] = function;
        return functionsCount++;
    }

    public CompiledExpression compileBytecode(ExpNode root){
        if (root.command == null){
            if (root.token != null) {
                if (root.token.tag == Token.Tag.VALUE) {
                    int constant = pushConstant(root.token.value);
                    bytecode[bytecodeLength++] = C_CONST;
                    bytecode[bytecodeLength++] = (byte) (constant);
                } else if (root.token.tag == Token.Tag.VARIABLE) {
                    bytecode[bytecodeLength++] = C_X;
                }
            } else {
                for (ExpNode node : root.nodes){
                    compileBytecode(node);
                }
            }
        } else {
            if (root.command.tag == Token.Tag.OPERATOR){
                for (ExpNode node : root.nodes){
                    compileBytecode(node);
                }
                if (root.nodes.size() == 1){
                    bytecode[bytecodeLength++] = (byte) unaryOp(root.command.string);
                } else {
                    bytecode[bytecodeLength++] = (byte) binaryOp(root.command.string);
                }
            } else if (root.command.tag == Token.Tag.FUNCTION){
                RunExpFunction function = RunExp.functions.get(root.command.string);
                int functionIndex = pushFunction(function);
                for (ExpNode node : root.nodes){
                    compileBytecode(node);
                }
                bytecode[bytecodeLength++] = C_CALL;
                bytecode[bytecodeLength++] = (byte) functionIndex;
            }
        }
        return new CompiledExpression(bytecode, constants, functions);
    }

    public Expression compile(ExpNode root){
        if (root.command == null){
            if (root.token != null) {
                if (root.token.tag == Token.Tag.VALUE) {
                    return new ConstantExpression(root.token.value);
                } else if (root.token.tag == Token.Tag.VARIABLE) {
                    return new ExpVariable();
                }
            } else {
                assert (root.nodes.size() == 1);
                return compile(root.nodes.get(0));
            }
        } else {
            if (root.command.tag == Token.Tag.OPERATOR){
                if (root.nodes.size() == 1){
                    return new ExpUnaryOperator(compile(root.nodes.get(0)));
                }
                return new ExpBinaryOperator(compile(root.nodes.get(0)),
                                             compile(root.nodes.get(1)),
                                             root.command.string.charAt(0));
            } else if (root.command.tag == Token.Tag.FUNCTION){
                RunExpFunction function = RunExp.functions.get(root.command.string);
                Expression[] args = new Expression[function.argCount];
                for (int i = 0; i < args.length; i++) {
                    args[i] = compile(root.nodes.get(i));
                }
                return new ExpCall(function, args);
            }
        }
        throw new IllegalStateException();
    }

    public static final int C_CONST = 1;
    public static final int C_X = 2;
    public static final int C_NEG = 3;

    public static final int C_MUL = 4;
    public static final int C_DIV = 8;
    public static final int C_ADD = 12;
    public static final int C_SUB = 16;
    public static final int C_MOD = 20;
    public static final int C_POW = 24;
    public static final int C_CALL = 28;

    public static int unaryOp(String text) {
        if ("-".equals(text)) {
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
            case "^": return C_POW;
        }
        throw new IllegalStateException(text);
    }
}
