package mihailris.runexp;

public class RunExpFunction {
    String name;
    int argCount;

    // jvm
    String className;
    String methodName;
    String argsFormat;
    boolean isDouble;

    public RunExpFunction(String name, int argCount, String className, String methodName, boolean isDouble) {
        this.name = name;
        this.argCount = argCount;
        this.className = className;
        this.methodName = methodName;
        this.argsFormat = argCount == 1 ? "(F)F" : "(FF)F";
        if (isDouble){
            this.argsFormat = argsFormat.replaceAll("F", "D");
        }
        this.isDouble = isDouble;
    }
}
