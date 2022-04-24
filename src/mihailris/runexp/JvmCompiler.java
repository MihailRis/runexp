package mihailris.runexp;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class JvmCompiler {
    public static Expression compile(ExpNode root){
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_6,
                ACC_PUBLIC | ACC_SUPER,
                "ExpressionN"+cw.hashCode(),
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
                    return super.defineClass("ExpressionN"+cw.hashCode(), bytes, 0, bytes.length);
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

    private static void compile(ExpNode node, MethodVisitor mv) {
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
                mv.visitInsn(FNEG); // TODO: other unaries theoritically support
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
            RunExpFunction function = RunExp.functions.get(name);

            boolean doubleFunc = function.isDouble;
            for (ExpNode subnode : node.nodes){
                compile(subnode, mv);
                if (doubleFunc)
                    mv.visitInsn(F2D);
            }

            mv.visitMethodInsn(INVOKESTATIC, function.className.replaceAll("\\.", "/"), function.methodName, function.argsFormat, false);
            if (doubleFunc)
                mv.visitInsn(D2F);
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
