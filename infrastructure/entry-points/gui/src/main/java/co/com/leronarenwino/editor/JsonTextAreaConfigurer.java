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

/**
 * Aligns in-editor behaviour with Jackson pretty-print (2 spaces per level): auto-indent on new lines,
 * Tab inserts spaces, and braces auto-close so structure stays consistent while typing.
 */
public final class JsonTextAreaConfigurer {

    /** Same step as {@link co.com.leronarenwino.TemplateValidator} pretty JSON output. */
    public static final int JSON_INDENT_SPACES = 2;

    private static final String CLOSING_ALIGN_LISTENER_KEY = "jsonClosingBraceAligner";

    private JsonTextAreaConfigurer() {
    }

    public static void apply(RSyntaxTextArea textArea) {
        textArea.setTabSize(JSON_INDENT_SPACES);
        textArea.setTabsEmulated(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setCloseCurlyBraces(true);
        if (textArea.isEditable() && textArea.getClientProperty(CLOSING_ALIGN_LISTENER_KEY) == null) {
            textArea.putClientProperty(CLOSING_ALIGN_LISTENER_KEY, Boolean.TRUE);
            textArea.getDocument().addDocumentListener(new JsonClosingBraceDocumentListener(textArea));
        }
    }
}
