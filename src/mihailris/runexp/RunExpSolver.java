package mihailris.runexp;

import java.util.*;

import static mihailris.runexp.ExpConstants.ERR_EMPTY_EXPRESSION;

/**
 * Isolated expressions compier/solver<br>
 * Implementation of produced expressions may contain reference to solver
 */
public class RunExpSolver {
    private final Tokenizer tokenizer;
    private final Parser parser;
    private final Compiler compiler;
    private final JvmCompiler jvmCompiler;
    /**
     * allow RunExp compile expressions into JVM-bytecode for the best performance<br>
     * not used for constant expressions (with no X)
     */
    public boolean allowJVM = true;
    /**
     * print debug information such as look of parsed expression
     */
    public boolean verbose = false;
    final Map<String, Float> constants;
    final Map<String, RunExpFunction> functions;
    final Set<String> xAliases;

    public RunExpSolver(){
        this.tokenizer = new Tokenizer();
        this.parser = new Parser(this);
        this.compiler = new Compiler(this);
        this.jvmCompiler = new JvmCompiler(this);

        this.constants = new HashMap<>();
        this.functions = new HashMap<>();
        this.xAliases = new HashSet<>();

        constants.putAll(RunExp.builtinConstants);
        functions.putAll(RunExp.builtinFunctions);
        xAliases.addAll(RunExp.builtinXAliases);
    }

    private ExpNode parse(String code, boolean constant) throws ExpCompileException {
        List<RawToken> tokens = tokenizer.perform(code);
        ExpNode root = parser.parse(tokens, constant);
        if (root.nodes.isEmpty() && root.token == null){
            throw new ExpCompileException("empty expression", 0, ERR_EMPTY_EXPRESSION);
        }
        return root;
    }

    /**
     * @param name constant name
     * @param value constant value (will be inlined in compiled expressions)
     */
    public void addConstant(String name, float value){
        constants.put(name, value);
    }

    /**
     * @param name name of function in expressions
     * @param sourceClass class that contains target method
     * @param methodName name of method that will be called as function
     * @param args arguments classes used to choose correct method overload
     * @throws NoSuchMethodException will be trown if there's no such method at given class
     */
    public void addFunction(String name, Class<?> sourceClass, String methodName, Class<?>... args) throws NoSuchMethodException {
        functions.put(name, new RunExpFunction(name, sourceClass, methodName, args));
    }

    /**
     * Use other method overload if target method has overloads
     * @param name name of function in expressions
     * @param sourceClass class that contains target method
     * @param methodName name of method that will be called as function
     * @throws NoSuchMethodException will be trown if there's no such method at given class
     */
    public void addFunction(String name, Class<?> sourceClass, String methodName) throws NoSuchMethodException {
        functions.put(name, new RunExpFunction(name, sourceClass, methodName, null));
    }

    public void addXAlias(String alias){
        xAliases.add(alias);
    }

    /**
     * @param code expression string
     * @return calculated constant value for given expression
     * @throws ExpCompileException when an error ocurred during expression parsing
     */
    public float eval(String code) throws ExpCompileException {
        return parse(code, true).token.value;
    }

    /**
     * @param code expression string
     * @return expression
     * @throws ExpCompileException when an error ocurred during expression parsing
     */
    public Expression compile(String code) throws ExpCompileException {
        return compile(code, false);
    }

    public Expression compile(String code, boolean constant) throws ExpCompileException {
        ExpNode root = parse(code, constant);
        assert (root.token != null); // it's impossible, see parse(..)
        if (root.nodes.isEmpty() && root.token.tag == Token.Tag.VALUE) {
            return new ConstantExpression(root.token.value);
        }
        if (verbose) {
            System.out.println("parsed as: "+root.toStringExpression());
        }

        if (allowJVM) {
            try {
                return jvmCompiler.compile(root);
            } catch (Exception e) {
                System.err.println("could not to compile expression into java bytecode");
                e.printStackTrace();
            }
        }

        return compiler.compile(root);
    }
}
