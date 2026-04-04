/*
 * This file is part of FreeMarker JSON/XML Toolkit.
 *
 * FreeMarker JSON/XML Toolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FreeMarker JSON/XML Toolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with FreeMarker JSON/XML Toolkit. If not, see <https://www.gnu.org/licenses/>.
 */

package co.com.leronarenwino.editor.syntax;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.swing.text.Segment;

/**
 * Syntax highlighting for FreeMarker templates mixed with markup (HTML/XML-style).
 * Aligns with the <a href="https://freemarker.apache.org/docs/dgui_template_exp.html">expression syntax</a>
 * (ranges, built-ins, raw strings, square-bracket comments, etc.).
 */
public class FreemarkerTokenMaker extends AbstractTokenMaker {

    /** Continuation of {@code [#-- ... --]} across lines (see {@link Token#COMMENT_DOCUMENTATION}). */
    private static final int FM_BRACKET_COMMENT = Token.COMMENT_DOCUMENTATION;

    @Override
    public TokenMap getWordsToHighlight() {
        TokenMap map = new TokenMap();
        String[] words = {
                "if", "else", "elseif", "list", "as", "sep", "break", "continue",
                "switch", "case", "default",
                "assign", "global", "local", "include", "import", "macro", "function",
                "return", "attempt", "recover", "compress", "noautoesc", "autoesc",
                "escape", "noescape", "noparse", "setting", "visit", "recurse", "fallback",
                "items", "transform", "stop", "flush", "lt", "lte", "rt", "t", "nt", "gt", "gte",
        };
        for (String w : words) {
            map.put(w, Token.RESERVED_WORD);
        }
        map.put("true", Token.LITERAL_BOOLEAN);
        map.put("false", Token.LITERAL_BOOLEAN);
        return map;
    }

    @Override
    public void addToken(Segment segment, int start, int end, int tokenType, int startOffset) {
        if (tokenType == Token.IDENTIFIER) {
            int mapped = wordsToHighlight.get(segment, start, end);
            if (mapped != -1) {
                tokenType = mapped;
            }
        }
        super.addToken(segment, start, end, tokenType, startOffset);
    }

    @Override
    public boolean getMarkOccurrencesOfTokenType(int type) {
        return type == Token.IDENTIFIER || type == Token.VARIABLE;
    }

    @Override
    public boolean isMarkupLanguage() {
        return true;
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        resetTokenList();

        char[] array = text.array;
        int offset = text.offset;
        int count = text.count;
        int end = offset + count;
        int newStartOffset = startOffset - offset;

        int i = offset;
        int tokenStart = offset;

        if (initialTokenType == Token.COMMENT_MULTILINE) {
            int close = indexOf(array, i, end, "-->");
            if (close >= 0) {
                addToken(text, i, close + 2, Token.COMMENT_MULTILINE, newStartOffset + i);
                i = close + 3;
            } else {
                addToken(text, i, end - 1, Token.COMMENT_MULTILINE, newStartOffset + i);
                return firstToken;
            }
        }

        if (initialTokenType == FM_BRACKET_COMMENT) {
            int close = indexOf(array, i, end, "--]");
            if (close >= 0) {
                addToken(text, i, close + 2, FM_BRACKET_COMMENT, newStartOffset + i);
                i = close + 3;
            } else {
                addToken(text, i, end - 1, FM_BRACKET_COMMENT, newStartOffset + i);
                return firstToken;
            }
        }

        while (i < end) {
            char c = array[i];

            if (c == ' ' || c == '\t') {
                flushRun(text, tokenStart, i, newStartOffset);
                int wsStart = i;
                while (i < end && (array[i] == ' ' || array[i] == '\t')) {
                    i++;
                }
                addToken(text, wsStart, i - 1, Token.WHITESPACE, newStartOffset + wsStart);
                tokenStart = i;
                continue;
            }

            if (c == '\n' || c == '\r') {
                flushRun(text, tokenStart, i, newStartOffset);
                addToken(text, i, i, Token.WHITESPACE, newStartOffset + i);
                i++;
                tokenStart = i;
                continue;
            }

            if (c == '<' && i + 3 < end && array[i + 1] == '#' && array[i + 2] == '-' && array[i + 3] == '-') {
                flushRun(text, tokenStart, i, newStartOffset);
                int commentStart = i;
                i += 4;
                int close = indexOf(array, i, end, "-->");
                if (close >= 0) {
                    addToken(text, commentStart, close + 2, Token.COMMENT_MULTILINE, newStartOffset + commentStart);
                    i = close + 3;
                } else {
                    addToken(text, commentStart, end - 1, Token.COMMENT_MULTILINE, newStartOffset + commentStart);
                    return firstToken;
                }
                tokenStart = i;
                continue;
            }

            if (c == '<' && i + 2 < end && array[i + 1] == '#' && array[i + 2] == '/') {
                flushRun(text, tokenStart, i, newStartOffset);
                int dirEnd = consumeDirectiveEnd(array, i, end);
                addDirectiveTokens(text, array, i, dirEnd, newStartOffset);
                i = dirEnd + 1;
                tokenStart = i;
                continue;
            }

            if (c == '<' && i + 1 < end && array[i + 1] == '#') {
                flushRun(text, tokenStart, i, newStartOffset);
                int dirEnd = consumeDirectiveEnd(array, i, end);
                addDirectiveTokens(text, array, i, dirEnd, newStartOffset);
                i = dirEnd + 1;
                tokenStart = i;
                continue;
            }

            if (c == '<' && i + 1 < end && array[i + 1] == '@') {
                flushRun(text, tokenStart, i, newStartOffset);
                int dirEnd = consumeDirectiveEnd(array, i, end);
                addDirectiveTokens(text, array, i, dirEnd, newStartOffset);
                i = dirEnd + 1;
                tokenStart = i;
                continue;
            }

            if (c == '$' && i + 1 < end && array[i + 1] == '{') {
                flushRun(text, tokenStart, i, newStartOffset);
                int exprEnd = consumeInterpolation(array, i, end);
                addInterpolationTokens(text, array, i, exprEnd, newStartOffset);
                i = exprEnd + 1;
                tokenStart = i;
                continue;
            }

            if (c == '#' && i + 1 < end && array[i + 1] == '{') {
                flushRun(text, tokenStart, i, newStartOffset);
                int exprEnd = consumeInterpolation(array, i, end);
                addLegacyInterpolationTokens(text, array, i, exprEnd, newStartOffset);
                i = exprEnd + 1;
                tokenStart = i;
                continue;
            }

            if (c == '[' && i + 3 < end && array[i + 1] == '#' && array[i + 2] == '-' && array[i + 3] == '-') {
                flushRun(text, tokenStart, i, newStartOffset);
                int commentStart = i;
                i += 4;
                int close = indexOf(array, i, end, "--]");
                if (close >= 0) {
                    addToken(text, commentStart, close + 2, FM_BRACKET_COMMENT, newStartOffset + commentStart);
                    i = close + 3;
                } else {
                    addToken(text, commentStart, end - 1, FM_BRACKET_COMMENT, newStartOffset + commentStart);
                    return firstToken;
                }
                tokenStart = i;
                continue;
            }

            if (c == '[' && i + 1 < end && array[i + 1] == '=') {
                flushRun(text, tokenStart, i, newStartOffset);
                int closeBracket = consumeBracketOutput(array, i, end);
                addBracketOutputTokens(text, array, i, closeBracket, newStartOffset);
                i = closeBracket + 1;
                tokenStart = i;
                continue;
            }

            if (c == '<') {
                flushRun(text, tokenStart, i, newStartOffset);
                i = addMarkupTagTokens(text, array, i, end, newStartOffset);
                tokenStart = i;
                continue;
            }

            if (c == '>') {
                flushRun(text, tokenStart, i, newStartOffset);
                addToken(text, i, i, Token.MARKUP_TAG_DELIMITER, newStartOffset + i);
                i++;
                tokenStart = i;
                continue;
            }

            if (c == '&') {
                flushRun(text, tokenStart, i, newStartOffset);
                int semi = indexOfChar(array, i + 1, end, ';');
                if (semi >= 0) {
                    addToken(text, i, semi, Token.MARKUP_ENTITY_REFERENCE, newStartOffset + i);
                    i = semi + 1;
                } else {
                    addToken(text, i, end - 1, Token.MARKUP_ENTITY_REFERENCE, newStartOffset + i);
                    i = end;
                }
                tokenStart = i;
                continue;
            }

            if ((c == 'r' || c == 'R') && i + 1 < end && (array[i + 1] == '"' || array[i + 1] == '\'')) {
                flushRun(text, tokenStart, i, newStartOffset);
                char quote = array[i + 1];
                int strEnd = consumeRawStringEnd(array, i + 1, end, quote);
                int stringType = quote == '"' ? Token.LITERAL_STRING_DOUBLE_QUOTE : Token.LITERAL_CHAR;
                int errorType = quote == '"' ? Token.ERROR_STRING_DOUBLE : Token.ERROR_CHAR;
                if (strEnd < end && array[strEnd] == quote) {
                    addToken(text, i, strEnd, stringType, newStartOffset + i);
                    i = strEnd + 1;
                    tokenStart = i;
                } else {
                    addToken(text, i, end - 1, errorType, newStartOffset + i);
                    return firstToken;
                }
                continue;
            }

            if (c == '"' || c == '\'') {
                flushRun(text, tokenStart, i, newStartOffset);
                int strEnd = consumeStringEnd(array, i, end, c);
                int stringType = c == '"' ? Token.LITERAL_STRING_DOUBLE_QUOTE : Token.LITERAL_CHAR;
                int errorType = c == '"' ? Token.ERROR_STRING_DOUBLE : Token.ERROR_CHAR;
                if (strEnd < end && array[strEnd] == c) {
                    addToken(text, i, strEnd, stringType, newStartOffset + i);
                    i = strEnd + 1;
                    tokenStart = i;
                } else {
                    addToken(text, i, end - 1, errorType, newStartOffset + i);
                    return firstToken;
                }
                continue;
            }

            if (RSyntaxUtilities.isDigit(c) || (c == '.' && i + 1 < end && RSyntaxUtilities.isDigit(array[i + 1]))) {
                flushRun(text, tokenStart, i, newStartOffset);
                i = emitNumericToken(text, array, i, end, newStartOffset);
                tokenStart = i;
                continue;
            }

            if (isIdentStart(c)) {
                flushRun(text, tokenStart, i, newStartOffset);
                int idStart = i;
                i++;
                while (i < end && isIdentPart(array[i])) {
                    i++;
                }
                addToken(text, idStart, i - 1, Token.IDENTIFIER, newStartOffset + idStart);
                tokenStart = i;
                continue;
            }

            flushRun(text, tokenStart, i, newStartOffset);
            addToken(text, i, i, Token.SEPARATOR, newStartOffset + i);
            i++;
            tokenStart = i;
        }

        flushRun(text, tokenStart, end, newStartOffset);

        addNullToken();
        return firstToken;
    }

    private void flushRun(Segment text, int tokenStart, int i, int newStartOffset) {
        if (tokenStart < i) {
            addToken(text, tokenStart, i - 1, Token.IDENTIFIER, newStartOffset + tokenStart);
        }
    }

    private static boolean isIdentStart(char c) {
        return RSyntaxUtilities.isLetter(c) || c == '_' || c == '$';
    }

    private static boolean isIdentPart(char c) {
        return RSyntaxUtilities.isLetterOrDigit(c) || c == '_' || c == '$' || c == '-' || c == '.';
    }

    private void addDirectiveTokens(Segment text, char[] array, int start, int gtIndex, int newStartOffset) {
        addToken(text, start, start, Token.MARKUP_TAG_DELIMITER, newStartOffset + start);
        int p = start + 1;
        if (p <= gtIndex && array[p] == '/') {
            addToken(text, p, p, Token.SEPARATOR, newStartOffset + p);
            p++;
        }
        if (p <= gtIndex && array[p] == '#') {
            addToken(text, p, p, Token.PREPROCESSOR, newStartOffset + p);
            p++;
        } else if (p <= gtIndex && array[p] == '@') {
            addToken(text, p, p, Token.FUNCTION, newStartOffset + p);
            p++;
        }
        if (p < gtIndex) {
            addExpressionLikeTokens(text, array, p, gtIndex, newStartOffset);
        }
        addToken(text, gtIndex, gtIndex, Token.MARKUP_TAG_DELIMITER, newStartOffset + gtIndex);
    }

    private void addInterpolationTokens(Segment text, char[] array, int start, int exprEnd, int newStartOffset) {
        addToken(text, start, start, Token.OPERATOR, newStartOffset + start);
        if (start + 1 <= exprEnd) {
            addToken(text, start + 1, start + 1, Token.SEPARATOR, newStartOffset + start + 1);
        }
        if (start + 2 < exprEnd) {
            addExpressionLikeTokens(text, array, start + 2, exprEnd, newStartOffset);
        }
        addToken(text, exprEnd, exprEnd, Token.SEPARATOR, newStartOffset + exprEnd);
    }

    private void addLegacyInterpolationTokens(Segment text, char[] array, int start, int exprEnd, int newStartOffset) {
        addToken(text, start, start, Token.PREPROCESSOR, newStartOffset + start);
        if (start + 1 <= exprEnd) {
            addToken(text, start + 1, start + 1, Token.SEPARATOR, newStartOffset + start + 1);
        }
        if (start + 2 < exprEnd) {
            addExpressionLikeTokens(text, array, start + 2, exprEnd, newStartOffset);
        }
        addToken(text, exprEnd, exprEnd, Token.SEPARATOR, newStartOffset + exprEnd);
    }

    private void addBracketOutputTokens(Segment text, char[] array, int start, int closeBracket, int newStartOffset) {
        addToken(text, start, start, Token.SEPARATOR, newStartOffset + start);
        if (start + 1 <= closeBracket) {
            addToken(text, start + 1, start + 1, Token.OPERATOR, newStartOffset + start + 1);
        }
        if (start + 2 < closeBracket) {
            addExpressionLikeTokens(text, array, start + 2, closeBracket, newStartOffset);
        }
        addToken(text, closeBracket, closeBracket, Token.SEPARATOR, newStartOffset + closeBracket);
    }

    /**
     * Highlights identifiers, literals, and punctuation in FreeMarker expressions and directive bodies.
     */
    private void addExpressionLikeTokens(Segment text, char[] array, int from, int toExclusive, int newStartOffset) {
        int i = from;
        int tokenStart = from;
        while (i < toExclusive) {
            char c = array[i];

            if (c == ' ' || c == '\t') {
                flushRun(text, tokenStart, i, newStartOffset);
                int wsStart = i;
                while (i < toExclusive && (array[i] == ' ' || array[i] == '\t')) {
                    i++;
                }
                addToken(text, wsStart, i - 1, Token.WHITESPACE, newStartOffset + wsStart);
                tokenStart = i;
                continue;
            }

            if (c == '\n' || c == '\r') {
                flushRun(text, tokenStart, i, newStartOffset);
                addToken(text, i, i, Token.WHITESPACE, newStartOffset + i);
                i++;
                tokenStart = i;
                continue;
            }

            if (c == '[' && i + 3 < toExclusive && array[i + 1] == '#' && array[i + 2] == '-' && array[i + 3] == '-') {
                flushRun(text, tokenStart, i, newStartOffset);
                int commentStart = i;
                i += 4;
                int close = indexOf(array, i, toExclusive, "--]");
                if (close >= 0) {
                    addToken(text, commentStart, close + 2, FM_BRACKET_COMMENT, newStartOffset + commentStart);
                    i = close + 3;
                } else {
                    addToken(text, commentStart, toExclusive - 1, FM_BRACKET_COMMENT, newStartOffset + commentStart);
                    i = toExclusive;
                }
                tokenStart = i;
                continue;
            }

            if (c == '$' && i + 1 < toExclusive && array[i + 1] == '{') {
                flushRun(text, tokenStart, i, newStartOffset);
                int exprEnd = consumeInterpolation(array, i, toExclusive);
                addInterpolationTokens(text, array, i, exprEnd, newStartOffset);
                i = exprEnd + 1;
                tokenStart = i;
                continue;
            }

            if (c == '#' && i + 1 < toExclusive && array[i + 1] == '{') {
                flushRun(text, tokenStart, i, newStartOffset);
                int exprEnd = consumeInterpolation(array, i, toExclusive);
                addLegacyInterpolationTokens(text, array, i, exprEnd, newStartOffset);
                i = exprEnd + 1;
                tokenStart = i;
                continue;
            }

            if (c == '[' && i + 1 < toExclusive && array[i + 1] == '=') {
                flushRun(text, tokenStart, i, newStartOffset);
                int closeBracket = consumeBracketOutput(array, i, toExclusive);
                addBracketOutputTokens(text, array, i, closeBracket, newStartOffset);
                i = closeBracket + 1;
                tokenStart = i;
                continue;
            }

            if (c == '?') {
                flushRun(text, tokenStart, i, newStartOffset);
                addToken(text, i, i, Token.OPERATOR, newStartOffset + i);
                i++;
                if (i < toExclusive && array[i] == '?') {
                    addToken(text, i, i, Token.OPERATOR, newStartOffset + i);
                    i++;
                    tokenStart = i;
                    continue;
                }
                if (i < toExclusive && (RSyntaxUtilities.isLetter(array[i]) || array[i] == '_')) {
                    int bStart = i;
                    i++;
                    while (i < toExclusive && isBuiltinNamePart(array[i])) {
                        i++;
                    }
                    addToken(text, bStart, i - 1, Token.RESERVED_WORD_2, newStartOffset + bStart);
                }
                tokenStart = i;
                continue;
            }

            if ((c == 'r' || c == 'R') && i + 1 < toExclusive && (array[i + 1] == '"' || array[i + 1] == '\'')) {
                flushRun(text, tokenStart, i, newStartOffset);
                char quote = array[i + 1];
                int strEnd = consumeRawStringEnd(array, i + 1, toExclusive, quote);
                int stringType = quote == '"' ? Token.LITERAL_STRING_DOUBLE_QUOTE : Token.LITERAL_CHAR;
                if (strEnd < toExclusive && array[strEnd] == quote) {
                    addToken(text, i, strEnd, stringType, newStartOffset + i);
                    i = strEnd + 1;
                    tokenStart = i;
                } else {
                    addToken(text, i, toExclusive - 1, quote == '"' ? Token.ERROR_STRING_DOUBLE : Token.ERROR_CHAR, newStartOffset + i);
                    i = toExclusive;
                    tokenStart = i;
                }
                continue;
            }

            if (c == '"' || c == '\'') {
                flushRun(text, tokenStart, i, newStartOffset);
                int strEnd = consumeStringEnd(array, i, toExclusive, c);
                int stringType = c == '"' ? Token.LITERAL_STRING_DOUBLE_QUOTE : Token.LITERAL_CHAR;
                if (strEnd < toExclusive && array[strEnd] == c) {
                    addToken(text, i, strEnd, stringType, newStartOffset + i);
                    i = strEnd + 1;
                    tokenStart = i;
                } else {
                    addToken(text, i, toExclusive - 1, c == '"' ? Token.ERROR_STRING_DOUBLE : Token.ERROR_CHAR, newStartOffset + i);
                    i = toExclusive;
                    tokenStart = i;
                }
                continue;
            }

            if (RSyntaxUtilities.isDigit(c) || (c == '.' && i + 1 < toExclusive && RSyntaxUtilities.isDigit(array[i + 1]))) {
                flushRun(text, tokenStart, i, newStartOffset);
                i = emitNumericToken(text, array, i, toExclusive, newStartOffset);
                tokenStart = i;
                continue;
            }

            if (isIdentStart(c)) {
                flushRun(text, tokenStart, i, newStartOffset);
                int idStart = i;
                i++;
                while (i < toExclusive && isIdentPart(array[i])) {
                    i++;
                }
                addToken(text, idStart, i - 1, Token.IDENTIFIER, newStartOffset + idStart);
                tokenStart = i;
                continue;
            }

            flushRun(text, tokenStart, i, newStartOffset);
            int opLen = ftlOperatorLength(array, i, toExclusive);
            if (opLen > 1) {
                addToken(text, i, i + opLen - 1, Token.OPERATOR, newStartOffset + i);
                i += opLen;
            } else {
                addToken(text, i, i, separatorOrOperator(c), newStartOffset + i);
                i++;
            }
            tokenStart = i;
        }
        flushRun(text, tokenStart, i, newStartOffset);
    }

    private static boolean isBuiltinNamePart(char c) {
        return RSyntaxUtilities.isLetterOrDigit(c) || c == '_';
    }

    /**
     * Raw string literals: {@code r"..."} / {@code r'...'} — no escape sequences ({@code \} is literal).
     */
    private static int consumeRawStringEnd(char[] array, int quoteIndex, int end, char quote) {
        int j = quoteIndex + 1;
        while (j < end) {
            if (array[j] == quote) {
                return j;
            }
            j++;
        }
        return end;
    }

    private static boolean startsWith(char[] array, int i, int end, String s) {
        if (i + s.length() > end) {
            return false;
        }
        for (int k = 0; k < s.length(); k++) {
            if (array[i + k] != s.charAt(k)) {
                return false;
            }
        }
        return true;
    }

    /**
     * FreeMarker operators: ranges ({@code ..}, {@code ..<}, {@code ..!}, {@code ..*}), lambdas ({@code ->}),
     * XML-friendly ({@code &lt;}, {@code &amp;&amp;}, …), and {@code \and} (2.3.27+).
     */
    private static int ftlOperatorLength(char[] array, int i, int end) {
        if (i + 1 >= end) {
            return 1;
        }
        if (startsWith(array, i, end, "&amp;&amp;")) {
            return 10;
        }
        if (startsWith(array, i, end, "&lt;=")) {
            return 5;
        }
        if (startsWith(array, i, end, "&gt;=")) {
            return 5;
        }
        if (startsWith(array, i, end, "&lt;")) {
            return 4;
        }
        if (startsWith(array, i, end, "&gt;")) {
            return 4;
        }
        if (startsWith(array, i, end, "\\and")) {
            return 4;
        }
        if (i + 2 < end && array[i] == '.' && array[i + 1] == '.' && array[i + 2] == '*') {
            return 3;
        }
        if (i + 2 < end && array[i] == '.' && array[i + 1] == '.' && array[i + 2] == '<') {
            return 3;
        }
        if (i + 2 < end && array[i] == '.' && array[i + 1] == '.' && array[i + 2] == '!') {
            return 3;
        }
        if (array[i] == '.' && array[i + 1] == '.') {
            return 2;
        }
        if (array[i] == '-' && array[i + 1] == '>') {
            return 2;
        }
        char c = array[i];
        char n = array[i + 1];
        if ((c == '=' || c == '!' || c == '<' || c == '>') && n == '=') {
            return 2;
        }
        if ((c == '&' && n == '&') || (c == '|' && n == '|')) {
            return 2;
        }
        if (c == '+' && n == '+') {
            return 2;
        }
        if (c == '-' && n == '-') {
            return 2;
        }
        return 1;
    }

    private static int separatorOrOperator(char c) {
        return switch (c) {
            case '+', '-', '*', '/', '%', '!', '?', ':', '|', '^', '~' -> Token.OPERATOR;
            default -> Token.SEPARATOR;
        };
    }

    private int emitNumericToken(Segment text, char[] array, int start, int lineEnd, int newStartOffset) {
        int i = start;
        if (i + 1 < lineEnd && array[i] == '0' && (array[i + 1] == 'x' || array[i + 1] == 'X')) {
            i += 2;
            int hexStart = i;
            while (i < lineEnd && RSyntaxUtilities.isHexCharacter(array[i])) {
                i++;
            }
            if (i > hexStart) {
                addToken(text, start, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL, newStartOffset + start);
                return i;
            }
            addToken(text, start, start, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + start);
            return start + 1;
        }
        while (i < lineEnd && RSyntaxUtilities.isDigit(array[i])) {
            i++;
        }
        boolean dot = i < lineEnd && array[i] == '.';
        if (dot) {
            i++;
            while (i < lineEnd && RSyntaxUtilities.isDigit(array[i])) {
                i++;
            }
        }
        boolean exp = i < lineEnd && (array[i] == 'e' || array[i] == 'E');
        if (exp) {
            i++;
            if (i < lineEnd && (array[i] == '+' || array[i] == '-')) {
                i++;
            }
            int expDigits = i;
            while (i < lineEnd && RSyntaxUtilities.isDigit(array[i])) {
                i++;
            }
            if (i > expDigits) {
                addToken(text, start, i - 1, Token.LITERAL_NUMBER_FLOAT, newStartOffset + start);
                return i;
            }
            i = expDigits;
        }
        if (dot) {
            addToken(text, start, i - 1, Token.LITERAL_NUMBER_FLOAT, newStartOffset + start);
        } else {
            addToken(text, start, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + start);
        }
        return i;
    }

    /**
     * Consumes a markup tag starting at {@code ltIndex} ({@code '<'}), through closing {@code >} or {@code />}.
     *
     * @return index after the closing delimiter
     */
    private int addMarkupTagTokens(Segment text, char[] array, int ltIndex, int end, int newStartOffset) {
        addToken(text, ltIndex, ltIndex, Token.MARKUP_TAG_DELIMITER, newStartOffset + ltIndex);
        int i = ltIndex + 1;
        if (i < end && array[i] == '/') {
            addToken(text, i, i, Token.SEPARATOR, newStartOffset + i);
            i++;
        }
        if (i < end && (RSyntaxUtilities.isLetter(array[i]) || array[i] == '/' || array[i] == '!' || array[i] == '?')) {
            int nameStart = i;
            if (array[i] == '/') {
                i++;
            }
            while (i < end && (RSyntaxUtilities.isLetterOrDigit(array[i]) || array[i] == '-' || array[i] == '_' || array[i] == ':')) {
                i++;
            }
            if (nameStart < i) {
                addToken(text, nameStart, i - 1, Token.MARKUP_TAG_NAME, newStartOffset + nameStart);
            }
        }
        while (i < end) {
            char ch = array[i];
            if (ch == '>') {
                addToken(text, i, i, Token.MARKUP_TAG_DELIMITER, newStartOffset + i);
                return i + 1;
            }
            if (ch == '/' && i + 1 < end && array[i + 1] == '>') {
                addToken(text, i, i, Token.SEPARATOR, newStartOffset + i);
                addToken(text, i + 1, i + 1, Token.MARKUP_TAG_DELIMITER, newStartOffset + i + 1);
                return i + 2;
            }
            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                int ws = i;
                while (i < end && (array[i] == ' ' || array[i] == '\t' || array[i] == '\n' || array[i] == '\r')) {
                    i++;
                }
                addToken(text, ws, i - 1, Token.WHITESPACE, newStartOffset + ws);
                continue;
            }
            if (ch == '$' && i + 1 < end && array[i + 1] == '{') {
                int exprEnd = consumeInterpolation(array, i, end);
                addInterpolationTokens(text, array, i, exprEnd, newStartOffset);
                i = exprEnd + 1;
                continue;
            }
            if (ch == '#' && i + 1 < end && array[i + 1] == '{') {
                int exprEnd = consumeInterpolation(array, i, end);
                addLegacyInterpolationTokens(text, array, i, exprEnd, newStartOffset);
                i = exprEnd + 1;
                continue;
            }
            if ((ch == 'r' || ch == 'R') && i + 1 < end && (array[i + 1] == '"' || array[i + 1] == '\'')) {
                char quote = array[i + 1];
                int strEnd = consumeRawStringEnd(array, i + 1, end, quote);
                if (strEnd < end && array[strEnd] == quote) {
                    addToken(text, i, strEnd, Token.MARKUP_TAG_ATTRIBUTE_VALUE, newStartOffset + i);
                    i = strEnd + 1;
                } else {
                    addToken(text, i, end - 1, quote == '"' ? Token.ERROR_STRING_DOUBLE : Token.ERROR_CHAR, newStartOffset + i);
                    return end;
                }
                continue;
            }
            if (ch == '"' || ch == '\'') {
                char quote = ch;
                int strEnd = consumeStringEnd(array, i, end, quote);
                if (strEnd < end && array[strEnd] == quote) {
                    addToken(text, i, strEnd, Token.MARKUP_TAG_ATTRIBUTE_VALUE, newStartOffset + i);
                    i = strEnd + 1;
                } else {
                    addToken(text, i, end - 1, quote == '"' ? Token.ERROR_STRING_DOUBLE : Token.ERROR_CHAR, newStartOffset + i);
                    return end;
                }
                continue;
            }
            if (isIdentStart(ch) || ch == ':' || ch == '_') {
                int attrStart = i;
                i++;
                while (i < end && (isIdentPart(array[i]) || array[i] == ':')) {
                    i++;
                }
                addToken(text, attrStart, i - 1, Token.MARKUP_TAG_ATTRIBUTE, newStartOffset + attrStart);
                while (i < end && (array[i] == ' ' || array[i] == '\t')) {
                    i++;
                }
                if (i < end && array[i] == '=') {
                    addToken(text, i, i, Token.SEPARATOR, newStartOffset + i);
                    i++;
                    while (i < end && (array[i] == ' ' || array[i] == '\t')) {
                        i++;
                    }
                    if (i < end) {
                        char v = array[i];
                        if (v == '"' || v == '\'') {
                            char quote = v;
                            int strEnd = consumeStringEnd(array, i, end, quote);
                            if (strEnd < end && array[strEnd] == quote) {
                                addToken(text, i, strEnd, Token.MARKUP_TAG_ATTRIBUTE_VALUE, newStartOffset + i);
                                i = strEnd + 1;
                            } else {
                                addToken(text, i, end - 1, quote == '"' ? Token.ERROR_STRING_DOUBLE : Token.ERROR_CHAR, newStartOffset + i);
                                return end;
                            }
                        } else {
                            int uv = i;
                            while (i < end && array[i] != '>' && array[i] != ' '
                                    && array[i] != '\t' && array[i] != '\n' && array[i] != '\r') {
                                if (array[i] == '/' && i + 1 < end && array[i + 1] == '>') {
                                    break;
                                }
                                i++;
                            }
                            if (uv < i) {
                                addToken(text, uv, i - 1, Token.MARKUP_TAG_ATTRIBUTE_VALUE, newStartOffset + uv);
                            }
                        }
                    }
                }
                continue;
            }
            addToken(text, i, i, Token.SEPARATOR, newStartOffset + i);
            i++;
        }
        return i;
    }

    private static int consumeStringEnd(char[] array, int start, int end, char quote) {
        int i = start + 1;
        while (i < end) {
            if (array[i] == '\\' && i + 1 < end) {
                i += 2;
                continue;
            }
            if (array[i] == quote) {
                return i;
            }
            i++;
        }
        return end;
    }

    private static int indexOf(char[] array, int from, int end, String needle) {
        outer:
        for (int p = from; p + needle.length() <= end; p++) {
            for (int k = 0; k < needle.length(); k++) {
                if (array[p + k] != needle.charAt(k)) {
                    continue outer;
                }
            }
            return p;
        }
        return -1;
    }

    private static int indexOfChar(char[] array, int from, int end, char ch) {
        for (int p = from; p < end; p++) {
            if (array[p] == ch) {
                return p;
            }
        }
        return -1;
    }

    /**
     * Finds the closing {@code >} of a directive or tag, respecting single/double quoted strings.
     */
    private static int consumeDirectiveEnd(char[] array, int start, int end) {
        int i = start;
        boolean inSq = false;
        boolean inDq = false;
        while (i < end) {
            char c = array[i];
            if (!inSq && !inDq) {
                if (c == '>') {
                    return i;
                }
                if (c == '"') {
                    inDq = true;
                } else if (c == '\'') {
                    inSq = true;
                }
            } else if (inDq) {
                if (c == '\\' && i + 1 < end) {
                    i++;
                } else if (c == '"') {
                    inDq = false;
                }
            } else {
                if (c == '\\' && i + 1 < end) {
                    i++;
                } else if (c == '\'') {
                    inSq = false;
                }
            }
            i++;
        }
        return end - 1;
    }

    /**
     * Consumes {@code ${...}} or legacy {@code #{...}} starting at {@code $} or at {@code #} for {@code #{}}.
     */
    private static int consumeInterpolation(char[] array, int dollarOrHash, int end) {
        int i = dollarOrHash;
        if (i + 1 < end && array[i] == '$' && array[i + 1] == '{') {
            i += 2;
        } else if (i + 1 < end && array[i] == '#' && array[i + 1] == '{') {
            i += 2;
        } else {
            return dollarOrHash;
        }
        int depth = 1;
        boolean inSq = false;
        boolean inDq = false;
        while (i < end) {
            char c = array[i];
            if (!inSq && !inDq) {
                if (c == '"') {
                    inDq = true;
                } else if (c == '\'') {
                    inSq = true;
                } else if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            } else if (inDq) {
                if (c == '\\' && i + 1 < end) {
                    i++;
                } else if (c == '"') {
                    inDq = false;
                }
            } else {
                if (c == '\\' && i + 1 < end) {
                    i++;
                } else if (c == '\'') {
                    inSq = false;
                }
            }
            i++;
        }
        return end - 1;
    }

    /**
     * Consumes bracket output {@code [= ... ]} (FreeMarker 2.3.27+).
     */
    private static int consumeBracketOutput(char[] array, int bracketStart, int end) {
        if (bracketStart + 1 >= end || array[bracketStart] != '[' || array[bracketStart + 1] != '=') {
            return bracketStart;
        }
        int i = bracketStart + 2;
        int depth = 1;
        boolean inSq = false;
        boolean inDq = false;
        while (i < end) {
            char c = array[i];
            if (!inSq && !inDq) {
                if (c == '"') {
                    inDq = true;
                } else if (c == '\'') {
                    inSq = true;
                } else if (c == '[') {
                    depth++;
                } else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            } else if (inDq) {
                if (c == '\\' && i + 1 < end) {
                    i++;
                } else if (c == '"') {
                    inDq = false;
                }
            } else {
                if (c == '\\' && i + 1 < end) {
                    i++;
                } else if (c == '\'') {
                    inSq = false;
                }
            }
            i++;
        }
        return end - 1;
    }
}
