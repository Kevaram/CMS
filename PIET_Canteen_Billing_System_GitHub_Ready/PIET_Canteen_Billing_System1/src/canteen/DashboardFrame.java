
package canteen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class DashboardFrame extends JFrame {
    private final String username, role;
    private JPanel content, sidebar;
    private JLabel salesValue, ordersValue, menuValue, stockValue, connectionValue;
    private JButton selectedNav;

    public DashboardFrame(String username, String role) {
        this.username = username;
        this.role = role == null ? "STAFF" : role.toUpperCase();

        AppStyle.apply();
        AppStyle.setWindowIcon(this);

        setTitle("PIET Canteen | Dashboard");
        setSize(1280, 790);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppStyle.PALE);

        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppStyle.NAVY);
        sidebar.setPreferredSize(new Dimension(225, 0));
        sidebar.setBorder(new EmptyBorder(22, 14, 18, 14));

        JLabel brand = new JLabel("  PIET CANTEEN");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(Color.WHITE);
        brand.setBorder(new EmptyBorder(0, 0, 5, 0));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        JLabel sub = new JLabel("  CAMPUS FOOD SERVICES");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sub.setForeground(new Color(172, 195, 230));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sub);
        sidebar.add(Box.createVerticalStrut(28));

        addNav("Dashboard", () -> showDashboard());
        addNav("New Bill", () -> new BillingFrame(username).setVisible(true));
        addNav("Food Menu", () -> new FoodMenuFrame(role, username).setVisible(true));
        addNav("Order History", () -> new OrderHistoryFrame(role, username).setVisible(true));

        if (AuditLog.isAdmin(role)) {
            addNav("Sales Reports", () -> new ReportFrame(role).setVisible(true));
            addNav("Stock Management", () -> new StockManagementFrame(username, role).setVisible(true));
            addNav("User Management", () -> new UserManagementFrame(username, role).setVisible(true));
            addNav("Settings", () -> new SettingsFrame(username, role).setVisible(true));
            addNav("Backup & Restore", () -> new BackupFrame(username, role).setVisible(true));
            addNav("Activity Log", () -> new ActivityLogFrame(role).setVisible(true));
        }

        addNav("Notifications", () -> new NotificationFrame(username, role).setVisible(true));
        addNav("Help & About", () -> new HelpAboutFrame().setVisible(true));

        sidebar.add(Box.createVerticalGlue());

        JLabel account = new JLabel("  " + username + "  •  " + role);
        account.setForeground(Color.WHITE);
        account.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(account);
        sidebar.add(Box.createVerticalStrut(10));

        JButton logout = AppStyle.button("LOG OUT", new Color(172, 54, 68));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logout.setPreferredSize(new Dimension(197, 40));
        logout.addActionListener(e -> logout());
        sidebar.add(logout);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(AppStyle.PALE);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel greet = new JLabel("Good day, " + username);
        greet.setFont(new Font("Segoe UI", Font.BOLD, 18));
        greet.setForeground(AppStyle.NAVY);

        JLabel date = new JLabel(LocalDate.now() + "   |   " + this.role);
        date.setForeground(AppStyle.MUTED);

        top.add(greet, BorderLayout.WEST);
        top.add(date, BorderLayout.EAST);
        right.add(top, BorderLayout.NORTH);

        content = new JPanel(new BorderLayout());
        content.setBackground(AppStyle.PALE);
        right.add(content, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        add(root);

        showDashboard();
    }

    private void addNav(String title, Runnable action) {
        JButton b = new JButton("  " + title);

        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setPreferredSize(new Dimension(197, 42));

        b.setBackground(AppStyle.NAVY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 12, 9, 8));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (b != selectedNav) {
                    b.setBackground(new Color(35, 62, 105));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (b != selectedNav) {
                    b.setBackground(AppStyle.NAVY);
                }
            }
        });

        b.addActionListener(e -> {
            if (selectedNav != null) {
                selectedNav.setBackground(AppStyle.NAVY);
                selectedNav.setForeground(Color.WHITE);
            }

            selectedNav = b;
            selectedNav.setBackground(AppStyle.BLUE);
            selectedNav.setForeground(Color.WHITE);

            action.run();
        });

        sidebar.add(b);
        sidebar.add(Box.createVerticalStrut(3));

        if ("Dashboard".equals(title)) {
            selectedNav = b;
            b.setBackground(AppStyle.BLUE);
        }
    }

    private void showDashboard() {
        JPanel page = new JPanel(new BorderLayout(16, 16));
        page.setBackground(AppStyle.PALE);
        page.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel intro = new JPanel(new BorderLayout());
        intro.setOpaque(false);

        JLabel title = AppStyle.heading("Canteen overview");
        JLabel caption = new JLabel("A live view of sales, orders and menu availability");
        caption.setForeground(AppStyle.MUTED);

        JPanel t = new JPanel(new GridLayout(2, 1, 0, 4));
        t.setOpaque(false);
        t.add(title);
        t.add(caption);
        intro.add(t, BorderLayout.WEST);

        JButton bill = AppStyle.button("+  CREATE NEW BILL", AppStyle.BLUE);
        bill.addActionListener(e -> new BillingFrame(username).setVisible(true));
        intro.add(bill, BorderLayout.EAST);
        page.add(intro, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 16));
        center.setOpaque(false);

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 14));
        cards.setOpaque(false);

        salesValue = stat(cards, "TODAY'S SALES", "₹ 0.00", new Color(38, 110, 220));
        ordersValue = stat(cards, "TODAY'S ORDERS", "0", new Color(32, 145, 118));
        menuValue = stat(cards, "AVAILABLE MENU ITEMS", "0", new Color(126, 83, 190));
        stockValue = stat(cards, "LOW STOCK ITEMS", "0", new Color(205, 133, 43));

        center.add(cards, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 14, 14));
        lower.setOpaque(false);

        JPanel recent = AppStyle.card();
        JLabel recentTitle = new JLabel("Recent orders");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        recentTitle.setForeground(AppStyle.NAVY);
        recent.add(recentTitle, BorderLayout.NORTH);

        JTextArea recentText = new JTextArea();
        recentText.setEditable(false);
        recentText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        recentText.setBackground(Color.WHITE);
        recentText.setForeground(new Color(35, 48, 70));
        recentText.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane recentScroll = new JScrollPane(recentText);
        recent.add(recentScroll, BorderLayout.CENTER);

        JPanel quick = AppStyle.card();
        JLabel quickTitle = new JLabel("Quick actions & service status");
        quickTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        quickTitle.setForeground(AppStyle.NAVY);
        quick.add(quickTitle, BorderLayout.NORTH);

        JPanel quickBody = new JPanel();
        quickBody.setOpaque(false);
        quickBody.setLayout(new BoxLayout(quickBody, BoxLayout.Y_AXIS));

        connectionValue = new JLabel("Checking database connection...");
        connectionValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        quickBody.add(connectionValue);
        quickBody.add(Box.createVerticalStrut(12));

        for (String s : new String[]{
            "Create a bill and print the receipt",
            "Check stock before the rush",
            "Review recent orders and payment methods"
        }) {
            JLabel l = new JLabel("•  " + s);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(new Color(45, 57, 77));
            l.setBorder(new EmptyBorder(8, 0, 8, 0));
            quickBody.add(l);
        }

        quick.add(quickBody, BorderLayout.NORTH);
        quick.add(new SalesChartPanel(), BorderLayout.CENTER);

        lower.add(recent);
        lower.add(quick);
        center.add(lower, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);

        JLabel footer = new JLabel(
            "Designed & Developed by Kevaram   •   PIET Canteen Billing System"
        );
        footer.setForeground(AppStyle.MUTED);
        page.add(footer, BorderLayout.SOUTH);

        content.removeAll();
        content.add(page, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();

        loadStats(recentText);
    }

    private JLabel stat(JPanel parent, String label, String value, Color accent) {
        JPanel p = AppStyle.card();

        JPanel strip = new JPanel();
        strip.setBackground(accent);
        strip.setPreferredSize(new Dimension(5, 10));
        p.add(strip, BorderLayout.WEST);

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 9));
        body.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(AppStyle.MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 23));
        v.setForeground(AppStyle.NAVY);

        body.add(l);
        body.add(v);
        p.add(body, BorderLayout.CENTER);
        parent.add(p);

        return v;
    }

    private void loadStats(JTextArea recent) {
        String q = "SELECT COUNT(*) n, COALESCE(SUM(final_total),0) sales "
                 + "FROM orders WHERE DATE(order_date)=CURDATE() "
                 + "AND status='COMPLETED'";

        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement()) {

            try (ResultSet r = s.executeQuery(q)) {
                if (r.next()) {
                    ordersValue.setText(r.getString("n"));
                    salesValue.setText(String.format("₹ %.2f", r.getDouble("sales")));
                }
            }

            try (ResultSet r = s.executeQuery(
                    "SELECT COUNT(*) FROM food_items "
                    + "WHERE available=TRUE AND stock_quantity>0")) {
                if (r.next()) {
                    menuValue.setText(r.getString(1));
                }
            }

            try (ResultSet r = s.executeQuery(
                    "SELECT COUNT(*) FROM food_items "
                    + "WHERE stock_quantity<=COALESCE(min_stock_threshold,10)")) {
                if (r.next()) {
                    stockValue.setText(r.getString(1));
                }
            }

            try (ResultSet r = s.executeQuery(
                    "SELECT order_id,order_date,username,final_total,payment_method "
                    + "FROM orders ORDER BY order_id DESC LIMIT 8")) {

                StringBuilder b = new StringBuilder(
                    "ORDER     DATE/TIME              TOTAL       PAYMENT\n"
                );

                while (r.next()) {
                    b.append(String.format(
                        "#%-8d %-20s ₹%-10.2f %s%n",
                        r.getInt(1),
                        r.getTimestamp(2),
                        r.getDouble(4),
                        r.getString(5)
                    ));
                }

                if (b.toString().endsWith("PAYMENT\n")) {
                    b.append("No orders recorded yet.");
                }

                recent.setText(b.toString());
            }

            connectionValue.setText("●  MySQL connection: Connected");
            connectionValue.setForeground(new Color(28, 140, 95));

        } catch (SQLException e) {
            connectionValue.setText("●  MySQL connection: Unavailable");
            connectionValue.setForeground(new Color(190, 45, 55));

            recent.setText(
                "Unable to load recent orders.\n"
                + "Check MySQL and the migration script.\n\n"
                + e.getMessage()
            );

            salesValue.setText("—");
            ordersValue.setText("—");
            menuValue.setText("—");
            stockValue.setText("—");
        }
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(
                this,
                "Log out of PIET Canteen?",
                "Confirm logout",
                JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION) {

            AuditLog.record(username, "LOGOUT", "User logged out");
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
