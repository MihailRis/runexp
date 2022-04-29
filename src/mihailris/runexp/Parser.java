package mihailris.runexp;

import java.util.ArrayList;
import java.util.List;

import static mihailris.runexp.ExpConstants.*;
import static mihailris.runexp.RunExpReflection.callFunc;
import static mihailris.runexp.RunExpReflection.performOperation;

public class Parser {
    private static final String[] BINARY_OPS_GROUPS = new String[]{
        "^",
        "*/%",
        "+-",
    };
    private static final String UNARY_OPS = "-+";

    final RunExpSolver solver;

    Parser(RunExpSolver solver) {
        this.solver = solver;
    }

    private boolean isUnary(String operator){
        return UNARY_OPS.contains(operator);
    }

    public List<ExpNode> performRawTokens(List<RawToken> tokens, boolean constant) throws ExpCompileException {
        List<ExpNode> nodes = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++){
            RawToken rawToken = tokens.get(i);

            Token token;
            switch (rawToken.tag){
                case NAME:{
                    if (i < tokens.size()-1 && tokens.get(i+1).tag == RawToken.Tag.OPEN){
                        if (solver.functions.get(rawToken.text) == null)
                            throw new ExpCompileException(
                                    "unknown function '"+rawToken.text+"'", rawToken.pos, ERR_UNKNOWN_FUNCTION);
                        token = new Token(Token.Tag.FUNCTION, rawToken.text, rawToken.pos);
                    } else {
                        Float constantValue = solver.constants.get(rawToken.text);
                        if (constantValue != null) {
                            token = new Token(Token.Tag.VALUE, constantValue, rawToken.pos);
                        } else {
                            if (constant) {
                                throw new ExpCompileException("unknown constant '" + rawToken.text + "'",
                                        rawToken.pos, ERR_UNKNOWN_CONSTANT);
                            }
                            if (!solver.xAliases.contains(rawToken.text)) {
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

    public ExpNode parse(List<RawToken> tokens, boolean constant) throws ExpCompileException {
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

        if (solver.verbose)
            System.out.println("runexp: simplified in "+iterations+" iterations");

        assert (nodes.size() > 0);
        if (nodes.size() > 1)
            throw new ExpCompileException("multiple result values (not supported)", 0, ERR_MULTIPLE_RESULT_VALUES);

        return nodes.get(0);
    }

    private int parseBlocks(List<ExpNode> source, List<ExpNode> nodes, int index, boolean closeable) throws ExpCompileException {
        for (; index < source.size(); index++) {
            ExpNode node = source.get(index);
            Token token = node.token;
            assert (token != null);
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

    private void parseCalls(List<ExpNode> source, List<ExpNode> nodes) {
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

    private void parseArguments(List<ExpNode> source, List<ExpNode> nodes, boolean call) throws ExpCompileException {
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
                node = new ExpNode(node.command, out);

                if (node.command != null && node.command.tag == Token.Tag.FUNCTION){
                    RunExpFunction function = solver.functions.get(node.command.string);
                    if (function.argCount != out.size())
                        throw new ExpCompileException(
                                "wrong arguments count passed to '"+function.name+"' "+out.size()+"/"+function.argCount,
                                node.command.pos, ERR_WRONG_ARGS_COUNT);
                }
                argument.add(node);
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

    private void parseUnary(List<ExpNode> source, List<ExpNode> nodes) throws ExpCompileException {
        for (int index = 0; index < source.size(); index++) {
            ExpNode node = source.get(index);
            if (node.token == null){
                List<ExpNode> out = new ArrayList<>();
                parseUnary(node.nodes, out);
                nodes.add(new ExpNode(node.command, out));
                continue;
            } else if (node.token.tag == Token.Tag.OPERATOR){
                if (source.size() <= index + 1)
                    throw new ExpCompileException("invalid operator use", node.token.pos, ERR_UNEXPECTED_TOKEN);
                ExpNode next = source.get(index+1);
                if (nodes.isEmpty()){
                    if (!next.isValue()){
                        throw new ExpCompileException("invalid operator use", node.token.pos, ERR_UNEXPECTED_TOKEN);
                    }
                    List<ExpNode> out = new ArrayList<>();
                    if (!next.nodes.isEmpty()) {
                        parseUnary(next.nodes, out);
                        next = new ExpNode(next.command,out);
                        out = new ArrayList<>();
                    }
                    out.add(next);
                    if (node.token.string.equals(UNARY_PLUS)){
                        nodes.add(out.get(0));
                        index++;
                        continue;
                    }
                    nodes.add(new ExpNode(node.token, out));
                    index++;
                    continue;
                }

                ExpNode prev = nodes.get(nodes.size()-1);
                if (prev.token != null && prev.token.tag == Token.Tag.OPERATOR) {
                    if (!isUnary(prev.token.string)) {
                        throw new ExpCompileException("unknown unary operator '"+prev.token.string+"'",
                                prev.token.pos, ERR_UNKNOWN_UNARY);
                    }
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

    private void parseBinary(List<ExpNode> source, List<ExpNode> nodes, String group){
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

    private boolean simplify(List<ExpNode> source, List<ExpNode> nodes){
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
                    RunExpFunction function = solver.functions.get(node.command.string);
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
                        if (subnode.command == null) {
                            simplify(subnode.nodes, out);
                        } else {
                            simplify(node.nodes, out);
                        }
                        nodes.add(new ExpNode(node.command, out));
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
