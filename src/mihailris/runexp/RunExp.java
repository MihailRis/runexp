package mihailris.runexp;

import java.util.*;

/**
 * Created by MihailRis 10th April 2021
 * GitHub Repository: https://github.com/MihailRis/runexp
 */
public class RunExp {
    public static final String VERSION_STRING = "1.0";
    public static final int VERSION = 1;

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
            addBuiltin("abs", Math.class, "abs", float.class);
            addBuiltin("sin", Math.class, "sin", double.class);
            addBuiltin("cos", Math.class, "cos", double.class);
            addBuiltin("tan", Math.class, "tan", double.class);
            addBuiltin("exp", Math.class, "exp", double.class);
            addBuiltin("sqrt", Math.class, "sqrt", double.class);
            addBuiltin("pow", Math.class, "pow", double.class, double.class);
            addBuiltin("sign", Math.class, "signum", float.class);
            builtinFunctions.put("signum", builtinFunctions.get("sign"));
            addBuiltin("min", Math.class, "min", float.class, float.class);
            addBuiltin("max", Math.class, "max", float.class, float.class);
            addBuiltin("round", Math.class, "round", double.class);
            addBuiltin("floor", Math.class, "floor", double.class);
            addBuiltin("ceil", Math.class, "ceil", double.class);

            addBuiltin("rand", ExpMaths.class, "rand", float.class);
            addBuiltin("smoother", ExpMaths.class, "smoother", float.class);
        }
        catch (NoSuchMethodException e){
            throw new RuntimeException(e);
        }
        solver = new RunExpSolver();
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
            case "round": return Math.round(args[0]);
            case "floor": return (float) Math.floor(args[0]);
            case "ceil": return (float) Math.ceil(args[0]);
            case "rand": return ExpMaths.rand(args[0]);
            case "smoother": return ExpMaths.smoother(args[0]);
        }
        throw new IllegalStateException();
    }

    private static void addBuiltin(String name, Class<?> klass, String methodName, Class<?>... args) throws NoSuchMethodException {
        builtinFunctions.put(name, new RunExpFunction(name, klass, methodName, args, true));
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
     * Internal debug method for visualize parsed syntax tree of ExpNode
     * @param nodes subnodes of root node (any node with subnodes)
     * @param indent root node indentation in resulting string
     * @return text visualization of parsed syntax tree
     */
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
