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

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

/**
 * Registers FreeMarker syntax style, folding, and related RSyntaxTextArea integration.
 */
public final class FreemarkerSyntaxSupport {

    private static volatile boolean registered;

    private FreemarkerSyntaxSupport() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        synchronized (FreemarkerSyntaxSupport.class) {
            if (registered) {
                return;
            }
            AbstractTokenMakerFactory factory = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
            factory.putMapping(
                    FreemarkerSyntaxConstants.SYNTAX_STYLE_FREEMARKER,
                    FreemarkerTokenMaker.class.getName()
            );
            FoldParserManager.get().addFoldParserMapping(
                    FreemarkerSyntaxConstants.SYNTAX_STYLE_FREEMARKER,
                    new FreemarkerFoldParser()
            );
            registered = true;
        }
    }
}
