package mihailris.runexp;

import java.util.ArrayList;
import java.util.List;

public class ExpNode {
    Token command;
    final Token token;
    final List<ExpNode> nodes;

    public ExpNode(Token token){
        this.token = token;
        this.nodes = new ArrayList<>();
    }

    ExpNode(Token command, List<ExpNode> nodes) {
        this.command = command;
        this.token = null;
        this.nodes = nodes;
    }

    ExpNode(List<ExpNode> nodes) {
        this.token = null;
        this.nodes = nodes;
    }

    ExpNode(){
        this.token = null;
        this.nodes = new ArrayList<>();
    }

    void put(ExpNode node){
        nodes.add(node);
    }

    ExpNode get(int index){
        if (nodes.size() <= index)
            return null;
        return nodes.get(index);
    }

    public String toStringExpression(){
        if (token != null){
            return token.text;
        } else {
            StringBuilder text = new StringBuilder();
            if (command != null){
                if (ExpConstants.BINARY_OPERATORS.contains(command.text)){
                    text.append(nodes.get(0).toStringExpression());
                    text.append(' ').append(command.text).append(' ');
                    text.append(nodes.get(1).toStringExpression());
                    return text.toString();
                } else {
                    text.append(command.text);
                }
            }
            text.append('(');
            for (int i = 0; i < nodes.size(); i++) {
                text.append(nodes.get(i).toStringExpression());
                if (i < nodes.size()-1){
                    text.append(", ");
                }
            }
            text.append(')');
            return text.toString();
        }
    }
}
