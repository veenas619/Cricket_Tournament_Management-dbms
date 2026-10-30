import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Home dashboard: one big button per section. Clicking a button opens that section's
 * screen; the "Back to Home" button returns here.
 *
 * To add another section, add one register(...) call in buildSections().
 */
public class Dashboard extends JFrame {

    private static final String HOME = "HOME";

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final List<String> order = new ArrayList<>();            // button order on home screen
    private final Map<String, Refreshable> screens = new HashMap<>();

    public Dashboard() {
        setTitle("Cricket Tournament Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 740);
        setLocationRelativeTo(null);

        buildSections();
        content.add(buildHome(), HOME);
        setContentPane(content);
        cards.show(content, HOME);
    }

    // =====================================================================
    // The sections (one register call each)
    // =====================================================================
    private void buildSections() {

        register("Manage Teams", new CrudPanel("Team",
                Field.autoId("Team_ID", "Team ID"),
                Field.text("Team_Name", "Team Name"),
                Field.text("City", "City"),
                Field.text("Team_Logo", "Team Logo (path)").nullable(),
                Field.fk("Captain_ID", "Captain",
                        "SELECT Player_ID, Player_Name FROM Player ORDER BY Player_Name").nullable()));

        register("Manage Players", new CrudPanel("Player",
                Field.autoId("Player_ID", "Player ID"),
                Field.text("Player_Name", "Player Name"),
                Field.fixed("P_Type", "Player Type", "Batsman", "Bowler", "All-rounder", "Wicketkeeper"),
                Field.text("DOB", "DOB (yyyy-mm-dd)"),
                Field.text("Position", "Position").nullable(),
                Field.fk("Team_ID", "Team",
                        "SELECT Team_ID, Team_Name FROM Team ORDER BY Team_Name")));

        register("Manage Coaches", new CrudPanel("Coach",
                Field.autoId("Coach_ID", "Coach ID"),
                Field.text("Coach_Name", "Coach Name"),
                Field.text("Contact_No", "Contact No (10 digits)"),
                Field.text("Experience", "Experience (years)"),
                Field.text("Salary", "Salary"),
                Field.text("Position", "Position"),
                Field.fk("Team_ID", "Team",
                        "SELECT Team_ID, Team_Name FROM Team ORDER BY Team_Name")));

        register("Manage Tournaments", new CrudPanel("Tournament",
                Field.autoId("Tournament_ID", "Tournament ID"),
                Field.text("Tour_Name", "Tournament Name"),
                Field.text("Start_Dt", "Start Date (yyyy-mm-dd)"),
                Field.text("End_Dt", "End Date (yyyy-mm-dd)"),
                Field.text("PrizePool", "Prize Pool")));

        register("Manage Umpires", new CrudPanel("Umpire",
                Field.autoId("Umpire_ID", "Umpire ID"),
                Field.text("Umpire_Name", "Umpire Name"),
                Field.text("Experience", "Experience (years)"),
                Field.text("Contact_No", "Contact No (10 digits)")));

        register("Manage Stadiums", new CrudPanel("Stadium",
                Field.autoId("Stadium_ID", "Stadium ID"),
                Field.text("Stadium_Name", "Stadium Name"),
                Field.text("Location", "Location"),
                Field.text("Capacity", "Capacity")));

        register("Manage Matches", new CrudPanel("Match",
                Field.autoId("Match_ID", "Match ID"),
                Field.text("M_Dt", "Date (yyyy-mm-dd)"),
                Field.text("St_Time", "Start Time (hh:mm:ss)"),
                Field.text("Overs", "Overs (1-50)"),
                Field.fk("WT_ID", "Winner",
                        "SELECT Team_ID, Team_Name FROM Team ORDER BY Team_Name").nullable(),
                Field.fk("Tour_ID", "Tournament",
                        "SELECT Tournament_ID, Tour_Name FROM Tournament"),
                Field.fk("U_ID", "Umpire",
                        "SELECT Umpire_ID, Umpire_Name FROM Umpire"),
                Field.fk("St_ID", "Stadium",
                        "SELECT Stadium_ID, Stadium_Name FROM Stadium")));

        register("Manage Sponsors", new CrudPanel("Sponsors",
                Field.autoId("Sponsor_ID", "Sponsor ID"),
                Field.text("Company_Name", "Company Name"),
                Field.text("Sponsor_Logo", "Sponsor Logo (path)").nullable(),
                Field.text("Contact_No", "Contact No (10 digits)")));

        register("Player Statistics", new CrudPanel("Statistics",
                Field.fk("Player_ID", "Player",
                        "SELECT Player_ID, Player_Name FROM Player ORDER BY Player_Name").key(),
                Field.fixed("Format", "Format", "T20", "ODI", "Test").key(),
                Field.text("Innings", "Innings"),
                Field.text("Total_Runs", "Total Runs"),
                Field.text("Dismissals", "Dismissals"),
                Field.text("Strike_Rate", "Strike Rate"),
                Field.text("Wickets", "Wickets"),
                Field.text("Overs_Bowled", "Overs Bowled")));

        register("Points Table", new PointsTablePanel());
        register("Match Results", new MatchWonPanel());

        // Link tables (many-to-many relationships)
        register("Player Achievements", new CrudPanel("Player_Achievements",
                Field.fk("Player_ID", "Player",
                        "SELECT Player_ID, Player_Name FROM Player ORDER BY Player_Name").key(),
                Field.text("Achievements", "Achievement").key()));

        register("Match Teams", new CrudPanel("Match_Team",
                Field.fk("Match_ID", "Match",
                        "SELECT Match_ID, CONCAT(M_Dt, ' ', St_Time) FROM `Match` ORDER BY Match_ID").key(),
                Field.fk("T_ID", "Team",
                        "SELECT Team_ID, Team_Name FROM Team ORDER BY Team_Name").key()));

        register("Tournament Sponsors", new CrudPanel("Tour_Sponsor",
                Field.fk("Tournament_ID", "Tournament",
                        "SELECT Tournament_ID, Tour_Name FROM Tournament").key(),
                Field.fk("Sponsor_ID", "Sponsor",
                        "SELECT Sponsor_ID, Company_Name FROM Sponsors ORDER BY Company_Name").key(),
                Field.text("Amount", "Amount")));
    }

    /** Wraps a section screen with a header bar and Back button, and adds it as a page. */
    private <T extends JPanel & Refreshable> void register(String name, T screen) {
        JButton back = Theme.button("\u2190 Back to Home", Theme.GREEN);
        back.addActionListener(e -> cards.show(content, HOME));

        JLabel title = new JLabel(name, SwingConstants.CENTER);
        title.setFont(Theme.HEADING);
        title.setForeground(Color.WHITE);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.DARK);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        bar.add(back, BorderLayout.WEST);
        bar.add(title, BorderLayout.CENTER);
        // invisible spacer so the title stays centred
        bar.add(Box.createRigidArea(new Dimension(back.getPreferredSize().width, 1)), BorderLayout.EAST);

        JPanel page = new JPanel(new BorderLayout());
        page.add(bar, BorderLayout.NORTH);
        page.add(screen, BorderLayout.CENTER);

        content.add(page, name);
        order.add(name);
        screens.put(name, screen);
    }

    private void open(String name) {
        screens.get(name).reload();      // always show fresh data
        cards.show(content, name);
    }

    // =====================================================================
    // Home screen
    // =====================================================================
    private JPanel buildHome() {
        JPanel home = new JPanel(new BorderLayout());
        home.setBackground(Theme.LIGHT);

        // ---------- header ----------
        JLabel title = new JLabel("Cricket Tournament Management System");
        title.setFont(Theme.TITLE);
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Organiser dashboard");
        subtitle.setFont(Theme.NORMAL);
        subtitle.setForeground(new Color(0xC8E6C9));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);

        JButton logout = Theme.button("Log out", Theme.GREY);
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.DARK);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        header.add(titles, BorderLayout.CENTER);
        header.add(logout, BorderLayout.EAST);
        home.add(header, BorderLayout.NORTH);

        // ---------- big buttons ----------
        JPanel grid = new JPanel(new GridLayout(0, 5, 18, 18));
        grid.setBackground(Theme.LIGHT);
        grid.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        for (String name : order) {
            JButton b = Theme.button(name, Theme.GREEN);
            b.setFont(new Font("SansSerif", Font.BOLD, 17));
            b.setPreferredSize(new Dimension(190, 110));
            b.addActionListener(e -> open(name));
            grid.add(b);
        }
        JPanel gridHolder = new JPanel(new BorderLayout());
        gridHolder.setBackground(Theme.LIGHT);
        gridHolder.add(grid, BorderLayout.NORTH);
        home.add(gridHolder, BorderLayout.CENTER);

        return home;
    }
}
