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
        positionLabel = new JLabel("1:1");
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create title label and top panel
        titleLabel = new JLabel(labelText);
        topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(positionLabel, BorderLayout.EAST);

        // Create find/replace panel
        findReplacePanel = new FindReplacePanel(textArea);

        // Create center panel to hold find/replace and scroll pane
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(findReplacePanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Initialize wrap button
        toggleWrapButton = createStyledButton("→", "Toggle line wrap", ButtonStyleUtil.ButtonStyle.SECONDARY);
        toggleWrapButton.setToolTipText("Toggle line wrap");
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