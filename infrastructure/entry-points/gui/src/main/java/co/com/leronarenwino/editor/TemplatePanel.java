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

import co.com.leronarenwino.editor.syntax.FreemarkerSyntaxConstants;
import co.com.leronarenwino.editor.syntax.FreemarkerTemplateSyntaxParser;
import co.com.leronarenwino.utils.ButtonStyleUtil;

import javax.swing.*;
import java.awt.*;

public class TemplatePanel extends EditorPanel {
    private static TemplatePanel instance;
    private JButton formatTemplateButton;
    private JButton singleLineButton;

    private TemplatePanel() {
        super("Template");
    }

    @Override
    protected void initComponents() {
        formatTemplateButton = createStyledButton("🔨", "Format Template", ButtonStyleUtil.ButtonStyle.SUCCESS);
        singleLineButton = createStyledButton("↔", "Convert to Single Line", ButtonStyleUtil.ButtonStyle.SECONDARY);
        formatTemplateButton.setToolTipText("Format Template");
        singleLineButton.setToolTipText("Convert to Single Line");
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