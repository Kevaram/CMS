
package canteen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OrderHistoryFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField, fromField, toField;
    private JComboBox<String> paymentFilter, statusFilter;

    private String role = "ADMIN", username = "system";
    public OrderHistoryFrame() { this("ADMIN", "system"); }
    public OrderHistoryFrame(String role, String username) { this.role=role; this.username=username;
        AppStyle.apply();
        AppStyle.setWindowIcon(this);
        setTitle("PIET - Order History");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        main.setBackground(new Color(245, 247, 252));

        JLabel title = new JLabel(
            "ORDER HISTORY", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(new Color(25, 55, 109));
        main.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"Order ID", "Date", "Username", "Subtotal",
                "Discount", "GST", "Final Total", "Payment", "Status"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        AppStyle.styleTable(table);
        table.setAutoCreateRowSorter(true);

        main.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottom.setBackground(Color.WHITE);

        searchField = new JTextField(12);
        fromField = new JTextField(9); toField = new JTextField(9);
        paymentFilter = new JComboBox<>(new String[]{"All", "Cash", "UPI", "Card"});
        statusFilter = new JComboBox<>(new String[]{"All", "COMPLETED", "CANCELLED", "REFUNDED"});

        JButton searchButton = createButton(
            "SEARCH ORDER", new Color(125, 75, 180));

        JButton refreshButton = createButton(
            "SHOW ALL", new Color(38, 110, 190));

        JButton detailsButton = createButton(
            "VIEW ITEMS", new Color(22, 145, 90));
        JButton refundButton = createButton("CANCEL / REFUND (ADMIN)", new Color(190, 45, 55));
        JButton exportButton = createButton("EXPORT CSV", new Color(30, 140, 150));

        // Back button
        JButton backButton = createButton(
            "BACK TO DASHBOARD", new Color(25, 55, 109));

        bottom.add(new JLabel("Search ID/user/food:"));
        bottom.add(searchField);
        bottom.add(new JLabel("From")); bottom.add(fromField); bottom.add(new JLabel("To")); bottom.add(toField);
        bottom.add(paymentFilter); bottom.add(statusFilter);
        bottom.add(searchButton);
        bottom.add(refreshButton);
        bottom.add(detailsButton);
        if (AuditLog.isAdmin(role)) bottom.add(refundButton);
        bottom.add(exportButton);
        bottom.add(backButton);

        main.add(bottom, BorderLayout.SOUTH);
        add(main);

        searchButton.addActionListener(e -> searchOrders());

        refreshButton.addActionListener(e -> {
            searchField.setText(""); fromField.setText(""); toField.setText(""); paymentFilter.setSelectedIndex(0); statusFilter.setSelectedIndex(0);
            loadOrders("");
        });

        detailsButton.addActionListener(e -> viewItems());
        refundButton.addActionListener(e -> cancelRefund());
        exportButton.addActionListener(e -> exportCsv());

        // Close this window and return to the existing dashboard
        backButton.addActionListener(e -> dispose());

        loadOrders("");
    }

    private JButton createButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void loadOrders(String keyword) {
        model.setRowCount(0);

        String sql = "SELECT o.order_id,o.order_date,o.username,o.subtotal,o.discount,o.gst,o.final_total,o.payment_method,o.status "
                   + "FROM orders o WHERE (CAST(o.order_id AS CHAR) LIKE ? OR o.username LIKE ? OR EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id=o.order_id AND oi.item_name LIKE ?)) "
                   + "AND (?='' OR DATE(o.order_date)>=?) AND (?='' OR DATE(o.order_date)<=?) "
                   + "AND (?='All' OR o.payment_method=?) AND (?='All' OR o.status=?) ORDER BY o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%"); ps.setString(2, "%" + keyword + "%"); ps.setString(3, "%" + keyword + "%");
            String from=fromField.getText().trim(), to=toField.getText().trim();
            ps.setString(4,from); ps.setString(5,from); ps.setString(6,to); ps.setString(7,to);
            String pay=paymentFilter.getSelectedItem().toString(), status=statusFilter.getSelectedItem().toString();
            ps.setString(8,pay); ps.setString(9,pay); ps.setString(10,status); ps.setString(11,status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("order_id"),
                        rs.getTimestamp("order_date"),
                        rs.getString("username"),
                        rs.getDouble("subtotal"),
                        rs.getDouble("discount"),
                        rs.getDouble("gst"),
                        rs.getDouble("final_total"),
                        rs.getString("payment_method"), rs.getString("status")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this, "Could not load orders: " + e.getMessage());
        }
    }


    private void cancelRefund() {
        if (!AuditLog.isAdmin(role)) { AuditLog.requireAdmin(this, role); return; }
        int selected = table.getSelectedRow();
        if (selected < 0) { JOptionPane.showMessageDialog(this, "Select an order first."); return; }
        int row = table.convertRowIndexToModel(selected);
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String status = model.getValueAt(row, 8).toString();
        if (!"COMPLETED".equalsIgnoreCase(status)) { JOptionPane.showMessageDialog(this, "Only completed orders can be cancelled/refunded. This order is " + status + "."); return; }
        String reason = JOptionPane.showInputDialog(this, "Reason for cancellation/refund (required):");
        if (reason == null) return;
        if (reason.trim().isEmpty()) { JOptionPane.showMessageDialog(this, "A reason is required."); return; }
        if (JOptionPane.showConfirmDialog(this, "Refund order #" + id + " and restore its stock?", "Confirm refund", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String st;
                try (PreparedStatement p = c.prepareStatement("SELECT status FROM orders WHERE order_id=? FOR UPDATE")) { p.setInt(1,id); try(ResultSet r=p.executeQuery()){ if(!r.next()) throw new SQLException("Order not found."); st=r.getString(1); } }
                if (!"COMPLETED".equalsIgnoreCase(st)) throw new SQLException("Order has already been cancelled/refunded.");
                try (PreparedStatement p = c.prepareStatement("SELECT item_id,quantity,item_name FROM order_items WHERE order_id=?")) {
                    p.setInt(1,id); try(ResultSet r=p.executeQuery()) { while(r.next()) {
                        int itemId=r.getInt("item_id"), qty=r.getInt("quantity"), before;
                        try(PreparedStatement q=c.prepareStatement("SELECT stock_quantity FROM food_items WHERE item_id=? FOR UPDATE")){q.setInt(1,itemId);try(ResultSet z=q.executeQuery()){if(!z.next())continue;before=z.getInt(1);}}
                        int after=before+qty;
                        try(PreparedStatement q=c.prepareStatement("UPDATE food_items SET stock_quantity=?,available=TRUE WHERE item_id=?")){q.setInt(1,after);q.setInt(2,itemId);q.executeUpdate();}
                        try(PreparedStatement q=c.prepareStatement("INSERT INTO stock_movements(item_id,previous_quantity,new_quantity,change_quantity,reason,username,created_at) VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP)")){q.setInt(1,itemId);q.setInt(2,before);q.setInt(3,after);q.setInt(4,qty);q.setString(5,"Refund order #"+id+": "+reason.trim());q.setString(6,username);q.executeUpdate();}
                    }}
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE orders SET status='REFUNDED',payment_status='REFUNDED',status_reason=?,status_changed_by=?,status_changed_at=CURRENT_TIMESTAMP WHERE order_id=? AND status='COMPLETED'")){p.setString(1,reason.trim());p.setString(2,username);p.setInt(3,id);if(p.executeUpdate()!=1)throw new SQLException("Order status changed. Refresh and try again.");}
                c.commit(); AuditLog.record(username,"ORDER_REFUNDED","Order #"+id+"; reason="+reason.trim()); JOptionPane.showMessageDialog(this,"Order refunded and stock restored.");loadOrders(searchField.getText().trim());
            } catch(Exception ex) { c.rollback(); throw ex; } finally { c.setAutoCommit(true); }
        } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Refund failed: "+ex.getMessage(),"Refund error",JOptionPane.ERROR_MESSAGE); }
    }
    private void exportCsv() {
        JFileChooser chooser=new JFileChooser(); chooser.setSelectedFile(new java.io.File("piet_order_history.csv"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        try(java.io.PrintWriter out=new java.io.PrintWriter(chooser.getSelectedFile(),"UTF-8")) {
            for(int c=0;c<model.getColumnCount();c++){if(c>0)out.print(',');out.print(csv(model.getColumnName(c)));}out.println();
            for(int r=0;r<model.getRowCount();r++){for(int c=0;c<model.getColumnCount();c++){if(c>0)out.print(',');out.print(csv(String.valueOf(model.getValueAt(r,c))));}out.println();}
            AuditLog.record(username,"ORDER_HISTORY_EXPORTED",chooser.getSelectedFile().getName());JOptionPane.showMessageDialog(this,"CSV exported successfully.");
        } catch(Exception ex){JOptionPane.showMessageDialog(this,"Export failed: "+ex.getMessage());}
    }
    private String csv(String value){return "\""+value.replace("\"","\"\"")+"\"";}

    private void searchOrders() {
        String from=fromField.getText().trim(),to=toField.getText().trim();
        if ((!from.isEmpty() && !validDate(from)) || (!to.isEmpty() && !validDate(to))) { JOptionPane.showMessageDialog(this,"Use date format YYYY-MM-DD."); return; }
        if (!from.isEmpty() && !to.isEmpty() && to.compareTo(from)<0) { JOptionPane.showMessageDialog(this,"End date must not precede start date."); return; }
        loadOrders(searchField.getText().trim());
    }
    private boolean validDate(String s){try{java.time.LocalDate.parse(s);return true;}catch(Exception e){return false;}}

    private void viewItems() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                this, "Please select an order first.");
            return;
        }

        // Convert displayed row to the model row after sorting
        int modelRow = table.convertRowIndexToModel(row);

        int orderId = Integer.parseInt(
            model.getValueAt(modelRow, 0).toString());

        String sql = "SELECT item_name, quantity, unit_price, line_total "
                   + "FROM order_items WHERE order_id = ?";

        StringBuilder details = new StringBuilder();
        details.append("Items for Order #")
               .append(orderId).append("\n\n");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.append(rs.getString("item_name"))
                        .append(" | Qty: ")
                        .append(rs.getInt("quantity"))
                        .append(" | Price: Rs. ")
                        .append(String.format("%.2f",
                            rs.getDouble("unit_price")))
                        .append(" | Amount: Rs. ")
                        .append(String.format("%.2f",
                            rs.getDouble("line_total")))
                        .append("\n");
                }
            }

            JOptionPane.showMessageDialog(
                this, details.toString(), "Order Details",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this, "Could not load order items: " + e.getMessage());
        }
    }
}
