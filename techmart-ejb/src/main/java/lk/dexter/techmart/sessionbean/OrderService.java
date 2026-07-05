package lk.dexter.techmart.sessionbean;

import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.*;
import lk.dexter.techmart.service.InventoryManagerRemote;
import lk.dexter.techmart.service.OrderServiceRemote;

import java.util.*;
import java.util.concurrent.Future;

@Stateless(name = "OrderService")
public class OrderService implements OrderServiceRemote {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    @EJB
    private InventoryManagerRemote inventoryManager;

    // 1. Single product order implementation
    @Override
    @Asynchronous
    public Future<Orders> placeOrderAsynchronously(User user, Integer productId, Integer warehouseId, int qty, double totalAmount) {
        try {
            Inventory inventory = em.createQuery(
                            "SELECT i FROM Inventory i WHERE i.product.productId = :pId AND i.warehouse.warehouse_id = :wId", Inventory.class)
                    .setParameter("pId", productId)
                    .setParameter("wId", warehouseId)
                    .getSingleResult();

            Orders order = createBaseOrder(user, totalAmount);

            if (inventoryManager.checkAndReduceStock(productId, warehouseId, qty)) {
                OrderItems item = new OrderItems();
                item.setOrder(order);
                item.setInventory(inventory);
                item.setQty(qty);
                item.setSubtotal(totalAmount);
                em.persist(item);
                return new AsyncResult<>(order);
            } else {
                order.setStatus(0);
                em.merge(order);
                return new AsyncResult<>(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AsyncResult<>(null);
        }
    }

    // 2. Map of Inventory items order implementation
    @Override
    @Asynchronous
    public Future<Orders> placeOrderAsynchronously(User user, Map<Inventory, Integer> cartItems, double totalAmount) {
        try {
            Orders order = createBaseOrder(user, totalAmount);
            for (Map.Entry<Inventory, Integer> entry : cartItems.entrySet()) {
                Inventory inv = entry.getKey();
                Integer qty = entry.getValue();

                if (inventoryManager.checkAndReduceStock(inv.getProduct().getProductId(), inv.getWarehouse().getWarehouse_id(), qty)) {
                    OrderItems item = new OrderItems();
                    item.setOrder(order);
                    item.setInventory(inv);
                    item.setQty(qty);
                    item.setSubtotal(inv.getProduct().getPrice() * qty);
                    em.persist(item);
                } else {
                    order.setStatus(0);
                    em.merge(order);
                    return new AsyncResult<>(order);
                }
            }
            return new AsyncResult<>(order);
        } catch (Exception e) {
            e.printStackTrace();
            return new AsyncResult<>(null);
        }
    }

    private Orders createBaseOrder(User user, double totalAmount) {
        Orders order = new Orders();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus(1);
        order.setCreatedAt(new Date());
        em.persist(order);
        return order;
    }

    @Override
    public List<Orders> getOrdersByUser(String userId) {
        em.getEntityManagerFactory().getCache().evictAll();
        return em.createQuery("SELECT o FROM Orders o WHERE o.user.id = :userId ORDER BY o.createdAt DESC", Orders.class)
                .setParameter("userId", userId).getResultList();
    }

    @Override
    public Orders getOrderById(Integer orderId) {
        return em.find(Orders.class, orderId);
    }

    @Override
    public List<OrderItems> getOrderItemsByOrderId(Integer orderId) {
        return em.createQuery("SELECT oi FROM OrderItems oi JOIN FETCH oi.inventory WHERE oi.order.orderId = :orderId", OrderItems.class)
                .setParameter("orderId", orderId).getResultList();
    }

    @Override
    public List<Orders> getAllOrders() {
        return em.createQuery("SELECT o FROM Orders o ORDER BY o.createdAt DESC", Orders.class).getResultList();
    }

    @Override
    public boolean updateOrderStatus(Integer orderId, int newStatus) {
        Orders order = em.find(Orders.class, orderId);
        if (order != null) {
            order.setStatus(newStatus);
            em.merge(order);
            return true;
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getSerializableOrderItemsByOrderId(Integer orderId) {
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        try {
            // 📦 DB එකෙන් Items ටික ගන්නවා
            List<OrderItems> itemsList = em.createQuery(
                            "SELECT oi FROM OrderItems oi JOIN FETCH oi.inventory WHERE oi.order.orderId = :orderId", OrderItems.class)
                    .setParameter("orderId", orderId).getResultList();

            for (OrderItems item : itemsList) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("qty", item.getQty());
                itemMap.put("subtotal", item.getSubtotal());

                if (item.getInventory() != null && item.getInventory().getProduct() != null) {
                    itemMap.put("productName", item.getInventory().getProduct().getProductName());
                    itemMap.put("productImg", item.getInventory().getProduct().getProductImg());
                } else {
                    itemMap.put("productName", "Unknown Product");
                    itemMap.put("productImg", "");
                }
                serializedItems.add(itemMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serializedItems;
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Total Orders
        Long totalOrders = em.createQuery("SELECT COUNT(o) FROM Orders o", Long.class).getSingleResult();
        stats.put("totalOrders", totalOrders);

        // 2. Best Selling Product
        List<Object[]> bestProduct = em.createQuery(
                        "SELECT oi.inventory.product.productName, SUM(oi.qty) FROM OrderItems oi GROUP BY oi.inventory.product.productName ORDER BY SUM(oi.qty) DESC", Object[].class)
                .setMaxResults(1).getResultList();
        stats.put("bestProduct", bestProduct.isEmpty() ? "N/A" : bestProduct.get(0)[0]);

        // 3. Best Selling Warehouse
        List<Object[]> bestWarehouse = em.createQuery(
                        "SELECT oi.inventory.warehouse.name, SUM(oi.qty) FROM OrderItems oi GROUP BY oi.inventory.warehouse.name ORDER BY SUM(oi.qty) DESC", Object[].class)
                .setMaxResults(1).getResultList();
        stats.put("bestWarehouse", bestWarehouse.isEmpty() ? "N/A" : bestWarehouse.get(0)[0]);

        return stats;
    }

}
