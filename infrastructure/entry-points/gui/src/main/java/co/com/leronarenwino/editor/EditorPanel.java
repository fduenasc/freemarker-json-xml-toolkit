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

import co.com.leronarenwino.i18n.UiMessages;
import co.com.leronarenwino.utils.ButtonStyleUtil;
import co.com.leronarenwino.utils.CaretUtil;
import co.com.leronarenwino.utils.FindReplacePanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public abstract class EditorPanel extends JPanel {
    protected RSyntaxTextArea textArea;
    protected RTextScrollPane scrollPane;
    protected JLabel positionLabel;
    private final JPanel editorFooterPanel;
    private final JLabel editorStatusLabel;
    protected JPanel bottomPanel;
    protected FindReplacePanel findReplacePanel;
    protected JPanel centerPanel;
    protected JPanel topPanel;
    protected JLabel titleLabel;
    protected JButton toggleWrapButton;
    protected boolean isWrapEnabled = false;

    public EditorPanel(String labelText) {
        setLayout(new BorderLayout());
        textArea = new MarkupBracketRSyntaxTextArea();
        scrollPane = new RTextScrollPane(textArea, true);
        positionLabel = new JLabel(UiMessages.caretLineColumn(1, 1));
        positionLabel.setHorizontalAlignment(SwingConstants.TRAILING);

        editorStatusLabel = new JLabel(" ");
        editorStatusLabel.setHorizontalAlignment(SwingConstants.LEADING);

        Color footerTop = UIManager.getColor("Component.borderColor");
        if (footerTop == null) {
            footerTop = UIManager.getColor("Separator.foreground");
        }
        if (footerTop == null) {
            footerTop = new Color(0xC8, 0xC8, 0xC8);
        }
        editorFooterPanel = new JPanel(new BorderLayout(10, 0));
        editorFooterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, footerTop),
                BorderFactory.createEmptyBorder(3, 4, 4, 4)));
        editorFooterPanel.add(editorStatusLabel, BorderLayout.WEST);
        editorFooterPanel.add(positionLabel, BorderLayout.EAST);
        Font baseFont = UIManager.getFont("Label.font");
        if (baseFont != null) {
            Font small = baseFont.deriveFont(Math.max(10f, baseFont.getSize2D() - 1f));
            editorStatusLabel.setFont(small);
            positionLabel.setFont(small);
        }

        JPanel editorBodyStack = new JPanel(new BorderLayout());
        editorBodyStack.add(scrollPane, BorderLayout.CENTER);
        editorBodyStack.add(editorFooterPanel, BorderLayout.SOUTH);

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        titleLabel = new JLabel(labelText);
        topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);

        findReplacePanel = new FindReplacePanel(textArea);

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(findReplacePanel, BorderLayout.NORTH);
        centerPanel.add(editorBodyStack, BorderLayout.CENTER);

        toggleWrapButton = createStyledButton("→", UiMessages.toggleLineWrapTooltip(), ButtonStyleUtil.ButtonStyle.SECONDARY);
        toggleWrapButton.setToolTipText(UiMessages.toggleLineWrapTooltip());
        toggleWrapButton.addActionListener(e -> toggleWrap());

        initComponents();
        setComponents();
        addComponents();

        bottomPanel.add(Box.createHorizontalGlue());
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.EAST);

        textArea.addCaretListener(e -> updateCaretPosition());
        addFindKeyBinding();
        addReplaceKeyBinding();
        addEscapeKeyBinding();
    }

    /**
     * Left side of the footer under the editor (validation hint). Right side is always {@code Ln, Col}.
     */
    protected void setEditorFooterStatus(String text, Color foreground, String toolTip) {
        editorStatusLabel.setText(text != null && !text.isEmpty() ? text : " ");
        editorStatusLabel.setForeground(foreground);
        editorStatusLabel.setToolTipText(toolTip != null && !toolTip.isBlank() ? toolTip : null);
    }

    protected void setEditorFooterStatusVisible(boolean visible) {
        editorStatusLabel.setVisible(visible);
    }

    /**
     * Hides the whole footer row (status + line/column). Use when the panel does not need caret position.
     */
    protected void setEditorFooterRowVisible(boolean visible) {
        editorFooterPanel.setVisible(visible);
    }

    public void toggleWrap() {
        isWrapEnabled = !isWrapEnabled;
        textArea.setLineWrap(isWrapEnabled);
        textArea.setWrapStyleWord(isWrapEnabled);
        toggleWrapButton.setText(isWrapEnabled ? "↵" : "→");
    }

    protected abstract void initComponents();

    protected abstract void setComponents();

    protected abstract void addComponents();

    private void updateCaretPosition() {
        CaretUtil.updateCaretPosition(textArea, positionLabel);
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    protected void setPanelTitle(String text) {
        titleLabel.setText(text);
    }

    /**
     * Refreshes shared chrome (wrap tooltip, find/replace bar, caret position label) after a UI language change.
     */
    protected void refreshCommonChrome() {
        toggleWrapButton.setToolTipText(UiMessages.toggleLineWrapTooltip());
        findReplacePanel.refreshUiLanguage();
        CaretUtil.updateCaretPosition(textArea, positionLabel);
    }

    protected JButton createStyledButton(String text, String tooltip, ButtonStyleUtil.ButtonStyle style) {
        return ButtonStyleUtil.createStyledButton(text, tooltip, style);
    }

    private void addFindKeyBinding() {
        InputMap im = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = textArea.getActionMap();

        im.put(KeyStroke.getKeyStroke("control F"), "showFindBar");
        am.put("showFindBar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String selectedText = textArea.getSelectedText();
                findReplacePanel.showPanel(false);

                if (selectedText != null && !selectedText.trim().isEmpty()) {
                    findReplacePanel.setSearchText(selectedText);
                }
            }
        });
    }

    protected void addReplaceKeyBinding() {
        InputMap im = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = textArea.getActionMap();

        im.put(KeyStroke.getKeyStroke("control R"), "showReplaceBar");
        am.put("showReplaceBar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String selectedText = textArea.getSelectedText();
                findReplacePanel.showPanel(true);

                if (selectedText != null && !selectedText.trim().isEmpty()) {
                    findReplacePanel.setReplaceText(selectedText);
                }
            }
        });
    }

    private void addEscapeKeyBinding() {
        InputMap im = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = textArea.getActionMap();

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "hideFindReplaceBar");
        am.put("hideFindReplaceBar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (findReplacePanel.isVisible()) {
                    findReplacePanel.hidePanel();
                }
            }
        });
    }
}
