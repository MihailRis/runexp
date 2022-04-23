package mihailris.runexp;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class JvmCompiler {
    private static final String EXP_MATHS = "mihailris/runexp/ExpMaths";

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
                    "()V");
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
                public Class defineClass(byte[] bytes) {
                    return super.defineClass("ExpressionN"+cw.hashCode(), bytes, 0, bytes.length);
                }
            }.defineClass(raw).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String functionClassName(String text){
        switch (text){
            case "smoother":
            case "rand":
                return EXP_MATHS;
            default:
                return "java/lang/Math";
        }
    }

    private static String functionClassMethod(String text){
        switch (text){
            case "sign":
                return "signum";
            default:
                return text;
        }
    }

    private static String functionArgsFormat(String text) {
        switch (text){
            case "max":
            case "min":
                return "(FF)F";
            case "pow":
                return "(DD)D";
            default:
                return isDoubleFunc(text) ? "(D)D" : "(F)F";
        }
    }

    private static boolean isDoubleFunc(String text) {
        switch (text){
            case "sin":
            case "cos":
            case "tan":
            case "exp":
            case "sqrt":
            case "pow":
                return true;
            default:
                return false;
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
                throw new IllegalStateException();
        }
    }

    private static void compile(ExpNode node, MethodVisitor mv) {
        if (node.token != null && node.token.tag == Token.Tag.NUMBER){
            mv.visitLdcInsn((float)Double.parseDouble(node.token.text));
            return;
        }
        if (node.command != null && node.command.text.equals(ExpConstants.POW_OP)){
            node.command.text = "pow";
            node.command.tag = Token.Tag.NAME;
        }
        if (node.command != null && node.command.tag == Token.Tag.OPERATOR){
            if (node.nodes.size() == 1){
                compile(node.get(0), mv);
                mv.visitInsn(FNEG); // TODO: other unaries theoritically support
            } else {
                ExpNode a = node.get(0);
                ExpNode b = node.get(1);

                boolean constA = (a.token != null && a.token.tag == Token.Tag.NUMBER);
                boolean constB = (b.token != null && b.token.tag == Token.Tag.NUMBER);

                assert (!constA || !constB);

                if (constA){
                    mv.visitLdcInsn((float)Double.parseDouble(a.token.text));
                } else {
                    compile(a, mv);
                }

                if (constB){
                    mv.visitLdcInsn((float)Double.parseDouble(b.token.text));
                } else {
                    compile(b, mv);
                }

                int opcode = binaryOp(node.command.text);
                mv.visitInsn(opcode);
            }
            return;
        }
        if (node.command != null && node.command.tag == Token.Tag.NAME){
            boolean doubleFunc = isDoubleFunc(node.command.text);
            for (ExpNode subnode : node.nodes){
                compile(subnode, mv);
                if (doubleFunc)
                    mv.visitInsn(F2D);
            }
            mv.visitMethodInsn(INVOKESTATIC, functionClassName(node.command.text), functionClassMethod(node.command.text), functionArgsFormat(node.command.text));
            if (doubleFunc)
                mv.visitInsn(D2F);
        }
        if (node.token != null && node.token.tag == Token.Tag.NAME){
            mv.visitVarInsn(FLOAD, 1);
            return;
        }
        if (node.command == null){
            assert (node.nodes.size() == 1);
            compile(node.get(0), mv);
        }
    }
}
