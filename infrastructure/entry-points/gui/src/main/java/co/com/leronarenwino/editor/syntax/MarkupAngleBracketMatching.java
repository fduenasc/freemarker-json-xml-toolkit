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

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Segment;

import java.awt.Point;

/**
 * Matches {@code <} with {@code >} when both are lexed as {@link org.fife.ui.rsyntaxtextarea.Token#MARKUP_TAG_DELIMITER}.
 * Anchor resolution prefers a markup delimiter immediately left of the caret, so {@code <[} does not jump to {@code [}.
 */
public final class MarkupAngleBracketMatching {

    private MarkupAngleBracketMatching() {
    }

    /**
     * Document offset of {@code <} or {@code >} under the caret, or {@code -1}.
     * Checks the character before the caret first, then the character at the caret (RSyntax “try right”),
     * but only for markup delimiters — never skips a leading {@code <} to use a following {@code [}.
     */
    public static int resolveMarkupBracketAnchor(RSyntaxTextArea textArea) {
        try {
            int caret = textArea.getCaretPosition();
            RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
            int left = caret - 1;
            if (left >= 0 && isMarkupAngleDelimiter(textArea, left)) {
                return left;
            }
            if (caret < doc.getLength() && isMarkupAngleDelimiter(textArea, caret)) {
                return caret;
            }
        } catch (BadLocationException e) {
            return -1;
        }
        return -1;
    }

    private static boolean isMarkupAngleDelimiter(RSyntaxTextArea textArea, int offset) throws BadLocationException {
        char c = textArea.getDocument().getText(offset, 1).charAt(0);
        if (c != '<' && c != '>') {
            return false;
        }
        Token t = textArea.modelToToken(offset);
        return t != null && t.getType() == Token.MARKUP_TAG_DELIMITER;
    }

    /**
     * @param anchorOffset document offset of {@code <} or {@code >} (markup delimiter)
     * @param input        recycled point; {@code x}=anchor, {@code y}=match, or {@code (-1,-1)}
     */
    public static void fillMatchForAnchor(RSyntaxTextArea textArea, int anchorOffset, Point input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        input.setLocation(-1, -1);
        try {
            RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
            char bracket = doc.charAt(anchorOffset);
            boolean open = bracket == '<';
            boolean close = bracket == '>';
            if (!open && !close) {
                return;
            }
            Token token = textArea.modelToToken(anchorOffset);
            if (token == null || token.getType() != Token.MARKUP_TAG_DELIMITER) {
                return;
            }
            int languageIndex = token.getLanguageIndex();
            int caretPosition = anchorOffset;

            Element map = doc.getDefaultRootElement();
            int curLine = map.getElementIndex(caretPosition);
            Element line = map.getElement(curLine);
            int start = line.getStartOffset();
            int end = line.getEndOffset();
            Segment charSegment = new Segment();

            if (open) {
                int lastLine = map.getElementCount();
                start = caretPosition + 1;
                int numEmbedded = 0;
                boolean haveTokenList = false;
                Token lineToken = null;

                while (true) {
                    doc.getText(start, end - start, charSegment);
                    int segOffset = charSegment.offset;

                    for (int i = segOffset; i < segOffset + charSegment.count; i++) {
                        char ch = charSegment.array[i];
                        if (ch == '<' || ch == '>') {
                            if (!haveTokenList) {
                                lineToken = textArea.getTokenListForLine(curLine);
                                haveTokenList = true;
                            }
                            int offset = start + (i - segOffset);
                            Token t = RSyntaxUtilities.getTokenAtOffset(lineToken, offset);
                            if (t != null && t.getType() == Token.MARKUP_TAG_DELIMITER
                                    && t.getLanguageIndex() == languageIndex) {
                                if (ch == '<') {
                                    numEmbedded++;
                                } else {
                                    if (numEmbedded == 0) {
                                        if (textArea.isCodeFoldingEnabled()
                                                && textArea.getFoldManager().isLineHidden(curLine)) {
                                            return;
                                        }
                                        input.setLocation(caretPosition, offset);
                                        return;
                                    }
                                    numEmbedded--;
                                }
                            }
                        }
                    }

                    if (++curLine == lastLine) {
                        return;
                    }
                    haveTokenList = false;
                    line = map.getElement(curLine);
                    start = line.getStartOffset();
                    end = line.getEndOffset();
                }
            }

            end = caretPosition;
            int numEmbedded = 0;
            boolean haveTokenList = false;
            Token lineToken = null;

            while (true) {
                doc.getText(start, end - start, charSegment);
                int segOffset = charSegment.offset;
                int iStart = segOffset + charSegment.count - 1;

                for (int i = iStart; i >= segOffset; i--) {
                    char ch = charSegment.array[i];
                    if (ch == '<' || ch == '>') {
                        if (!haveTokenList) {
                            lineToken = textArea.getTokenListForLine(curLine);
                            haveTokenList = true;
                        }
                        int offset = start + (i - segOffset);
                        Token t2 = RSyntaxUtilities.getTokenAtOffset(lineToken, offset);
                        if (t2 != null && t2.getType() == Token.MARKUP_TAG_DELIMITER
                                && t2.getLanguageIndex() == languageIndex) {
                            if (ch == '>') {
                                numEmbedded++;
                            } else {
                                if (numEmbedded == 0) {
                                    input.setLocation(caretPosition, offset);
                                    return;
                                }
                                numEmbedded--;
                            }
                        }
                    }
                }

                if (--curLine == -1) {
                    return;
                }
                haveTokenList = false;
                line = map.getElement(curLine);
                start = line.getStartOffset();
                end = line.getEndOffset();
            }
        } catch (BadLocationException e) {
            input.setLocation(-1, -1);
        }
    }
}
