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

import co.com.leronarenwino.FreemarkerProcessor;
import co.com.leronarenwino.TemplateValidator;
import co.com.leronarenwino.editor.syntax.FreemarkerSyntaxSupport;
import co.com.leronarenwino.settings.Settings;
import co.com.leronarenwino.utils.ButtonStyleUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import utils.SettingsSingleton;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.com.leronarenwino.TemplateValidator.formatFreemarkerTemplateCombined;
import static co.com.leronarenwino.editor.TemplateUtils.formatJsonSafely;
import static co.com.leronarenwino.editor.TemplateUtils.parseDataModel;
import static co.com.leronarenwino.settings.Settings.PROPERTIES_FILE;
import static utils.PropertiesManager.loadProperties;
import static utils.SettingsSingleton.defaultAppProperties;
import static utils.SettingsSingleton.setSettingsFromProperties;

public class TemplateEditor extends JFrame {

    private static final Logger LOG = Logger.getLogger(TemplateEditor.class.getName());

    // Main container panel
    private JPanel mainPanel;

    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem exitItem;
    private JMenuItem openSettingsItem;
    private JMenu viewMenu;
    private JCheckBoxMenuItem toggleExpectedFieldsItem;

    // Panels for layout
    private JPanel columnsPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel bottomPanel;

    private JSplitPane mainSplitPane;

    // Component for template input
    private TemplatePanel templatePanel;

    // Components for data input
    private DataPanel dataPanel;

    // Components for expected fields
    private ExpectedFieldsPanel expectedFieldsPanel;

    // Components for output/result area
    private OutputPanel outputPanel;

    // Last formatted output and data input
    private String lastFormattedResultOutput;
    private String lastFormattedDataInput;
    private String lastValidDataInput;

    private RSyntaxTextArea[] textAreas;

    private final TemplateValidator templateValidator = new TemplateValidator(new FreemarkerProcessor());


    public TemplateEditor() {
        FreemarkerSyntaxSupport.register();

        // Disable FlatLaf custom window decorations globally
        System.setProperty("flatlaf.useWindowDecorations", "false");

        // Apply FlatLaf button styles
        ButtonStyleUtil.applyFlatLafButtonStyles();

        // Set default properties from file or create new ones
        setSettingsFromProperties(
                loadProperties(
                        PROPERTIES_FILE,
                        defaultAppProperties()
                )
        );

        String theme = SettingsSingleton.getTheme();
        try {
            switch (theme) {
                case "Flat Dark" -> com.formdev.flatlaf.FlatDarkLaf.setup();
                case "Flat Light" -> com.formdev.flatlaf.FlatLightLaf.setup();
                case "Flat IntelliJ" -> com.formdev.flatlaf.FlatIntelliJLaf.setup();
                case "Flat Darcula" -> com.formdev.flatlaf.FlatDarculaLaf.setup();
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            System.err.println("Could not apply FlatLaf theme");
        }

        // Initialize components
        initComponents();

        // Set components
        setComponents();

        // Add components
        addComponents();

        // Paint components
        paintComponents();

    }

    public void initComponents() {

        // Main panels
        mainPanel = new JPanel(new BorderLayout(10, 10));
        columnsPanel = new JPanel(new GridLayout(1, 2, 5, 5));

        // Menu bar and menu items
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        exitItem = new JMenuItem("Exit");
        openSettingsItem = new JMenuItem("Settings...");
        viewMenu = new JMenu("View");
        toggleExpectedFieldsItem = new JCheckBoxMenuItem("Show Expected Fields Panel", SettingsSingleton.isExpectedFieldsVisible());

        // Left, right, and options panels
        leftPanel = new JPanel();
        rightPanel = new JPanel();
        bottomPanel = new JPanel(new BorderLayout(5, 5));

        // Template input
        templatePanel = TemplatePanel.getInstance();

        // Data input
        dataPanel = DataPanel.getInstance();

        // Expected fields input
        expectedFieldsPanel = ExpectedFieldsPanel.getInstance();

        // Output/result area
        outputPanel = OutputPanel.getInstance();

        // Initialize arrays for easy access
        textAreas = new RSyntaxTextArea[]{templatePanel.getTextArea(), dataPanel.getTextArea(), expectedFieldsPanel.getTextArea(), outputPanel.getTextArea()};

        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        mainSplitPane.setResizeWeight(0.6);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setBorder(null);

    }

    public void setComponents() {

        // Main setup
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        columnsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Main split pane setup
        mainSplitPane.setTopComponent(columnsPanel);
        mainSplitPane.setBottomComponent(bottomPanel);
        mainSplitPane.setDividerSize(1);
        mainSplitPane.setOneTouchExpandable(false);

        mainSplitPane.setBorder(null);
        mainSplitPane.setUI(createMainSplitPaneUi());

        // Left and right panels setup
        leftPanel.setLayout(new BorderLayout(5, 5));
        rightPanel.setLayout(new BorderLayout(5, 5));

        // Set default configuration to JFrame
        setTitle("FreeMarker JSON/XML Toolkit (Apache FreeMarker 2.3.34)");
        setMinimumSize(new Dimension(600, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setJMenuBar(menuBar);
        setContentPane(mainPanel);

    }

    public void addComponents() {
        // Menu bar addition
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(openSettingsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        viewMenu.add(toggleExpectedFieldsItem);
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        // Add columns panel components
        addLeftPanelComponents();
        addRightPanelComponents();

        // Columns addition
        addColumnsPanelComponents();

        // Bottom panel addition
        bottomPanel.add(outputPanel, BorderLayout.CENTER);
        bottomPanel.add(expectedFieldsPanel, BorderLayout.SOUTH);

        // Button panel addition actions
        openSettingsItem.addActionListener(e -> {
            Settings settingsDialog = new Settings(this);
            settingsDialog.setVisible(true);
        });

        // Toggle expected fields panel visibility
        toggleExpectedFieldsItem.addActionListener(e -> {
            boolean visible = toggleExpectedFieldsItem.isSelected();
            SettingsSingleton.setExpectedFieldsVisible(visible);
            saveViewSettings();
            toggleExpectedFieldsPanel(visible);
        });

        // Button actions
        dataPanel.getValidateDataModelButton().addActionListener(e -> formatDataInputJson());
        templatePanel.getFormatTemplateButton().addActionListener(e -> formatTemplateInputArea());
        templatePanel.getSingleLineButton().addActionListener(e -> setTemplateToSingleLine());
        outputPanel.getProcessTemplateButton().addActionListener(e -> processTemplateOutput());
        outputPanel.getFormatJsonButton().addActionListener(e -> formatJsonOutput());
        outputPanel.getClearOutputButton().addActionListener(e -> outputPanel.getTextArea().setText(""));
        expectedFieldsPanel.getValidateFieldsButton().addActionListener(e -> validateOutputFields());

        // Add to main panel
        addMainPanelComponents();

    }

    // Groups and adds all main sections to the mainPanel
    private void addMainPanelComponents() {
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);
    }

    // Groups and adds left-side components (template area)
    private void addLeftPanelComponents() {
        leftPanel.add(templatePanel, BorderLayout.CENTER);
    }

    // Groups and adds right-side components (data area)
    private void addRightPanelComponents() {
        rightPanel.add(dataPanel, BorderLayout.CENTER);
    }

    // Groups and adds components to columnsPanel
    private void addColumnsPanelComponents() {
        columnsPanel.add(leftPanel);
        columnsPanel.add(rightPanel);
    }

    public void paintComponents() {

        // Apply theme and styles
        applyRSyntaxThemeToAllAreas(this);

        // Apply initial expected fields panel visibility from settings
        toggleExpectedFieldsPanel(SettingsSingleton.isExpectedFieldsVisible());

    }

    private void applyRSyntaxThemeToAllAreas(Component parent) {
        String rsyntaxTheme = SettingsSingleton.getRSyntaxTheme();
        String themePath = "/themes/" + rsyntaxTheme;
        for (RSyntaxTextArea area : textAreas) {
            try {
                UiConfig.applyRSyntaxTheme(area, themePath, parent);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Could not apply RSyntaxTextArea theme: " + themePath, ex);
            }
        }
    }

    private void processTemplateOutput() {
        String templateContent = templatePanel.getTextArea().getText();
        try {
            Map<String, Object> dataModel = getDataModelFromInput();
            String output = templateValidator.processTemplate(templateContent, dataModel);
            outputPanel.getTextArea().setText(output);
        } catch (Exception ex) {
            outputPanel.getTextArea().setText("Error processing template: " + ex.getMessage());
        }
    }

    private void validateOutputFields() {
        String output = outputPanel.getTextArea().getText();
        expectedFieldsPanel.validateFields(output);
    }

    private Map<String, Object> getDataModelFromInput() throws Exception {
        String json = dataPanel.getTextArea().getText().trim();
        return parseDataModel(json);
    }

    private void formatJsonOutput() {
        TemplateUtils.formatJsonIfNeeded(outputPanel.getTextArea(), lastFormattedResultOutput, formatted -> lastFormattedResultOutput = formatted);
    }

    private void formatDataInputJson() {
        formatJsonSafely(
                this,
                dataPanel.getTextArea(),
                lastFormattedDataInput,
                lastValidDataInput,
                formatted -> {
                    lastFormattedDataInput = formatted;
                    lastValidDataInput = formatted;
                }
        );
    }

    private void formatTemplateInputArea() {
        String template = templatePanel.getTextArea().getText();
        String formatted = formatFreemarkerTemplateCombined(template);
        templatePanel.getTextArea().beginAtomicEdit();
        try {
            templatePanel.getTextArea().setText(formatted);
        } finally {
            templatePanel.getTextArea().endAtomicEdit();
        }
    }

    private void setTemplateToSingleLine() {
        String template = templatePanel.getTextArea().getText();
        String singleLine = TemplateValidator.toSingleLine(template);
        singleLine = singleLine.replaceAll("}>\\s+\\{", "}>{");
        templatePanel.getTextArea().setText(singleLine);
    }

    private void toggleExpectedFieldsPanel(boolean visible) {
        expectedFieldsPanel.setVisible(visible);
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    private void saveViewSettings() {
        Properties props = loadProperties(PROPERTIES_FILE, defaultAppProperties());
        props.setProperty(SettingsSingleton.EXPECTED_FIELDS_VISIBLE, String.valueOf(SettingsSingleton.isExpectedFieldsVisible()));
        utils.PropertiesManager.saveProperties(PROPERTIES_FILE, props);
    }

    public void updateSplitPaneUI() {
        mainSplitPane.setUI(createMainSplitPaneUi());
    }

    private static BasicSplitPaneUI createMainSplitPaneUi() {
        return new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        Color dividerColor = UIManager.getColor("Component.borderColor");
                        if (dividerColor == null) {
                            dividerColor = UIManager.getColor("Separator.foreground");
                        }
                        if (dividerColor == null) {
                            dividerColor = Color.GRAY;
                        }
                        g.setColor(dividerColor);
                        g.fillRect(0, 0, getSize().width, getSize().height);
                    }
                };
            }
        };
    }

}
