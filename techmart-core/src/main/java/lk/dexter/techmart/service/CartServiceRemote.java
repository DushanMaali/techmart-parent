package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import lk.dexter.techmart.entity.CartItems;
import lk.dexter.techmart.entity.Product;
import lk.dexter.techmart.entity.User;

import java.util.List;
import java.util.Map;

@Remote
public interface CartServiceRemote {

    void initializeCartForUser(User user);
    void addItem(Product product, int quantity);
    void removeItem(Integer productId);
    Map<Product, Integer> getCartItems();
    List<CartItems> getCartItemsByUserId(String userId);
    List<CartItems> getOrderItemsByCartId(Integer cartId);
    double getTotal();
    void clearCart();

}