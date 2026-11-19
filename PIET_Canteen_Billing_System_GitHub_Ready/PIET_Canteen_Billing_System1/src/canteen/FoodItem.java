
package canteen;

public class FoodItem {

    private int itemId;
    private String itemName;
    private String category;
    private double price;
    private boolean available;
    private int stockQuantity;
    private String imagePath;

    // Existing constructor: keeps older code compatible
    public FoodItem(int itemId, String itemName, String category,
                    double price, boolean available, int stockQuantity) {
        this(itemId, itemName, category, price, available,
             stockQuantity, null);
    }

    // Constructor with image path
    public FoodItem(int itemId, String itemName, String category,
                    double price, boolean available, int stockQuantity,
                    String imagePath) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.price = price;
        this.available = available;
        this.stockQuantity = stockQuantity;
        this.imagePath = imagePath;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return itemName + " - Rs. " + price;
    }
}
