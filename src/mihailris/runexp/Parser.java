package mihailris.runexp;

import java.util.ArrayList;
import java.util.List;

import static mihailris.runexp.ExpConstants.*;

public class Parser {
    public static ExpNode parse(List<Token> tokens, boolean constant) throws ExpCompileException {
        List<ExpNode> nodes = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++){
            Token token = tokens.get(i);
            if (token.tag == Token.Tag.NAME){
                switch (token.text){
                    case "e":
                        token = new Token(Token.Tag.NUMBER, String.valueOf(Math.E), token.pos);
                        break;
                    case "pi":
                        token = new Token(Token.Tag.NUMBER, String.valueOf(Math.PI), token.pos);
                        break;
                    case "pi2":
                        token = new Token(Token.Tag.NUMBER, String.valueOf(Math.PI*2.0), token.pos);
                        break;
                    case "raddeg":
                        token = new Token(Token.Tag.NUMBER, String.valueOf(180.0/Math.PI), token.pos);
                        break;
                    case "degrad":
                        token = new Token(Token.Tag.NUMBER, String.valueOf(Math.PI/180.0), token.pos);
                        break;
                }
                // if token is still name
                if (token.tag == Token.Tag.NAME && !(i < tokens.size()-1 && tokens.get(i+1).tag == Token.Tag.OPEN)) {
                    if (constant) {
                        throw new ExpCompileException("unknown constant '" + token.text + "'", token.pos, ERR_UNKNOWN_CONSTANT);
                    } else if (!RunExp.xAliases.contains(token.text)) {
                        throw new ExpCompileException("unknown name '" + token.text + "'", token.pos, ERR_UNKNOWN_NAME);
                    }
                }
            }
            nodes.add(new ExpNode(token));
        }
        List<ExpNode> newNodes = new ArrayList<>();
        parseBlocks(nodes, newNodes, 0, false);
        nodes = newNodes;

        if (RunExp.verbose)
            System.out.println("Parser.parse BLOCKS "+RunExp.ast2Str(nodes, 0));

        newNodes = new ArrayList<>();
        parseCalls(nodes, newNodes);
        nodes = newNodes;

        if (RunExp.verbose)
            System.out.println("Parser.parse CALLS "+RunExp.ast2Str(nodes, 0));

        newNodes = new ArrayList<>();
        parseArguments(nodes, newNodes, false);
        nodes = newNodes;

        newNodes = new ArrayList<>();
        parseUnary(nodes, newNodes);
        nodes = newNodes;

        String[] groups = new String[]{
            "^",
            "*/%",
            "+-",
        };
        for (String group : groups) {
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
        System.out.println("runexp: simplified in "+iterations+" iterations");
        return new ExpNode(nodes);
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
            if (prev != null && prev.token != null && prev.token.tag == Token.Tag.NAME && node.token == null) {
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
            } else if (node.token.tag == Token.Tag.OPERATOR && group.contains(node.token.text)){
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

    private static boolean simplify(List<ExpNode> source, List<ExpNode> nodes){
        boolean changed = false;
        for (ExpNode node : source) {
            if (node.command != null && node.command.tag == Token.Tag.OPERATOR) {
                if (node.nodes.size() == 1) {
                    ExpNode subnode = node.get(0);
                    if (subnode.token != null && subnode.token.tag == Token.Tag.NUMBER) {
                        if (!node.command.text.equals("+")) {
                            subnode.token.text = node.command.text + subnode.token.text;
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
                } else if (node.nodes.size() > 1) {
                    ExpNode a = node.get(0);
                    ExpNode b = node.get(1);
                    if (a.token != null && a.token.tag == Token.Tag.NUMBER) {
                        if (b.token != null && b.token.tag == Token.Tag.NUMBER) {
                            Token simplified;
                            double valA = Double.parseDouble(a.token.text);
                            double valB = Double.parseDouble(b.token.text);
                            switch (node.command.text) {
                                case "*":
                                    simplified = new Token(Token.Tag.NUMBER,
                                            String.valueOf(valA * valB), a.token.pos);
                                    break;
                                case "^":
                                    simplified = new Token(Token.Tag.NUMBER,
                                            String.valueOf(Math.pow(valA, valB)), a.token.pos);
                                    break;
                                case "/":
                                    simplified = new Token(Token.Tag.NUMBER,
                                            String.valueOf(valA / valB), a.token.pos);
                                    break;
                                case "+":
                                    simplified = new Token(Token.Tag.NUMBER,
                                            String.valueOf(valA + valB), a.token.pos);
                                    break;
                                case "-":
                                    simplified = new Token(Token.Tag.NUMBER,
                                            String.valueOf(valA - valB), a.token.pos);
                                    break;
                                case "%":
                                    simplified = new Token(Token.Tag.NUMBER,
                                            String.valueOf(valA % valB), a.token.pos);
                                    break;
                                default:
                                    throw new IllegalStateException();
                            }
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
