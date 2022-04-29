package mihailris.runexp.treeexps;

import mihailris.runexp.Expression;

public class ExpUnaryOperator implements Expression {
    final Expression a;

    public ExpUnaryOperator(Expression a) {
        this.a = a;
    }

    @Override
    public float eval(float x) {
        return -a.eval(x);
    }
}
