package mihailris.runexp;

import java.util.Arrays;

import static mihailris.runexp.Compiler.*;

/**
 * Simple stack machine
 * @deprecated two times slower than OOP implementation
 */
@Deprecated
public class CompiledExpression implements Expression {
    final RunExpFunction[] functions;
    final byte[] bytecode;
    final float[] constants;
    final float[] stack = new float[16];
    public CompiledExpression(byte[] bytecode, float[] constants, RunExpFunction[] functions){
        this.bytecode = bytecode;
        this.constants = constants;
        this.functions = functions;
    }

    @Override
    public float eval(float x) {
        final byte[] bytecode = this.bytecode;
        final float[] constants = this.constants;
        final RunExpFunction[] functions = this.functions;
        synchronized (this.stack) {
            final float[] stack = this.stack;
            int stackptr = 0;
            byte code;
            int pos = 0;

            float a;
            float b;
            RunExpFunction function;
            while ((code = bytecode[pos++]) != 0) {
                switch (code) {
                    case C_CONST:
                        stack[stackptr++] = constants[bytecode[pos++]];
                        break;
                    case C_X:
                        stack[stackptr++] = x;
                        break;
                    case C_NEG:
                        stack[stackptr-1] *= -1;
                        break;
                    case C_MUL:
                        b = stack[--stackptr];
                        a = stack[stackptr-1];
                        stack[stackptr-1] = a * b;
                        break;
                    case C_DIV:
                        b = stack[--stackptr];
                        a = stack[stackptr-1];
                        stack[stackptr-1] = a / b;
                        break;
                    case C_ADD:
                        b = stack[--stackptr];
                        a = stack[stackptr-1];
                        stack[stackptr-1] = a + b;
                        break;
                    case C_SUB:
                        b = stack[--stackptr];
                        a = stack[stackptr-1];
                        stack[stackptr-1] = a - b;
                        break;
                    case C_MOD:
                        b = stack[--stackptr];
                        a = stack[stackptr-1];
                        stack[stackptr-1] = a % b;
                        break;
                    case C_POW:
                        b = stack[--stackptr];
                        a = stack[stackptr-1];
                        stack[stackptr-1] = (float) Math.pow(a, b);
                        break;
                    case C_CALL:
                        function = functions[bytecode[pos++]];
                        stackptr -= function.argCount - 1;
                        stack[stackptr] = Parser.callFunc(function, stack, stackptr-1, function.argCount);
                        break;
                }
            }
            return stack[stackptr-1];
        }
    }
}
