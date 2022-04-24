package mihailris.runexp;

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
