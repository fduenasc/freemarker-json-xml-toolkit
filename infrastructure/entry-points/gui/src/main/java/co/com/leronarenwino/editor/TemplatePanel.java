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

import co.com.leronarenwino.FreemarkerTemplateSyntaxChecker;
import co.com.leronarenwino.TemplateValidator.FreemarkerTemplateSyntaxCheck;
import co.com.leronarenwino.i18n.UiMessages;
import co.com.leronarenwino.editor.syntax.FreemarkerSyntaxConstants;
import co.com.leronarenwino.editor.syntax.FreemarkerTemplateSyntaxParser;
import co.com.leronarenwino.utils.ButtonStyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class TemplatePanel extends EditorPanel {
    private static TemplatePanel instance;
    private JButton formatTemplateButton;
    private JButton singleLineButton;

    private TemplatePanel() {
        super(UiMessages.panelTemplate());
    }

    @Override
    protected void initComponents() {
        formatTemplateButton = createStyledButton("🔨", UiMessages.formatTemplateAccessible(), ButtonStyleUtil.ButtonStyle.SUCCESS);
        singleLineButton = createStyledButton("↔", UiMessages.singleLineAccessible(), ButtonStyleUtil.ButtonStyle.SECONDARY);
        formatTemplateButton.setToolTipText(UiMessages.formatTemplateTooltip());
        singleLineButton.setToolTipText(UiMessages.singleLineTooltip());
    }

    @Override
    protected void setComponents() {
        textArea.setSyntaxEditingStyle(FreemarkerSyntaxConstants.SYNTAX_STYLE_FREEMARKER);
        textArea.setCodeFoldingEnabled(true);
        textArea.addParser(new FreemarkerTemplateSyntaxParser());
        textArea.setLineWrap(isWrapEnabled);
        textArea.setWrapStyleWord(isWrapEnabled);
        textArea.setHighlightCurrentLine(false);

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        refreshTemplateSyntaxFooter();
    }

    /**
     * Re-runs FreeMarker syntax check and updates the panel footer (debounced from {@link TemplateEditor}).
     */
    public void refreshTemplateSyntaxFooter() {
        emitTemplateStatus(FreemarkerTemplateSyntaxChecker.check(editorSnapshotText()));
    }

    private String editorSnapshotText() {
        return Objects.requireNonNullElse(textArea.getText(), "");
    }

    private void emitTemplateStatus(FreemarkerTemplateSyntaxCheck check) {
        if (!check.syntaxValid()) {
            StringBuilder sb = new StringBuilder(UiMessages.invalidTemplate());
            if (check.line() > 0) {
                sb.append(" (").append(UiMessages.footerLineAbbrev()).append(' ').append(check.line());
                if (check.column() > 0) {
                    sb.append(", ").append(UiMessages.footerColAbbrev()).append(' ').append(check.column());
                }
                sb.append(')');
            }
            String tip = check.message();
            setEditorFooterStatus(sb.toString(), Color.RED, tip != null && !tip.isBlank() ? tip : null);
            return;
        }
        setEditorFooterStatus(UiMessages.templateOk(), new Color(0, 128, 0), null);
    }

    public void refreshLocalizedChrome() {
        refreshCommonChrome();
        setPanelTitle(UiMessages.panelTemplate());
        formatTemplateButton.setToolTipText(UiMessages.formatTemplateTooltip());
        formatTemplateButton.getAccessibleContext().setAccessibleName(UiMessages.formatTemplateAccessible());
        singleLineButton.setToolTipText(UiMessages.singleLineTooltip());
        singleLineButton.getAccessibleContext().setAccessibleName(UiMessages.singleLineAccessible());
        refreshTemplateSyntaxFooter();
    }

    @Override
    protected void addComponents() {
        bottomPanel.add(formatTemplateButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(singleLineButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(toggleWrapButton);
    }

    public static TemplatePanel getInstance() {
        if (instance == null) {
            instance = new TemplatePanel();
        }
        return instance;
    }

    public JButton getFormatTemplateButton() {
        return formatTemplateButton;
    }

    public JButton getSingleLineButton() {
        return singleLineButton;
    }
}