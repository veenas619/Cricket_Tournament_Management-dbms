import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * First screen of the app. Only organisers who know the login can enter.
 * RUN THIS FILE to start the application.
 *
 * Sample login:  username = admin     password = cricket@2026
 */
public class LoginFrame extends JFrame {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "cricket@2026";

    private final JTextField userField = new JTextField(16);
    private final JPasswordField passField = new JPasswordField(16);

    public LoginFrame() {
        setTitle("Organiser Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 430);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.LIGHT);

        // ---------- green header ----------
        JLabel title = new JLabel(
                "<html><div style='text-align:center'>Cricket Tournament<br>Management System</div></html>",
                SwingConstants.CENTER);
        title.setFont(Theme.TITLE);
        title.setForeground(Color.WHITE);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.DARK);
        header.setBorder(BorderFactory.createEmptyBorder(24, 10, 24, 10));
        header.add(title, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // ---------- login form ----------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.LIGHT);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        JLabel heading = new JLabel("Organiser login");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.DARK);
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        form.add(heading, g);

        g.gridwidth = 1;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(Theme.NORMAL);
        g.gridy = 1;
        form.add(userLabel, g);
        userField.setFont(Theme.NORMAL);
        g.gridx = 1;
        form.add(userField, g);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(Theme.NORMAL);
        g.gridx = 0;
        g.gridy = 2;
        form.add(passLabel, g);
        passField.setFont(Theme.NORMAL);
        g.gridx = 1;
        form.add(passField, g);

        JButton loginBtn = Theme.button("Log in", Theme.GREEN);
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(loginBtn, g);

        loginBtn.addActionListener(e -> attemptLogin());
        userField.addActionListener(e -> attemptLogin());
        passField.addActionListener(e -> attemptLogin());

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void attemptLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (!user.equals(USERNAME) || !pass.equals(PASSWORD)) {
            JOptionPane.showMessageDialog(this, "Wrong username or password.",
                    "Login failed", JOptionPane.ERROR_MESSAGE);
            passField.setText("");
            return;
        }

        // Check the database is reachable before opening the dashboard
        try (Connection con = DBConnection.getConnection()) {
            dispose();
            new Dashboard().setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Login is correct, but MySQL could not be reached:\n" + e.getMessage()
                            + "\n\nCheck that MySQL is running and the password in DBConnection.java is right.",
                    "Cannot connect to database", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
