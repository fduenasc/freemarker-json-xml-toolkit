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
import java.awt.*;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static co.com.leronarenwino.config.FreemarkerConfigProvider.reloadConfiguration;
import static utils.PropertiesManager.loadProperties;
import static utils.SettingsSingleton.defaultAppProperties;

public class Settings extends JDialog {

    public static final String PROPERTIES_FILE = "config.properties";

    private JPanel mainPanel;
    private JTabbedPane tabbedPane;

    private JPanel editorPanel;
    private JComboBox<String> themeCombo;
    private JComboBox<String> uiLanguageCombo;
    private JLabel labelAppTheme;
    private JLabel labelUiLang;

    private JPanel freemarkerPanel;
    private JComboBox<String> localeCombo;
    private JComboBox<String> timeZoneCombo;
    private JLabel labelFmLocale;
    private JLabel labelFmTz;

    private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton okButton;
    private JButton applyButton;

    private JPanel rsyntaxPanel;
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
        setSize(430, 310);
        setResizable(false);
        setLocationRelativeTo(parent);

        initComponents();
        setComponents();
        addComponents();
        loadSettings();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(0, 10));
        tabbedPane = new JTabbedPane();

        editorPanel = new JPanel();
        themeCombo = new JComboBox<>(new String[]{
                "Flat Light", "Flat Dark", "Flat IntelliJ", "Flat Darcula"
        });
        uiLanguageCombo = new JComboBox<>(new String[]{"English", "Español"});

        freemarkerPanel = new JPanel();
        localeCombo = new JComboBox<>(new String[]{"en_US", "es_CO", "fr_FR"});
        timeZoneCombo = new JComboBox<>(new String[]{"America/Los_Angeles", "UTC"});

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        applyButton = new JButton("Apply");

        rsyntaxPanel = new JPanel();
        java.util.List<String> sortedThemes = new java.util.ArrayList<>(THEME_DISPLAY_TO_FILE.keySet());
        java.util.Collections.sort(sortedThemes);
        rsyntaxThemeCombo = new JComboBox<>(sortedThemes.toArray(new String[0]));

        props = loadProperties(PROPERTIES_FILE, defaultAppProperties());
        if (props.isEmpty()) {
            props = defaultAppProperties();
        }

        labelAppTheme = new JLabel();
        labelUiLang = new JLabel();
        labelRsyntaxTheme = new JLabel();
        labelFmLocale = new JLabel();
        labelFmTz = new JLabel();
    }

    private void setComponents() {
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
        editorPanel.add(labeledRow(labelAppTheme, themeCombo));
        editorPanel.add(Box.createVerticalStrut(5));
        editorPanel.add(labeledRow(labelUiLang, uiLanguageCombo));

        rsyntaxPanel.setLayout(new BoxLayout(rsyntaxPanel, BoxLayout.Y_AXIS));
        rsyntaxPanel.add(labeledRow(labelRsyntaxTheme, rsyntaxThemeCombo));

        freemarkerPanel.setLayout(new BoxLayout(freemarkerPanel, BoxLayout.Y_AXIS));
        localeCombo.setSelectedItem(props.getProperty(SettingsSingleton.FREEMARKER_LOCALE));
        timeZoneCombo.setSelectedItem(props.getProperty(SettingsSingleton.FREEMARKER_TIME_ZONE));
        freemarkerPanel.add(labeledRow(labelFmLocale, localeCombo));
        freemarkerPanel.add(Box.createVerticalStrut(5));
        freemarkerPanel.add(labeledRow(labelFmTz, timeZoneCombo));
    }

    private JPanel labeledRow(JLabel label, JComboBox<String> combo) {
        Font compactFont = new Font("SansSerif", Font.PLAIN, 11);
        label.setFont(compactFont);
        label.setMaximumSize(new Dimension(160, 22));
        combo.setFont(compactFont);
        combo.setMaximumSize(new Dimension(200, 24));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(combo);
        return panel;
    }

    private void addComponents() {
        tabbedPane.addTab("Editor", editorPanel);
        tabbedPane.addTab("Syntax Theme", rsyntaxPanel);
        tabbedPane.addTab("FreeMarker", freemarkerPanel);

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(applyButton);

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

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

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
        tabbedPane.setTitleAt(0, UiMessages.tabEditor());
        tabbedPane.setTitleAt(1, UiMessages.tabSyntaxTheme());
        tabbedPane.setTitleAt(2, UiMessages.tabFreemarker());
        labelAppTheme.setText(UiMessages.labelAppTheme());
        labelUiLang.setText(UiMessages.labelUiLanguage());
        labelRsyntaxTheme.setText(UiMessages.labelRsyntaxTheme());
        labelFmLocale.setText(UiMessages.labelLocale());
        labelFmTz.setText(UiMessages.labelTimeZone());
        okButton.setText(UiMessages.buttonOk());
        cancelButton.setText(UiMessages.buttonCancel());
        applyButton.setText(UiMessages.buttonApply());
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
