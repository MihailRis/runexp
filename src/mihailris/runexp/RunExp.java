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
    public static final int VERSION = 1;
    public static boolean verbose;
    private static final Tokenizer tokenizer = new Tokenizer();
    static Set<String> xAliases = new HashSet<>();

    static {
        for (char c = 'a'; c <= 'z'; c++) {
            xAliases.add(String.valueOf(c));
        }
    }

    public static void addXAlias(String alias){
        xAliases.add(alias);
    }

    public static float eval(String code) throws ExpCompileException {
        return compile(code, true).eval(0);
    }

    public static Expression compile(String code) throws ExpCompileException {
        return compile(code, false);
    }

    public static Expression compile(String code, boolean constant) throws ExpCompileException {
        List<Token> tokens = tokenizer.perform(code);
        ExpNode root = Parser.parse(tokens, constant);
        if (root.nodes.isEmpty()){
            throw new ExpCompileException("empty expression", 0, ERR_EMPTY_EXPRESSION);
        }
        System.out.println("simplified: "+root.toStringExpression());
        if (RunExp.verbose)
            System.out.println(ast2Str(root.nodes, 0));
        Expression expression;
        try {
            expression = JvmCompiler.compile(root.get(0));
        } catch (Exception e){
            System.err.println("could not to compile expression into java bytecode");
            e.printStackTrace();

            expression = Compiler.compile(root.get(0));

            if (verbose)
                System.out.println("RunExp.compile "+ Arrays.toString(((CompiledExpression)expression).bytecode));
        }
        return expression;
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
