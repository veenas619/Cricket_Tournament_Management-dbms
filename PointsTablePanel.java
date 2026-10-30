import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

/**
 * Reports screen:
 *  1. Points table of a tournament  -> calls the stored procedure points_table(tour_id)
 *  2. Team info                     -> uses function PlayerCount(team_id)
 *                                      and procedure sp_team_salary_cursor(team_id)
 */
public class PointsTablePanel extends JPanel implements Refreshable {

    private final JComboBox<Item> tournamentBox = new JComboBox<>();
    private final JComboBox<Item> teamBox = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable grid = new JTable(model);
    private final JLabel info = new JLabel("Pick a team and click \"Show team info\".");

    public PointsTablePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ---------- top: choose tournament ----------
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(Theme.LIGHT);
        JLabel tourLabel = new JLabel("Tournament:");
        tourLabel.setFont(Theme.NORMAL);
        tournamentBox.setFont(Theme.NORMAL);
        JButton showPoints = Theme.button("Show points table", Theme.GREEN);
        showPoints.addActionListener(e -> showPoints());
        top.add(tourLabel);
        top.add(tournamentBox);
        top.add(showPoints);
        add(top, BorderLayout.NORTH);

        // ---------- middle: result table ----------
        Theme.styleTable(grid);
        JScrollPane scroll = new JScrollPane(grid);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ---------- bottom: team info ----------
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createLineBorder(Theme.GREEN, 2));
        JLabel teamLabel = new JLabel("Team:");
        teamLabel.setFont(Theme.NORMAL);
        teamBox.setFont(Theme.NORMAL);
        info.setFont(Theme.NORMAL);
        JButton showInfo = Theme.button("Show team info", Theme.GREEN);
        showInfo.addActionListener(e -> showTeamInfo());
        bottom.add(teamLabel);
        bottom.add(teamBox);
        bottom.add(showInfo);
        bottom.add(info);
        add(bottom, BorderLayout.SOUTH);
    }

    @Override
    public void reload() {
        try {
            tournamentBox.removeAllItems();
            for (Item item : DBConnection.items("SELECT Tournament_ID, Tour_Name FROM Tournament")) {
                tournamentBox.addItem(item);
            }
            teamBox.removeAllItems();
            for (Item item : DBConnection.items("SELECT Team_ID, Team_Name FROM Team ORDER BY Team_Name")) {
                teamBox.addItem(item);
            }
        } catch (SQLException e) {
            DBConnection.showError(this, e);
        }
        model.setRowCount(0);
        info.setText("Pick a team and click \"Show team info\".");
    }

    private void showPoints() {
        Item tournament = (Item) tournamentBox.getSelectedItem();
        if (tournament == null) {
            return;
        }
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL points_table(?)}")) {
            cs.setInt(1, Integer.parseInt(tournament.id));
            try (ResultSet rs = cs.executeQuery()) {
                DBConnection.fill(model, rs);
            }
        } catch (SQLException e) {
            DBConnection.showError(this, e);
        }
    }

    private void showTeamInfo() {
        Item team = (Item) teamBox.getSelectedItem();
        if (team == null) {
            return;
        }
        int teamId = Integer.parseInt(team.id);
        try (Connection con = DBConnection.getConnection()) {
            int players = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT PlayerCount(?)")) {
                ps.setInt(1, teamId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        players = rs.getInt(1);
                    }
                }
            }
            BigDecimal salary = BigDecimal.ZERO;
            try (CallableStatement cs = con.prepareCall("{CALL sp_team_salary_cursor(?)}")) {
                cs.setInt(1, teamId);
                try (ResultSet rs = cs.executeQuery()) {
                    if (rs.next()) {
                        salary = rs.getBigDecimal("Total_Coach_Salary");
                    }
                }
            }
            info.setText("Players: " + players + "   |   Total coach salary: Rs. " + salary);
        } catch (SQLException e) {
            DBConnection.showError(this, e);
        }
    }
}
