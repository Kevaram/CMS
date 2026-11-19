
package canteen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.math.*;
import java.util.*;
import java.util.List;

public class BillingFrame extends JFrame {

    private JComboBox<FoodItem> foodBox;
    private JSpinner quantitySpinner;
    private JPanel menuCardsPanel;
    private JTable billTable;
    private DefaultTableModel model;
    private JLabel totalLabel;
    private JTextField discountField;
    private JComboBox<String> paymentBox;

    private final List<FoodItem> billItems = new ArrayList<>();
    private final List<Integer> quantities = new ArrayList<>();
    private final FoodItemDAO foodDAO = new FoodItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final String username;
    private final double gstRate;
    private boolean saving = false;

    public BillingFrame(String username) {
        this.username = username;
        gstRate = OrderDAO.getGstRate();

        AppStyle.apply();
        AppStyle.setWindowIcon(this);

        setTitle("PIET Canteen | Point of Sale");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(AppStyle.PALE);
        main.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        titles.add(AppStyle.heading("Create a new bill"));

        JLabel sub = new JLabel(
            "Select menu items, confirm payment and print the receipt"
        );
        sub.setForeground(AppStyle.MUTED);
        titles.add(sub);

        header.add(titles, BorderLayout.WEST);

        JLabel cashier = new JLabel("Cashier: " + username);
        cashier.setForeground(AppStyle.MUTED);
        header.add(cashier, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);

        JPanel addPanel = AppStyle.card();
        addPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        foodBox = new JComboBox<>();
        foodBox.setPreferredSize(new Dimension(300, 34));

        quantitySpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, 100, 1)
        );

        JButton add = AppStyle.button("+ Add item", AppStyle.BLUE);

        addPanel.add(new JLabel("Menu item"));
        addPanel.add(foodBox);
        addPanel.add(new JLabel("Qty"));
        addPanel.add(quantitySpinner);
        addPanel.add(add);

        menuCardsPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        menuCardsPanel.setBackground(AppStyle.PALE);

        JScrollPane menuScroll = new JScrollPane(menuCardsPanel);
        menuScroll.setPreferredSize(new Dimension(600, 190));
        menuScroll.setBorder(BorderFactory.createTitledBorder(
            "Available food items — choose Add to cart"
        ));

        JPanel menuArea = new JPanel(new BorderLayout(6, 6));
        menuArea.setOpaque(false);
        menuArea.add(addPanel, BorderLayout.NORTH);
        menuArea.add(menuScroll, BorderLayout.CENTER);
        center.add(menuArea, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{
                "Item Name", "Quantity",
                "Rate per Unit (₹)", "Total Rate (₹)"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 1 ? Integer.class : String.class;
            }
        };

        billTable = new JTable(model);
        AppStyle.styleTable(billTable);

        billTable.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE
                    && e.getColumn() == 1) {
                int row = e.getFirstRow();

                if (row >= 0 && row < quantities.size()) {
                    try {
                        int q = Integer.parseInt(
                            model.getValueAt(row, 1).toString()
                        );

                        FoodItem f = billItems.get(row);

                        if (q < 1 || q > f.getStockQuantity()) {
                            throw new NumberFormatException();
                        }

                        quantities.set(row, q);
                        refreshTable(false);

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Quantity must be between 1 and current stock."
                        );
                        refreshTable(false);
                    }
                }
            }
        });

        center.add(new JScrollPane(billTable), BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);

        JPanel bottom = AppStyle.card();
        bottom.setLayout(new BorderLayout(10, 8));

        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        options.setOpaque(false);

        discountField = new JTextField("0", 5);
        paymentBox = new JComboBox<>(new String[]{"Cash", "UPI", "Card"});

        JButton remove = AppStyle.button(
            "Remove selected", new Color(183, 58, 68)
        );
        JButton clear = AppStyle.button(
            "Clear bill", new Color(105, 116, 134)
        );

        options.add(new JLabel("Discount (%)"));
        options.add(discountField);
        options.add(new JLabel("Payment method"));
        options.add(paymentBox);
        options.add(remove);
        options.add(clear);
        bottom.add(options, BorderLayout.NORTH);

        totalLabel = new JLabel(
            "Subtotal ₹0.00   |   Discount ₹0.00   |   GST ("
            + gstRate + "%) ₹0.00   |   Payable ₹0.00"
        );
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(AppStyle.NAVY);
        bottom.add(totalLabel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);

        JButton back = AppStyle.button("Back", AppStyle.NAVY);
        JButton complete = AppStyle.button(
            "Complete payment", new Color(22, 145, 105)
        );

        actions.add(back);
        actions.add(complete);
        bottom.add(actions, BorderLayout.SOUTH);
        main.add(bottom, BorderLayout.SOUTH);
        add(main);

        add.addActionListener(e -> addItem());
        remove.addActionListener(e -> removeItem());
        clear.addActionListener(e -> clearBill());
        back.addActionListener(e -> dispose());
        complete.addActionListener(e -> completePayment(complete));

        discountField.addActionListener(e -> calculateTotal());
        discountField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                calculateTotal();
            }
        });

        loadFoodItems();
        calculateTotal();
    }

    // Loads images from database paths or packaged project resources.
    private ImageIcon loadFoodImage(FoodItem item) {
        String path = item.getImagePath();

        if (path != null && !path.trim().isEmpty()) {
            path = path.trim();

            // Try a full local file path.
            java.io.File file = new java.io.File(path);

            if (file.isFile()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());

                if (icon.getIconWidth() > 0) {
                    return icon;
                }
            }

            // Try a packaged resource path.
            String resourcePath = path.replace("\\", "/");

            if (resourcePath.startsWith("src/")) {
                resourcePath = resourcePath.substring(4);
            }

            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }

            if (!resourcePath.startsWith("/canteen/")) {
                resourcePath = "/canteen/" +
                    resourcePath.substring(1);
            }

            java.net.URL url =
                BillingFrame.class.getResource(resourcePath);

            if (url != null) {
                ImageIcon icon = new ImageIcon(url);

                if (icon.getIconWidth() > 0) {
                    return icon;
                }
            }

            // If the database stores only a filename.
            String filename = new java.io.File(path).getName();

            url = BillingFrame.class.getResource(
                "/canteen/food_images/" + filename
            );

            if (url != null) {
                ImageIcon icon = new ImageIcon(url);

                if (icon.getIconWidth() > 0) {
                    return icon;
                }
            }
        }

        // Fallback mappings for food names and available image files.
        String name = item.getItemName()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_|_$", "");

        Map<String, String> images = new HashMap<>();

        images.put("samosa", "samosa.jpg");
        images.put("veg_sandwich", "paneer_sandwich.jpg");
        images.put("paneer_sandwich", "paneer_sandwich.jpg");
        images.put("masala_dosa", "masala_dosa.jpg");
        images.put("veg_burger", "veg_burger.jpg");
        images.put("french_fries", "french_fries.jpg");
        images.put("cold_coffee", "cold_coffee.jpg");
        images.put("veg_thali", "veg_thali.jpg");
        images.put("masala_maggi", "masala_maggi.jpg");
        images.put("veg_noodles", "veg_noodles.jpg");
        images.put("veg_pizza", "veg_pizza.jpg");
        images.put("gulab_jamun", "gulab_jamun.jpg");
        images.put("paneer_roll", "paneer_roll.jpg");

        String filename = images.get(name);

        if (filename == null) {
            filename = name + ".jpg";
        }

        java.net.URL url = BillingFrame.class.getResource(
            "/canteen/food_images/" + filename
        );

        if (url != null) {
            ImageIcon icon = new ImageIcon(url);

            if (icon.getIconWidth() > 0) {
                return icon;
            }
        }

        return null;
    }

    private void loadFoodItems() {
        foodBox.removeAllItems();
        menuCardsPanel.removeAll();

        try {
            for (FoodItem item : foodDAO.getAvailableItems()) {
                if (item.getStockQuantity() <= 0) {
                    continue;
                }

                foodBox.addItem(item);

                JPanel card = AppStyle.card();
                card.setLayout(new BorderLayout(4, 4));

                JLabel image = new JLabel(
                    "Image unavailable", SwingConstants.CENTER
                );
                image.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                image.setPreferredSize(new Dimension(150, 88));

                ImageIcon icon = loadFoodImage(item);

                if (icon != null) {
                    Image scaled = icon.getImage().getScaledInstance(
                        150, 88, Image.SCALE_SMOOTH
                    );

                    image.setText("");
                    image.setIcon(new ImageIcon(scaled));
                }

                JPanel details = new JPanel(new GridLayout(0, 1, 2, 2));
                details.setOpaque(false);

                JLabel name = new JLabel(item.getItemName());
                name.setFont(new Font("Segoe UI", Font.BOLD, 12));

                JLabel info = new JLabel(
                    item.getCategory() + "  •  ₹"
                    + String.format("%.2f", item.getPrice())
                );
                info.setForeground(AppStyle.MUTED);

                JLabel stock = new JLabel(
                    "Stock: " + item.getStockQuantity()
                );
                stock.setForeground(
                    item.getStockQuantity() <= 10
                        ? new Color(190, 100, 25)
                        : new Color(35, 135, 90)
                );

                details.add(name);
                details.add(info);
                details.add(stock);

                JButton choose = AppStyle.button(
                    "Add to cart", AppStyle.BLUE
                );

                choose.addActionListener(e -> {
                    foodBox.setSelectedItem(item);
                    addItem();
                });

                card.add(image, BorderLayout.NORTH);
                card.add(details, BorderLayout.CENTER);
                card.add(choose, BorderLayout.SOUTH);

                menuCardsPanel.add(card);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Could not load food menu: " + e.getMessage()
            );
        }

        menuCardsPanel.revalidate();
        menuCardsPanel.repaint();
    }

    private void addItem() {
        FoodItem item = (FoodItem) foodBox.getSelectedItem();

        if (item == null) {
            JOptionPane.showMessageDialog(
                this, "No available food items. Check menu stock."
            );
            return;
        }

        int q = (Integer) quantitySpinner.getValue();
        int idx = -1;

        for (int i = 0; i < billItems.size(); i++) {
            if (billItems.get(i).getItemId() == item.getItemId()) {
                idx = i;
                break;
            }
        }

        if (idx >= 0) {
            if (quantities.get(idx) + q > item.getStockQuantity()) {
                JOptionPane.showMessageDialog(
                    this, "Not enough stock available."
                );
                return;
            }

            quantities.set(idx, quantities.get(idx) + q);
        } else {
            if (q > item.getStockQuantity()) {
                JOptionPane.showMessageDialog(
                    this, "Not enough stock available."
                );
                return;
            }

            billItems.add(item);
            quantities.add(q);
        }

        refreshTable(true);
    }

    private void refreshTable(boolean calculate) {
        model.setRowCount(0);

        for (int i = 0; i < billItems.size(); i++) {
            FoodItem f = billItems.get(i);
            int q = quantities.get(i);

            model.addRow(new Object[]{
                f.getItemName(),
                q,
                String.format("%.2f", f.getPrice()),
                String.format("%.2f", f.getPrice() * q)
            });
        }

        if (calculate) {
            calculateTotal();
        }
    }

    private double discountPercent() {
        double d = Double.parseDouble(discountField.getText().trim());

        if (!Double.isFinite(d) || d < 0 || d > 100) {
            throw new IllegalArgumentException();
        }

        return Math.round(d * 100.0) / 100.0;
    }

    private double[] totals() {
        BigDecimal sub = BigDecimal.ZERO;

        for (int i = 0; i < billItems.size(); i++) {
            sub = sub.add(
                BigDecimal.valueOf(billItems.get(i).getPrice())
                    .multiply(BigDecimal.valueOf(quantities.get(i)))
            );
        }

        sub = sub.setScale(2, RoundingMode.HALF_UP);

        BigDecimal d = sub.multiply(
            BigDecimal.valueOf(discountPercent())
        ).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal taxable = sub.subtract(d);

        BigDecimal gst = taxable.multiply(
            BigDecimal.valueOf(gstRate)
        ).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return new double[]{
            sub.doubleValue(),
            d.doubleValue(),
            gst.doubleValue(),
            taxable.add(gst).setScale(2, RoundingMode.HALF_UP)
                .doubleValue()
        };
    }

    private void calculateTotal() {
        try {
            double[] t = totals();

            totalLabel.setText(String.format(
                "Subtotal ₹%.2f   |   Discount ₹%.2f   |   "
                + "GST (%.2f%%) ₹%.2f   |   Payable ₹%.2f",
                t[0], t[1], gstRate, t[2], t[3]
            ));

        } catch (Exception e) {
            totalLabel.setText(
                "Enter a valid discount percentage between 0 and 100."
            );
        }
    }

    private void completePayment(JButton button) {
        if (saving) {
            return;
        }

        if (billItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add food items first.");
            return;
        }

        final double[] t;

        try {
            t = totals();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this, "Discount must be between 0 and 100."
            );
            return;
        }

        String method = (String) paymentBox.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            String.format(
                "Confirm payment?\n\nSubtotal: ₹%.2f\nDiscount: ₹%.2f"
                + "\nGST: ₹%.2f\nFINAL PAYABLE: ₹%.2f"
                + "\nPayment method: %s\n\n"
                + "Complete and save this order?",
                t[0], t[1], t[2], t[3], method
            ),
            "Confirm payment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        saving = true;
        button.setEnabled(false);

        try {
            int id = orderDAO.saveOrder(
                username, billItems, quantities,
                discountPercent(), gstRate, method
            );

            showReceipt(id);
            clearBill();
            loadFoodItems();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Bill was not completed. No partial stock/order changes "
                + "were saved.\n\n" + e.getMessage(),
                "Billing error",
                JOptionPane.ERROR_MESSAGE
            );
            loadFoodItems();

        } finally {
            saving = false;
            button.setEnabled(true);
        }
    }

    private void showReceipt(int id) {
        StringBuilder b = new StringBuilder(
            "PIET CANTEEN\n"
            + "Poornima Institute of Engineering & Technology\n"
            + "----------------------------------------\n"
            + "Receipt / Order #"
        ).append(id)
         .append("\nCashier: ").append(username)
         .append("\nDate: ").append(new java.util.Date())
         .append("\n\n");

        try (
            Connection c = DBConnection.getConnection();
            PreparedStatement p = c.prepareStatement(
                "SELECT subtotal,discount,gst,final_total,payment_method,"
                + "discount_percent,gst_rate FROM orders WHERE order_id=?"
            )
        ) {
            p.setInt(1, id);

            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    try (PreparedStatement q = c.prepareStatement(
                        "SELECT item_name,quantity,unit_price,line_total "
                        + "FROM order_items WHERE order_id=?"
                    )) {
                        q.setInt(1, id);

                        try (ResultSet items = q.executeQuery()) {
                            while (items.next()) {
                                b.append(String.format(
                                    "%-20s %2d x %7.2f = %8.2f%n",
                                    items.getString(1),
                                    items.getInt(2),
                                    items.getDouble(3),
                                    items.getDouble(4)
                                ));
                            }
                        }
                    }

                    b.append("\n----------------------------------------\n");
                    b.append(String.format(
                        "Subtotal:                 ₹%8.2f%n",
                        r.getDouble("subtotal")
                    ));
                    b.append(String.format(
                        "Discount (%.2f%%):        -₹%8.2f%n",
                        r.getDouble("discount_percent"),
                        r.getDouble("discount")
                    ));
                    b.append(String.format(
                        "GST (%.2f%%):              ₹%8.2f%n",
                        r.getDouble("gst_rate"), r.getDouble("gst")
                    ));
                    b.append(String.format(
                        "TOTAL PAID:                ₹%8.2f%n",
                        r.getDouble("final_total")
                    ));
                    b.append("Payment: ")
                     .append(r.getString("payment_method"))
                     .append("\nStatus: PAID / COMPLETED\n\n"
                         + "Thank you! Visit again.\n"
                         + "Designed & Developed by Kevaram");
                }
            }

        } catch (SQLException e) {
            b.append("\nReceipt details could not be loaded: ")
             .append(e.getMessage());
        }

        JTextArea area = new JTextArea(b.toString(), 25, 48);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JScrollPane scroll = new JScrollPane(area);
        JButton print = AppStyle.button("Print receipt", AppStyle.BLUE);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(print, BorderLayout.SOUTH);

        JDialog d = new JDialog(
            this, "Payment complete • Order #" + id, true
        );
        d.getContentPane().add(panel);
        d.pack();
        d.setLocationRelativeTo(this);

        print.addActionListener(e -> {
            try {
                area.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(
                    d, "Could not print receipt: " + ex.getMessage()
                );
            }
        });

        d.setVisible(true);
    }

    private void removeItem() {
        int row = billTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                this, "Select an item to remove."
            );
            return;
        }

        row = billTable.convertRowIndexToModel(row);
        billItems.remove(row);
        quantities.remove(row);
        refreshTable(true);
    }

    private void clearBill() {
        billItems.clear();
        quantities.clear();
        discountField.setText("0");
        quantitySpinner.setValue(1);
        refreshTable(true);
    }
}
