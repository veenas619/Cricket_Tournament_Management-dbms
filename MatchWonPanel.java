import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Match Results screen (read-only):
 * shows the view Match_Won, which joins Match and Match_Team
 * to give each match's winning team, Team 1 and Team 2.
 */
public class MatchWonPanel extends JPanel implements Refreshable {

    private final DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable grid = new JTable(model);

    public MatchWonPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        Theme.styleTable(grid);
        JScrollPane scroll = new JScrollPane(grid);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void reload() {
        try {
            DBConnection.selectAll("Match_Won", model);
        } catch (SQLException e) {
            DBConnection.showError(this, e);
        }
    }
}
