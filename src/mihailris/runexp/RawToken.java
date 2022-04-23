package mihailris.runexp;

class RawToken {
    Tag tag;
    String text;
    int pos;

    RawToken(Tag tag, String text, int pos){
        this.tag = tag;
        this.text = text;
        this.pos = pos;
    }

    @Override
    public String toString(){
        return "<"+tag+":"+text+">";
    }

    public enum Tag {
        UNDEFINED,
        NUMBER,
        OPEN,
        CLOSE,
        NAME,
        OPERATOR,
        SEPARATOR,
    }
}
