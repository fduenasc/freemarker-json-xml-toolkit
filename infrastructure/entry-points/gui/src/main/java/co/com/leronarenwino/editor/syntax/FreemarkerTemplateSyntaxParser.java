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
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import javax.swing.text.BadLocationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Underlines FreeMarker parse errors using the same configuration as template evaluation.
 */
public class FreemarkerTemplateSyntaxParser extends AbstractParser {

    private static final Logger LOG = Logger.getLogger(FreemarkerTemplateSyntaxParser.class.getName());

    @Override
    public ParseResult parse(RSyntaxDocument doc, String style) {
        DefaultParseResult result = new DefaultParseResult(this);
        if (!FreemarkerSyntaxConstants.SYNTAX_STYLE_FREEMARKER.equals(style)) {
            return result;
        }
        try {
            String text = doc.getText(0, doc.getLength());
            FreemarkerSyntaxDiagnostics.Result r = FreemarkerSyntaxDiagnostics.check(text);
            if (!r.ok()) {
                int line0 = r.line1Based() > 0 ? r.line1Based() - 1 : 0;
                String msg = r.message() != null ? r.message() : "Parse error";
                result.addNotice(new DefaultParserNotice(this, msg, line0));
            }
        } catch (BadLocationException e) {
            LOG.log(Level.FINE, "Could not read document for syntax check", e);
        }
        return result;
    }
}
