package lk.dexter.techmart.sessionbean;

import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.OrderItems;
import lk.dexter.techmart.entity.Orders;
import lk.dexter.techmart.entity.Product;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.service.InventoryManagerRemote;
import lk.dexter.techmart.service.OrderServiceRemote;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Stateless(name = "OrderService")
public class OrderService implements OrderServiceRemote {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    @EJB
    private InventoryManagerRemote inventoryManager;

    @Override
    @Asynchronous
    public Future<Orders> placeOrderAsynchronously(User user, Map<Product, Integer> cartItems, double totalAmount) {
        try {
            Orders order = new Orders();
            order.setUser(user);
            order.setTotalAmount(totalAmount);
            order.setStatus(1);
            order.setCreatedAt(new Date());
            em.persist(order);
            for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
                Product product = entry.getKey();
                Integer qty = entry.getValue();

                boolean stockReduced = inventoryManager.checkAndReduceStock(product.getProductId(), qty);

                if (stockReduced) {
                    OrderItems item = new OrderItems();
                    item.setOrder(order);
                    item.setProduct(product);
                    item.setQty(qty);
                    item.setSubtotal(product.getPrice() * qty);
                    em.persist(item);
                } else {

                    order.setStatus(0);
                    em.merge(order);
                    return new AsyncResult<>(order);
                }
            }

            em.merge(order);

            return new AsyncResult<>(order);

        } catch (Exception e) {
            e.printStackTrace();
            return new AsyncResult<>(null);
        }
    }

    @Override
    public List<Orders> getOrdersByUser(String userId) {
        return em.createQuery("SELECT o FROM Orders o WHERE o.user.id = :userId ORDER BY o.createdAt DESC", Orders.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public Orders getOrderById(Integer orderId) {
        return em.find(Orders.class, orderId);
    }

    @Override
    public List<OrderItems> getOrderItemsByOrderId(Integer orderId) {
        try {
            return em.createQuery(
                            "SELECT oi FROM OrderItems oi " +
                                    "JOIN FETCH oi.product " +
                                    "WHERE oi.order.orderId = :orderId", OrderItems.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public List<Orders> getAllOrders() {
        return em.createQuery("SELECT o FROM Orders o ORDER BY o.createdAt DESC", Orders.class)
                .getResultList();
    }

    @Override
    public boolean updateOrderStatus(Integer orderId, int newStatus) {
        try {
            Orders order = em.find(Orders.class, orderId);
            if (order != null) {
                order.setStatus(newStatus);
                em.merge(order);
                System.out.println("📦 [TECHMART LOG] Order ID " + orderId + " status successfully updated to: " + newStatus);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
