import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** Cricket-green look shared by every screen. */
public class Theme {

    public static final Color DARK = new Color(0x1B5E20);   // pitch green (headers)
    public static final Color GREEN = new Color(0x2E7D32);  // buttons
    public static final Color LIGHT = new Color(0xE8F5E9);  // page background
    public static final Color RED = new Color(0xB71C1C);    // delete
    public static final Color GREY = new Color(0x546E7A);   // clear / secondary

    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 28);
    public static final Font HEADING = new Font("SansSerif", Font.BOLD, 20);
    public static final Font BIG = new Font("SansSerif", Font.BOLD, 16);
    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 14);

    /** Call once at start-up so colours look the same on Windows, Mac and Linux. */
    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // keep the default look if this fails
        }
    }

    public static JButton button(String text, Color background) {
        JButton b = new JButton(text);
        b.setUI(new BasicButtonUI());          // makes the background colour show everywhere
        b.setBackground(background);
        b.setForeground(Color.WHITE);
        b.setFont(BIG);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void styleTable(JTable table) {
        table.setFont(NORMAL);
        table.setRowHeight(26);
        table.setGridColor(new Color(0xC8E6C9));
        table.setSelectionBackground(GREEN);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setBackground(DARK);
        header.setForeground(Color.WHITE);
        header.setFont(BIG);
        header.setReorderingAllowed(false);
    }
}
