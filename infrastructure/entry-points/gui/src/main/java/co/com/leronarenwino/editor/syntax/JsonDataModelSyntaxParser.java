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

import co.com.leronarenwino.TemplateValidator;
import co.com.leronarenwino.TemplateValidator.EditorJsonSyntaxFailure;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

/**
 * Squiggle-underlines JSON parse errors in the data model editor (Jackson), with {@link org.fife.ui.rsyntaxtextarea.ErrorStrip} support.
 */
public class JsonDataModelSyntaxParser extends AbstractParser {

    @Override
    public ParseResult parse(RSyntaxDocument doc, String style) {
        DefaultParseResult result = new DefaultParseResult(this);
        if (!SyntaxConstants.SYNTAX_STYLE_JSON.equals(style)) {
            return result;
        }
        try {
            String text = doc.getText(0, doc.getLength());
            if (text.trim().isEmpty()) {
                return result;
            }
            EditorJsonSyntaxFailure fail = TemplateValidator.findJsonSyntaxFailureInFullText(text);
            if (fail == null) {
                return result;
            }
            int docLen = doc.getLength();
            int line0 = fail.line1Based() > 0 ? fail.line1Based() - 1 : 0;
            int offset = -1;
            if (fail.hasCharOffset() && fail.charOffset() >= 0 && fail.charOffset() < docLen) {
                offset = (int) fail.charOffset();
            } else if (fail.line1Based() > 0) {
                offset = offsetFromLineColumn(doc, line0, fail.column1Based(), docLen);
            }
            String msg = String.valueOf(fail.message());
            if (offset >= 0 && offset < docLen) {
                result.addNotice(new DefaultParserNotice(this, msg, line0, offset, 1));
            } else {
                result.addNotice(new DefaultParserNotice(this, msg, Math.max(0, line0)));
            }
        } catch (BadLocationException ignored) {
            // skip notice
        }
        return result;
    }

    private static int offsetFromLineColumn(RSyntaxDocument doc, int line0, int column1Based, int docLen) {
        try {
            Element root = doc.getDefaultRootElement();
            int count = root.getElementCount();
            if (count <= 0) {
                return -1;
            }
            int li = Math.min(Math.max(0, line0), count - 1);
            Element lineEl = root.getElement(li);
            int lineStart = lineEl.getStartOffset();
            int lineEnd = lineEl.getEndOffset();
            int col0 = column1Based > 0 ? column1Based - 1 : 0;
            return Math.min(lineStart + col0, Math.max(lineStart, lineEnd - 1));
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }
}
