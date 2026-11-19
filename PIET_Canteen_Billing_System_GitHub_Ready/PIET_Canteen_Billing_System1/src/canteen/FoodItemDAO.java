
package canteen;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDAO {

    public List<FoodItem> getAllItems() {
        List<FoodItem> foods = new ArrayList<>();

        String sql = "SELECT * FROM food_items ORDER BY item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                foods.add(readFood(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foods;
    }

    public List<FoodItem> getAvailableItems() {
        List<FoodItem> foods = new ArrayList<>();

        String sql = "SELECT * FROM food_items "
                   + "WHERE available = TRUE AND stock_quantity > 0 "
                   + "ORDER BY item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                foods.add(readFood(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foods;
    }

    public List<FoodItem> searchItems(String keyword) {
        List<FoodItem> foods = new ArrayList<>();

        String sql = "SELECT * FROM food_items "
                   + "WHERE item_name LIKE ? OR category LIKE ? "
                   + "ORDER BY item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    foods.add(readFood(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foods;
    }

    public boolean addItem(FoodItem item) {
        String sql = "INSERT INTO food_items "
                   + "(item_name, category, price, available, "
                   + "stock_quantity, image_path) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setDouble(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.setInt(5, item.getStockQuantity());
            ps.setString(6, item.getImagePath());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateItem(FoodItem item) {
        String sql = "UPDATE food_items SET item_name = ?, "
                   + "category = ?, price = ?, available = ?, "
                   + "stock_quantity = ?, image_path = ? "
                   + "WHERE item_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setDouble(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.setInt(5, item.getStockQuantity());
            ps.setString(6, item.getImagePath());
            ps.setInt(7, item.getItemId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItem(int itemId) {
        String sql = "UPDATE food_items SET available = FALSE WHERE item_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStock(int itemId, int newQuantity) {
        String sql = "UPDATE food_items SET stock_quantity = ?, "
                   + "available = ? WHERE item_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, newQuantity);
            ps.setBoolean(2, newQuantity > 0);
            ps.setInt(3, itemId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private FoodItem readFood(ResultSet rs) throws SQLException {
        return new FoodItem(
            rs.getInt("item_id"),
            rs.getString("item_name"),
            rs.getString("category"),
            rs.getDouble("price"),
            rs.getBoolean("available"),
            rs.getInt("stock_quantity"),
            rs.getString("image_path")
        );
    }
}
