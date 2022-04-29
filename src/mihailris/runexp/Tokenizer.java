package mihailris.runexp;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private List<RawToken> tokens;
    private RawToken.Tag tag;
    private StringBuilder builder;
    private int lpos;

    public List<RawToken> perform(String code) throws ExpCompileException {
        tokens = new ArrayList<>();
        tag = RawToken.Tag.UNDEFINED;
        final int length = code.length();
        int pos = 0;
        lpos = 0;
        char c;
        for (; pos < length; pos++, lpos++) {
            c = code.charAt(pos);

            if (c == ' ' || c == '\t' || c == '\r'){
                flush();
                continue;
            }
            if (c == ','){
                flush();
                tag = RawToken.Tag.SEPARATOR;
                put(c);
                flush();
                continue;
            }
            if (c == '('){
                flush();
                tag = RawToken.Tag.OPEN;
                put(c);
                flush();
                continue;
            }
            if (c == ')'){
                flush();
                tag = RawToken.Tag.CLOSE;
                put(c);
                flush();
                continue;
            }
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%'){
                flush();
                tag = RawToken.Tag.OPERATOR;
                put(c);
                flush();
                continue;
            }

            switch (tag){
                case UNDEFINED: {
                    if (Character.isAlphabetic(c)){
                        tag = RawToken.Tag.NAME;
                        put(c);
                    } else if (Character.isDigit(c)){
                        tag = RawToken.Tag.NUMBER;
                        put(c);
                    } else {
                        throw new ExpCompileException("unexpected '"+c+"'", lpos, ExpConstants.ERR_UNEXPECTED_TOKEN);
                    }
                    break;
                }
                case NAME: {
                    if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_'){
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
            if (tag == RawToken.Tag.NUMBER) {
                text = text.replaceAll("_", "");
                try {
                    Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    throw new ExpCompileException(text, lpos, ExpConstants.ERR_INVALID_NUMBER);
                }
            }
            RawToken token = new RawToken(tag, text, lpos);
            tokens.add(token);
            builder = null;
        }
        tag = RawToken.Tag.UNDEFINED;
    }

    private void put(char c) {
        if (builder == null)
            builder = new StringBuilder();
        builder.append(c);
    }
}
