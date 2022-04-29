package mihailris.runexp.treeexps;

import mihailris.runexp.Expression;

public class ExpBinaryOperator implements Expression {
    final Expression a;
    final Expression b;
    final char operator;

    public ExpBinaryOperator(Expression a, Expression b, char operator) {
        this.a = a;
        this.b = b;
        this.operator = operator;
    }

    @Override
    public float eval(float x) {
        float valueA = a.eval(x);
        float valueB = b.eval(x);
        switch (operator){
            case '+': return valueA + valueB;
            case '-': return valueA - valueB;
            case '*': return valueA * valueB;
            case '/': return valueA / valueB;
            case '%': return valueA % valueB;
            case '^': return (float) Math.pow(valueA, valueB);
        }
        throw new IllegalStateException();
    }
}
