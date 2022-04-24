package mihailris.runexp;

import java.util.*;

import static mihailris.runexp.ExpConstants.ERR_EMPTY_EXPRESSION;

/**
 * Created by MihailRis 10th April 2021
 * TODO:
 * - add double support
 * - add functions (expressions with multiple input values)
 */
public class RunExp {
    public static final String VERSION_STRING = "1.0";
    public static final int VERSION = 1;
    public static boolean verbose;
    public static boolean allowJVM = true;
    private static final Tokenizer tokenizer = new Tokenizer();
    static Map<String, Float> constants = new HashMap<>();
    static Map<String, RunExpFunction> functions = new HashMap<>();
    static Set<String> xAliases = new HashSet<>();

    private static final String MATH = "java.lang.Math";
    private static final String EXP_MATHS = "mihailris.runexp.ExpMaths";

    static {
        for (char c = 'a'; c <= 'z'; c++) {
            xAliases.add(String.valueOf(c));
        }
        constants.put("e", (float) Math.E);
        constants.put("pi", (float) Math.PI);
        constants.put("pi2", (float) (Math.PI * 2));
        constants.put("raddeg", (float) (180.0/Math.PI));
        constants.put("degrad", (float) (Math.PI/180.0));

        functions.put("sin", new RunExpFunction("sin", 1, MATH, "sin", true));
        functions.put("cos", new RunExpFunction("cos", 1, MATH, "cos", true));
        functions.put("tan", new RunExpFunction("tan", 1, MATH, "tan", true));
        functions.put("exp", new RunExpFunction("exp", 1, MATH, "exp", true));
        functions.put("sqrt", new RunExpFunction("sqrt", 1, MATH, "sqrt", true));
        functions.put("pow", new RunExpFunction("pow", 2, MATH, "pow", true));

        functions.put("sign", new RunExpFunction("sign", 1, MATH, "signum", false));
        functions.put("signum", new RunExpFunction("sign", 1, MATH, "signum", false));

        functions.put("min", new RunExpFunction("min", 2, MATH, "min", false));
        functions.put("max", new RunExpFunction("max", 2, MATH, "max", false));

        functions.put("rand", new RunExpFunction("rand", 1, EXP_MATHS, "rand", false));
        functions.put("smoother", new RunExpFunction("smoother", 1, EXP_MATHS, "smoother", false));
    }

    public static void addConstant(String name, float value){
        constants.put(name, value);
    }

    public static void addXAlias(String alias){
        xAliases.add(alias);
    }

    public static float eval(String code) throws ExpCompileException {
        return parse(code, true).token.value;
    }

    public static Expression compile(String code) throws ExpCompileException {
        return compile(code, false);
    }

    private static ExpNode parse(String code, boolean constant) throws ExpCompileException {
        List<RawToken> tokens = tokenizer.perform(code);
        ExpNode root = Parser.parse(tokens, constant);
        if (root.nodes.isEmpty()) {
            if (root.token == null)
                throw new ExpCompileException("empty expression", 0, ERR_EMPTY_EXPRESSION);
        }
        return root;
    }

    public static Expression compile(String code, boolean constant) throws ExpCompileException {
        ExpNode root = parse(code, constant);
        if (root.nodes.isEmpty() && root.token.tag == Token.Tag.VALUE) {
            return new ConstantExpression(root.token.value);
        }
        if (RunExp.verbose) {
            System.out.println("simplified: "+root.toStringExpression());
            System.out.println(ast2Str(root.nodes, 0));
        }

        if (allowJVM) {
            try {
                return JvmCompiler.compile(root);
            } catch (Exception e) {
                System.err.println("could not to compile expression into java bytecode");
                e.printStackTrace();
            }
        }

        return Compiler.compile(root);
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
