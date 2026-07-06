package lk.dexter.techmart.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.entity.Orders;
import lk.dexter.techmart.service.NotificationServiceRemote;
import lk.dexter.techmart.service.OrderServiceRemote;
import lk.dexter.techmart.service.CartServiceRemote;
import lk.dexter.techmart.service.CheckoutServiceRemote;

import java.io.IOException;
import java.util.concurrent.Future;

@WebServlet("/place-order")
public class PlaceOrderServlet extends HttpServlet {

    @EJB
    private OrderServiceRemote orderService;

    @EJB
    private CheckoutServiceRemote checkoutService;

    @EJB(beanName = "NotificationService")
    private NotificationServiceRemote notificationService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("LOGGED_IN_USER") == null) {
            resp.getWriter().write("{\"success\": false, \"message\": \"Unauthorized\"}");
            return;
        }

        User user = (User) session.getAttribute("LOGGED_IN_USER");
        String type = req.getParameter("type");
        double totalAmount = Double.parseDouble(req.getParameter("total"));

        try {
            Future<Orders> orderFuture = null;

            if ("cart".equals(type)) {
                CartServiceRemote cartService = (CartServiceRemote) session.getAttribute("USER_CART_SERVICE");

                if (cartService != null) {

                    orderFuture = checkoutService.processCartCheckoutAsynchronously(user, cartService, totalAmount);
                }
            } else {
                Integer productId = Integer.parseInt(req.getParameter("pid"));
                Integer warehouseId = Integer.parseInt(req.getParameter("wid"));
                int qty = Integer.parseInt(req.getParameter("qty"));

                orderFuture = orderService.placeOrderAsynchronously(user, productId, warehouseId, qty, totalAmount);
            }

            if (orderFuture != null && orderFuture.get() != null && orderFuture.get().getStatus() == 1) {

                Orders createdOrder = orderFuture.get();
                notificationService.sendNotificationToQueue(
                        user.getId(),
                        user.getFname() + " placed an order of LKR " + totalAmount + ". Order Id: " + createdOrder.getOrderId(),
                        4
                );

                if ("cart".equals(type)) {
                    CartServiceRemote cartService = (CartServiceRemote) session.getAttribute("USER_CART_SERVICE");
                    if (cartService != null) cartService.clearCart();
                }
                resp.getWriter().write("{\"success\": true}");
            } else {
                resp.getWriter().write("{\"success\": false, \"message\": \"Stock reduction failed or empty cart\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("{\"success\": false, \"message\": \"Server error occurred.\"}");
        }
    }
}