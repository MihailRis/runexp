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
    ReturnType returns;

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
        format.append(')');

        this.klass = klass;
        this.method = klass.getMethod(methodName, argsClasses);

        Class<?> returnType = method.getReturnType();
        if (returnType.isArray()){
            throw new IllegalArgumentException("method '"+methodName+"' returns an array");
        }
        for (ReturnType type : ReturnType.values()){
            if (returnType.equals(type.klass)){
                returns = type;
                format.append(type.notation);
                break;
            }
        }
        if (returns == null){
            throw new IllegalArgumentException("unsupported return type '"+returnType+"'");
        }
        this.argsFormat = format.toString();
    }

    enum ReturnType {
        FLOAT(float.class, "F"),
        DOUBLE(double.class, "D"),
        CHAR(char.class, "I"),
        SHORT(short.class, "I"),
        INT(int.class, "I"),
        LONG(long.class, "J"),
        ;
        Class<?> klass;
        String notation;
        ReturnType(Class<?> klass, String notation){
            this.klass = klass;
            this.notation = notation;
        }
    }
}
