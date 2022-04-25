package mihailris.runexp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static mihailris.runexp.ExpConstants.*;

public class Parser {
    private static final String[] BINARY_OPS_GROUPS = new String[]{
        "^",
        "*/%",
        "+-",
    };

    public static List<ExpNode> performRawTokens(List<RawToken> tokens, boolean constant) throws ExpCompileException {
        List<ExpNode> nodes = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++){
            RawToken rawToken = tokens.get(i);

            Token token;
            switch (rawToken.tag){
                case NAME:{
                    if (i < tokens.size()-1 && tokens.get(i+1).tag == RawToken.Tag.OPEN){
                        token = new Token(Token.Tag.FUNCTION, rawToken.text, rawToken.pos);
                    } else {
                        Float constantValue = RunExp.constants.get(rawToken.text);
                        if (constantValue != null) {
                            token = new Token(Token.Tag.VALUE, constantValue, rawToken.pos);
                        } else {
                            if (constant) {
                                throw new ExpCompileException("unknown constant '" + rawToken.text + "'",
                                        rawToken.pos, ERR_UNKNOWN_CONSTANT);
                            }
                            if (!RunExp.xAliases.contains(rawToken.text)) {
                                throw new ExpCompileException("unknown name '" + rawToken.text + "'",
                                        rawToken.pos, ERR_UNKNOWN_NAME);
                            }
                            token = new Token(Token.Tag.VARIABLE, rawToken.text, rawToken.pos);
                        }
                    }
                    break;
                }
                case NUMBER: {
                    token = new Token(Token.Tag.VALUE, Float.parseFloat(rawToken.text), rawToken.pos);
                    break;
                }
                case OPERATOR: {
                    token = new Token(Token.Tag.OPERATOR, rawToken.text, rawToken.pos);
                    break;
                }
                case OPEN: token = new Token(Token.Tag.OPEN, rawToken.pos); break;
                case CLOSE: token = new Token(Token.Tag.CLOSE, rawToken.pos); break;
                case SEPARATOR: token = new Token(Token.Tag.SEPARATOR, rawToken.pos); break;
                default:
                    throw new IllegalStateException();
            }
            nodes.add(new ExpNode(token));
        }
        return nodes;
    }

    public static ExpNode parse(List<RawToken> tokens, boolean constant) throws ExpCompileException {
        List<ExpNode> nodes = performRawTokens(tokens, constant);

        List<ExpNode> newNodes = new ArrayList<>();
        parseBlocks(nodes, newNodes, 0, false);
        nodes = newNodes;

        newNodes = new ArrayList<>();
        parseCalls(nodes, newNodes);
        nodes = newNodes;

        newNodes = new ArrayList<>();
        parseArguments(nodes, newNodes, false);
        nodes = newNodes;

        newNodes = new ArrayList<>();
        parseUnary(nodes, newNodes);
        nodes = newNodes;

        for (String group : BINARY_OPS_GROUPS) {
            newNodes = new ArrayList<>();
            parseBinary(nodes, newNodes, group);
            nodes = newNodes;
        }

        boolean changed = true;
        int iterations = 0;
        while (changed){
            newNodes = new ArrayList<>();
            changed = simplify(nodes, newNodes);
            nodes = newNodes;
            iterations++;
        }

        if (RunExp.verbose)
            System.out.println("runexp: simplified in "+iterations+" iterations");

        ExpNode root;
        if (nodes.size() == 1){
            root = nodes.get(0);
        } else {
            root = new ExpNode(nodes);
        }
        return root;
    }

    private static int parseBlocks(List<ExpNode> source, List<ExpNode> nodes, int index, boolean closeable) throws ExpCompileException {
        for (; index < source.size(); index++) {
            ExpNode node = source.get(index);
            Token token = node.token;
            if (token.tag == Token.Tag.OPEN){
                List<ExpNode> out = new ArrayList<>();
                index = parseBlocks(source, out, index+1, true);
                nodes.add(new ExpNode(out));
                continue;
            }
            if (token.tag == Token.Tag.CLOSE) {
                if (!closeable){
                    throw new ExpCompileException("closing bracket without opening", token.pos, ERR_INVALID_BRACKET);
                }
                return index;
            }
            nodes.add(node);
        }
        return index;
    }

    private static void parseCalls(List<ExpNode> source, List<ExpNode> nodes){
        ExpNode prev;
        ExpNode node = null;
        for (ExpNode expNode : source) {
            prev = node;
            node = expNode;
            if (prev != null && prev.token != null && prev.token.tag == Token.Tag.FUNCTION && node.token == null) {
                nodes.remove(prev);
                List<ExpNode> out = new ArrayList<>();
                parseCalls(node.nodes, out);
                nodes.add(new ExpNode(prev.token, out));
                continue;
            }
            if (node.token == null) {
                List<ExpNode> out = new ArrayList<>();
                parseCalls(node.nodes, out);
                nodes.add(new ExpNode(node.command, out));
                continue;
            }
            nodes.add(node);
        }
    }

    private static void parseArguments(List<ExpNode> source, List<ExpNode> nodes, boolean call) {
        List<ExpNode> argument = new ArrayList<>();
        for (ExpNode node : source) {
            if (node.token != null && node.token.tag == Token.Tag.SEPARATOR) {
                List<ExpNode> out = new ArrayList<>();
                parseArguments(argument, out, call);
                nodes.add(new ExpNode(out));
                argument = new ArrayList<>();
                continue;
            }
            if (node.token == null) {
                List<ExpNode> out = new ArrayList<>();
                parseArguments(node.nodes, out, node.command != null);
                argument.add(new ExpNode(node.command, out));
                continue;
            }
            argument.add(node);
        }
        if (!argument.isEmpty()){
            if (call)
                nodes.add(new ExpNode(argument));
            else
                nodes.addAll(argument);
        }
    }

    private static void parseUnary(List<ExpNode> source, List<ExpNode> nodes){
        for (int index = 0; index < source.size(); index++) {
            ExpNode node = source.get(index);
            if (node.token == null){
                List<ExpNode> out = new ArrayList<>();
                parseUnary(node.nodes, out);
                nodes.add(new ExpNode(node.command, out));
                continue;
            } else if (node.token.tag == Token.Tag.OPERATOR){
                ExpNode next = source.get(index+1);
                if (nodes.isEmpty()){
                    List<ExpNode> out = new ArrayList<>();
                    out.add(next);
                    nodes.add(new ExpNode(node.token, out));
                    index++;
                    continue;
                }
                ExpNode prev = nodes.get(nodes.size()-1);
                if (prev.token != null && prev.token.tag == Token.Tag.OPERATOR){
                    List<ExpNode> out = new ArrayList<>();
                    out.add(next);
                    nodes.add(new ExpNode(node.token, out));
                    index++;
                    continue;
                }
            }
            nodes.add(node);
        }
    }

    private static void parseBinary(List<ExpNode> source, List<ExpNode> nodes, String group){
        for (int index = 0; index < source.size(); index++) {
            ExpNode node = source.get(index);
            if (node.token == null){
                List<ExpNode> out = new ArrayList<>();
                parseBinary(node.nodes, out, group);
                nodes.add(new ExpNode(node.command, out));
                continue;
            } else if (node.token.tag == Token.Tag.OPERATOR && group.contains(node.token.string)){
                ExpNode prev = nodes.get(nodes.size()-1);
                ExpNode next = source.get(index+1);
                if (next.token == null){
                    List<ExpNode> out = new ArrayList<>();
                    parseBinary(next.nodes, out, group);
                    next = new ExpNode(next.command, out);
                }
                nodes.remove(prev);
                List<ExpNode> out = new ArrayList<>();
                out.add(prev);
                out.add(next);
                nodes.add(new ExpNode(node.token, out));
                index++;
                continue;
            }
            nodes.add(node);
        }
    }

    private static float performOperation(float a, float b, String op){
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

    public static float callFunc(RunExpFunction function, float[] args, int offset, int argc){
        if (function.isBuiltin)
            return RunExp.callBuiltinFunc(function.name, args);

        // anyway java reflection is a lot of pain for GC
        try {
            Method method = function.method;
            Object[] values = new Object[argc];
            if (function.isDouble){
                for (int i = 0; i < argc; i++) {
                    values[i] = (double)args[offset+i];
                }
                return (float) (double) method.invoke(null, values);
            } else {
                for (int i = 0; i < argc; i++) {
                    values[i] = args[offset+i];
                }
                return (float) method.invoke(null, values);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean simplify(List<ExpNode> source, List<ExpNode> nodes){
        boolean changed = false;
        for (ExpNode node : source) {
            if (node.command != null && node.command.tag == Token.Tag.FUNCTION){
                boolean isconstant = true;
                for (ExpNode arg : node.nodes){
                    if (!(arg.token != null && arg.token.tag == Token.Tag.VALUE)){
                        isconstant = false;
                        break;
                    }
                }
                if (isconstant){
                    RunExpFunction function = RunExp.functions.get(node.command.string);
                    float[] args = new float[node.nodes.size()];
                    for (int i = 0; i < node.nodes.size(); i++) {
                        args[i] = node.nodes.get(i).token.value;
                    }
                    node = new ExpNode(new Token(Token.Tag.VALUE, callFunc(function, args, 0, args.length), node.command.pos));
                }
            }
            if (node.command != null && node.command.tag == Token.Tag.OPERATOR) {
                // unary operators
                if (node.nodes.size() == 1) {
                    ExpNode subnode = node.get(0);
                    if (subnode.token != null && subnode.token.tag == Token.Tag.VALUE) {
                        if (node.command.string.equals("-")) {
                            subnode.token.value *= -1;
                            nodes.add(subnode);
                            changed = true;
                            continue;
                        }
                        continue;
                    } else if (subnode.token == null) {
                        List<ExpNode> out = new ArrayList<>();
                        simplify(subnode.nodes, out);
                        nodes.add(new ExpNode(subnode.command, out));
                        changed = true;
                        continue;
                    }
                // binary operators
                } else if (node.nodes.size() > 1) {
                    ExpNode a = node.get(0);
                    ExpNode b = node.get(1);
                    if (a.token != null && a.token.tag == Token.Tag.VALUE) {
                        if (b.token != null && b.token.tag == Token.Tag.VALUE) {
                            float result = performOperation(a.token.value, b.token.value, node.command.string);
                            Token simplified = new Token(Token.Tag.VALUE, result, node.command.pos);
                            nodes.add(new ExpNode(simplified));
                            changed = true;
                            continue;
                        }
                    }
                } else {
                    changed = true;
                    continue;
                }
            }
            // unpacking unnecessary blocks
            if (node.token == null) {
                List<ExpNode> out = new ArrayList<>();
                for (ExpNode subnode : node.nodes) {
                    if (subnode.token == null && subnode.command == null && subnode.nodes.size() == 1) {
                        changed = true;
                        out.add(subnode.get(0));
                    } else {
                        out.add(subnode);
                    }
                }
                node = new ExpNode(node.command, out);
                out = new ArrayList<>();

                boolean nchanged = simplify(node.nodes, out);
                changed = changed || nchanged;
                node = new ExpNode(node.command, out);
                nodes.add(node);
                continue;
            }
            nodes.add(node);
        }
        return changed;
    }
}
