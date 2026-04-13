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
import utils.SettingsSingleton;

import javax.swing.*;
import java.awt.*;

/**
 * Footer strip for expected output fields: summary, validation message, and actions (no separate status bar).
 */
public class ExpectedFieldsPanel extends JPanel {
    private static ExpectedFieldsPanel instance;
    private final JLabel titleLabel;
    private final JLabel summaryLabel;
    private JButton configureButton;
    private JButton validateFieldsButton;
    private JLabel validationResultLabel;
    private String validationIdleText;
    private Runnable persistSettings;

    private ExpectedFieldsPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 6, 6, 6));
        setOpaque(true);

        titleLabel = new JLabel(UiMessages.panelExpectedFields());
        Font titleFont = titleLabel.getFont();
        if (titleFont != null) {
            titleLabel.setFont(titleFont.deriveFont(Font.BOLD));
        }
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);

        summaryLabel = new JLabel();
        Color muted = UIManager.getColor("Label.disabledForeground");
        if (muted == null) {
            muted = Color.GRAY;
        }
        summaryLabel.setForeground(muted);
        summaryLabel.setVerticalAlignment(SwingConstants.CENTER);

        configureButton = ButtonStyleUtil.createStyledButton(
                UiMessages.expectedFieldsConfigureButton(),
                UiMessages.configureExpectedFieldsTooltip(),
                ButtonStyleUtil.ButtonStyle.SECONDARY);
        configureButton.getAccessibleContext().setAccessibleName(UiMessages.configureExpectedFieldsAccessible());
        configureButton.setVerticalAlignment(SwingConstants.CENTER);

        validationIdleText = UiMessages.validationResultPlaceholder();
        validationResultLabel = new JLabel(validationIdleText);
        validationResultLabel.setForeground(Color.GRAY);
        validationResultLabel.setHorizontalAlignment(SwingConstants.LEADING);
        validationResultLabel.setVerticalAlignment(SwingConstants.CENTER);
        validationResultLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        validateFieldsButton = ButtonStyleUtil.createStyledButton("🔍", UiMessages.validateExpectedFieldsTooltip(), ButtonStyleUtil.ButtonStyle.PRIMARY);
        validateFieldsButton.getAccessibleContext().setAccessibleName(UiMessages.validateExpectedFieldsAccessible());
        validateFieldsButton.setVerticalAlignment(SwingConstants.CENTER);

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        west.setOpaque(false);
        west.add(titleLabel);
        west.add(summaryLabel);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        east.setOpaque(false);
        east.add(configureButton);
        east.add(validateFieldsButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(west, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(validationResultLabel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(east, gbc);

        configureButton.addActionListener(e -> openConfigureDialog());
        refreshSummaryFromSettings();
    }

    private void openConfigureDialog() {
        Window w = SwingUtilities.getWindowAncestor(this);
        ExpectedFieldsDialog.showDialog(w, persistSettings);
        refreshSummaryFromSettings();
    }

    public void refreshSummaryFromSettings() {
        int n = SettingsSingleton.getExpectedFieldCount();
        String text = switch (n) {
            case 0 -> UiMessages.expectedFieldsSummaryNone();
            case 1 -> UiMessages.expectedFieldsSummaryOne();
            default -> UiMessages.expectedFieldsSummaryMany(n);
        };
        summaryLabel.setText(text);
    }

    public static ExpectedFieldsPanel getInstance() {
        if (instance == null) {
            instance = new ExpectedFieldsPanel();
        }
        return instance;
    }

    public JButton getValidateFieldsButton() {
        return validateFieldsButton;
    }

    /**
     * Persists {@link SettingsSingleton} to disk after the configure dialog applies changes.
     */
    public void setPersistSettingsRunnable(Runnable persistSettings) {
        this.persistSettings = persistSettings;
    }

    public void validateFields(String output) {
        if (output.contains("\\\"")) {
            output = output.replace("\\\"", "\"");
        }

        String[] expectedFields = SettingsSingleton.expectedFieldsForValidator();
        if (expectedFields.length == 0) {
            setValidationMessage(UiMessages.noExpectedFieldsSpecified(), Color.GRAY);
            return;
        }

        try {
            java.util.List<String> missing = TemplateUtils.validateFields(output, expectedFields);
            if (missing.isEmpty()) {
                setValidationMessage(UiMessages.allExpectedFieldsPresent(), new Color(0, 128, 0));
            } else {
                setValidationMessage(UiMessages.missingFieldsPrefix() + String.join(", ", missing), Color.RED);
            }
        } catch (Exception e) {
            setValidationMessage(UiMessages.invalidJsonOutput(), Color.RED);
        }
    }

    private void setValidationMessage(String msg, Color color) {
        validationResultLabel.setText(msg);
        validationResultLabel.setForeground(color);
        validationResultLabel.setToolTipText(msg != null && msg.length() > 72 ? msg : null);
    }

    public void refreshLocalizedChrome() {
        titleLabel.setText(UiMessages.panelExpectedFields());
        configureButton.setText(UiMessages.expectedFieldsConfigureButton());
        configureButton.setToolTipText(UiMessages.configureExpectedFieldsTooltip());
        configureButton.getAccessibleContext().setAccessibleName(UiMessages.configureExpectedFieldsAccessible());
        validateFieldsButton.setToolTipText(UiMessages.validateExpectedFieldsTooltip());
        validateFieldsButton.getAccessibleContext().setAccessibleName(UiMessages.validateExpectedFieldsAccessible());
        refreshSummaryFromSettings();
        if (validationResultLabel.getForeground().equals(Color.GRAY)
                && validationResultLabel.getText().equals(validationIdleText)) {
            validationIdleText = UiMessages.validationResultPlaceholder();
            validationResultLabel.setText(validationIdleText);
            validationResultLabel.setToolTipText(null);
        } else {
            validationIdleText = UiMessages.validationResultPlaceholder();
        }
    }
}
