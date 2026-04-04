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

import co.com.leronarenwino.config.FreemarkerConfigProvider;
import freemarker.core.ParseException;
import freemarker.template.Template;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import javax.swing.text.BadLocationException;
import java.io.StringReader;
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
            new Template("syntax-check", new StringReader(text), FreemarkerConfigProvider.getConfiguration());
        } catch (ParseException e) {
            int line = Math.max(0, e.getLineNumber() - 1);
            String msg = e.getMessage() != null ? e.getMessage() : "Parse error";
            result.addNotice(new DefaultParserNotice(this, msg, line));
        } catch (BadLocationException e) {
            LOG.log(Level.FINE, "Could not read document for syntax check", e);
        } catch (Exception e) {
            LOG.log(Level.FINE, "Template syntax check failed", e);
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            result.addNotice(new DefaultParserNotice(this, msg, 0));
        }
        return result;
    }
}
