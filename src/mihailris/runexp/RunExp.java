package mihailris.runexp;

import java.util.*;

/**
 * Created by MihailRis 10th April 2021
 * TODO:
 * - add double support
 * - add functions (expressions with multiple input values)
 */
public class RunExp {
    public static final String VERSION_STRING = "1.0";
    public static final int VERSION = 1;
    /**
     * allow RunExp compile expressions into JVM-bytecode for the best performance<br>
     * not used for constant expressions (with no X)
     */


    static Map<String, Float> builtinConstants = new HashMap<>();
    static Map<String, RunExpFunction> builtinFunctions = new HashMap<>();
    static Set<String> builtinXAliases = new HashSet<>();
    public static RunExpSolver solver;

    static {
        for (char c = 'a'; c <= 'z'; c++) {
            builtinXAliases.add(String.valueOf(c));
        }
        builtinConstants.put("e", (float) Math.E);
        builtinConstants.put("pi", (float) Math.PI);
        builtinConstants.put("pi2", (float) (Math.PI * 2));
        builtinConstants.put("raddeg", (float) (180.0/Math.PI));
        builtinConstants.put("degrad", (float) (Math.PI/180.0));

        try {
            // declaring built-in functions
            // every built-in function must be added to switch in RunExp.callBuiltinFunc method
            builtinFunctions.put("abs", new RunExpFunction("abs", 1, Math.class, "abs", false, true));
            builtinFunctions.put("sin", new RunExpFunction("sin", 1, Math.class, "sin", true, true));
            builtinFunctions.put("cos", new RunExpFunction("cos", 1, Math.class, "cos", true, true));
            builtinFunctions.put("tan", new RunExpFunction("tan", 1, Math.class, "tan", true, true));
            builtinFunctions.put("exp", new RunExpFunction("exp", 1, Math.class, "exp", true, true));
            builtinFunctions.put("sqrt", new RunExpFunction("sqrt", 1, Math.class, "sqrt", true, true));
            builtinFunctions.put("pow", new RunExpFunction("pow", 2, Math.class, "pow", true, true));
            builtinFunctions.put("sign", new RunExpFunction("sign", 1, Math.class, "signum", false, true));
            builtinFunctions.put("signum", builtinFunctions.get("sign"));
            builtinFunctions.put("min", new RunExpFunction("min", 2, Math.class, "min", false, true));
            builtinFunctions.put("max", new RunExpFunction("max", 2, Math.class, "max", false, true));
            builtinFunctions.put("rand", new RunExpFunction("rand", 1, ExpMaths.class, "rand", false, true));
            builtinFunctions.put("smoother", new RunExpFunction("smoother", 1, ExpMaths.class, "smoother", false, true));
        }
        catch (NoSuchMethodException e){
            throw new RuntimeException(e);
        }
        solver = new RunExpSolver();
    }

    /**
     * @param expressionString target expression string
     * @return calculated constant value for given expression
     * @throws ExpCompileException when an error ocurred during expression parsing
     */
    public static float eval(String expressionString) throws ExpCompileException {
        return solver.eval(expressionString);
    }

    /**
     * @param expressionString target expression string
     * @return expression
     * @throws ExpCompileException when an error ocurred during expression parsing
     */
    public static Expression compile(String expressionString) throws ExpCompileException {
        return solver.compile(expressionString);
    }

    public static ConstantExpression compileConstant(String code) throws ExpCompileException {
        return (ConstantExpression) solver.compile(code, true);
    }

    /**
     * Created to avoid java-reflection GC-hell if no one custom function used when RunExp.allowJVM is false
     * @param name function name
     * @param args array of input values
     * @return result of function execution
     */
    static float callBuiltinFunc(String name, float[] args){
        switch (name){
            case "abs": return Math.abs(args[0]);
            case "sin": return (float) Math.sin(args[0]);
            case "cos": return (float) Math.cos(args[0]);
            case "tan": return (float) Math.tan(args[0]);
            case "exp": return (float) Math.exp(args[0]);
            case "sqrt": return (float) Math.sqrt(args[0]);
            case "pow": return (float) Math.pow(args[0], args[1]);
            case "sign":
            case "signum": return Math.signum(args[0]);
            case "min": return Math.min(args[0], args[1]);
            case "max": return Math.max(args[0], args[1]);
            case "rand": return ExpMaths.rand(args[0]);
            case "smoother": return ExpMaths.smoother(args[0]);
        }
        throw new IllegalStateException();
    }

    static String ast2Str(List<ExpNode> nodes, int indent){
        StringBuilder builder = new StringBuilder();
        for (ExpNode node : nodes){
            for (int i = 0; i < indent; i++)
                builder.append("  ");
            if (node.token != null){
                builder.append(node.token);
            } else {
                if (node.command == null)
                    builder.append("node (").append(node.getClass().getSimpleName()).append("):\n");
                else
                    builder.append("node ").append(node.command).append(":\n");
                if (node.nodes.isEmpty()){
                    for (int i = 0; i < indent+1; i++)
                        builder.append("  ");
                    builder.append("empty\n");
                }
                builder.append(ast2Str(node.nodes, indent+1));
                continue;
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
