package org.demoiselle.jee.crud;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

public class CrudFieldUtils {
    private CrudFieldUtils() {
    }

    enum TokenType {
        START,
        FIELD,
        COMMA,
        OPEN_PAREN,
        CLOSE_PAREN
    }

    static class Token {
        public final String str;
        public final TokenType type;

        private Token(String str, TokenType type) {
            this.str = str;
            this.type = type;
        }

        public static Token fromString(String strToken) {
            switch(strToken) {
                case "(":
                    return Token.openParen();
                case ")":
                    return Token.closeParen();
                case ",":
                    return Token.comma();
                default:
                    return Token.field(strToken);
            }
        }

        public static Token start() {
            return new Token("START", TokenType.START);
        }

        public static Token field(String field) {
            return new Token(field, TokenType.FIELD);
        }

        public static Token comma() {
            return new Token(",", TokenType.COMMA);
        }

        public static Token openParen() {
            return new Token("(", TokenType.OPEN_PAREN);
        }

        public static Token closeParen() {
            return new Token(")", TokenType.CLOSE_PAREN);
        }
    }


    public static List<String> parseFieldList(String strFields) {
        StringTokenizer tokenizer = new StringTokenizer(strFields, "(),", true);
        Stack<String> fieldStack = new Stack<>();
        Token lastToken = Token.start();
        int balance = 0;
        int tokenCount = 0;
        List<String> flatFields = new ArrayList<>();
        while(tokenizer.hasMoreElements() || tokenizer.hasMoreTokens()) {
            String strToken = tokenizer.nextToken();
            Token token = Token.fromString(strToken);
            tokenCount++;
            switch(token.type) {
                case START:
                    break;
                case FIELD:
                    if (lastToken.type == TokenType.FIELD) {
                        throw new IllegalArgumentException("Near token "+lastToken.str+": fields must be separated by a comma");
                    }
                    fieldStack.push(token.str);
                    break;
                case COMMA:
                    if (lastToken.type != TokenType.FIELD && lastToken.type != TokenType.CLOSE_PAREN) {
                        throw new IllegalArgumentException("Near token "+lastToken.str+": Commas must only occur after a field name or a ) character");
                    }
                    if (lastToken.type == TokenType.FIELD) {
                        flatFields.add(StringUtils.join(fieldStack, "."));
                    }
                    fieldStack.pop();
                    break;
                case OPEN_PAREN:
                    if (lastToken.type != TokenType.FIELD) {
                        throw new IllegalArgumentException("Near token "+lastToken.str+": Open parenthesis must only occur after a field name");
                    }
                    balance++;
                    break;
                case CLOSE_PAREN:
                    balance--;
                    if (balance < 0) {
                        throw new IllegalArgumentException("Near token "+lastToken.str+": A ) character was found without a matching ( before!");
                    }
                    if (lastToken.type == TokenType.FIELD) {
                        flatFields.add(StringUtils.join(fieldStack, "."));
                        fieldStack.pop();
                    }
                    break;
            }
            lastToken = token;
        }
        if (balance > 0) {
            throw new IllegalArgumentException("Unbalanced parenthesis: a ( character was found without a matching )");
        } else if (balance < 0) {
            throw new IllegalArgumentException("Unbalanced parenthesis: a ) character was found without a matching (");
        }
        if (lastToken.type == TokenType.FIELD) {
            flatFields.add(lastToken.str);
        }
        return flatFields;
    }
}
