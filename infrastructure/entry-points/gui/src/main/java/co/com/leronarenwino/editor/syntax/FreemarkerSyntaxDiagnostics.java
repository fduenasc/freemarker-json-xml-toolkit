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

import co.com.leronarenwino.FreemarkerTemplateSyntaxChecker;
import co.com.leronarenwino.TemplateValidator;

/**
 * Maps {@link FreemarkerTemplateSyntaxChecker} results for {@link FreemarkerTemplateSyntaxParser} (line indices).
 */
public final class FreemarkerSyntaxDiagnostics {

    /**
     * @param line1Based 1-based line when known, else {@code 0}
     * @param column1Based 1-based column when known, else {@code 0}
     */
    public record Result(boolean ok, int line1Based, int column1Based, String message) {
    }

    private FreemarkerSyntaxDiagnostics() {
    }

    public static Result check(String source) {
        TemplateValidator.FreemarkerTemplateSyntaxCheck c = FreemarkerTemplateSyntaxChecker.check(source);
        if (c.syntaxValid()) {
            return new Result(true, 0, 0, null);
        }
        int line = c.line() > 0 ? c.line() : 0;
        int col = c.column() > 0 ? c.column() : 0;
        String msg = c.message() != null ? c.message() : "Parse error";
        return new Result(false, line, col, msg);
    }
}
