package runexp;

class TestExpression implements Expression {
    @Override
    public float eval(float x) {
        return Math.signum(x);
    }
}
