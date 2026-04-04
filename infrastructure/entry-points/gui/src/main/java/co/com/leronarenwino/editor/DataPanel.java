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
import co.com.leronarenwino.TemplateValidator.JsonSyntaxCheck;
import co.com.leronarenwino.utils.ButtonStyleUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class DataPanel extends EditorPanel {
    private static DataPanel instance;
    private JButton validateDataModelButton;
    private JButton formatDataModelButton;
    private JLabel validationStatusLabel;

    private DataPanel() {
        super("Data Model");
    }

    @Override
    protected void initComponents() {
        validateDataModelButton = createStyledButton("🔍", "Validate data model JSON (no changes)", ButtonStyleUtil.ButtonStyle.PRIMARY);
        validateDataModelButton.setToolTipText("Validate JSON without formatting");
        formatDataModelButton = createStyledButton("🔨", "Format data model JSON", ButtonStyleUtil.ButtonStyle.SUCCESS);
        formatDataModelButton.setToolTipText("Pretty-print JSON (invalid JSON shows an error)");
        validationStatusLabel = new JLabel();
        validationStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        applyValidationResult(TemplateValidator.checkDataModelJsonSyntax(""));
    }

    @Override
    protected void setComponents() {
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        textArea.setLineWrap(isWrapEnabled);
        textArea.setWrapStyleWord(isWrapEnabled);
        textArea.setHighlightCurrentLine(false);

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        Font base = validationStatusLabel.getFont();
        validationStatusLabel.setFont(base.deriveFont(Font.PLAIN, base.getSize2D() * 0.92f));
    }

    @Override
    protected void addComponents() {
        topPanel.removeAll();
        topPanel.setLayout(new BorderLayout(8, 0));
        JPanel headerRow = new JPanel(new BorderLayout(10, 0));
        headerRow.setOpaque(false);
        headerRow.add(titleLabel, BorderLayout.WEST);
        headerRow.add(validationStatusLabel, BorderLayout.CENTER);
        headerRow.add(positionLabel, BorderLayout.EAST);
        topPanel.add(headerRow, BorderLayout.CENTER);

        bottomPanel.add(validateDataModelButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(formatDataModelButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(toggleWrapButton);
    }

    /**
     * Re-runs syntax check and updates the status label (e.g. after edits or format).
     */
    public void refreshJsonValidationStatus() {
        applyValidationResult(TemplateValidator.checkDataModelJsonSyntax(textArea.getText()));
    }

    /**
     * Validates JSON, updates the label, and moves the caret to the reported error position when possible.
     */
    public void validateDataModelAndFocusError() {
        JsonSyntaxCheck check = TemplateValidator.checkDataModelJsonSyntax(textArea.getText());
        applyValidationResult(check);
        if (!check.syntaxValid() && check.line() > 0) {
            moveCaretTo(check.line(), check.column());
        }
    }

    private void moveCaretTo(int jacksonLine1Based, int jacksonColumn1Based) {
        int lineIndex = jacksonLine1Based - 1;
        if (lineIndex < 0) {
            return;
        }
        try {
            int start = textArea.getLineStartOffset(lineIndex);
            int lineEnd = textArea.getLineEndOffset(lineIndex);
            int col = Math.max(0, jacksonColumn1Based - 1);
            int pos = Math.min(start + col, Math.max(start, lineEnd - 1));
            textArea.setCaretPosition(pos);
            textArea.requestFocusInWindow();
        } catch (BadLocationException ignored) {
            // line out of range after edit
        }
    }

    private static final int STATUS_DISPLAY_MAX_CHARS = 72;

    private void applyValidationResult(JsonSyntaxCheck check) {
        Color color;
        String fullText;
        if (!check.syntaxValid()) {
            color = Color.RED;
            StringBuilder sb = new StringBuilder("Invalid JSON");
            if (check.line() > 0) {
                sb.append(" (line ").append(check.line());
                if (check.column() > 0) {
                    sb.append(", col ").append(check.column());
                }
                sb.append(')');
            }
            sb.append(": ").append(check.message());
            fullText = sb.toString();
        } else if (check.message() != null && !check.message().isEmpty()) {
            color = new Color(180, 120, 0);
            fullText = check.message();
        } else {
            color = new Color(0, 128, 0);
            fullText = "Data model JSON is valid";
        }
        validationStatusLabel.setForeground(color);
        if (fullText.length() <= STATUS_DISPLAY_MAX_CHARS) {
            validationStatusLabel.setText(fullText);
            validationStatusLabel.setToolTipText(null);
        } else {
            validationStatusLabel.setText(fullText.substring(0, STATUS_DISPLAY_MAX_CHARS - 1) + "…");
            validationStatusLabel.setToolTipText(fullText);
        }
    }

    public static DataPanel getInstance() {
        if (instance == null) {
            instance = new DataPanel();
        }
        return instance;
    }

    public JButton getValidateDataModelButton() {
        return validateDataModelButton;
    }

    public JButton getFormatDataModelButton() {
        return formatDataModelButton;
    }
}
