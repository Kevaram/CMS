package canteen;
import java.sql.*;
public final class AuditLog {
    private AuditLog() {}
    public static void record(String username, String action, String details) {
        String sql="INSERT INTO activity_logs(username, action_type, details, created_at) VALUES(?,?,?,CURRENT_TIMESTAMP)";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,username); p.setString(2,action); p.setString(3,details); p.executeUpdate();
        } catch(SQLException e) { System.err.println("Audit log could not be written: "+e.getMessage()); }
    }
    public static boolean isAdmin(String role) { return role != null && role.equalsIgnoreCase("ADMIN"); }
    public static void requireAdmin(java.awt.Component parent, String role) {
        if(!isAdmin(role)) javax.swing.JOptionPane.showMessageDialog(parent,"This action requires administrator access.","Access denied",javax.swing.JOptionPane.WARNING_MESSAGE);
    }
}
