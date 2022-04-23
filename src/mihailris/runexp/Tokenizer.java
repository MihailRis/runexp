package mihailris.runexp;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private List<Token> tokens;
    private Token.Tag tag;
    private StringBuilder builder;
    private int lpos;

    public List<Token> perform(String code) throws ExpCompileException {
        tokens = new ArrayList<>();
        tag = Token.Tag.UNDEFINED;
        final int length = code.length();
        int pos = 0;
        lpos = 0;
        char c;
        for (; pos < length; pos++, lpos++) {
            c = code.charAt(pos);

            if (c == ' ' || c == '\t' || c == '\r'){
                flush();
            }
            if (c == ','){
                flush();
                tag = Token.Tag.SEPARATOR;
                put(c);
                flush();
            }
            if (c == '('){
                flush();
                tag = Token.Tag.OPEN;
                put(c);
                flush();
            }
            if (c == ')'){
                flush();
                tag = Token.Tag.CLOSE;
                put(c);
                flush();
            }
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%'){
                flush();
                tag = Token.Tag.OPERATOR;
                put(c);
                flush();
            }

            switch (tag){
                case UNDEFINED: {
                    if (Character.isAlphabetic(c)){
                        tag = Token.Tag.NAME;
                        put(c);
                    } else if (Character.isDigit(c)){
                        tag = Token.Tag.NUMBER;
                        put(c);
                    }
                    break;
                }
                case NAME: {
                    if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '-'){
                        put(c);
                    } else {
                        throw new ExpCompileException(builder.toString()+c, lpos, ExpConstants.ERR_INVALID_IDENTIFIER);
                    }
                    break;
                }
                case NUMBER: {
                    if (Character.isDigit(c) || c == '_' || c == '.'){
                        put(c);
                    } else {
                        throw new ExpCompileException(builder.toString()+c, lpos, ExpConstants.ERR_INVALID_NUMBER);
                    }
                    break;
                }
            }
        }
        flush();
        return tokens;
    }

    private void flush() throws ExpCompileException {
        if (builder != null){
            String text = builder.toString();
            if (tag == Token.Tag.NUMBER) {
                text = text.replaceAll("_", "");
                try {
                    Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    throw new ExpCompileException(text, lpos, ExpConstants.ERR_INVALID_NUMBER);
                }
            }
            Token token = new Token(tag, text, lpos);
            tokens.add(token);
            builder = null;
        }
        tag = Token.Tag.UNDEFINED;
    }

    private void putEmpty(){
        if (builder == null)
            builder = new StringBuilder();
    }

    private void put(char c) {
        if (builder == null)
            builder = new StringBuilder();
        builder.append(c);
    }
}
