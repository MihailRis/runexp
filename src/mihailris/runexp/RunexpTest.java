package mihailris.runexp;

public class RunexpTest {
    public static void main(String[] args) throws ExpCompileException {
        String text = "(x*sin(x))";
        Expression expression = RunExp.compile(text);
        System.out.println("f(x) = "+text);
        System.out.println("f(0) = "+expression.eval(0));
        System.out.println("f(-1) = "+ expression.eval(-1));
        System.out.println("f(1) = "+expression.eval(1));
        System.out.println("f(PI) = "+expression.eval((float) Math.PI));
        System.out.println("f(0.5) = "+expression.eval(0.5f));
    }
}
