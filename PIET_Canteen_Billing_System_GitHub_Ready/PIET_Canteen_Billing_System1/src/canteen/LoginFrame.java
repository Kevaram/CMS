package canteen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPassword;
    private JButton loginButton;

    private static final Color NAVY = new Color(8, 34, 72);
    private static final Color BLUE = new Color(39, 105, 226);
    private static final Color TEXT = new Color(13, 39, 79);
    private static final Color MUTED = new Color(112, 133, 170);

    public LoginFrame() {
        AppStyle.apply();
        AppStyle.setWindowIcon(this);
        setTitle("PIET Canteen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 850);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.53;
        root.add(createBrandPanel(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.47;
        root.add(createSignInPanel(), gbc);
        setContentPane(root);

        // Keep first-time setup available without changing the approved clean login design.
        KeyStroke setupKey = KeyStroke.getKeyStroke(KeyEvent.VK_A,
                InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(setupKey, "firstAdminSetup");
        getRootPane().getActionMap().put("firstAdminSetup", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { setupAdmin(); }
        });
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel createBrandPanel() {
        PhotoPanel brand = new PhotoPanel();
        brand.setLayout(new BorderLayout(0, 20));
        brand.setBorder(new EmptyBorder(45, 42, 30, 36));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        top.setOpaque(false);
        JLabel logo = new JLabel(new BrandIcon(86));
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("PIET CANTEEN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        JLabel subtitle = new JLabel("CAMPUS DINING • MANAGEMENT SYSTEM");
        subtitle.setForeground(new Color(241, 246, 255));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        names.add(title);
        names.add(Box.createVerticalStrut(4));
        names.add(subtitle);
        top.add(logo);
        top.add(names);

        JPanel message = new JPanel();
        message.setOpaque(false);
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        JLabel headline = new JLabel("<html>Campus food.<br>Made simple.</html>");
        headline.setForeground(Color.WHITE);
        headline.setFont(new Font("Segoe UI", Font.BOLD, 54));
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel line = new JLabel("Smart billing, faster service, better campus dining.");
        line.setForeground(Color.WHITE);
        line.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel college = new JLabel("POORNIMA INSTITUTE OF ENGINEERING & TECHNOLOGY");
        college.setOpaque(true);
        college.setBackground(new Color(28, 83, 174));
        college.setForeground(Color.WHITE);
        college.setFont(new Font("Segoe UI", Font.BOLD, 13));
        college.setBorder(new EmptyBorder(13, 17, 13, 17));
        college.setAlignmentX(Component.LEFT_ALIGNMENT);
        message.add(headline);
        message.add(Box.createVerticalStrut(17));
        message.add(line);
        message.add(Box.createVerticalStrut(28));
        message.add(college);

        JLabel footer = new JLabel("Designed & Developed by Kevaram");
        footer.setForeground(new Color(203, 218, 242));
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        brand.add(top, BorderLayout.NORTH);
        brand.add(message, BorderLayout.CENTER);
        brand.add(footer, BorderLayout.SOUTH);
        return brand;
    }

    private JPanel createSignInPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(new Color(243, 247, 253));
        right.setBorder(new EmptyBorder(32, 35, 28, 35));

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(222, 230, 244), 1, true),
                new EmptyBorder(38, 36, 34, 36)));
        card.setPreferredSize(new Dimension(520, 605));
        card.setMaximumSize(new Dimension(570, 620));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel portal = centeredLabel("STAFF PORTAL", BLUE, 16, Font.BOLD);
        JPanel accent = new JPanel();
        accent.setBackground(BLUE);
        accent.setMaximumSize(new Dimension(72, 3));
        accent.setPreferredSize(new Dimension(72, 3));
        accent.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel welcome = centeredLabel("Welcome back", TEXT, 40, Font.BOLD);
        JLabel hint = centeredLabel("Sign in to manage your workspace", MUTED, 17, Font.PLAIN);
        card.add(portal);
        card.add(Box.createVerticalStrut(15));
        card.add(accent);
        card.add(Box.createVerticalStrut(24));
        card.add(welcome);
        card.add(Box.createVerticalStrut(10));
        card.add(hint);
        card.add(Box.createVerticalStrut(42));

        card.add(fieldLabel("USERNAME", new UserIcon()));
        card.add(Box.createVerticalStrut(10));
        usernameField = new JTextField();
        styleField(usernameField, "Enter your username");
        card.add(usernameField);
        card.add(Box.createVerticalStrut(31));

        card.add(fieldLabel("PASSWORD", new LockIcon()));
        card.add(Box.createVerticalStrut(10));
        passwordField = new JPasswordField();
        passwordField.setEchoChar('•');
        styleField(passwordField, "Enter your password");
        card.add(passwordField);
        card.add(Box.createVerticalStrut(14));

        showPassword = new JCheckBox("Show password");
        showPassword.setOpaque(false);
        showPassword.setForeground(new Color(52, 76, 117));
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        showPassword.setFocusPainted(false);
        showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPassword.addActionListener(e -> passwordField.setEchoChar(
                showPassword.isSelected() ? (char) 0 : '•'));
        card.add(showPassword);
        card.add(Box.createVerticalStrut(27));

        loginButton = new JButton("Sign in   →");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(BLUE);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        loginButton.setPreferredSize(new Dimension(440, 58));
        loginButton.addActionListener(e -> login());
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { loginButton.setBackground(new Color(27, 87, 203)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { loginButton.setBackground(BLUE); }
        });
        card.add(loginButton);
        card.add(Box.createVerticalStrut(35));

        JLabel access = new JLabel("Authorized staff access only", new LockIcon(), SwingConstants.CENTER);
        access.setIconTextGap(10);
        access.setForeground(MUTED);
        access.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        access.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(access);

        column.add(card);
        column.add(Box.createVerticalStrut(28));
        JLabel version = centeredLabel("PIET Canteen Management System • v1.0", MUTED, 13, Font.PLAIN);
        column.add(version);
        right.add(column);
        return right;
    }

    private JLabel centeredLabel(String text, Color color, int size, int style) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", style, size));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JLabel fieldLabel(String text, Icon icon) {
        JLabel label = new JLabel(text, icon, SwingConstants.LEFT);
        label.setIconTextGap(13);
        label.setForeground(new Color(105, 127, 165));
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleField(JTextField field, String hint) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        field.setForeground(new Color(48, 68, 104));
        field.setBackground(Color.WHITE);
        field.setCaretColor(BLUE);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(207, 219, 239), 1, true),
                new EmptyBorder(12, 15, 12, 15)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        field.setPreferredSize(new Dimension(440, 58));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setToolTipText(hint);
    }

    private static class PhotoPanel extends JPanel {
        private final Image image;
        PhotoPanel() {
            java.net.URL url = LoginFrame.class.getResource("/canteen/canteen_bg.jpg");
            image = url == null ? null : new ImageIcon(url).getImage();
            setBackground(NAVY);
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (image != null && image.getWidth(this) > 0) {
                double scale = Math.max((double)getWidth()/image.getWidth(this), (double)getHeight()/image.getHeight(this));
                int w = (int)(image.getWidth(this)*scale), h = (int)(image.getHeight(this)*scale);
                g.drawImage(image, (getWidth()-w)/2, (getHeight()-h)/2, w, h, this);
            } else {
                g.setPaint(new GradientPaint(0, 0, new Color(18, 53, 103), getWidth(), getHeight(), NAVY));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setColor(new Color(3, 25, 59, 72));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    private static class BrandIcon implements Icon {
        private final int size;
        BrandIcon(int size) { this.size = size; }
        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE); g.setStroke(new BasicStroke(4f));
            g.draw(new Ellipse2D.Double(x+3,y+3,size-6,size-6));
            g.draw(new Ellipse2D.Double(x+13,y+13,size-26,size-26));
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x+size/2,y+18,x+size/2,y+size-17);
            g.drawLine(x+size/2-10,y+24,x+size/2-10,y+39);
            g.drawLine(x+size/2+10,y+24,x+size/2+10,y+39);
            g.dispose();
        }
    }

    private static class UserIcon implements Icon {
        public int getIconWidth() { return 22; } public int getIconHeight() { return 22; }
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g=(Graphics2D)graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(83,105,146)); g.fill(new Ellipse2D.Double(x+7,y+2,9,9));
            g.fill(new RoundRectangle2D.Double(x+3,y+12,17,9,7,7)); g.dispose();
        }
    }
    private static class LockIcon implements Icon {
        public int getIconWidth() { return 22; } public int getIconHeight() { return 22; }
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g=(Graphics2D)graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(83,105,146)); g.setStroke(new BasicStroke(2.5f));
            g.drawRoundRect(x+6,y+2,10,12,5,5); g.fillRoundRect(x+3,y+9,16,12,3,3);
            g.setColor(Color.WHITE); g.fill(new Ellipse2D.Double(x+9,y+12,4,4)); g.fillRoundRect(x+10,y+15,2,3,1,1); g.dispose();
        }
    }

    private void login() {
        String username=usernameField.getText().trim(); char[] pass=passwordField.getPassword();
        if(username.isEmpty()||pass.length==0){ JOptionPane.showMessageDialog(this,"Enter your username and password."); return; }
        String sql="SELECT role,password_hash,password,must_change_password,active FROM users WHERE username=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,username); try(ResultSet r=p.executeQuery()) {
                if(!r.next()||!r.getBoolean("active")){ JOptionPane.showMessageDialog(this,"Invalid credentials or inactive account.","Sign in failed",JOptionPane.ERROR_MESSAGE); return; }
                String hash=r.getString("password_hash"), legacy=r.getString("password"); boolean ok=SecurityUtil.verify(pass,hash);
                if(!ok && hash==null && legacy!=null && legacy.equals(new String(pass))) {
                    String upgraded=SecurityUtil.hash(pass); try(PreparedStatement u=c.prepareStatement("UPDATE users SET password_hash=?, password=NULL WHERE username=?")){u.setString(1,upgraded);u.setString(2,username);u.executeUpdate();} ok=true;
                }
                if(!ok){ JOptionPane.showMessageDialog(this,"Invalid credentials.","Sign in failed",JOptionPane.ERROR_MESSAGE); return; }
                String role=r.getString("role"); boolean mustChange=r.getBoolean("must_change_password");
                AuditLog.record(username,"LOGIN","Successful login");
                if(mustChange && !changePasswordDialog(c,username)){ return; }
                new DashboardFrame(username,role).setVisible(true); dispose();
            }
        } catch(SQLException ex){ JOptionPane.showMessageDialog(this,"Cannot connect to the database.\nStart MySQL, run database/01_migration.sql, and check DB settings.\n\n"+ex.getMessage(),"Database connection",JOptionPane.ERROR_MESSAGE); }
        finally { Arrays.fill(pass,'\0'); }
    }
    private boolean changePasswordDialog(Connection c,String username) {
        JPasswordField a=new JPasswordField(18), b=new JPasswordField(18); JPanel p=new JPanel(new GridLayout(0,1,6,6)); p.add(new JLabel("Set a new password (at least 8 characters):"));p.add(a);p.add(new JLabel("Confirm new password:"));p.add(b);
        if(JOptionPane.showConfirmDialog(this,p,"Change temporary password",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return false;
        char[] x=a.getPassword(), y=b.getPassword();
        if(x.length<8||!Arrays.equals(x,y)){JOptionPane.showMessageDialog(this,"Passwords must match and contain at least 8 characters.");return false;}
        try(PreparedStatement u=c.prepareStatement("UPDATE users SET password_hash=?, password=NULL, must_change_password=FALSE WHERE username=?")){u.setString(1,SecurityUtil.hash(x));u.setString(2,username);u.executeUpdate();AuditLog.record(username,"PASSWORD_CHANGED","Temporary password changed");return true;}
        catch(SQLException e){JOptionPane.showMessageDialog(this,"Password update failed: "+e.getMessage());return false;} finally {Arrays.fill(x,'\0');Arrays.fill(y,'\0');}
    }
    private void setupAdmin() {
        try(Connection c=DBConnection.getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT COUNT(*) FROM users")) {
            r.next(); if(r.getInt(1)>0){JOptionPane.showMessageDialog(this,"Initial setup is disabled because users already exist. Ask an existing administrator to create an account.");return;}
            String expectedToken=System.getenv("PIET_ADMIN_SETUP_TOKEN"); if(expectedToken==null||expectedToken.length()<16){JOptionPane.showMessageDialog(this,"For security, set the PIET_ADMIN_SETUP_TOKEN environment variable to a private random value of at least 16 characters, then restart the app before first-admin setup.");return;} JTextField token=new JTextField(); JTextField user=new JTextField();JPasswordField a=new JPasswordField(),b=new JPasswordField(); JPanel p=new JPanel(new GridLayout(0,1,5,5));p.add(new JLabel("One-time setup token:"));p.add(token);p.add(new JLabel("Create the first administrator username:"));p.add(user);p.add(new JLabel("Password (minimum 10 characters):"));p.add(a);p.add(new JLabel("Confirm password:"));p.add(b);
            if(JOptionPane.showConfirmDialog(this,p,"One-time administrator setup",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
            char[] x=a.getPassword(),y=b.getPassword(); if(!expectedToken.equals(token.getText())||user.getText().trim().isEmpty()||x.length<10||!Arrays.equals(x,y)){JOptionPane.showMessageDialog(this,"Invalid setup token, username, or password. Use the exact token and matching password of at least 10 characters.");return;}
            try(PreparedStatement i=c.prepareStatement("INSERT INTO users(username,role,password_hash,password,must_change_password,active) VALUES(?,'ADMIN',?,NULL,FALSE,TRUE)")){i.setString(1,user.getText().trim());i.setString(2,SecurityUtil.hash(x));i.executeUpdate();}
            Arrays.fill(x,'\0');Arrays.fill(y,'\0');AuditLog.record(user.getText().trim(),"FIRST_ADMIN_CREATED","One-time setup");JOptionPane.showMessageDialog(this,"Administrator created. Sign in with the new account.");usernameField.setText(user.getText().trim());
        } catch(SQLException e){JOptionPane.showMessageDialog(this,"Setup unavailable. Run the database migration first.\n"+e.getMessage(),"Setup error",JOptionPane.ERROR_MESSAGE);}
    }
}
