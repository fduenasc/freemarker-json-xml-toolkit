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
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modal editor for expected output fields: table rows (path + optional type) and bulk import.
 */
final class ExpectedFieldsDialog extends JDialog {

    private static final int COL_INDEX = 0;
    private static final int COL_PATH = 1;
    private static final int COL_TYPE = 2;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final Runnable onPersist;
    private boolean renumbering;
    private boolean dirty;

    private ExpectedFieldsDialog(Window owner, Runnable onPersist) {
        super(owner, UiMessages.expectedFieldsDialogTitle(), ModalityType.APPLICATION_MODAL);
        this.onPersist = onPersist;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        String[] cols = {
                UiMessages.columnExpectedFieldIndex(),
                UiMessages.columnExpectedFieldPath(),
                UiMessages.columnExpectedFieldType()
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != COL_INDEX;
            }
        };

        tableModel.addTableModelListener(e -> {
            if (renumbering) {
                return;
            }
            if (e.getType() == TableModelEvent.UPDATE) {
                dirty = true;
            }
        });

        for (String entry : SettingsSingleton.getExpectedFieldEntries()) {
            String[] p = entry.split(":", 2);
            String path = p[0].trim();
            String type = p.length > 1 ? p[1].trim() : "";
            tableModel.addRow(new Object[]{0, path, type});
        }
        renumberRows();

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(520, 240));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setRowHeight(table.getRowHeight() + 6);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        Color grid = UIManager.getColor("Component.borderColor");
        if (grid == null) {
            grid = new Color(0xC8, 0xC8, 0xC8);
        }
        table.setGridColor(grid);
        table.setIntercellSpacing(new Dimension(1, 1));

        StripedTableRenderer striped = new StripedTableRenderer();
        table.setDefaultRenderer(Object.class, striped);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        Font hf = header.getFont();
        if (hf != null) {
            header.setFont(hf.deriveFont(Font.BOLD));
        }

        if (table.getColumnCount() >= 3) {
            table.getColumnModel().getColumn(COL_INDEX).setPreferredWidth(44);
            table.getColumnModel().getColumn(COL_PATH).setPreferredWidth(300);
            table.getColumnModel().getColumn(COL_TYPE).setPreferredWidth(140);
        }

        JButton addRow = ButtonStyleUtil.createStyledButton(UiMessages.expectedFieldsAddRow(), UiMessages.expectedFieldsAddRow(), ButtonStyleUtil.ButtonStyle.SECONDARY);
        addRow.addActionListener(e -> {
            tableModel.addRow(new Object[]{0, "", ""});
            renumberRows();
            dirty = true;
            int r = tableModel.getRowCount() - 1;
            SwingUtilities.invokeLater(() -> focusAndEditPath(r));
        });

        JButton modifyRow = ButtonStyleUtil.createStyledButton(
                UiMessages.expectedFieldsModifyRow(),
                UiMessages.expectedFieldsModifyRowTooltip(),
                ButtonStyleUtil.ButtonStyle.SECONDARY);
        modifyRow.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, UiMessages.expectedFieldsSelectRowToEdit(), UiMessages.expectedFieldsDialogTitle(), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            focusAndEditPath(r);
        });

        JButton deleteRow = ButtonStyleUtil.createStyledButton(
                UiMessages.expectedFieldsDeleteRow(),
                UiMessages.expectedFieldsDeleteRowTooltip(),
                ButtonStyleUtil.ButtonStyle.SECONDARY);
        deleteRow.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            if (rows.length == 0) {
                JOptionPane.showMessageDialog(this, UiMessages.expectedFieldsSelectRowToDelete(), UiMessages.expectedFieldsDialogTitle(), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            for (int i = rows.length - 1; i >= 0; i--) {
                tableModel.removeRow(rows[i]);
            }
            renumberRows();
            dirty = true;
        });

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        tableActions.add(addRow);
        tableActions.add(modifyRow);
        tableActions.add(deleteRow);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(grid),
                        UiMessages.expectedFieldsTableLegend(),
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        table.getFont().deriveFont(Font.PLAIN, 11f)),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        JPanel tableTab = new JPanel(new BorderLayout(0, 8));
        tableTab.add(tableActions, BorderLayout.NORTH);
        tableTab.add(tableScroll, BorderLayout.CENTER);
        tableTab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTextArea importArea = new JTextArea(10, 48);
        importArea.setLineWrap(true);
        importArea.setWrapStyleWord(true);

        JLabel importHelp = new JLabel("<html>" + UiMessages.expectedFieldsImportHelp().replace("\n", "<br/>") + "</html>");
        importHelp.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JButton loadFromText = ButtonStyleUtil.createStyledButton(UiMessages.expectedFieldsLoadFromText(), UiMessages.expectedFieldsLoadFromText(), ButtonStyleUtil.ButtonStyle.SECONDARY);
        loadFromText.addActionListener(e -> applyImportText(importArea.getText()));

        JPanel importTab = new JPanel(new BorderLayout(0, 8));
        JPanel importNorth = new JPanel(new BorderLayout());
        importNorth.add(importHelp, BorderLayout.NORTH);
        importNorth.add(loadFromText, BorderLayout.SOUTH);
        importTab.add(importNorth, BorderLayout.NORTH);
        importTab.add(new JScrollPane(importArea), BorderLayout.CENTER);
        importTab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(UiMessages.expectedFieldsTabTable(), tableTab);
        tabs.addTab(UiMessages.expectedFieldsTabImport(), importTab);

        JButton save = ButtonStyleUtil.createStyledButton(UiMessages.expectedFieldsSave(), UiMessages.expectedFieldsSaveTooltip(), ButtonStyleUtil.ButtonStyle.PRIMARY);
        save.addActionListener(e -> persistFromTable());

        JButton close = ButtonStyleUtil.createStyledButton(UiMessages.expectedFieldsClose(), UiMessages.expectedFieldsClose(), ButtonStyleUtil.ButtonStyle.SECONDARY);
        close.addActionListener(e -> attemptClose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        south.add(save);
        south.add(close);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(tabs, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                attemptClose();
            }
        });

        pack();
        setLocationRelativeTo(owner);
    }

    private void focusAndEditPath(int row) {
        table.revalidate();
        table.repaint();
        table.getSelectionModel().setSelectionInterval(row, row);
        Rectangle cell = table.getCellRect(row, COL_PATH, true);
        table.scrollRectToVisible(cell);
        if (table.editCellAt(row, COL_PATH)) {
            Component ed = table.getEditorComponent();
            if (ed != null) {
                ed.requestFocusInWindow();
            }
        }
    }

    private void renumberRows() {
        renumbering = true;
        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt(i + 1, i, COL_INDEX);
            }
        } finally {
            renumbering = false;
        }
    }

    private void persistFromTable() {
        SettingsSingleton.setExpectedFieldEntries(rowsToEntries(tableModel));
        if (onPersist != null) {
            onPersist.run();
        }
        dirty = false;
        JOptionPane.showMessageDialog(this, UiMessages.expectedFieldsSavedMessage(), UiMessages.expectedFieldsSave(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void attemptClose() {
        if (dirty) {
            int r = JOptionPane.showConfirmDialog(
                    this,
                    UiMessages.expectedFieldsUnsavedMessage(),
                    UiMessages.expectedFieldsUnsavedTitle(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
    }

    private void applyImportText(String raw) {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
        if (raw != null && !raw.isBlank()) {
            for (String token : raw.split("\\s*,\\s*|\\s+|\\R")) {
                String t = token.trim();
                if (t.isEmpty()) {
                    continue;
                }
                String[] p = t.split(":", 2);
                String path = p[0].trim();
                String type = p.length > 1 ? p[1].trim() : "";
                if (!path.isEmpty()) {
                    tableModel.addRow(new Object[]{0, path, type});
                }
            }
        }
        renumberRows();
        dirty = true;
    }

    private static List<String> rowsToEntries(DefaultTableModel model) {
        List<String> out = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            Object pathObj = model.getValueAt(r, COL_PATH);
            Object typeObj = model.getValueAt(r, COL_TYPE);
            String path = pathObj != null ? pathObj.toString().trim() : "";
            if (path.isEmpty()) {
                continue;
            }
            String type = typeObj != null ? typeObj.toString().trim() : "";
            if (type.isEmpty()) {
                out.add(path);
            } else {
                out.add(path + ":" + type.toLowerCase());
            }
        }
        return out;
    }

    static void showDialog(Window owner, Runnable onPersist) {
        ExpectedFieldsDialog d = new ExpectedFieldsDialog(owner, onPersist);
        d.setVisible(true);
    }

    private static final class StripedTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            if (!isSelected) {
                Color baseBg = UIManager.getColor("Table.background");
                if (baseBg == null) {
                    baseBg = table.getBackground();
                }
                Color alt = UIManager.getColor("Table.alternateRowColor");
                if (alt == null && baseBg != null) {
                    alt = new Color(
                            Math.max(0, baseBg.getRed() - 14),
                            Math.max(0, baseBg.getGreen() - 14),
                            Math.max(0, baseBg.getBlue() - 14));
                }
                Color bg = (row % 2 == 0) ? baseBg : alt;
                if (bg != null) {
                    c.setBackground(bg);
                }
            }
            if (column == COL_INDEX) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEADING);
            }
            return c;
        }
    }
}
