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

import co.com.leronarenwino.TemplateValidator;
import co.com.leronarenwino.UiTextKeys;
import co.com.leronarenwino.i18n.UiMessages;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TemplateUtils {

    public static void formatJsonIfNeeded(RSyntaxTextArea textArea, String lastFormatted, Consumer<String> updateLastFormatted) {
        String currentText = textArea.getText();
        if (currentText.equals(lastFormatted)) return;
        try {
            String formatted = TemplateValidator.formatFlexibleJson(currentText);
            textArea.beginAtomicEdit();
            try {
                textArea.setText(formatted);
            } finally {
                textArea.endAtomicEdit();
            }
            updateLastFormatted.accept(formatted);
        } catch (Exception ex) {
            String detail = ex.getMessage();
            if (UiTextKeys.isJsonFormatFailure(detail)) {
                detail = UiTextKeys.jsonFormatFailureDetail(detail);
            }
            showCopyableErrorDialog(textArea, UiMessages.invalidJsonDialogPrefix() + detail);
        }
    }

    public static void formatJsonSafely(
            JFrame parent,
            RSyntaxTextArea textArea,
            String lastFormatted,
            String lastValid,
            Consumer<String> updateFormatted) {

        String currentText = textArea.getText();
        if (currentText.equals(lastFormatted)) return;

        try {
            String formatted = TemplateValidator.formatFlexibleJson(currentText);
            textArea.beginAtomicEdit();
            try {
                textArea.setText(formatted);
            } finally {
                textArea.endAtomicEdit();
            }
            updateFormatted.accept(formatted);
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (UiTextKeys.isJsonFormatFailure(message)) {
                message = UiMessages.jsonFormatErrorBody(UiTextKeys.jsonFormatFailureDetail(message));
            }
            JTextArea errorTextArea = new JTextArea(message);
            errorTextArea.setEditable(false);
            errorTextArea.setWrapStyleWord(true);
            errorTextArea.setLineWrap(true);

            JScrollPane scrollPane = new JScrollPane(errorTextArea);
            scrollPane.setPreferredSize(new Dimension(600, 200));

            Object[] options = {UiMessages.restoreLastValid(), UiMessages.close()};
            int choice = JOptionPane.showOptionDialog(
                    parent,
                    scrollPane,
                    UiMessages.dataModelErrorTitle(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == JOptionPane.YES_OPTION && lastValid != null) {
                textArea.setText(lastValid);
            }
        }
    }

    public static void showCopyableErrorDialog(Component parent, String message) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 200));

        JOptionPane.showMessageDialog(parent, scrollPane, UiMessages.formatJsonErrorTitle(), JOptionPane.ERROR_MESSAGE);
    }

    public static List<String> validateFields(String output, String[] expectedFields) throws Exception {
        return TemplateValidator.validateFieldsPresentWithTypes(output, expectedFields);
    }

    public static Map<String, Object> parseDataModel(String json) throws Exception {
        return TemplateValidator.parseJsonToDataModel(json.isEmpty() ? "{}" : json);
    }
}