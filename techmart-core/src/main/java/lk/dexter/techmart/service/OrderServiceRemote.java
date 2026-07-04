package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import lk.dexter.techmart.entity.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Remote
public interface OrderServiceRemote {

    Future<Orders> placeOrderAsynchronously(User user, Integer productId, Integer warehouseId, int qty, double totalAmount);
    Future<Orders> placeOrderAsynchronously(User user, Map<Inventory, Integer> cartItems, double totalAmount);
    List<Orders> getOrdersByUser(String userId);
    Orders getOrderById(Integer orderId);
    List<OrderItems> getOrderItemsByOrderId(Integer orderId);
    List<Orders> getAllOrders();
    boolean updateOrderStatus(Integer orderId, int newStatus);

}