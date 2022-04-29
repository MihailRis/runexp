package mihailris.runexp;

import java.util.ArrayList;
import java.util.List;

public class ExpNode {
    /**
     * function name or operator
     */
    Token command;
    /**
     * constant value or variable
     */
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

    ExpNode get(int index){
        if (nodes.size() <= index)
            return null;
        return nodes.get(index);
    }

    public boolean isValue() {
        return token == null || token.tag != Token.Tag.OPERATOR;
    }

    /**
     * @return reconstructed expression string
     */
    public String toStringExpression(){
        if (token != null){
            if (token.tag == Token.Tag.VALUE)
                return String.valueOf(token.value);
            if (token.tag == Token.Tag.VARIABLE)
                return token.string;
            throw new IllegalStateException(String.valueOf(token.tag));
        } else {
            StringBuilder text = new StringBuilder();
            if (command != null){
                if (command.tag == Token.Tag.OPERATOR){
                    text.append('(');
                    text.append(nodes.get(0).toStringExpression());
                    text.append(' ').append(command.string).append(' ');
                    text.append(nodes.get(1).toStringExpression());
                    text.append(')');
                    return text.toString();
                } else if (command.tag == Token.Tag.FUNCTION){
                    text.append(command.string);
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
