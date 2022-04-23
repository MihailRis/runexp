package mihailris.runexp;

public class Token {
    Tag tag;
    float value;
    String string;
    int pos;

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

    public enum Tag {
        VALUE,
        VARIABLE,
        OPERATOR,
        FUNCTION,
        OPEN,
        CLOSE,
        SEPARATOR
    }
}
