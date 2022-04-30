package mihailris.runexp;

/**
 * Expression wrapper for compiled constant expressions with float value as result
 */
public class ConstantExpression implements Expression {
    private final float value;

    public ConstantExpression(float value) {
        this.value = value;
    }

    @Override
    public float eval(float x) {
        return value;
    }

    public float getValue() {
        return value;
    }
}
