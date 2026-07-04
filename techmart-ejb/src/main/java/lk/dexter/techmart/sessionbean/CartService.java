package lk.dexter.techmart.sessionbean;

import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.*;
import lk.dexter.techmart.service.CartServiceRemote;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateful(name = "CartService")
public class CartService implements CartServiceRemote, Serializable {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    private Cart activeCart;
    private Map<Integer, CartItems> memoryCart = new HashMap<>();

    @Override
    public void initializeCartForUser(User user) {
        Map<Integer, CartItems> guestItems = new HashMap<>(this.memoryCart);
        this.memoryCart.clear();
        List<Cart> cartList = em.createQuery("SELECT c FROM Cart c WHERE c.user.id = :userId", Cart.class)
                .setParameter("userId", user.getId())
                .getResultList();

        if (!cartList.isEmpty()) {
            this.activeCart = cartList.get(0);

            List<CartItems> items = em.createQuery("SELECT ci FROM CartItems ci WHERE ci.cart.cartId = :cartId", CartItems.class)
                    .setParameter("cartId", activeCart.getCartId())
                    .getResultList();

            for (CartItems item : items) {
                memoryCart.put(item.getInventory().getInventoryId(), item);
            }
        } else {
            activeCart = new Cart();
            activeCart.setUser(user);
            activeCart.setTotal(0.0);
            em.persist(activeCart);
        }

        for (CartItems guestItem : guestItems.values()) {
            addItem(guestItem.getInventory().getProduct().getProductId(),
                    guestItem.getInventory().getWarehouse().getWarehouse_id(),
                    guestItem.getQty());
        }
    }

    @Override
    public void addItem(Integer productId, Integer warehouseId, int quantity) {
        Inventory inventory = em.createQuery("SELECT i FROM Inventory i WHERE i.product.productId = :pId AND i.warehouse.warehouse_id = :wId", Inventory.class)
                .setParameter("pId", productId)
                .setParameter("wId", warehouseId)
                .getSingleResult();

        if (activeCart != null) {
            CartItems item = memoryCart.get(inventory.getInventoryId());
            if (item != null) {
                item.setQty(item.getQty() + quantity);
                item.setSubTotal(item.getQty() * inventory.getProduct().getPrice());
                em.merge(item);
            } else {

                CartItems newItem = new CartItems();
                newItem.setCart(activeCart);
                newItem.setInventory(inventory);
                newItem.setQty(quantity);
                newItem.setSubTotal(quantity * inventory.getProduct().getPrice());
                em.persist(newItem);
                memoryCart.put(inventory.getInventoryId(), newItem);
            }
            updateCartTotal();
        } else {

            CartItems guestItem = new CartItems();
            guestItem.setInventory(inventory);
            guestItem.setQty(quantity);
            memoryCart.put(inventory.getInventoryId(), guestItem);
        }
    }

    @Override
    public void removeItem(Integer inventoryId) {
        CartItems item = memoryCart.get(inventoryId);
        if (item != null) {
            if (activeCart != null) {
                em.remove(item);
            }
            memoryCart.remove(inventoryId);
            updateCartTotal();
        }
    }

    @Override
    public Map<Product, Integer> getCartItems() {
        return memoryCart.values().stream()
                .collect(Collectors.toMap(
                        item -> item.getInventory().getProduct(),
                        CartItems::getQty,
                        Integer::sum
                ));
    }

    @Override
    public Integer getCartIdFromUserId(String userId) {
        try {
            return em.createQuery("SELECT c.cartId FROM Cart c WHERE c.user.id = :userId", Integer.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (Exception e) { return null; }
    }

    @Override
    public List<CartItems> getCartItemsByCartId(Integer cartId) {
        return em.createQuery("SELECT ci FROM CartItems ci WHERE ci.cart.cartId = :cartId", CartItems.class)
                .setParameter("cartId", cartId)
                .getResultList();
    }

    @Override
    public double getTotal() {
        return activeCart != null ? activeCart.getTotal() : 0.0;
    }

    @Override
    public void clearCart() {
        if (activeCart == null) return;
        em.createQuery("DELETE FROM CartItems ci WHERE ci.cart.cartId = :cId")
                .setParameter("cId", activeCart.getCartId()).executeUpdate();
        memoryCart.clear();
        activeCart.setTotal(0.0);
        em.merge(activeCart);
    }

    private void updateCartTotal() {
        double total = memoryCart.values().stream().mapToDouble(CartItems::getSubTotal).sum();
        activeCart.setTotal(total);
        em.merge(activeCart);
    }

}
