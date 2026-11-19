package canteen;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

public final class AppStyle {
    public static final Color NAVY = new Color(18, 38, 78);
    public static final Color BLUE = new Color(38, 110, 220);
    public static final Color PALE = new Color(243, 247, 252);
    public static final Color TEXT = new Color(35, 48, 68);
    public static final Color MUTED = new Color(105, 118, 138);
    public static final Color WHITE = Color.WHITE;
    private AppStyle() {}
    public static void apply() {
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("PasswordField.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
    }
    public static void setWindowIcon(JFrame frame) {
        java.net.URL icon = AppStyle.class.getResource("/canteen/app_icon.png");
        if (icon != null) frame.setIconImage(new ImageIcon(icon).getImage());
    }
    public static JButton button(String text, Color color) {
        JButton b = new JButton(text); b.setBackground(color); b.setForeground(WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 15, 10, 15)); return b;
    }
    public static JLabel heading(String text) {
        JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 24)); l.setForeground(NAVY); return l;
    }
    public static JPanel card() {
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(225,232,242)), new EmptyBorder(16,16,16,16))); return p;
    }
    public static void styleTable(JTable table) {
        table.setRowHeight(30); table.setShowVerticalLines(false); table.setGridColor(new Color(232,237,245));
        table.setSelectionBackground(new Color(221,235,255)); table.setSelectionForeground(TEXT); table.setFillsViewportHeight(true);
        JTableHeader h=table.getTableHeader(); h.setFont(new Font("Segoe UI",Font.BOLD,12)); h.setBackground(new Color(233,240,250)); h.setForeground(NAVY); h.setPreferredSize(new Dimension(0,34));
    }
    public static JPanel page(String title) {
        JPanel p = new JPanel(new BorderLayout(14,14)); p.setBackground(PALE); p.setBorder(new EmptyBorder(22,24,20,24));
        p.add(heading(title), BorderLayout.NORTH); return p;
    }
}
