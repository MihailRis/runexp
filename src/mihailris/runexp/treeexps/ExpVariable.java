package mihailris.runexp.treeexps;

import mihailris.runexp.Expression;

public class ExpVariable implements Expression {
    public static final ExpVariable INSTANCE = new ExpVariable();

    @Override
    public float eval(float x) {
        return x;
    }
}
