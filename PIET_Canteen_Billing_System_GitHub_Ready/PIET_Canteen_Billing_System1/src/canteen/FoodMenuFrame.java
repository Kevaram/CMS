
package canteen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

public class FoodMenuFrame extends JFrame {

    private JTextField nameField, categoryField, priceField;
    private JTextField stockField, searchField;
    private JTable table;
    private DefaultTableModel model;
    private final FoodItemDAO dao = new FoodItemDAO();

    private JLabel imagePreview;
    private String selectedImagePath;
    private String currentRole = "ADMIN";
    private String currentUser = "system";

    private final Color NAVY = new Color(20, 43, 88);
    private final Color BLUE = new Color(38, 110, 220);
    private final Color PALE = new Color(243, 247, 253);
    private final Color MUTED = new Color(105, 119, 141);

    public FoodMenuFrame() {
        this("ADMIN", "system");
    }

    public FoodMenuFrame(String role, String username) {
        currentRole = role == null ? "STAFF" : role;
        currentUser = username == null ? "system" : username;

        if (!AuditLog.isAdmin(currentRole)) {
            JOptionPane.showMessageDialog(
                null, "Only administrators can manage the food menu."
            );
            dispose();
            return;
        }

        AppStyle.apply();
        AppStyle.setWindowIcon(this);

        setTitle("PIET Canteen | Food Menu Management");
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(PALE);

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 5));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("PIET CANTEEN");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 13));
        brand.setForeground(new Color(160, 197, 255));

        JLabel title = new JLabel("Food Menu Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
            "Manage food items, prices, images and stock availability"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(220, 230, 245));

        heading.add(brand);
        heading.add(Box.createVerticalStrut(4));
        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitle);

        JButton back = button("← Dashboard", NAVY);
        back.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(110, 145, 195)),
            new EmptyBorder(9, 12, 9, 12)
        ));
        back.addActionListener(e -> dispose());

        header.add(heading, BorderLayout.WEST);
        header.add(back, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Main workspace
        JPanel workspace = new JPanel(new BorderLayout(14, 14));
        workspace.setOpaque(false);
        workspace.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Table
        model = new DefaultTableModel(
            new String[]{
                "ID", "Food Name", "Category", "Price (₹)",
                "Available", "Stock", "Image Path"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        AppStyle.styleTable(table);

        // Keep image path in the model but hide it from the table.
        table.removeColumn(table.getColumnModel().getColumn(6));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Color.WHITE);

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout(8, 12));

        JLabel listTitle = new JLabel("Food Items");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        listTitle.setForeground(NAVY);

        JLabel listSubtitle = new JLabel(
            "Select an item to edit its details or image."
        );
        listSubtitle.setForeground(MUTED);

        JPanel tableHeading = new JPanel(new GridLayout(2, 1, 0, 4));
        tableHeading.setOpaque(false);
        tableHeading.add(listTitle);
        tableHeading.add(listSubtitle);

        tableCard.add(tableHeading, BorderLayout.NORTH);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // Editor
        JPanel editor = card();
        editor.setLayout(new BorderLayout(10, 12));
        editor.setPreferredSize(new Dimension(330, 0));

        JLabel editorTitle = new JLabel("Food Item Details");
        editorTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        editorTitle.setForeground(NAVY);
        editor.add(editorTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.setOpaque(false);

        nameField = new JTextField();
        categoryField = new JTextField();
        priceField = new JTextField();
        stockField = new JTextField();

        addField(form, "Food name", nameField);
        addField(form, "Category", categoryField);
        addField(form, "Price (₹)", priceField);
        addField(form, "Stock quantity", stockField);

        imagePreview = new JLabel(
            "Choose a food image", SwingConstants.CENTER
        );
        imagePreview.setPreferredSize(new Dimension(180, 115));
        imagePreview.setMinimumSize(new Dimension(180, 115));
        imagePreview.setOpaque(true);
        imagePreview.setBackground(new Color(246, 249, 255));
        imagePreview.setForeground(MUTED);
        imagePreview.setBorder(BorderFactory.createLineBorder(
            new Color(215, 224, 238)
        ));

        JButton chooseImage = button("Choose Image", BLUE);
        JButton removeImage = button(
            "Remove Image", new Color(105, 116, 134)
        );

        chooseImage.addActionListener(e -> chooseImage());

        removeImage.addActionListener(e -> {
            selectedImagePath = null;
            showDefaultFoodImage(nameField.getText());
        });

JPanel imageActions = new JPanel(new GridLayout(1, 2, 7, 0));
        imageActions.setOpaque(false);
        imageActions.add(chooseImage);
        imageActions.add(removeImage);

        JPanel imageSection = new JPanel(new BorderLayout(7, 7));
        imageSection.setOpaque(false);

        JLabel imageTitle = new JLabel("Food Image");
        imageTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        imageTitle.setForeground(NAVY);

        imageSection.add(imageTitle, BorderLayout.NORTH);
        imageSection.add(imagePreview, BorderLayout.CENTER);
        imageSection.add(imageActions, BorderLayout.SOUTH);

        JPanel editorBody = new JPanel(new BorderLayout(0, 14));
        editorBody.setOpaque(false);
        editorBody.add(form, BorderLayout.NORTH);
        editorBody.add(imageSection, BorderLayout.CENTER);

        editor.add(editorBody, BorderLayout.CENTER);

        JPanel editorButtons = new JPanel(new GridLayout(3, 2, 7, 7));
        editorButtons.setOpaque(false);

        JButton add = button("ADD FOOD", new Color(22, 145, 90));
        JButton update = button("UPDATE", BLUE);
        JButton delete = button("DELETE", new Color(190, 45, 55));
        JButton clear = button("CLEAR", new Color(105, 116, 134));
        JButton search = button("SEARCH", new Color(125, 75, 180));
        JButton showAll = button("SHOW ALL", new Color(30, 140, 150));

        searchField = new JTextField();
        searchField.setToolTipText("Enter food name or category");

        add.addActionListener(e -> addFood());
        update.addActionListener(e -> updateFood());
        delete.addActionListener(e -> deleteFood());
        clear.addActionListener(e -> clearFields());
        search.addActionListener(e -> searchFood());
        showAll.addActionListener(e -> loadFoods());

        editorButtons.add(add);
        editorButtons.add(update);
        editorButtons.add(delete);
        editorButtons.add(clear);
        editorButtons.add(search);
        editorButtons.add(showAll);

        JPanel editorBottom = new JPanel(new BorderLayout(6, 8));
        editorBottom.setOpaque(false);
        editorBottom.add(new JLabel("Find a food item"), BorderLayout.NORTH);
        editorBottom.add(searchField, BorderLayout.CENTER);
        editorBottom.add(editorButtons, BorderLayout.SOUTH);

        editor.add(editorBottom, BorderLayout.SOUTH);

        workspace.add(tableCard, BorderLayout.CENTER);
        workspace.add(editor, BorderLayout.EAST);

        root.add(workspace, BorderLayout.CENTER);

        JLabel footer = new JLabel(
            "Designed & Developed by Kevaram  •  PIET Canteen Billing System"
        );
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(MUTED);
        footer.setBorder(new EmptyBorder(0, 20, 12, 20));
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.convertRowIndexToModel(
                    table.getSelectedRow()
                );

                nameField.setText(model.getValueAt(row, 1).toString());
                categoryField.setText(model.getValueAt(row, 2).toString());
                priceField.setText(model.getValueAt(row, 3).toString());
                stockField.setText(model.getValueAt(row, 5).toString());

                Object path = model.getValueAt(row, 6);
                selectedImagePath = path == null ? null : path.toString();

                if (selectedImagePath != null
                        && !selectedImagePath.trim().isEmpty()
                        && showImage(selectedImagePath)) {
                    // Saved image loaded successfully.
                } else {
                    showDefaultFoodImage(nameField.getText());
                }
            }
        });

        loadFoods();
    }

    private JPanel card() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 233, 243)),
            new EmptyBorder(14, 14, 14, 14)
        ));
        return panel;
    }

    private void addField(JPanel panel, String title, JTextField field) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(NAVY);

        field.setPreferredSize(new Dimension(200, 32));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(label);
        panel.add(field);
    }

    private JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(110, 36));
        return b;
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Food Image");
        chooser.setFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files (JPG, PNG, GIF)",
                "jpg", "jpeg", "png", "gif"
            )
        );

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File source = chooser.getSelectedFile();

        try {
            Path folder = Paths.get("food_images").toAbsolutePath();
            Files.createDirectories(folder);

            String filename = System.currentTimeMillis() + "_"
                + source.getName().replaceAll(
                    "[^a-zA-Z0-9._-]", "_"
                );

            Path destination = folder.resolve(filename);

            Files.copy(
                source.toPath(), destination,
                StandardCopyOption.REPLACE_EXISTING
            );

            selectedImagePath = destination.toString();
            showImage(selectedImagePath);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this, "Could not save image: " + ex.getMessage()
            );
        }
    }

    private String defaultFilename(String itemName) {
        String key = itemName.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_|_$", "");

        if (key.contains("samosa")) return "samosa.jpg";
        if (key.contains("sandwich")) return "paneer_sandwich.jpg";
        if (key.contains("dosa")) return "masala_dosa.jpg";
        if (key.contains("burger")) return "veg_burger.jpg";
        if (key.contains("fries")) return "french_fries.jpg";
        if (key.contains("coffee")) return "cold_coffee.jpg";
        if (key.contains("thali")) return "veg_thali.jpg";
        if (key.contains("maggi")) return "masala_maggi.jpg";
        if (key.contains("noodles")) return "veg_noodles.jpg";
        if (key.contains("pizza")) return "veg_pizza.jpg";
        if (key.contains("gulab") || key.contains("jamun")) {
            return "gulab_jamun.jpg";
        }
        if (key.contains("roll")) return "paneer_roll.jpg";

        return key + ".jpg";
    }

    private boolean showDefaultFoodImage(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            showPlaceholder("Choose a food image");
            return false;
        }

        String filename = defaultFilename(itemName);

        java.net.URL resource = FoodMenuFrame.class.getResource(
            "/canteen/food_images/" + filename
        );

        if (resource == null) {
            showPlaceholder("Image unavailable");
            return false;
        }

        ImageIcon icon = new ImageIcon(resource);

        if (icon.getIconWidth() <= 0) {
            showPlaceholder("Image unavailable");
            return false;
        }

        setPreview(icon);
        return true;
    }

    private boolean showImage(String path) {
        if (path == null || path.trim().isEmpty()) {
            showPlaceholder("Choose a food image");
            return false;
        }

        String cleanPath = path.trim();
        File file = new File(cleanPath);

        if (file.isFile()) {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());

            if (icon.getIconWidth() > 0) {
                setPreview(icon);
                return true;
            }
        }

        String resourcePath = cleanPath.replace("\\", "/");

        if (resourcePath.startsWith("src/")) {
            resourcePath = resourcePath.substring(4);
        }

        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }

        java.net.URL url = FoodMenuFrame.class.getResource(resourcePath);

        if (url == null && !resourcePath.startsWith("/canteen/")) {
            url = FoodMenuFrame.class.getResource(
                "/canteen/" + resourcePath.substring(1)
            );
        }

        if (url == null) {
            String filename = new File(cleanPath).getName();
            url = FoodMenuFrame.class.getResource(
                "/canteen/food_images/" + filename
            );
        }

        if (url != null) {
            ImageIcon icon = new ImageIcon(url);

            if (icon.getIconWidth() > 0) {
                setPreview(icon);
                return true;
            }
        }

        showPlaceholder("Image unavailable");
        return false;
    }

    private void setPreview(ImageIcon icon) {
        Image scaled = icon.getImage().getScaledInstance(
            180, 115, Image.SCALE_SMOOTH
        );

        imagePreview.setText("");
        imagePreview.setIcon(new ImageIcon(scaled));
    }

    private void showPlaceholder(String text) {
        imagePreview.setIcon(null);
        imagePreview.setText(text);
    }

    private void loadFoods() {
        model.setRowCount(0);

        try {
            List<FoodItem> foods = dao.getAllItems();

            for (FoodItem food : foods) {
                model.addRow(new Object[]{
                    food.getItemId(),
                    food.getItemName(),
                    food.getCategory(),
                    food.getPrice(),
                    food.isAvailable(),
                    food.getStockQuantity(),
                    food.getImagePath()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Could not load food items:\n" + ex.getMessage()
            );
        }
    }

    private void addFood() {
        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());

            if (name.isEmpty() || category.isEmpty()
                    || price <= 0 || stock < 0) {
                JOptionPane.showMessageDialog(
                    this, "Enter valid food details."
                );
                return;
            }

            FoodItem food = new FoodItem(
                0, name, category, price, stock > 0,
                stock, selectedImagePath
            );

            if (dao.addItem(food)) {
                AuditLog.record(
                    currentUser, "FOOD_ADDED",
                    name + " | stock=" + stock
                );
                JOptionPane.showMessageDialog(
                    this, "Food added successfully!"
                );
                clearFields();
                loadFoods();
            } else {
                JOptionPane.showMessageDialog(
                    this, "Could not add food. Check the database."
                );
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this, "Enter a valid price and stock."
            );
        }
    }

    private void updateFood() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                this, "Select a food item first."
            );
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);

        try {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());

            if (name.isEmpty() || category.isEmpty()
                    || price <= 0 || stock < 0) {
                JOptionPane.showMessageDialog(
                    this, "Enter valid food details."
                );
                return;
            }

            FoodItem food = new FoodItem(
                id, name, category, price, stock > 0,
                stock, selectedImagePath
            );

            if (dao.updateItem(food)) {
                AuditLog.record(
                    currentUser, "FOOD_UPDATED",
                    name + " | stock=" + stock
                );
                JOptionPane.showMessageDialog(
                    this, "Food updated successfully!"
                );
                clearFields();
                loadFoods();
            } else {
                JOptionPane.showMessageDialog(
                    this, "Update failed. Check the database."
                );
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this, "Enter a valid price and stock."
            );
        }
    }

    private void deleteFood() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                this, "Select a food item first."
            );
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String name = model.getValueAt(row, 1).toString();

        int choice = JOptionPane.showConfirmDialog(
            this, "Delete " + name + "?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (dao.deleteItem(id)) {
                AuditLog.record(
                    currentUser, "FOOD_DELETED", "Food item ID " + id
                );
                JOptionPane.showMessageDialog(
                    this, "Food deleted successfully!"
                );
                clearFields();
                loadFoods();
            } else {
                JOptionPane.showMessageDialog(
                    this, "Delete failed. Check the database."
                );
            }
        }
    }

    private void searchFood() {
        String keyword = searchField.getText().trim();
        model.setRowCount(0);

        try {
            for (FoodItem food : dao.searchItems(keyword)) {
                model.addRow(new Object[]{
                    food.getItemId(),
                    food.getItemName(),
                    food.getCategory(),
                    food.getPrice(),
                    food.isAvailable(),
                    food.getStockQuantity(),
                    food.getImagePath()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Search failed:\n" + ex.getMessage()
            );
        }
    }

    private void clearFields() {
        nameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        stockField.setText("");
        searchField.setText("");
        selectedImagePath = null;

        showPlaceholder("Choose a food image");
        table.clearSelection();
    }
}
