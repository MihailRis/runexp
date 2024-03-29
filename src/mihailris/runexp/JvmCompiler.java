package mihailris.runexp;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;


public class JvmCompiler {
    private final RunExpSolver solver;

    public JvmCompiler(RunExpSolver solver) {
        this.solver = solver;
    }

    public Expression compile(ExpNode root){
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_6,
                ACC_PUBLIC | ACC_SUPER,
                "Expression$"+cw.hashCode(),
                null,
                "java/lang/Object",
                new String[]{Expression.class.getName().replace(".", "/")});

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL,
                    "java/lang/Object",
                    "<init>",
                    "()V", false);

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        for (Method call : Expression.class.getDeclaredMethods()) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, call.getName(), Type.getMethodDescriptor(call),  null, null);
            compile(root, mv);
            mv.visitInsn(FRETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();
        byte[] raw = cw.toByteArray();
        try {
            return (Expression) new ClassLoader(Expression.class.getClassLoader()) {
                public Class<?> defineClass(byte[] bytes) {
                    return super.defineClass("Expression$"+cw.hashCode(), bytes, 0, bytes.length);
                }
            }.defineClass(raw).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int binaryOp(String text){
        switch (text){
            case "*": return FMUL;
            case "/": return FDIV;
            case "+": return FADD;
            case "-": return FSUB;
            case "%": return FREM;
            default:
                throw new IllegalArgumentException(text);
        }
    }

    private void compile(ExpNode node, MethodVisitor mv) {
        if (node.token != null && node.token.tag == Token.Tag.VALUE){
            mv.visitLdcInsn(node.token.value);
            return;
        }
        if (node.command != null && node.command.string.equals(ExpConstants.POW_OP)){
            node.command.string = "pow";
            node.command.tag = Token.Tag.FUNCTION;
        }
        if (node.command != null && node.command.tag == Token.Tag.OPERATOR){
            if (node.nodes.size() == 1){
                compile(node.get(0), mv);
                mv.visitInsn(FNEG); // currently it's the only unary operation
            } else {
                ExpNode a = node.get(0);
                ExpNode b = node.get(1);

                boolean constA = (a.token != null && a.token.tag == Token.Tag.VALUE);
                boolean constB = (b.token != null && b.token.tag == Token.Tag.VALUE);

                assert (!constA || !constB);

                if (constA){
                    mv.visitLdcInsn(a.token.value);
                } else {
                    compile(a, mv);
                }

                if (constB){
                    mv.visitLdcInsn(b.token.value);
                } else {
                    compile(b, mv);
                }

                int opcode = binaryOp(node.command.string);
                mv.visitInsn(opcode);
            }
            return;
        }
        if (node.command != null && node.command.tag == Token.Tag.FUNCTION){
            String name = node.command.string;
            RunExpFunction function = solver.functions.get(name);

            for (int i = 0; i < node.nodes.size(); i++){
                ExpNode subnode = node.get(i);
                compile(subnode, mv);
                RunExpReflection.Type type = RunExpReflection.getValueType(function.argsClasses[i]);
                assert (type != null);

                switch (type){
                    case FLOAT: break;
                    case DOUBLE: mv.visitInsn(F2D); break;
                    case INT: mv.visitInsn(F2I); break;
                    case LONG: mv.visitInsn(F2L); break;
                    default:
                        throw new IllegalStateException(type.name());
                }
            }

            mv.visitMethodInsn(INVOKESTATIC, function.className.replaceAll("\\.", "/"), function.methodName, function.argsFormat, false);

            switch (function.returns){
                case FLOAT: break;
                case DOUBLE: mv.visitInsn(D2F); break;
                case CHAR:
                case SHORT:
                case INT: mv.visitInsn(I2F); break;
                case LONG: mv.visitInsn(L2F); break;
            }
        }
        if (node.token != null && node.token.tag == Token.Tag.VARIABLE){
            mv.visitVarInsn(FLOAD, 1);
            return;
        }
        if (node.command == null){
            assert (node.nodes.size() == 1);
            compile(node.get(0), mv);
        }
    }
}
