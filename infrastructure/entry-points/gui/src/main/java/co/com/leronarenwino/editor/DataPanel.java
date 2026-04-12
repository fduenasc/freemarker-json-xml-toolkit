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
import co.com.leronarenwino.i18n.UiMessages;
import co.com.leronarenwino.editor.syntax.JsonDataModelSyntaxParser;
import co.com.leronarenwino.utils.ButtonStyleUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class DataPanel extends EditorPanel {
    private JButton formatDataModelButton;

    DataPanel() {
        super(UiMessages.panelDataModel());
    }

    @Override
    protected void initComponents() {
        formatDataModelButton = createStyledButton("🔨", UiMessages.formatDataModelAccessible(), ButtonStyleUtil.ButtonStyle.SUCCESS);
        formatDataModelButton.setToolTipText(UiMessages.formatDataModelTooltip());
    }

    @Override
    protected void setComponents() {
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        JsonTextAreaConfigurer.apply(textArea);
        textArea.setCodeFoldingEnabled(true);
        textArea.setLineWrap(isWrapEnabled);
        textArea.setWrapStyleWord(isWrapEnabled);
        textArea.setHighlightCurrentLine(false);
        textArea.addParser(new JsonDataModelSyntaxParser());

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
    }

    @Override
    protected void addComponents() {
        bottomPanel.add(formatDataModelButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(toggleWrapButton);
    }

    /**
     * Re-runs JSON syntax check and updates the panel footer.
     */
    public void refreshJsonValidationStatus() {
        emitStatus(checkCurrentDataModelJson());
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

    private void emitStatus(JsonSyntaxCheck check) {
        applyJsonCheckToFooter(check);
    }

    private void applyJsonCheckToFooter(JsonSyntaxCheck check) {
        if (!check.syntaxValid()) {
            StringBuilder sb = new StringBuilder(UiMessages.invalidJson());
            if (check.line() > 0) {
                sb.append(" (").append(UiMessages.footerLineAbbrev()).append(' ').append(check.line());
                if (check.column() > 0) {
                    sb.append(", ").append(UiMessages.footerColAbbrev()).append(' ').append(check.column());
                }
                sb.append(')');
            }
            String tip = UiMessages.resolveDataModelMessage(check.message());
            setEditorFooterStatus(sb.toString(), Color.RED, tip != null && !tip.isBlank() ? tip : null);
            return;
        }
        if (check.message() != null && !check.message().isEmpty()) {
            setEditorFooterStatus(UiMessages.resolveDataModelMessage(check.message()), new Color(180, 120, 0), null);
            return;
        }
        setEditorFooterStatus(UiMessages.validJson(), new Color(0, 128, 0), null);
    }

    public void refreshLocalizedChrome() {
        refreshCommonChrome();
        setPanelTitle(UiMessages.panelDataModel());
        formatDataModelButton.setToolTipText(UiMessages.formatDataModelTooltip());
        formatDataModelButton.getAccessibleContext().setAccessibleName(UiMessages.formatDataModelAccessible());
        refreshJsonValidationStatus();
    }

    public JButton getFormatDataModelButton() {
        return formatDataModelButton;
    }
}
