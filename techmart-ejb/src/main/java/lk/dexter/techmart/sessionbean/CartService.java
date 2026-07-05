package lk.dexter.techmart.sessionbean;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.*;
import lk.dexter.techmart.service.CartServiceRemote;
import lk.dexter.techmart.service.InventoryManagerRemote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateful(name = "CartService")
public class CartService implements CartServiceRemote, Serializable {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    @EJB
    private InventoryManagerRemote inventoryManager;

    private Cart activeCart;
    private Map<Integer, CartItems> memoryCart = new HashMap<>();

    @Override
    public void initializeCartForUser(User user) {
        if (this.activeCart != null) {
            return;
        }

        Map<Integer, CartItems> guestMemoryCart = new HashMap<>(this.memoryCart);
        this.memoryCart.clear();

        List<Cart> cartList = em.createQuery("SELECT c FROM Cart c WHERE c.user.id = :userId", Cart.class)
                .setParameter("userId", user.getId())
                .getResultList();

        if (!cartList.isEmpty()) {
            this.activeCart = cartList.get(0);

            List<CartItems> dbItems = em.createQuery("SELECT ci FROM CartItems ci WHERE ci.cart.cartId = :cartId", CartItems.class)
                    .setParameter("cartId", activeCart.getCartId())
                    .getResultList();

            for (CartItems item : dbItems) {
                memoryCart.put(item.getInventory().getInventoryId(), item);
            }
        } else {
            activeCart = new Cart();
            activeCart.setUser(user);
            activeCart.setTotal(0.0);
            em.persist(activeCart);
        }

        if (!guestMemoryCart.isEmpty()) {
            for (CartItems guestItem : guestMemoryCart.values()) {
                Inventory inventory = em.find(Inventory.class, guestItem.getInventory().getInventoryId());
                CartItems existingDbItem = memoryCart.get(inventory.getInventoryId());

                if (existingDbItem != null) {

                    int mergedQty = existingDbItem.getQty() + guestItem.getQty();
                    existingDbItem.setQty(mergedQty);
                    existingDbItem.setSubTotal(mergedQty * inventory.getProduct().getPrice());
                    em.merge(existingDbItem);
                } else {
                    CartItems newItem = new CartItems();
                    newItem.setCart(activeCart);
                    newItem.setInventory(inventory);
                    newItem.setQty(guestItem.getQty());
                    newItem.setSubTotal(guestItem.getQty() * inventory.getProduct().getPrice());
                    em.persist(newItem);
                    memoryCart.put(inventory.getInventoryId(), newItem);
                }
            }
            updateCartTotal();
        }
    }

    @Override
    public void addItem(Integer productId, Integer warehouseId, int quantity) {
        Inventory inventory = em.createQuery("SELECT i FROM Inventory i WHERE i.product.productId = :pId AND i.warehouse.warehouse_id = :wId", Inventory.class)
                .setParameter("pId", productId)
                .setParameter("wId", warehouseId)
                .getSingleResult();

        CartItems item = memoryCart.get(inventory.getInventoryId());
        int currentQtyInCart = (item != null) ? item.getQty() : 0;
        int requestedNewQty = currentQtyInCart + quantity;

        if (requestedNewQty <= 0) {
            removeItem(inventory.getInventoryId());
            return;
        }

        int availableStock = inventoryManager.getAvailableStock(productId, warehouseId);
        if (requestedNewQty > availableStock) {
            throw new IllegalArgumentException("Not enough stock available! Available: " + availableStock);
        }

        if (item != null) {
            item.setQty(requestedNewQty);
            item.setSubTotal(requestedNewQty * inventory.getProduct().getPrice());

            if (activeCart != null) {
                em.merge(item);
            }
        } else {
            CartItems newItem = new CartItems();
            newItem.setInventory(inventory);
            newItem.setQty(requestedNewQty);
            newItem.setSubTotal(requestedNewQty * inventory.getProduct().getPrice());

            if (activeCart != null) {
                newItem.setCart(activeCart);
                em.persist(newItem);
            }

            memoryCart.put(inventory.getInventoryId(), newItem);
        }

        if (activeCart != null) {
            updateCartTotal();
        }
    }

    @Override
    public void removeItem(Integer inventoryId) {
        CartItems item = memoryCart.get(inventoryId);
        if (item != null) {
            if (activeCart != null) {
                em.remove(em.contains(item) ? item : em.merge(item));
            }
            memoryCart.remove(inventoryId);

            if (activeCart != null) {
                updateCartTotal();
            }
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
                    .setParameter("userId", Integer.parseInt(userId))
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
        if (activeCart != null) {
            return activeCart.getTotal();
        } else {
            return memoryCart.values().stream().mapToDouble(CartItems::getSubTotal).sum();
        }
    }

    @Override
    public void clearCart() {
        if (activeCart != null) {
            em.createQuery("DELETE FROM CartItems ci WHERE ci.cart.cartId = :cId")
                    .setParameter("cId", activeCart.getCartId()).executeUpdate();
            activeCart.setTotal(0.0);
            em.merge(activeCart);
        }
        memoryCart.clear();
    }

    @Override
    public List<CartItems> getActiveMemoryCartItems() {
        return new java.util.ArrayList<>(this.memoryCart.values());
    }

    private void updateCartTotal() {
        if (activeCart != null) {
            double total = memoryCart.values().stream().mapToDouble(CartItems::getSubTotal).sum();
            activeCart.setTotal(total);
            em.merge(activeCart);
        }
    }

//    @Override
//    public List<Map<String, Object>> getSerializableCartItems() {
//        List<Map<String, Object>> serializedList = new ArrayList<>();
//
//        for (CartItems item : this.memoryCart.values()) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("cartItemsId", item.getCartItemsId());
//            map.put("inventoryId", item.getInventory().getInventoryId());
//            map.put("productName", item.getInventory().getProduct().getProductName());
//            map.put("price", item.getInventory().getProduct().getPrice());
//            map.put("qty", item.getQty());
//            map.put("subTotal", item.getSubTotal());
//            map.put("productId", item.getInventory().getProduct().getProductId());
//            map.put("warehouseId", item.getInventory().getWarehouse().getWarehouse_id());
//
//            serializedList.add(map);
//        }
//        return serializedList;
//    }

    @Override
    public List<Map<String, Object>> getSerializableCartItems() {
        List<Map<String, Object>> list = new ArrayList<>();

        if (this.memoryCart != null && !this.memoryCart.isEmpty()) {
            for (CartItems item : this.memoryCart.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("inventoryId", item.getInventory().getInventoryId());
                map.put("qty", item.getQty());
                map.put("price", item.getInventory().getProduct().getPrice());
                map.put("productId", item.getInventory().getProduct().getProductId());
                map.put("warehouseId", item.getInventory().getWarehouse().getWarehouse_id());

                map.put("productName", item.getInventory().getProduct().getProductName());
                map.put("subTotal", item.getSubTotal());
                map.put("cartItemsId", item.getCartItemsId());

                list.add(map);
            }
        }
        return list;
    }

}