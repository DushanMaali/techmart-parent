package lk.dexter.techmart.sessionbean;

import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.Cart;
import lk.dexter.techmart.entity.CartItems;
import lk.dexter.techmart.entity.Product;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.service.CartServiceRemote;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateful(name = "CartService")
public class CartService implements CartServiceRemote, Serializable {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    private User currentUser;
    private Cart activeCart;
    private Map<Product, Integer> memoryCart = new HashMap<>();

    @Override
    public void initializeCartForUser(User user) {
        this.currentUser = user;
        this.memoryCart.clear();

        try {
            List<Cart> cartList = em.createQuery("SELECT c FROM Cart c WHERE c.user.id = :userId", Cart.class)
                    .setParameter("userId", user.getId())
                    .getResultList();

            if (!cartList.isEmpty()) {
                this.activeCart = cartList.get(0);

                List<CartItems> items = em.createQuery(
                                "SELECT ci FROM CartItems ci JOIN FETCH ci.product WHERE ci.cart.cartId = :cartId", CartItems.class)
                        .setParameter("cartId", activeCart.getCartId())
                        .getResultList();

                for (CartItems item : items) {
                    memoryCart.put(item.getProduct(), item.getQty());
                }
            } else {
                activeCart = new Cart();
                activeCart.setUser(user);
                activeCart.setTotal(0.0);
                em.persist(activeCart);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addItem(Product product, int quantity) {
        if (activeCart == null) return;

        try {
            List<CartItems> existingItems = em.createQuery(
                            "SELECT ci FROM CartItems ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :prodId", CartItems.class)
                    .setParameter("cartId", activeCart.getCartId())
                    .setParameter("prodId", product.getProductId())
                    .getResultList();

            if (!existingItems.isEmpty()) {
                CartItems item = existingItems.get(0);
                item.setQty(item.getQty() + quantity);
                item.setSubTotal(product.getPrice() * item.getQty());
                em.merge(item);

                memoryCart.put(product, item.getQty());
            } else {
                CartItems newItem = new CartItems();
                newItem.setCart(activeCart);
                newItem.setProduct(product);
                newItem.setQty(quantity);
                newItem.setSubTotal(product.getPrice() * quantity);
                em.persist(newItem);

                memoryCart.put(product, quantity);
            }

            updateCartTotal();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeItem(Integer productId) {
        if (activeCart == null) return;
        try {
            em.createQuery("DELETE FROM CartItems ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :prodId")
                    .setParameter("cartId", activeCart.getCartId())
                    .setParameter("prodId", productId)
                    .executeUpdate();

            memoryCart.keySet().removeIf(product -> product.getProductId().equals(productId));

            updateCartTotal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<Product, Integer> getCartItems() {
        return memoryCart;
    }

    @Override
    public List<CartItems> getCartItemsByUserId(String userId) {
        return em.createQuery(
                        "SELECT ci FROM CartItems ci JOIN FETCH ci.product WHERE ci.cart.user.id = :userId", CartItems.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<CartItems> getOrderItemsByCartId(Integer cartId) {
        return em.createQuery(
                        "SELECT ci FROM CartItems ci JOIN FETCH ci.product WHERE ci.cart.cartId = :cartId", CartItems.class)
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
        try {
            em.createQuery("DELETE FROM CartItems ci WHERE ci.cart.cartId = :cartId")
                    .setParameter("cartId", activeCart.getCartId())
                    .executeUpdate();

            memoryCart.clear();
            activeCart.setTotal(0.0);
            em.merge(activeCart);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCartTotal() {
        double total = memoryCart.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
        activeCart.setTotal(total);
        em.merge(activeCart);
    }

}
