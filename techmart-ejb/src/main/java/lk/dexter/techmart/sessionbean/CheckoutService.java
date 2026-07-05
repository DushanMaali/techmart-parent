package lk.dexter.techmart.sessionbean;

import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.*;
import lk.dexter.techmart.service.CartServiceRemote;
import lk.dexter.techmart.service.CheckoutServiceRemote;
import lk.dexter.techmart.service.InventoryManagerRemote;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Stateless(name = "CheckoutService")
public class CheckoutService implements CheckoutServiceRemote {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    @EJB
    private InventoryManagerRemote inventoryManager;

    @Override
    @Asynchronous
    public Future<Orders> processCartCheckoutAsynchronously(User user, CartServiceRemote cartService, double totalAmount) {
        try {
            Orders order = new Orders();
            order.setUser(user);
            order.setTotalAmount(totalAmount);
            order.setStatus(1); // 1 = Pending / Processing
            order.setCreatedAt(new Date());
            em.persist(order);

            List<Map<String, Object>> serializedItems = cartService.getSerializableCartItems();

            for (Map<String, Object> itemMap : serializedItems) {
                int inventoryId = Integer.parseInt(String.valueOf(itemMap.get("inventoryId")));
                int qty = Integer.parseInt(String.valueOf(itemMap.get("qty")));
                double price = Double.parseDouble(String.valueOf(itemMap.get("price")));
                int productId = Integer.parseInt(String.valueOf(itemMap.get("productId")));
                int warehouseId = Integer.parseInt(String.valueOf(itemMap.get("warehouseId")));

                Inventory inv = em.find(Inventory.class, inventoryId);

                if (inv != null && inventoryManager.checkAndReduceStock(productId, warehouseId, qty)) {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setOrder(order);
                    orderItem.setInventory(inv);
                    orderItem.setQty(qty);
                    orderItem.setSubtotal(price * qty);
                    em.persist(orderItem);
                } else {
                    order.setStatus(0); // 0 = Failed
                    em.merge(order);
                    return new AsyncResult<>(order);
                }
            }

            cartService.clearCart();

            return new AsyncResult<>(order);
        } catch (Exception e) {
            e.printStackTrace();
            return new AsyncResult<>(null);
        }
    }
}