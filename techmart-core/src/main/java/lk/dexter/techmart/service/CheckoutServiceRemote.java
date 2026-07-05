package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import java.util.concurrent.Future;
import lk.dexter.techmart.entity.Orders;
import lk.dexter.techmart.entity.User;

@Remote
public interface CheckoutServiceRemote {
    Future<Orders> processCartCheckoutAsynchronously(User user, CartServiceRemote cartService, double totalAmount);
}