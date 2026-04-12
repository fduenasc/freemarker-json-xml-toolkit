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
import co.com.leronarenwino.TemplateValidator.EditorJsonSyntaxFailure;
import co.com.leronarenwino.TemplateValidator.JsonSyntaxCheck;
import co.com.leronarenwino.editor.syntax.JsonDataModelSyntaxParser;
import co.com.leronarenwino.utils.ButtonStyleUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.Objects;

public class DataPanel extends EditorPanel {
    private static DataPanel instance;
    private JButton validateDataModelButton;
    private JButton formatDataModelButton;

    private DataPanel() {
        super("Data Model");
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
        textArea.addParser(new JsonDataModelSyntaxParser());

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
        emitStatus(checkCurrentDataModelJson());
    }

    /**
     * Validates JSON, updates status, selects the error span when possible, and scrolls it into view.
     */
    public void validateDataModelAndFocusError() {
        String json = editorSnapshotText();
        JsonSyntaxCheck check = TemplateValidator.checkDataModelJsonSyntax(json);
        emitStatus(check);
        if (check.syntaxValid()) {
            return;
        }
        EditorJsonSyntaxFailure fail = TemplateValidator.findJsonSyntaxFailureInFullText(json);
        if (fail != null) {
            focusJsonSyntaxFailure(fail);
        }
    }

    private JsonSyntaxCheck checkCurrentDataModelJson() {
        return TemplateValidator.checkDataModelJsonSyntax(editorSnapshotText());
    }

    /**
     * Swing's {@code getText()} is annotated nullable in some JDK/editor stubs; validators expect a non-null {@link String}.
     */
    private String editorSnapshotText() {
        return Objects.requireNonNullElse(textArea.getText(), "");
    }

    private void focusJsonSyntaxFailure(EditorJsonSyntaxFailure fail) {
        int len = textArea.getDocument().getLength();
        if (len <= 0) {
            return;
        }
        int pos = -1;
        if (fail.hasCharOffset() && fail.charOffset() >= 0 && fail.charOffset() < len) {
            pos = (int) fail.charOffset();
        } else if (fail.line1Based() > 0) {
            try {
                int lineIndex = fail.line1Based() - 1;
                int lineStart = textArea.getLineStartOffset(lineIndex);
                int lineEnd = textArea.getLineEndOffset(lineIndex);
                int col = Math.max(0, fail.column1Based() - 1);
                pos = Math.min(lineStart + col, Math.max(lineStart, lineEnd - 1));
            } catch (BadLocationException ignored) {
                // ignore
            }
        }
        if (pos < 0) {
            return;
        }
        textArea.requestFocusInWindow();
        int end = Math.min(pos + 1, len);
        textArea.select(pos, end);
        try {
            Rectangle r = textArea.modelToView(pos);
            if (r != null) {
                textArea.scrollRectToVisible(r);
            }
        } catch (BadLocationException ignored) {
            // ignore
        }
    }

    private void emitStatus(JsonSyntaxCheck check) {
        applyJsonCheckToFooter(check);
    }

    private void applyJsonCheckToFooter(JsonSyntaxCheck check) {
        if (!check.syntaxValid()) {
            StringBuilder sb = new StringBuilder("JSON no válido");
            if (check.line() > 0) {
                sb.append(" (Ln ").append(check.line());
                if (check.column() > 0) {
                    sb.append(", Col ").append(check.column());
                }
                sb.append(')');
            }
            String tip = check.message();
            setEditorFooterStatus(sb.toString(), Color.RED, tip != null && !tip.isBlank() ? tip : null);
            return;
        }
        if (check.message() != null && !check.message().isEmpty()) {
            setEditorFooterStatus(check.message(), new Color(180, 120, 0), null);
            return;
        }
        setEditorFooterStatus("JSON válido", new Color(0, 128, 0), null);
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
