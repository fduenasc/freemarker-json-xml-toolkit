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

import co.com.leronarenwino.utils.ButtonStyleUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public class ExpectedFieldsPanel extends EditorPanel {
    private static ExpectedFieldsPanel instance;
    private JButton validateFieldsButton;
    private JLabel validationResultLabel;
    private BiConsumer<String, Color> statusBarSink;

    private ExpectedFieldsPanel() {
        super("Expected fields");
    }

    @Override
    protected void initComponents() {
        validateFieldsButton = createStyledButton("🔍", "Validate Expected Fields", ButtonStyleUtil.ButtonStyle.PRIMARY);
        validateFieldsButton.setToolTipText("Validate Expected Fields");
        validationResultLabel = new JLabel("Validation result will appear here");
        validationResultLabel.setForeground(Color.GRAY);
    }

    @Override
    protected void setComponents() {
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        textArea.setLineWrap(false);
        textArea.setCodeFoldingEnabled(true);
        textArea.setWrapStyleWord(false);
        textArea.setHighlightCurrentLine(false);
        scrollPane.setFoldIndicatorEnabled(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        setEditorFooterRowVisible(false);
    }

    @Override
    protected void addComponents() {
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(validationResultLabel);
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(validateFieldsButton);
    }

    public static ExpectedFieldsPanel getInstance() {
        if (instance == null) {
            instance = new ExpectedFieldsPanel();
        }
        return instance;
    }

    public JButton getValidateFieldsButton() {
        return validateFieldsButton;
    }

    /**
     * Optional sink for the main window status bar (message without category prefix).
     */
    public void setStatusBarSink(BiConsumer<String, Color> sink) {
        this.statusBarSink = sink;
    }

    public void validateFields(String output) {
        if (output.contains("\\\"")) {
            output = output.replace("\\\"", "\"");
        }

        String expectedFieldsText = textArea.getText();
        if (expectedFieldsText.trim().isEmpty()) {
            String msg = "No expected fields specified";
            Color c = Color.GRAY;
            validationResultLabel.setText(msg);
            validationResultLabel.setForeground(c);
            emitStatusBar(msg, c);
            return;
        }

        String[] expectedFields = expectedFieldsText.split("\\s*,\\s*|\\s+");
        try {
            java.util.List<String> missing = TemplateUtils.validateFields(output, expectedFields);
            if (missing.isEmpty()) {
                String msg = "All expected fields are present";
                Color c = new Color(0, 128, 0);
                validationResultLabel.setText(msg);
                validationResultLabel.setForeground(c);
                emitStatusBar(msg, c);
            } else {
                String msg = "Missing fields: " + String.join(", ", missing);
                Color c = Color.RED;
                validationResultLabel.setText(msg);
                validationResultLabel.setForeground(c);
                emitStatusBar(msg, c);
            }
        } catch (Exception e) {
            String msg = "Invalid JSON output";
            Color c = Color.RED;
            validationResultLabel.setText(msg);
            validationResultLabel.setForeground(c);
            emitStatusBar(msg, c);
        }
    }

    private void emitStatusBar(String message, Color color) {
        if (statusBarSink != null) {
            statusBarSink.accept(message, color);
        }
    }
}