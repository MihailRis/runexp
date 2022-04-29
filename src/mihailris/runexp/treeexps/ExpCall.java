package mihailris.runexp.treeexps;

import mihailris.runexp.Expression;
import mihailris.runexp.RunExpFunction;
import mihailris.runexp.RunExpReflection;

public class ExpCall implements Expression {
    private final RunExpFunction function;
    private final Expression[] args;
    private final float[] values;

    public ExpCall(RunExpFunction function, Expression[] args) {
        this.function = function;
        this.args = args;
        this.values = new float[args.length];
    }

    @Override
    public float eval(float x) {
        for (int i = 0; i < args.length; i++) {
            values[i] = args[i].eval(x);
        }
        return RunExpReflection.callFunc(function, values, 0, values.length);
    }
}
