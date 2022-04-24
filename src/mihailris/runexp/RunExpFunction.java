package mihailris.runexp;

import java.lang.reflect.Method;

public class RunExpFunction {
    String name;
    int argCount;
    boolean isBuiltin;

    // jvm
    String className;
    String methodName;
    String argsFormat;
    boolean isDouble;

    Class<?>[] argsClasses;
    Class<?> klass;
    Method method;

    public RunExpFunction(String name, int argCount, Class<?> klass, String methodName, boolean isDouble) throws NoSuchMethodException {
        this(name, argCount, klass, methodName, isDouble, false);
    }

    RunExpFunction(String name, int argCount, Class<?> klass, String methodName, boolean isDouble, boolean isBuiltin) throws NoSuchMethodException {
        this.name = name;
        this.argCount = argCount;
        this.className = klass.getName();
        this.methodName = methodName;
        this.isDouble = isDouble;
        this.isBuiltin = isBuiltin;

        argsClasses = new Class<?>[argCount];
        StringBuilder format = new StringBuilder("(");
        for (int i = 0; i < argCount; i++) {
            if (isDouble){
                format.append('D');
                argsClasses[i] = double.class;
            } else {
                format.append('F');
                argsClasses[i] = float.class;
            }
        }
        if (isDouble)
            format.append(")D");
        else
            format.append(")F");
        this.argsFormat = format.toString();

        this.klass = klass;
        this.method = klass.getMethod(methodName, argsClasses);
    }
}
