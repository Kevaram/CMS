package canteen;

import java.sql.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    public int saveOrder(String username, List<FoodItem> items, List<Integer> quantities,
                         double discountPercent, double gstPercent, String paymentMethod) throws Exception {
        if(items==null||quantities==null||items.isEmpty()||items.size()!=quantities.size()) throw new IllegalArgumentException("Add at least one valid item.");
        if(discountPercent<0||discountPercent>100||gstPercent<0||gstPercent>100) throw new IllegalArgumentException("Invalid discount or GST rate.");
        if(!List.of("Cash","UPI","Card").contains(paymentMethod)) throw new IllegalArgumentException("Invalid payment method.");
        try(Connection con=DBConnection.getConnection()){
            con.setAutoCommit(false);
            try{
                BigDecimal subtotal=BigDecimal.ZERO;List<String> names=new ArrayList<>();List<BigDecimal> prices=new ArrayList<>();
                for(int i=0;i<items.size();i++){
                    int qty=quantities.get(i);if(qty<=0)throw new SQLException("Quantity must be greater than zero.");
                    try(PreparedStatement ps=con.prepareStatement("SELECT item_name,price,available,stock_quantity FROM food_items WHERE item_id=? FOR UPDATE")){
                        ps.setInt(1,items.get(i).getItemId());try(ResultSet rs=ps.executeQuery()){
                            if(!rs.next())throw new SQLException("Food item not found.");String name=rs.getString("item_name");int stock=rs.getInt("stock_quantity");
                            if(!rs.getBoolean("available")||stock<qty)throw new SQLException("Insufficient stock for "+name+" (available: "+stock+").");
                            BigDecimal price=rs.getBigDecimal("price").setScale(2,RoundingMode.HALF_UP);names.add(name);prices.add(price);subtotal=subtotal.add(price.multiply(BigDecimal.valueOf(qty)));
                        }
                    }
                }
                subtotal=subtotal.setScale(2,RoundingMode.HALF_UP);BigDecimal discount=subtotal.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);BigDecimal taxable=subtotal.subtract(discount);BigDecimal gst=taxable.multiply(BigDecimal.valueOf(gstPercent)).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);BigDecimal total=taxable.add(gst).setScale(2,RoundingMode.HALF_UP);
                int orderId;
                try(PreparedStatement ps=con.prepareStatement("INSERT INTO orders(username,subtotal,discount,gst,final_total,payment_method,status,payment_status,gst_rate,discount_percent) VALUES(?,?,?,?,?,?,'COMPLETED','PAID',?,?)",Statement.RETURN_GENERATED_KEYS)){
                    ps.setString(1,username);ps.setBigDecimal(2,subtotal);ps.setBigDecimal(3,discount);ps.setBigDecimal(4,gst);ps.setBigDecimal(5,total);ps.setString(6,paymentMethod);ps.setBigDecimal(7,BigDecimal.valueOf(gstPercent));ps.setBigDecimal(8,BigDecimal.valueOf(discountPercent));ps.executeUpdate();try(ResultSet keys=ps.getGeneratedKeys()){if(!keys.next())throw new SQLException("Order ID was not generated.");orderId=keys.getInt(1);}
                }
                String itemSql="INSERT INTO order_items(order_id,item_id,item_name,quantity,unit_price,line_total) VALUES(?,?,?,?,?,?)";
                for(int i=0;i<items.size();i++){
                    int itemId=items.get(i).getItemId(),qty=quantities.get(i);BigDecimal price=prices.get(i),line=price.multiply(BigDecimal.valueOf(qty)).setScale(2,RoundingMode.HALF_UP);
                    try(PreparedStatement ps=con.prepareStatement(itemSql)){ps.setInt(1,orderId);ps.setInt(2,itemId);ps.setString(3,names.get(i));ps.setInt(4,qty);ps.setBigDecimal(5,price);ps.setBigDecimal(6,line);ps.executeUpdate();}
                    int before;try(PreparedStatement ps=con.prepareStatement("SELECT stock_quantity FROM food_items WHERE item_id=? FOR UPDATE")){ps.setInt(1,itemId);try(ResultSet rs=ps.executeQuery()){rs.next();before=rs.getInt(1);}}
                    try(PreparedStatement ps=con.prepareStatement("UPDATE food_items SET stock_quantity=stock_quantity-?,available=(stock_quantity-?>0) WHERE item_id=? AND stock_quantity>=?")){ps.setInt(1,qty);ps.setInt(2,qty);ps.setInt(3,itemId);ps.setInt(4,qty);if(ps.executeUpdate()!=1)throw new SQLException("Stock changed while processing bill. Please try again.");}
                    int after=before-qty;try(PreparedStatement ps=con.prepareStatement("INSERT INTO stock_movements(item_id,previous_quantity,new_quantity,change_quantity,reason,username,created_at) VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP)")){ps.setInt(1,itemId);ps.setInt(2,before);ps.setInt(3,after);ps.setInt(4,-qty);ps.setString(5,"Sale order #"+orderId);ps.setString(6,username);ps.executeUpdate();}
                    int threshold=10;try(PreparedStatement ps=con.prepareStatement("SELECT COALESCE(min_stock_threshold,10) FROM food_items WHERE item_id=?")){ps.setInt(1,itemId);try(ResultSet rs=ps.executeQuery()){if(rs.next())threshold=rs.getInt(1);}}
                    if(after<=threshold){try(PreparedStatement ps=con.prepareStatement("INSERT INTO notifications(notification_type,message,is_read,created_at) VALUES('LOW_STOCK',?,FALSE,CURRENT_TIMESTAMP)")){ps.setString(1,names.get(i)+" has "+after+" units left after order #"+orderId);ps.executeUpdate();}}
                }
                con.commit();AuditLog.record(username,"ORDER_COMPLETED","Order #"+orderId+"; total="+total+"; payment="+paymentMethod);return orderId;
            }catch(Exception e){con.rollback();throw e;}finally{con.setAutoCommit(true);}
        }
    }
    public static double getGstRate() {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT setting_value FROM app_settings WHERE setting_key='gst_percent'");ResultSet r=p.executeQuery()){if(r.next())return Math.round(Double.parseDouble(r.getString(1))*100.0)/100.0;}catch(Exception ignored){}return 5.0;
    }
}
