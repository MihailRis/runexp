package mihailris.runexp.treeexps;

import mihailris.runexp.Expression;

public class ExpVariable implements Expression {
    @Override
    public float eval(float x) {
        return x;
    }
}
