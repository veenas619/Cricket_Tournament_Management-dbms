import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One screen for one table: a form on top, Add / Update / Delete / Clear buttons,
 * and a table showing all rows. Click a row to load it into the form.
 * Every section of the dashboard is just this class with a different list of fields.
 */
public class CrudPanel extends JPanel implements Refreshable {

    private final String table;          // table name in backticks, e.g. `Match`
    private final Field[] fields;
    private final JComponent[] inputs;

    private final DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable grid = new JTable(model);

    public CrudPanel(String tableName, Field... fields) {
        this.table = "`" + tableName + "`";
        this.fields = fields;
        this.inputs = new JComponent[fields.length];

        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ---------- form (two fields per row) ----------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GREEN, 2),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            JComponent input;
            if (f.kind == Field.Kind.FIXED) {
                input = new JComboBox<String>(f.options);
            } else if (f.kind == Field.Kind.FK) {
                input = new JComboBox<Item>();
            } else {
                input = new JTextField(18);
            }
            input.setFont(Theme.NORMAL);
            inputs[i] = input;

            int col = (i % 2) * 2;
            int row = i / 2;

            JLabel label = new JLabel(f.label + ":");
            label.setFont(Theme.NORMAL);
            g.gridx = col;
            g.gridy = row;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            form.add(label, g);

            g.gridx = col + 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            form.add(input, g);
        }

        // ---------- buttons ----------
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttons.setBackground(Theme.LIGHT);

        JButton addBtn = Theme.button("Add", Theme.GREEN);
        JButton updateBtn = Theme.button("Update", Theme.GREEN);
        JButton deleteBtn = Theme.button("Delete", Theme.RED);
        JButton clearBtn = Theme.button("Clear", Theme.GREY);
        addBtn.addActionListener(e -> add());
        updateBtn.addActionListener(e -> update());
        deleteBtn.addActionListener(e -> delete());
        clearBtn.addActionListener(e -> clearForm());
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.LIGHT);
        top.add(form, BorderLayout.NORTH);
        top.add(buttons, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // ---------- table ----------
        Theme.styleTable(grid);
        grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        grid.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = grid.getSelectedRow();
            if (row >= 0) {
                fillForm(row);
            }
        });
        JScrollPane scroll = new JScrollPane(grid);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    // =====================================================================
    // Loading data
    // =====================================================================

    /** Called by the dashboard every time this screen is opened. */
    @Override
    public void reload() {
        try {
            loadDropdowns();
            loadTable();
        } catch (SQLException e) {
            DBConnection.showError(this, e);
        }
        clearForm();
    }

    @SuppressWarnings("unchecked")
    private void loadDropdowns() throws SQLException {
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (f.kind != Field.Kind.FK) {
                continue;
            }
            JComboBox<Item> box = (JComboBox<Item>) inputs[i];
            box.removeAllItems();
            if (f.nullable) {
                box.addItem(new Item("", ""));   // blank choice
            }
            for (Item item : DBConnection.items(f.query)) {
                box.addItem(item);
            }
        }
    }

    private void loadTable() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table + " ORDER BY 1")) {
            DBConnection.fill(model, rs);
        }
    }

    // =====================================================================
    // Form helpers
    // =====================================================================

    private String getValue(int i) {
        JComponent in = inputs[i];
        if (in instanceof JTextField) {
            return ((JTextField) in).getText().trim();
        }
        Object selected = ((JComboBox<?>) in).getSelectedItem();
        if (selected == null) {
            return "";
        }
        if (selected instanceof Item) {
            return ((Item) selected).id;
        }
        return selected.toString();
    }

    private void setValue(int i, String value) {
        JComponent in = inputs[i];
        if (in instanceof JTextField) {
            ((JTextField) in).setText(value);
            return;
        }
        JComboBox<?> box = (JComboBox<?>) in;
        if (fields[i].kind == Field.Kind.FIXED) {
            box.setSelectedItem(value);
            return;
        }
        for (int k = 0; k < box.getItemCount(); k++) {
            if (((Item) box.getItemAt(k)).id.equals(value)) {
                box.setSelectedIndex(k);
                break;
            }
        }
    }

    /** Copies the clicked table row into the form. Key columns are locked. */
    private void fillForm(int row) {
        for (int i = 0; i < fields.length; i++) {
            int col = model.findColumn(fields[i].column);
            if (col < 0) {
                continue;
            }
            Object v = model.getValueAt(row, col);
            setValue(i, v == null ? "" : v.toString());
            inputs[i].setEnabled(!fields[i].key);
        }
    }

    private void clearForm() {
        grid.clearSelection();
        for (int i = 0; i < fields.length; i++) {
            JComponent in = inputs[i];
            if (in instanceof JTextField) {
                ((JTextField) in).setText("");
            } else {
                JComboBox<?> box = (JComboBox<?>) in;
                if (box.getItemCount() > 0) {
                    box.setSelectedIndex(0);
                }
            }
            in.setEnabled(!fields[i].autoId);   // auto IDs are never typed in
        }
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Check your input",
                JOptionPane.WARNING_MESSAGE);
    }

    // =====================================================================
    // Add / Update / Delete
    // =====================================================================

    private void add() {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (f.autoId) {
                continue;
            }
            String v = getValue(i);
            if (v.isEmpty() && !f.nullable) {
                warn("Please fill in: " + f.label);
                return;
            }
            columns.add("`" + f.column + "`");
            values.add(v);
        }
        String sql = "INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES ("
                + String.join(", ", Collections.nCopies(columns.size(), "?")) + ")";
        run(sql, values, "Record added.");
    }

    private void update() {
        if (grid.getSelectedRow() < 0) {
            warn("Click a row in the table first.");
            return;
        }
        List<String> sets = new ArrayList<>();
        List<String> wheres = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<String> keyValues = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            String v = getValue(i);
            if (f.key) {
                wheres.add("`" + f.column + "` = ?");
                keyValues.add(v);
            } else {
                if (v.isEmpty() && !f.nullable) {
                    warn("Please fill in: " + f.label);
                    return;
                }
                sets.add("`" + f.column + "` = ?");
                values.add(v);
            }
        }
        if (sets.isEmpty()) {
            warn("Nothing to change in this table. Delete the row and add a new one instead.");
            return;
        }
        values.addAll(keyValues);
        String sql = "UPDATE " + table + " SET " + String.join(", ", sets)
                + " WHERE " + String.join(" AND ", wheres);
        run(sql, values, "Record updated.");
    }

    private void delete() {
        if (grid.getSelectedRow() < 0) {
            warn("Click a row in the table first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete the selected record?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        List<String> wheres = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].key) {
                wheres.add("`" + fields[i].column + "` = ?");
                values.add(getValue(i));
            }
        }
        String sql = "DELETE FROM " + table + " WHERE " + String.join(" AND ", wheres);
        run(sql, values, "Record deleted.");
    }

    /** Runs one INSERT / UPDATE / DELETE. Errors (including trigger messages) are shown in a pop-up. */
    private void run(String sql, List<String> params, String successMessage) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                String v = params.get(i);
                if (v == null || v.isEmpty()) {
                    ps.setNull(i + 1, Types.NULL);
                } else {
                    ps.setString(i + 1, v);
                }
            }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, successMessage);
            reload();
        } catch (SQLException e) {
            DBConnection.showError(this, e);
        }
    }
}
