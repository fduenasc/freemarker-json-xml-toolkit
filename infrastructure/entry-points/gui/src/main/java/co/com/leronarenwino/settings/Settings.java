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

package co.com.leronarenwino.settings;

import co.com.leronarenwino.i18n.UiMessages;
import co.com.leronarenwino.utils.ButtonStyleUtil;
import utils.PropertiesManager;
import utils.SettingsSingleton;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static co.com.leronarenwino.config.FreemarkerConfigProvider.reloadConfiguration;
import static co.com.leronarenwino.settings.SettingsNav.CATEGORY_APPEARANCE;
import static co.com.leronarenwino.settings.SettingsNav.CATEGORY_EDITOR;
import static co.com.leronarenwino.settings.SettingsNav.CATEGORY_FREEMARKER;
import static co.com.leronarenwino.settings.SettingsNav.PAGE_APPEARANCE;
import static co.com.leronarenwino.settings.SettingsNav.PAGE_FREEMARKER;
import static co.com.leronarenwino.settings.SettingsNav.PAGE_SYNTAX;
import static co.com.leronarenwino.settings.SettingsNav.CategoryRef;
import static co.com.leronarenwino.settings.SettingsNav.PageRef;
import static utils.PropertiesManager.loadProperties;
import static utils.SettingsSingleton.defaultAppProperties;

public class Settings extends JDialog {

    public static final String PROPERTIES_FILE = "config.properties";

    private JPanel mainPanel;
    private JSplitPane splitPane;
    private JTree navTree;
    private DefaultTreeModel treeModel;
    private JTextField searchField;
    private JLabel searchNoResultsLabel;
    private Timer navFilterDebounce;
    private JLabel breadcrumbLabel;
    private JButton restoreDefaultsButton;
    private JPanel cardsPanel;
    private CardLayout cardLayout;
    private String selectedPageKey = PAGE_APPEARANCE;

    private JPanel appearancePanel;
    private JPanel syntaxPanel;
    private JPanel freemarkerPanel;

    private JComboBox<String> themeCombo;
    private JComboBox<String> uiLanguageCombo;
    private JLabel labelAppTheme;
    private JLabel labelUiLang;

    private JComboBox<String> localeCombo;
    private JComboBox<String> timeZoneCombo;
    private JLabel labelFmLocale;
    private JLabel labelFmTz;

    private JButton cancelButton;
    private JButton okButton;
    private JButton applyButton;
    private JButton helpButton;

    private JComboBox<String> rsyntaxThemeCombo;
    private JLabel labelRsyntaxTheme;

    private Properties props;

    private static final Map<String, String> THEME_DISPLAY_TO_FILE = Map.of(
            "Dark", "dark.xml",
            "Default", "default.xml",
            "Eclipse", "eclipse.xml",
            "IDEA", "idea.xml",
            "IDEA Dark", "idea-dark.xml",
            "Monokai", "monokai.xml",
            "Monokai Dark", "monokai-dark.xml",
            "VS", "vs.xml",
            "VS Dark", "vs-dark.xml"
    );

    public Settings(JFrame parent) {
        super(parent, "Settings", true);
        setMinimumSize(new Dimension(640, 420));
        setSize(780, 520);
        setLocationRelativeTo(parent);

        initComponents();
        buildLayout();
        wireActions();
        loadSettings();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(0, 0));

        searchField = new JTextField();
        searchField.setToolTipText(UiMessages.settingsSearchTooltip());
        searchField.putClientProperty("JTextField.placeholderText", UiMessages.settingsSearchPlaceholder());

        appearancePanel = new JPanel();
        themeCombo = new JComboBox<>(new String[]{
                "Flat Light", "Flat Dark", "Flat IntelliJ", "Flat Darcula"
        });
        uiLanguageCombo = new JComboBox<>(new String[]{"English", "Español"});

        syntaxPanel = new JPanel();
        java.util.List<String> sortedThemes = new java.util.ArrayList<>(THEME_DISPLAY_TO_FILE.keySet());
        java.util.Collections.sort(sortedThemes);
        rsyntaxThemeCombo = new JComboBox<>(sortedThemes.toArray(new String[0]));

        freemarkerPanel = new JPanel();
        localeCombo = new JComboBox<>(new String[]{"en_US", "es_CO", "fr_FR"});
        timeZoneCombo = new JComboBox<>(new String[]{"America/Los_Angeles", "UTC"});

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        applyButton = new JButton("Apply");
        helpButton = new JButton("?");
        helpButton.setMargin(new Insets(2, 10, 2, 10));

        props = loadProperties(PROPERTIES_FILE, defaultAppProperties());
        if (props.isEmpty()) {
            props = defaultAppProperties();
        }

        labelAppTheme = new JLabel();
        labelUiLang = new JLabel();
        labelRsyntaxTheme = new JLabel();
        labelFmLocale = new JLabel();
        labelFmTz = new JLabel();

        appearancePanel.setLayout(new BoxLayout(appearancePanel, BoxLayout.Y_AXIS));
        appearancePanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        appearancePanel.add(labeledRow(labelAppTheme, themeCombo));
        appearancePanel.add(Box.createVerticalStrut(8));
        appearancePanel.add(labeledRow(labelUiLang, uiLanguageCombo));

        syntaxPanel.setLayout(new BoxLayout(syntaxPanel, BoxLayout.Y_AXIS));
        syntaxPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        syntaxPanel.add(labeledRow(labelRsyntaxTheme, rsyntaxThemeCombo));

        freemarkerPanel.setLayout(new BoxLayout(freemarkerPanel, BoxLayout.Y_AXIS));
        freemarkerPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        localeCombo.setSelectedItem(props.getProperty(SettingsSingleton.FREEMARKER_LOCALE));
        timeZoneCombo.setSelectedItem(props.getProperty(SettingsSingleton.FREEMARKER_TIME_ZONE));
        freemarkerPanel.add(labeledRow(labelFmLocale, localeCombo));
        freemarkerPanel.add(Box.createVerticalStrut(8));
        freemarkerPanel.add(labeledRow(labelFmTz, timeZoneCombo));

        searchNoResultsLabel = new JLabel(" ");
        searchNoResultsLabel.setVisible(false);
        searchNoResultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Font snrFont = searchNoResultsLabel.getFont();
        if (snrFont != null) {
            searchNoResultsLabel.setFont(snrFont.deriveFont(Font.ITALIC, Math.max(10f, snrFont.getSize2D() - 1f)));
        }
        Color dim = UIManager.getColor("Label.disabledForeground");
        if (dim != null) {
            searchNoResultsLabel.setForeground(dim);
        }

        navFilterDebounce = new Timer(180, e -> applyNavTreeFilter());
        navFilterDebounce.setRepeats(false);

        treeModel = new DefaultTreeModel(buildFullTreeRoot());
        navTree = new JTree(treeModel);
        navTree.setRootVisible(false);
        navTree.setShowsRootHandles(true);
        navTree.setRowHeight(22);
        navTree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        navTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree,
                    Object value,
                    boolean sel,
                    boolean expanded,
                    boolean leaf,
                    int row,
                    boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode n) {
                    Object uo = n.getUserObject();
                    if (uo instanceof PageRef p) {
                        setText(UiMessages.settingsPageTitle(p.cardKey()));
                    } else if (uo instanceof CategoryRef c) {
                        setText(UiMessages.settingsCategoryTitle(c.key()));
                    }
                }
                return this;
            }
        });

        restoreDefaultsButton = new JButton(UiMessages.settingsRestoreDefaults());
        restoreDefaultsButton.setToolTipText(UiMessages.settingsRestoreDefaultsTooltip());
        styleLinkButton(restoreDefaultsButton);
    }

    private static DefaultMutableTreeNode buildFullTreeRoot() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultMutableTreeNode ab = new DefaultMutableTreeNode(new CategoryRef(CATEGORY_APPEARANCE));
        ab.add(new DefaultMutableTreeNode(new PageRef(PAGE_APPEARANCE)));
        DefaultMutableTreeNode ed = new DefaultMutableTreeNode(new CategoryRef(CATEGORY_EDITOR));
        ed.add(new DefaultMutableTreeNode(new PageRef(PAGE_SYNTAX)));
        DefaultMutableTreeNode fm = new DefaultMutableTreeNode(new CategoryRef(CATEGORY_FREEMARKER));
        fm.add(new DefaultMutableTreeNode(new PageRef(PAGE_FREEMARKER)));
        root.add(ab);
        root.add(ed);
        root.add(fm);
        return root;
    }

    /**
     * Hides whole branches when the search text matches neither the category nor any page under it.
     */
    private static DefaultMutableTreeNode buildFilteredTreeRoot(String queryLower) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        addCategoryIfMatching(root, CATEGORY_APPEARANCE, new String[]{PAGE_APPEARANCE}, queryLower);
        addCategoryIfMatching(root, CATEGORY_EDITOR, new String[]{PAGE_SYNTAX}, queryLower);
        addCategoryIfMatching(root, CATEGORY_FREEMARKER, new String[]{PAGE_FREEMARKER}, queryLower);
        return root;
    }

    private static void addCategoryIfMatching(
            DefaultMutableTreeNode root,
            String categoryKey,
            String[] pageKeys,
            String queryLower) {
        boolean categoryHit = UiMessages.settingsCategoryTitle(categoryKey).toLowerCase().contains(queryLower);
        DefaultMutableTreeNode cat = new DefaultMutableTreeNode(new CategoryRef(categoryKey));
        for (String pk : pageKeys) {
            if (categoryHit || pageMatchesQuery(pk, queryLower)) {
                cat.add(new DefaultMutableTreeNode(new PageRef(pk)));
            }
        }
        if (cat.getChildCount() > 0) {
            root.add(cat);
        }
    }

    private static boolean pageMatchesQuery(String pageKey, String queryLower) {
        String hay = (UiMessages.settingsPageTitle(pageKey) + " "
                + UiMessages.settingsBreadcrumb(pageKey) + " "
                + UiMessages.settingsCategoryTitle(categoryKeyForPage(pageKey))).toLowerCase();
        return hay.contains(queryLower);
    }

    private static void styleLinkButton(JButton b) {
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color link = UIManager.getColor("Component.linkColor");
        if (link == null) {
            link = UIManager.getColor("textHighlight");
        }
        if (link == null) {
            link = new Color(0x38, 0x6E, 0xBF);
        }
        b.setForeground(link);
    }

    private void buildLayout() {
        setContentPane(mainPanel);

        Color sep = UIManager.getColor("Component.borderColor");
        if (sep == null) {
            sep = UIManager.getColor("Separator.foreground");
        }
        if (sep == null) {
            sep = new Color(0xC0, 0xC0, 0xC0);
        }
        final Color dividerLine = sep;

        JPanel navColumn = new JPanel(new BorderLayout(0, 6));
        navColumn.setMinimumSize(new Dimension(200, 120));
        navColumn.setPreferredSize(new Dimension(220, 400));
        Border navRight = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, dividerLine),
                BorderFactory.createEmptyBorder(8, 10, 8, 8));
        navColumn.setBorder(navRight);
        navColumn.add(searchField, BorderLayout.NORTH);
        navColumn.add(new JScrollPane(navTree), BorderLayout.CENTER);
        navColumn.add(searchNoResultsLabel, BorderLayout.SOUTH);

        breadcrumbLabel = new JLabel(" ");
        breadcrumbLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Font base = breadcrumbLabel.getFont();
        if (base != null) {
            breadcrumbLabel.setFont(base.deriveFont(Font.PLAIN, base.getSize2D() + 1f));
        }

        JPanel crumbRow = new JPanel(new BorderLayout(8, 0));
        crumbRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        crumbRow.add(breadcrumbLabel, BorderLayout.CENTER);
        crumbRow.add(restoreDefaultsButton, BorderLayout.EAST);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.add(wrapPage(appearancePanel), PAGE_APPEARANCE);
        cardsPanel.add(wrapPage(syntaxPanel), PAGE_SYNTAX);
        cardsPanel.add(wrapPage(freemarkerPanel), PAGE_FREEMARKER);

        JPanel detail = new JPanel(new BorderLayout(0, 0));
        detail.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        detail.add(crumbRow, BorderLayout.NORTH);
        detail.add(cardsPanel, BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navColumn, detail);
        splitPane.setResizeWeight(0.26);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setDividerSize(3);

        JPanel south = new JPanel(new BorderLayout());
        south.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, dividerLine),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 0));
        actions.add(okButton);
        actions.add(cancelButton);
        actions.add(applyButton);

        south.add(helpButton, BorderLayout.WEST);
        south.add(actions, BorderLayout.EAST);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(south, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);

        SwingUtilities.invokeLater(() -> {
            expandAllNavRows();
            selectPageNode(PAGE_APPEARANCE);
        });
    }

    private static JScrollPane wrapPage(JPanel inner) {
        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private void wireActions() {
        cancelButton.addActionListener(e -> dispose());

        okButton.addActionListener(e -> {
            saveSettings();
            loadSettings();
            applyThemeToParent();
            reloadConfiguration();
            dispose();
        });
        applyButton.addActionListener(e -> {
            saveSettings();
            loadSettings();
            applyThemeToParent();
            reloadConfiguration();
        });

        helpButton.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                UiMessages.settingsHelpMessage(),
                UiMessages.settingsHelpTooltip(),
                JOptionPane.INFORMATION_MESSAGE));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                navFilterDebounce.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                navFilterDebounce.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                navFilterDebounce.restart();
            }
        });

        restoreDefaultsButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    UiMessages.settingsRestoreDefaultsConfirm(),
                    UiMessages.settingsRestoreDefaults(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                applyDefaultsToForm();
            }
        });

        navTree.addTreeSelectionListener(e -> {
            TreePath path = e.getNewLeadSelectionPath();
            if (path == null) {
                return;
            }
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object uo = n.getUserObject();
            if (uo instanceof CategoryRef && n.getChildCount() > 0) {
                DefaultMutableTreeNode first = (DefaultMutableTreeNode) n.getFirstChild();
                SwingUtilities.invokeLater(() -> navTree.setSelectionPath(new TreePath(first.getPath())));
                return;
            }
            if (uo instanceof PageRef pr) {
                selectedPageKey = pr.cardKey();
                cardLayout.show(cardsPanel, pr.cardKey());
                breadcrumbLabel.setText(UiMessages.settingsBreadcrumb(pr.cardKey()));
            }
        });

        themeCombo.addActionListener(e -> {
            String selected = (String) themeCombo.getSelectedItem();
            try {
                ButtonStyleUtil.applyFlatLafButtonStyles();

                switch (selected != null ? selected : SettingsSingleton.DEFAULT_THEME) {
                    case "Flat Light" -> UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                    case "Flat Dark" -> UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                    case "Flat Darcula" -> UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarculaLaf());
                    default -> UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatIntelliJLaf());
                }
                SwingUtilities.updateComponentTreeUI(getParent());
                SwingUtilities.updateComponentTreeUI(this);

                if (getParent() instanceof co.com.leronarenwino.editor.TemplateEditor editor) {
                    SwingUtilities.invokeLater(editor::updateSplitPaneUI);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, UiMessages.themeApplyFailed(ex.getMessage()));
            }
        });

        rsyntaxThemeCombo.addActionListener(e -> {
            String selectedDisplay = (String) rsyntaxThemeCombo.getSelectedItem();
            String fileName = THEME_DISPLAY_TO_FILE.get(selectedDisplay);
            if (fileName != null) {
                SettingsSingleton.setRSyntaxTheme(fileName);
                applyThemeToParent();
            }
        });
    }

    private void expandAllNavRows() {
        for (int i = 0; i < navTree.getRowCount(); i++) {
            navTree.expandRow(i);
        }
    }

    private void selectPageNode(String pageKey) {
        findAndSelectPageKey((DefaultMutableTreeNode) treeModel.getRoot(), pageKey);
    }

    private void applyNavTreeFilter() {
        String q = searchField.getText().trim();
        DefaultMutableTreeNode newRoot = q.isEmpty() ? buildFullTreeRoot() : buildFilteredTreeRoot(q.toLowerCase());
        treeModel.setRoot(newRoot);
        treeModel.nodeStructureChanged(newRoot);
        expandAllNavRows();

        boolean filtered = !q.isEmpty();
        searchNoResultsLabel.setVisible(filtered && newRoot.getChildCount() == 0);
        searchNoResultsLabel.setText(UiMessages.settingsSearchNoResults());

        if (filtered && newRoot.getChildCount() == 0) {
            return;
        }
        selectBestAfterFilter();
    }

    private void selectBestAfterFilter() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        if (findAndSelectPageKey(root, selectedPageKey)) {
            return;
        }
        Enumeration<TreeNode> en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) en.nextElement();
            if (n.getUserObject() instanceof PageRef) {
                TreePath tp = new TreePath(n.getPath());
                navTree.setSelectionPath(tp);
                navTree.scrollPathToVisible(tp);
                return;
            }
        }
    }

    private boolean findAndSelectPageKey(DefaultMutableTreeNode root, String pageKey) {
        Enumeration<TreeNode> en = root.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) en.nextElement();
            if (n.getUserObject() instanceof PageRef pr && pageKey.equals(pr.cardKey())) {
                TreePath tp = new TreePath(n.getPath());
                navTree.setSelectionPath(tp);
                navTree.scrollPathToVisible(tp);
                return true;
            }
        }
        return false;
    }

    private void applyDefaultsToForm() {
        Properties d = defaultAppProperties();
        themeCombo.setSelectedItem(SettingsSingleton.normalizeAppTheme(d.getProperty(SettingsSingleton.APP_THEME)));
        String uil = d.getProperty(SettingsSingleton.UI_LANGUAGE, "en");
        uiLanguageCombo.setSelectedIndex(uil.trim().toLowerCase().startsWith("es") ? 1 : 0);
        String fileName = d.getProperty(SettingsSingleton.RSYNTAX_THEME, "idea.xml");
        rsyntaxThemeCombo.setSelectedItem(THEME_FILE_TO_DISPLAY.getOrDefault(fileName, "IDEA"));
        localeCombo.setSelectedItem(d.getProperty(SettingsSingleton.FREEMARKER_LOCALE));
        timeZoneCombo.setSelectedItem(d.getProperty(SettingsSingleton.FREEMARKER_TIME_ZONE));
    }

    private static String categoryKeyForPage(String pageKey) {
        return switch (pageKey) {
            case PAGE_APPEARANCE -> CATEGORY_APPEARANCE;
            case PAGE_SYNTAX -> CATEGORY_EDITOR;
            case PAGE_FREEMARKER -> CATEGORY_FREEMARKER;
            default -> "";
        };
    }

    private JPanel labeledRow(JLabel label, JComboBox<String> combo) {
        Font rowFont = label.getFont() != null ? label.getFont().deriveFont(12f) : new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        label.setFont(rowFont);
        label.setPreferredSize(new Dimension(180, 26));
        combo.setFont(rowFont);
        combo.setMaximumSize(new Dimension(320, 28));

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.add(label, BorderLayout.WEST);
        row.add(combo, BorderLayout.CENTER);
        return row;
    }

    private void saveSettings() {
        Properties props = loadProperties(PROPERTIES_FILE, defaultAppProperties());
        props.setProperty(SettingsSingleton.FREEMARKER_LOCALE, (String) localeCombo.getSelectedItem());
        props.setProperty(SettingsSingleton.FREEMARKER_TIME_ZONE, (String) timeZoneCombo.getSelectedItem());
        props.setProperty(SettingsSingleton.APP_THEME, (String) themeCombo.getSelectedItem());
        String uiLang = uiLanguageCombo.getSelectedIndex() == 1 ? "es" : "en";
        props.setProperty(SettingsSingleton.UI_LANGUAGE, uiLang);
        String selectedDisplay = (String) rsyntaxThemeCombo.getSelectedItem();
        String fileName = THEME_DISPLAY_TO_FILE.get(selectedDisplay);
        props.setProperty(SettingsSingleton.RSYNTAX_THEME, fileName);
        PropertiesManager.saveProperties(PROPERTIES_FILE, props);
    }

    private void loadSettings() {
        Properties props = loadProperties(PROPERTIES_FILE, defaultAppProperties());

        if (props.isEmpty()) {
            props = defaultAppProperties();
        }

        localeCombo.setSelectedItem(props.getProperty(SettingsSingleton.FREEMARKER_LOCALE));
        timeZoneCombo.setSelectedItem(props.getProperty(SettingsSingleton.FREEMARKER_TIME_ZONE));
        themeCombo.setSelectedItem(SettingsSingleton.normalizeAppTheme(props.getProperty(SettingsSingleton.APP_THEME)));
        String fileName = props.getProperty(SettingsSingleton.RSYNTAX_THEME, "idea.xml");
        String displayName = THEME_FILE_TO_DISPLAY.getOrDefault(fileName, "IDEA");
        rsyntaxThemeCombo.setSelectedItem(displayName);
        SettingsSingleton.setRSyntaxTheme(fileName);

        String uil = props.getProperty(SettingsSingleton.UI_LANGUAGE, "en");
        uiLanguageCombo.setSelectedIndex(uil.trim().toLowerCase().startsWith("es") ? 1 : 0);

        SettingsSingleton.setSettingsFromProperties(props);

        refreshDialogLocalized();
    }

    private void refreshDialogLocalized() {
        setTitle(UiMessages.settingsDialogTitle());
        searchField.putClientProperty("JTextField.placeholderText", UiMessages.settingsSearchPlaceholder());
        searchField.setToolTipText(UiMessages.settingsSearchTooltip());
        helpButton.setToolTipText(UiMessages.settingsHelpTooltip());
        labelAppTheme.setText(UiMessages.labelAppTheme());
        labelUiLang.setText(UiMessages.labelUiLanguage());
        labelRsyntaxTheme.setText(UiMessages.labelRsyntaxTheme());
        labelFmLocale.setText(UiMessages.labelLocale());
        labelFmTz.setText(UiMessages.labelTimeZone());
        okButton.setText(UiMessages.buttonOk());
        cancelButton.setText(UiMessages.buttonCancel());
        applyButton.setText(UiMessages.buttonApply());
        restoreDefaultsButton.setText(UiMessages.settingsRestoreDefaults());
        restoreDefaultsButton.setToolTipText(UiMessages.settingsRestoreDefaultsTooltip());
        breadcrumbLabel.setText(UiMessages.settingsBreadcrumb(selectedPageKey));
        navTree.repaint();
    }

    private static final Map<String, String> THEME_FILE_TO_DISPLAY = THEME_DISPLAY_TO_FILE.entrySet()
            .stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    private void applyThemeToParent() {
        if (getParent() instanceof co.com.leronarenwino.editor.TemplateEditor editor) {
            editor.paintComponents();
            editor.refreshUiLanguage();
            editor.repaint();

            SwingUtilities.invokeLater(editor::updateSplitPaneUI);
        }
    }
}
