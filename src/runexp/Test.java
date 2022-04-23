package runexp;

import java.util.Scanner;

public class Test {
    public static void main(String[] args){
        RunExp.verbose = false;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(">: ");
            String expr = scanner.nextLine();
            if (expr.equals("quit") || expr.equals("exit"))
                break;
            try {
                Expression expression = RunExp.compile(expr);
                System.out.println("formula: "+expr);
                while (true){
                    System.out.print("x = ");
                    String value = scanner.nextLine().replace('\n', ' ').trim();
                    if (value.isEmpty() || value.equals("next"))
                        break;
                    float arg;
                    try {
                        arg = Float.parseFloat(value);
                    } catch (NumberFormatException e){
                        try {
                            arg = RunExp.eval(value);
                        } catch (ExpCompileException e1){
                            System.out.println(e1.message);
                            continue;
                        }
                    }
                    float result = expression.eval(arg);
                    System.out.println("-> "+result);
                }
                System.out.println(RunExp.compile(expr).eval(1));
            } catch (ExpCompileException e) {
                System.out.println(e.message+" at " +e.pos + " ["+e.errorCode+"]");
            }
        }
    }
}
