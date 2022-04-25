package mihailris.runexp;


import mihailris.runexp.treeexps.ExpBinaryOperator;
import mihailris.runexp.treeexps.ExpCall;
import mihailris.runexp.treeexps.ExpUnaryOperator;
import mihailris.runexp.treeexps.ExpVariable;

public class Compiler {
    public Expression compile(ExpNode root){
        if (root.command == null){
            if (root.token != null) {
                if (root.token.tag == Token.Tag.VALUE) {
                    return new ConstantExpression(root.token.value);
                } else if (root.token.tag == Token.Tag.VARIABLE) {
                    return ExpVariable.INSTANCE;
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
}
