package mihailris.runexp.tests;

import mihailris.runexp.ExpCompileException;
import mihailris.runexp.Expression;
import mihailris.runexp.RunExp;

public class Benchmark {
    public static void main(String[] args) throws ExpCompileException {
        int iterations = 10_000_000;
        System.out.println(iterations+" iterations");
        String expressionString = "x + abs(x - 1) * max(x*3, x+x^2)";
        RunExp.allowJVM = true;
        System.out.println("RunExp.allowJVM=true: "+test(expressionString, iterations)+" ms");
        RunExp.allowJVM = false;
        System.out.println("RunExp.allowJVM=false: "+test(expressionString, iterations)+" ms");
    }

    private static long test(String expressionString, int iterations) throws ExpCompileException {
        Expression expression = RunExp.compile(expressionString);
        long tm = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            expression.eval(42);
        }
        return System.currentTimeMillis() - tm;
    }
}
