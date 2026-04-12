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

import java.io.StringReader;

/**
 * Shared FreeMarker parse check for squiggles ({@link FreemarkerTemplateSyntaxParser}) and editor footer status.
 */
public final class FreemarkerSyntaxDiagnostics {

    /**
     * @param line1Based 1-based line when {@link ParseException} provides it, else {@code 0}
     * @param column1Based 1-based column when available, else {@code 0}
     */
    public record Result(boolean ok, int line1Based, int column1Based, String message) {
    }

    private FreemarkerSyntaxDiagnostics() {
    }

    public static Result check(String source) {
        String t = source == null ? "" : source;
        try {
            new Template("syntax-check", new StringReader(t), FreemarkerConfigProvider.getConfiguration());
            return new Result(true, 0, 0, null);
        } catch (ParseException e) {
            int line = Math.max(0, e.getLineNumber());
            int col = Math.max(0, e.getColumnNumber());
            String msg = e.getMessage() != null ? e.getMessage() : "Parse error";
            return new Result(false, line, col, msg);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return new Result(false, 0, 0, msg);
        }
    }
}
