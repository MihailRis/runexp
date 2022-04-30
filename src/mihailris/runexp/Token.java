package mihailris.runexp;

public class Token {
    Tag tag;
    float value;
    String string;
    final int pos;

    public Token(Tag tag, String string, int pos) {
        this.tag = tag;
        this.string = string;
        this.pos = pos;
    }

    public Token(Tag tag, float value, int pos) {
        this.tag = tag;
        this.value = value;
        this.pos = pos;
    }

    public Token(Tag tag, int pos) {
        this.tag = tag;
        this.pos = pos;
    }

    @Override
    public String toString(){
        if (tag == Tag.VALUE)
            return "<"+tag+":"+value+">";
        return "<"+tag+":"+string+">";
    }

    public enum Tag {
        VALUE, // constant number (value)
        VARIABLE, // dynamic variable name (string)
        OPERATOR, // operator (string)
        FUNCTION, // function name (string)

        // nodes used in Parser but don't appear in parsed nodes tree
        OPEN, // open bracket
        CLOSE, // close bracket
        COMMA
    }
}
