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
import java.util.function.Consumer;

public class DataPanel extends EditorPanel {
    private static DataPanel instance;
    private JButton validateDataModelButton;
    private JButton formatDataModelButton;
    private Consumer<JsonSyntaxCheck> jsonStatusSink;

    private DataPanel() {
        super("Data Model");
    }

    /**
     * Receives JSON syntax checks (e.g. the frame status bar). Set from {@link TemplateEditor}.
     */
    public void setJsonStatusSink(Consumer<JsonSyntaxCheck> sink) {
        this.jsonStatusSink = sink;
    }

    @Override
    protected void initComponents() {
        validateDataModelButton = createStyledButton("🔍", "Validate data model JSON (no changes)", ButtonStyleUtil.ButtonStyle.PRIMARY);
        validateDataModelButton.setToolTipText("Validate JSON without formatting");
        formatDataModelButton = createStyledButton("🔨", "Format data model JSON", ButtonStyleUtil.ButtonStyle.SUCCESS);
        formatDataModelButton.setToolTipText("Pretty-print JSON (invalid JSON shows an error)");
    }

    @Override
    protected void setComponents() {
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        textArea.setLineWrap(isWrapEnabled);
        textArea.setWrapStyleWord(isWrapEnabled);
        textArea.setHighlightCurrentLine(false);

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
    }

    @Override
    protected void addComponents() {
        bottomPanel.add(validateDataModelButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(formatDataModelButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(toggleWrapButton);
    }

    /**
     * Re-runs syntax check and forwards to the status sink (e.g. main window status bar).
     */
    public void refreshJsonValidationStatus() {
        emitStatus(TemplateValidator.checkDataModelJsonSyntax(textArea.getText()));
    }

    /**
     * Validates JSON, updates status, and moves the caret to the reported error position when possible.
     */
    public void validateDataModelAndFocusError() {
        JsonSyntaxCheck check = TemplateValidator.checkDataModelJsonSyntax(textArea.getText());
        emitStatus(check);
        if (!check.syntaxValid() && check.line() > 0) {
            moveCaretTo(check.line(), check.column());
        }
    }

    private void emitStatus(JsonSyntaxCheck check) {
        if (jsonStatusSink != null) {
            jsonStatusSink.accept(check);
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
