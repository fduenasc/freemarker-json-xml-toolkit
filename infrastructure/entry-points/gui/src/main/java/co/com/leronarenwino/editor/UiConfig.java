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
import org.fife.ui.rsyntaxtextarea.Theme;

import co.com.leronarenwino.i18n.UiMessages;

import java.awt.*;
import java.io.InputStream;

public class UiConfig {


    public static void applyRSyntaxTheme(RSyntaxTextArea textArea, String themeResourcePath, Component parent) {
        try (InputStream in = UiConfig.class.getResourceAsStream(themeResourcePath)) {
            if (in != null) {
                Theme theme = Theme.load(in);
                theme.apply(textArea);
            } else {
                TemplateUtils.showCopyableErrorDialog(parent, UiMessages.themeNotFound(themeResourcePath));
            }
        } catch (Exception e) {
            TemplateUtils.showCopyableErrorDialog(parent, UiMessages.themeLoadError(e.getMessage()));
        }
    }


}