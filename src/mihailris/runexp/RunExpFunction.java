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
    RunExpReflection.Type returns;

    Class<?>[] argsClasses;
    Class<?> klass;
    Method method;

    RunExpFunction(String name, Class<?> klass, String methodName, Class<?>[] args) throws NoSuchMethodException {
        this(name, klass, methodName, args, false);
    }

    RunExpFunction(String name, Class<?> klass, String methodName, Class<?>[] args, boolean isBuiltin) throws NoSuchMethodException {
        if (args == null){
            Method[] methods = klass.getMethods();
            Method methodFound = null;
            for (Method method : methods){
                if (method.getName().equals(methodName)){
                    if (methodFound != null)
                        throw new NoSuchMethodException("method '"+method+"' has overloads, use other constructor");
                    methodFound = method;
                }
            }
            if (methodFound == null)
                throw new NoSuchMethodException(klass+" has no suitable '"+methodName+"' method");
            args = methodFound.getParameterTypes();
        }
        final int argCount = args.length;
        argsClasses = args;

        this.name = name;
        this.argCount = argCount;
        this.className = klass.getName();
        this.methodName = methodName;
        this.isBuiltin = isBuiltin;

        StringBuilder format = new StringBuilder("(");
        for (Class<?> arg : args) {
            RunExpReflection.Type argType = RunExpReflection.getValueType(arg);
            if (argType == null)
                throw new IllegalArgumentException("unsupported argument type " + arg);
            format.append(argType.signature);
        }
        format.append(')');

        this.klass = klass;
        this.method = klass.getMethod(methodName, argsClasses);

        Class<?> returnType = method.getReturnType();
        if (returnType.isArray()){
            throw new IllegalArgumentException("method '"+methodName+"' returns an array");
        }
        for (RunExpReflection.Type type : RunExpReflection.Type.values()){
            if (returnType.equals(type.klass)){
                returns = type;
                format.append(type.signature);
                break;
            }
        }
        if (returns == null){
            throw new IllegalArgumentException("unsupported return type '"+returnType+"'");
        }
        this.argsFormat = format.toString();
    }


}
