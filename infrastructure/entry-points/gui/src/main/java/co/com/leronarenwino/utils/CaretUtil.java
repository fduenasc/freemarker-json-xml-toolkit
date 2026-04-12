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

package co.com.leronarenwino.utils;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;

public class CaretUtil {
    public static void updateCaretPosition(RSyntaxTextArea textArea, JLabel label) {
        int caretPos = textArea.getCaretPosition();
        try {
            int line = textArea.getLineOfOffset(caretPos) + 1;
            int column = caretPos - textArea.getLineStartOffset(line - 1) + 1;
            label.setText("Ln " + line + ", Col " + column);
        } catch (Exception ex) {
            label.setText("Ln ?, Col ?");
        }
    }
}