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

package co.com.leronarenwino;

import co.com.leronarenwino.config.FreemarkerConfigProvider;
import freemarker.core.ParseException;
import freemarker.template.Template;

import java.io.StringReader;

/**
 * Syntax-only validation of FreeMarker template text using the same {@link freemarker.template.Configuration}
 * as {@link FreemarkerProcessor}.
 */
public final class FreemarkerTemplateSyntaxChecker {

    private FreemarkerTemplateSyntaxChecker() {
    }

    /**
     * @param source full template source; {@code null} treated as empty (valid)
     */
    public static TemplateValidator.FreemarkerTemplateSyntaxCheck check(String source) {
        String t = source == null ? "" : source;
        if (t.isEmpty()) {
            return new TemplateValidator.FreemarkerTemplateSyntaxCheck(true, "", -1, -1);
        }
        try {
            new Template("syntax-check", new StringReader(t), FreemarkerConfigProvider.getConfiguration());
            return new TemplateValidator.FreemarkerTemplateSyntaxCheck(true, "", -1, -1);
        } catch (ParseException e) {
            int line = e.getLineNumber();
            int col = e.getColumnNumber();
            if (line < 1) {
                line = -1;
            }
            if (col < 1) {
                col = -1;
            }
            String msg = e.getMessage() != null ? e.getMessage() : "Parse error";
            return new TemplateValidator.FreemarkerTemplateSyntaxCheck(false, msg, line, col);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return new TemplateValidator.FreemarkerTemplateSyntaxCheck(false, msg, -1, -1);
        }
    }
}
