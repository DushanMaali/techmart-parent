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
    void addItem(Integer productId, Integer warehouseId, int quantity);
    void removeItem(Integer cartItemId);
    Map<Product, Integer> getCartItems();
    Integer getCartIdFromUserId(String userId);
    List<CartItems> getCartItemsByCartId(Integer cartId);
    double getTotal();
    void clearCart();

}