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

package co.com.leronarenwino.editor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Keeps closing delimiters aligned with a bare opening-brace or opening-bracket line:
 * <ul>
 *   <li>When you type a lone closing brace or bracket on an over-indented line (auto-indent).</li>
 *   <li>When you press Enter between an empty pair so the editor inserts a newline and indents the line
 *       that still contains the closing delimiter — that line is dedented to match the opening delimiter.</li>
 * </ul>
 */
final class JsonClosingBraceDocumentListener implements DocumentListener {

    private final RSyntaxTextArea textArea;
    private boolean alignInProgress;

    JsonClosingBraceDocumentListener(RSyntaxTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (alignInProgress) {
            return;
        }
        try {
            Document doc = e.getDocument();
            int len = e.getLength();
            int off = e.getOffset();
            if (len == 2) {
                String pair = doc.getText(off, 2);
                if ("\r\n".equals(pair)) {
                    SwingUtilities.invokeLater(() -> fixBraceLineAfterNewline(off, 2));
                }
                return;
            }
            if (len != 1) {
                return;
            }
            String ch = doc.getText(off, 1);
            if ("\n".equals(ch) || "\r".equals(ch)) {
                SwingUtilities.invokeLater(() -> fixBraceLineAfterNewline(off, 1));
                return;
            }
            if (!"}".equals(ch) && !"]".equals(ch)) {
                return;
            }
            SwingUtilities.invokeLater(() -> alignClosingDelimiter(off, ch.charAt(0)));
        } catch (BadLocationException ignored) {
            // skip
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        // no-op
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        // no-op
    }

    /**
     * After Enter inside an empty pair, the next line often becomes only spaces plus the closing brace (auto-indent).
     * Dedent that line to match a bare opening line above.
     */
    private void fixBraceLineAfterNewline(int insertOff, int insertLen) {
        if (!textArea.isEditable()) {
            return;
        }
        alignInProgress = true;
        try {
            String text = textArea.getText();
            int[] nl = newlineSequenceAt(text, insertOff, insertLen);
            if (nl == null) {
                return;
            }
            int nlStart = nl[0];
            int nextLineStart = nl[1];

            int prevLineStart = lineStart(text, nlStart);
            String prevSegment = text.substring(prevLineStart, nlStart);
            int openIdx = firstOpenBracketOffset(prevSegment, prevLineStart);
            if (openIdx < 0) {
                return;
            }
            char open = text.charAt(openIdx);
            if (!isBareOpenLine(text, openIdx, open)) {
                return;
            }

            int closeLineStart = nextLineStart;
            int closeLineEnd = lineEndExclusive(text, closeLineStart);
            if (closeLineEnd <= closeLineStart) {
                return;
            }
            String closeLine = text.substring(closeLineStart, closeLineEnd);
            char close = matchingClose(open);
            if (!closeLine.trim().equals(String.valueOf(close))) {
                return;
            }

            int closeCharIdx = closeLineStart;
            while (closeCharIdx < closeLineEnd && text.charAt(closeCharIdx) != close) {
                closeCharIdx++;
            }
            if (closeCharIdx >= closeLineEnd) {
                return;
            }
            if (findMatchingOpenPosition(text, closeCharIdx, close) != openIdx) {
                return;
            }

            String prefix = text.substring(prevLineStart, openIdx);
            String replacement = prefix + close;
            if (closeLine.equals(replacement)) {
                return;
            }

            textArea.replaceRange(replacement, closeLineStart, closeLineStart + closeLine.length());
            textArea.setCaretPosition(Math.min(closeLineStart + replacement.length(), textArea.getDocument().getLength()));
        } finally {
            alignInProgress = false;
        }
    }

    /** {@code [newlineStart, indexAfterNewline]} */
    private static int[] newlineSequenceAt(String text, int insertOff, int insertLen) {
        if (insertLen == 2 && insertOff + 1 < text.length()
                && text.charAt(insertOff) == '\r' && text.charAt(insertOff + 1) == '\n') {
            return new int[]{insertOff, insertOff + 2};
        }
        if (insertLen == 1 && insertOff < text.length()) {
            char c = text.charAt(insertOff);
            if (c == '\n' || c == '\r') {
                return new int[]{insertOff, insertOff + 1};
            }
        }
        return null;
    }

    private static int firstOpenBracketOffset(String lineSegment, int segmentBaseOffset) {
        for (int i = 0; i < lineSegment.length(); i++) {
            char c = lineSegment.charAt(i);
            if (c == '{' || c == '[') {
                return segmentBaseOffset + i;
            }
        }
        return -1;
    }

    private static char matchingClose(char open) {
        return open == '{' ? '}' : ']';
    }

    private void alignClosingDelimiter(int closeIdx, char close) {
        if (!textArea.isEditable()) {
            return;
        }
        alignInProgress = true;
        try {
            String text = textArea.getText();
            if (closeIdx < 0 || closeIdx >= text.length() || text.charAt(closeIdx) != close) {
                return;
            }
            int openIdx = findMatchingOpenPosition(text, closeIdx, close);
            if (openIdx < 0) {
                return;
            }
            char open = text.charAt(openIdx);
            if (!isBareOpenLine(text, openIdx, open)) {
                return;
            }
            int closeLineStart = lineStart(text, closeIdx);
            int closeLineEnd = lineEndExclusive(text, closeIdx);
            String closeLine = text.substring(closeLineStart, closeLineEnd);
            String trimmed = closeLine.trim();
            if (!trimmed.equals(String.valueOf(close))) {
                return;
            }
            int ls = lineStart(text, openIdx);
            String prefix = text.substring(ls, openIdx);
            String replacement = prefix + close;
            if (closeLine.equals(replacement)) {
                return;
            }
            textArea.replaceRange(replacement, closeLineStart, closeLineStart + closeLine.length());
            int newCaret = closeLineStart + replacement.length();
            textArea.setCaretPosition(Math.min(newCaret, textArea.getDocument().getLength()));
        } finally {
            alignInProgress = false;
        }
    }

    /**
     * Line is only optional whitespace + open + optional whitespace (a lone opening brace or bracket).
     */
    private static boolean isBareOpenLine(String text, int openIdx, char open) {
        int ls = lineStart(text, openIdx);
        int le = lineEndExclusive(text, openIdx);
        String line = text.substring(ls, le);
        return line.trim().equals(String.valueOf(open));
    }

    private static int lineStart(String text, int offset) {
        int i = offset;
        while (i > 0 && text.charAt(i - 1) != '\n') {
            i--;
        }
        return i;
    }

    /** End of line excluding {@code \n} / {@code \r\n}. */
    private static int lineEndExclusive(String text, int offset) {
        int i = offset;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\n') {
                return i;
            }
            if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                return i;
            }
            if (c == '\r') {
                return i;
            }
            i++;
        }
        return text.length();
    }

    /**
     * Returns index of the matching opening brace or bracket for the closing delimiter at {@code closeIdx}.
     */
    private static int findMatchingOpenPosition(String text, int closeIdx, char close) {
        Deque<Integer> stack = new ArrayDeque<>();
        boolean inString = false;
        boolean escape = false;
        for (int i = 0; i <= closeIdx; i++) {
            char c = text.charAt(i);
            if (inString) {
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '{' || c == '[') {
                stack.push(i);
            } else if (c == '}') {
                if (!stack.isEmpty() && text.charAt(stack.peek()) == '{') {
                    int openPos = stack.pop();
                    if (i == closeIdx) {
                        return openPos;
                    }
                }
            } else if (c == ']') {
                if (!stack.isEmpty() && text.charAt(stack.peek()) == '[') {
                    int openPos = stack.pop();
                    if (i == closeIdx) {
                        return openPos;
                    }
                }
            }
        }
        return -1;
    }
}
