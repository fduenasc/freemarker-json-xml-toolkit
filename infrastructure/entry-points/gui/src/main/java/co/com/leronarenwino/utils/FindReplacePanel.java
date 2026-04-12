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

package co.com.leronarenwino.utils;

import co.com.leronarenwino.i18n.UiMessages;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.*;

public class FindReplacePanel extends JPanel {
    private final RSyntaxTextArea area;
    private final JToolBar findBar;
    private final JToolBar replaceBar;
    private final JTextField searchField;
    private final JTextField replaceField;
    private final JCheckBox regexCB;
    private final JCheckBox matchCaseCB;
    private final JLabel matchInfoLabel;
    private final JButton findPrevBtn;
    private final JButton findNextBtn;
    private final JButton closeBtn;
    private final JButton replaceBtn;
    private final JButton replaceAllBtn;
    private String searchPlaceholderText;
    private String replacePlaceholderText;
    private boolean replaceMode = false;
    private int currentMatchIndex = 0;
    private java.util.List<int[]> matches = java.util.Collections.emptyList();

    public FindReplacePanel(RSyntaxTextArea area) {
        this.area = area;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        setBackground(UIManager.getColor("Panel.background"));

        // Find bar
        findBar = new JToolBar();
        findBar.setFloatable(false);
        searchField = new JTextField(12);
        searchField.setToolTipText(UiMessages.findSearchFieldTooltip());
        searchPlaceholderText = UiMessages.findSearchPlaceholder();
        setPlaceholder(searchField, searchPlaceholderText);
        regexCB = new JCheckBox(".*");
        regexCB.setToolTipText(UiMessages.findRegexTooltip());
        matchCaseCB = new JCheckBox("Cc");
        matchCaseCB.setToolTipText(UiMessages.findMatchCaseTooltip());
        matchInfoLabel = new JLabel(UiMessages.findZeroResults());
        matchInfoLabel.setPreferredSize(new Dimension(70, 20));
        findPrevBtn = new JButton("↑");
        findPrevBtn.setToolTipText(UiMessages.findPrevTooltip());
        findNextBtn = new JButton("↓");
        findNextBtn.setToolTipText(UiMessages.findNextTooltip());
        closeBtn = new JButton("✕");
        closeBtn.setToolTipText(UiMessages.findCloseTooltip());

        findBar.add(searchField);
        findBar.add(findPrevBtn);
        findBar.add(findNextBtn);
        findBar.add(matchCaseCB);
        findBar.add(Box.createHorizontalStrut(8));
        findBar.add(regexCB);
        findBar.add(Box.createHorizontalStrut(8));
        findBar.add(matchInfoLabel);
        findBar.add(Box.createHorizontalStrut(10));
        findBar.add(closeBtn);

        // Replace bar
        replaceBar = new JToolBar();
        replaceBar.setFloatable(false);
        replaceField = new JTextField(12);
        replaceField.setToolTipText(UiMessages.findReplaceFieldTooltip());
        replacePlaceholderText = UiMessages.findReplacePlaceholder();
        setPlaceholder(replaceField, replacePlaceholderText);
        replaceBtn = new JButton(UiMessages.replaceButton());
        replaceBtn.setToolTipText(UiMessages.replaceCurrentTooltip());
        replaceAllBtn = new JButton(UiMessages.replaceAllButton());
        replaceAllBtn.setToolTipText(UiMessages.replaceAllTooltip());
        // replaceBar.add(new JLabel("Reemplazar:"));
        replaceBar.add(replaceField);
        replaceBar.add(replaceBtn);
        replaceBar.add(replaceAllBtn);

        JPanel barsPanel = new JPanel();
        barsPanel.setLayout(new BoxLayout(barsPanel, BoxLayout.Y_AXIS));
        barsPanel.add(findBar);
        barsPanel.add(replaceBar);

        add(barsPanel, BorderLayout.NORTH);

        setVisible(false);
        setReplaceMode(false);

        // Listeners
        findPrevBtn.addActionListener(e -> findPrev());
        findNextBtn.addActionListener(e -> findNext());
        replaceBtn.addActionListener(e -> replace());
        replaceAllBtn.addActionListener(e -> replaceAll());
        closeBtn.addActionListener(e -> hidePanel());

        // Update matches when search or text changes
        javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateMatches();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateMatches();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateMatches();
            }
        };
        searchField.getDocument().addDocumentListener(docListener);
        area.getDocument().addDocumentListener(docListener);
        regexCB.addActionListener(e -> updateMatches());
        matchCaseCB.addActionListener(e -> updateMatches());

        // Enter in search = next, Shift+Enter = previous
        searchField.addActionListener(e -> findNext());
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && e.isShiftDown()) {
                    findPrev();
                }
                // Show replace bar if Ctrl+R is pressed while searchField is focused
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_R && e.isControlDown()) {
                    setReplaceMode(true);
                }
            }
        });

        // Enter in replace = replace current
        replaceField.addActionListener(e -> replace());

        // Escape closes the bar
        searchField.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "closeBar");
        searchField.getActionMap().put("closeBar", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                hidePanel();
            }
        });
        replaceField.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "closeBar");
        replaceField.getActionMap().put("closeBar", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                hidePanel();
            }
        });
    }

    public void refreshUiLanguage() {
        String newSearch = UiMessages.findSearchPlaceholder();
        swapPlaceholderText(searchField, searchPlaceholderText, newSearch);
        searchPlaceholderText = newSearch;
        String newReplace = UiMessages.findReplacePlaceholder();
        swapPlaceholderText(replaceField, replacePlaceholderText, newReplace);
        replacePlaceholderText = newReplace;

        searchField.setToolTipText(UiMessages.findSearchFieldTooltip());
        regexCB.setToolTipText(UiMessages.findRegexTooltip());
        matchCaseCB.setToolTipText(UiMessages.findMatchCaseTooltip());
        findPrevBtn.setToolTipText(UiMessages.findPrevTooltip());
        findNextBtn.setToolTipText(UiMessages.findNextTooltip());
        closeBtn.setToolTipText(UiMessages.findCloseTooltip());
        replaceField.setToolTipText(UiMessages.findReplaceFieldTooltip());
        replaceBtn.setText(UiMessages.replaceButton());
        replaceBtn.setToolTipText(UiMessages.replaceCurrentTooltip());
        replaceAllBtn.setText(UiMessages.replaceAllButton());
        replaceAllBtn.setToolTipText(UiMessages.replaceAllTooltip());

        if (isVisible()) {
            updateMatches();
        } else {
            matchInfoLabel.setText(UiMessages.findZeroResults());
        }
    }

    private void swapPlaceholderText(JTextField field, String oldPlaceholder, String newPlaceholder) {
        if (oldPlaceholder != null && field.getText().equals(oldPlaceholder)) {
            field.setText(newPlaceholder);
            field.setForeground(Color.GRAY);
        }
        field.putClientProperty("JTextField.placeholderText", newPlaceholder);
    }

    // Utility for placeholder in JTextField
    private void setPlaceholder(JTextField field, String placeholder) {
        field.putClientProperty("JTextField.placeholderText", placeholder);
        // Basic compatibility if L&F does not support native placeholder:
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(UIManager.getColor("TextField.foreground"));
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        // Initialize placeholder if empty
        if (field.getText().isEmpty()) {
            field.setText(placeholder);
            field.setForeground(Color.GRAY);
        }
    }

    public void showPanel(boolean replace) {
        setReplaceMode(replace);
        setVisible(true);
        searchField.requestFocusInWindow();
        updateMatches();
    }

    public void hidePanel() {
        setVisible(false);
        area.requestFocusInWindow();
    }

    public void setReplaceMode(boolean replace) {
        this.replaceMode = replace;
        replaceBar.setVisible(replace);
        revalidate();
        repaint();
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        findBar.setVisible(aFlag);
        if (!replaceMode) replaceBar.setVisible(false);
    }

    public void setSearchText(String text) {
        searchField.setText(text);
        searchField.selectAll();
        updateMatches();
    }

    public void setReplaceText(String text) {
        replaceField.setText(text);
        replaceField.selectAll();
    }

    private void updateMatches() {
        String search = searchField.getText();
        String text = area.getText();
        boolean matchCase = matchCaseCB.isSelected();
        boolean regex = regexCB.isSelected();
        matches = getAllMatchesUtil(text, search, matchCase, regex);
        int total = matches.size();
        if (total == 0) {
            matchInfoLabel.setText(UiMessages.findZeroResults());
            currentMatchIndex = 0;
        } else {
            if (currentMatchIndex < 1 || currentMatchIndex > total) {
                currentMatchIndex = 1;
            }
            matchInfoLabel.setText(currentMatchIndex + "/" + total);
            // Only select and scroll to the current match if the search field is focused (not while editing the main area)
            if (searchField.isFocusOwner()) {
                scrollToCurrentMatch();
            }
        }
    }

    private void findNext() {
        if (matches.isEmpty()) {
            updateMatches();
            return;
        }
        currentMatchIndex = (currentMatchIndex < matches.size()) ? currentMatchIndex + 1 : 1;
        scrollToCurrentMatch();
        matchInfoLabel.setText(currentMatchIndex + "/" + matches.size());
    }

    private void findPrev() {
        if (matches.isEmpty()) {
            updateMatches();
            return;
        }
        currentMatchIndex = (currentMatchIndex > 1) ? currentMatchIndex - 1 : matches.size();
        scrollToCurrentMatch();
        searchField.requestFocusInWindow();
        matchInfoLabel.setText(currentMatchIndex + "/" + matches.size());
    }

    // Utility method to select and scroll to the current match
    private void scrollToCurrentMatch() {
        if (matches.isEmpty() || currentMatchIndex < 1 || currentMatchIndex > matches.size()) return;
        int[] m = matches.get(currentMatchIndex - 1);
        area.select(m[0], m[1]);
        try {
            Rectangle rect = area.modelToView2D(m[0]).getBounds();
            if (rect != null) {
                area.scrollRectToVisible(rect);
            }
        } catch (Exception ignore) {}
    }

    private void replace() {
        if (matches.isEmpty() || currentMatchIndex < 1 || currentMatchIndex > matches.size()) return;
        int[] m = matches.get(currentMatchIndex - 1);
        area.select(m[0], m[1]);
        area.replaceSelection(replaceField.getText());
        int prevIndex = currentMatchIndex;
        updateMatches();
        if (!matches.isEmpty()) {
            currentMatchIndex = Math.min(prevIndex, matches.size());
            scrollToCurrentMatch();
            matchInfoLabel.setText(currentMatchIndex + "/" + matches.size());
        }
    }

    private void replaceAll() {
        if (matches.isEmpty()) return;
        String replacement = replaceField.getText();
        for (int i = matches.size() - 1; i >= 0; i--) {
            int[] m = matches.get(i);
            area.select(m[0], m[1]);
            area.replaceSelection(replacement);
        }
        updateMatches();
    }

    // Extracted utility to avoid code duplication
    public static java.util.List<int[]> getAllMatchesUtil(String text, String search, boolean matchCase, boolean regex) {
        java.util.List<int[]> result = new java.util.ArrayList<>();
        if (search == null || search.isEmpty() || text.isEmpty()) return result;
        if (regex) {
            try {
                java.util.regex.Pattern pattern = matchCase
                        ? java.util.regex.Pattern.compile(search)
                        : java.util.regex.Pattern.compile(search, java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    result.add(new int[]{matcher.start(), matcher.end()});
                }
            } catch (Exception e) {
                // ignore invalid regex
            }
        } else {
            String searchFor = matchCase ? search : search.toLowerCase();
            String haystack = matchCase ? text : text.toLowerCase();
            int idx = 0;
            while ((idx = haystack.indexOf(searchFor, idx)) != -1) {
                result.add(new int[]{idx, idx + searchFor.length()});
                idx += (!searchFor.isEmpty() ? searchFor.length() : 1);
            }
        }
        return result;
    }
}
