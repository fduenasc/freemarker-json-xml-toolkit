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

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldType;

import javax.swing.text.BadLocationException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Folds multi-line FreeMarker block directives ({@code <#if>…</#if>}, {@code <#list>…</#list>}, etc.).
 */
public class FreemarkerFoldParser implements FoldParser {

    private static final Set<String> BLOCK_DIRECTIVES = Set.of(
            "if", "list", "items", "sep", "macro", "function", "switch",
            "attempt", "compress", "transform", "group", "autoesc", "noautoesc",
            "escape", "noescape", "noparse"
    );

    @Override
    @SuppressWarnings("rawtypes")
    public List getFolds(RSyntaxTextArea textArea) {
        List<Fold> folds = new ArrayList<>();
        String t = textArea.getText();
        int len = t.length();
        Deque<Fold> foldStack = new ArrayDeque<>();
        Deque<String> nameStack = new ArrayDeque<>();

        try {
            int i = 0;
            while (i < len) {
                char c = t.charAt(i);
                if (c == '<' && i + 3 < len && t.startsWith("<#--", i)) {
                    int endComment = t.indexOf("-->", i + 4);
                    if (endComment < 0) {
                        break;
                    }
                    i = endComment + 3;
                    continue;
                }
                if (c == '<' && i + 2 < len && t.charAt(i + 1) == '#' && t.charAt(i + 2) == '/') {
                    int nameStart = i + 3;
                    if (nameStart >= len || !isDirectiveNameStart(t.charAt(nameStart))) {
                        i++;
                        continue;
                    }
                    int j = nameStart + 1;
                    while (j < len && isDirectiveNamePart(t.charAt(j))) {
                        j++;
                    }
                    String closeName = t.substring(nameStart, j).toLowerCase(Locale.ROOT);
                    int gt = indexOfDirectiveGt(t, j, len);
                    int next = gt >= 0 ? gt + 1 : j;
                    if (gt >= 0 && !nameStack.isEmpty() && closeName.equals(nameStack.peek())) {
                        Fold f = foldStack.pop();
                        nameStack.pop();
                        f.setEndOffset(gt);
                        if (f.isOnSingleLine()) {
                            removeFold(f, folds);
                        }
                    }
                    i = next;
                    continue;
                }
                if (c == '<' && i + 1 < len && t.charAt(i + 1) == '#') {
                    if (i + 2 < len && t.charAt(i + 2) == '/') {
                        i++;
                        continue;
                    }
                    int nameStart = i + 2;
                    if (nameStart >= len || !isDirectiveNameStart(t.charAt(nameStart))) {
                        i++;
                        continue;
                    }
                    int j = nameStart + 1;
                    while (j < len && isDirectiveNamePart(t.charAt(j))) {
                        j++;
                    }
                    String openName = t.substring(nameStart, j).toLowerCase(Locale.ROOT);
                    if (!BLOCK_DIRECTIVES.contains(openName)) {
                        i++;
                        continue;
                    }
                    int gt = indexOfDirectiveGt(t, j, len);
                    if (gt < 0) {
                        i++;
                        continue;
                    }
                    int next = gt + 1;

                    Fold parent = foldStack.peek();
                    Fold nf;
                    if (parent == null) {
                        nf = new Fold(FoldType.CODE, textArea, i);
                        folds.add(nf);
                    } else {
                        nf = parent.createChild(FoldType.CODE, i);
                    }
                    foldStack.push(nf);
                    nameStack.push(openName);

                    i = next;
                    continue;
                }
                i++;
            }
        } catch (BadLocationException e) {
            // Offsets are derived from String length; mismatches should not occur
        }

        return folds;
    }

    private static boolean isDirectiveNameStart(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    private static boolean isDirectiveNamePart(char ch) {
        return isDirectiveNameStart(ch) || (ch >= '0' && ch <= '9');
    }

    /**
     * Index of the first {@code >} that closes a FreeMarker directive, ignoring {@code >} inside quoted strings.
     */
    private static int indexOfDirectiveGt(String t, int from, int len) {
        boolean inSq = false;
        boolean inDq = false;
        int i = from;
        while (i < len) {
            char c = t.charAt(i);
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
                if (c == '\\' && i + 1 < len) {
                    i++;
                } else if (c == '"') {
                    inDq = false;
                }
            } else {
                if (c == '\\' && i + 1 < len) {
                    i++;
                } else if (c == '\'') {
                    inSq = false;
                }
            }
            i++;
        }
        return -1;
    }

    private static void removeFold(Fold fold, List<Fold> folds) {
        if (!fold.removeFromParent()) {
            folds.remove(folds.size() - 1);
        }
    }
}
