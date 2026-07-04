package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import lk.dexter.techmart.entity.OrderItems;
import lk.dexter.techmart.entity.Orders;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.entity.Product;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Remote
public interface OrderServiceRemote {

    Future<Orders> placeOrderAsynchronously(User user, Map<Product, Integer> cartItems, double totalAmount);
    List<Orders> getOrdersByUser(String userId);
    Orders getOrderById(Integer orderId);
    List<OrderItems> getOrderItemsByOrderId(Integer orderId);
    List<Orders> getAllOrders();
    boolean updateOrderStatus(Integer orderId, int newStatus);

}