package mihailris.runexp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RunExpReflection {
    static float performOperation(float a, float b, String op){
        switch (op) {
            case "*": return a * b;
            case "^": return  (float) Math.pow(a, b);
            case "/": return a / b;
            case "%": return a % b;
            case "+": return a + b;
            case "-": return a - b;
            default:
                throw new IllegalStateException();
        }
    }

    private static Object castValue(float value, Class<?> arg){
        Type type = getValueType(arg);
        assert (type != null);
        switch (type){
            case FLOAT: return value;
            case DOUBLE: return (double)value;
            case INT: return (int)value;
            case LONG: return (long)value;
        }
        return value;
    }

    private static float castToFloat(Object value, Type type) {
        switch (type){
            case FLOAT: return (float)value;
            case DOUBLE: return (float)(double)value;
            case INT: return (float)(int)value;
            case LONG: return (float)(long)value;
        }
        throw new IllegalStateException(type.name());
    }

    public static float callFunc(RunExpFunction function, float[] args, int offset, int argc){
        if (function.isBuiltin)
            return RunExp.callBuiltinFunc(function.name, args);

        // anyway java reflection is a lot of pain for GC
        try {
            Method method = function.method;
            Object[] values = new Object[argc];

            for (int i = 0; i < argc; i++) {
                for (Class<?> arg : function.argsClasses){
                    values[i] = castValue(args[offset+i], arg);
                }
            }
            return castToFloat(method.invoke(null, values), function.returns);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static Type getValueType(Class<?> klass){
        for (Type type : Type.values()){
            if (klass.equals(type.klass)){
                return type;
            }
        }
        return null;
    }

    enum Type {
        FLOAT(float.class, "F"),
        DOUBLE(double.class, "D"),
        CHAR(char.class, "I"),
        SHORT(short.class, "I"),
        INT(int.class, "I"),
        LONG(long.class, "J"),
        ;
        final Class<?> klass;
        final String signature;
        Type(Class<?> klass, String signature){
            this.klass = klass;
            this.signature = signature;
        }
    }
}
