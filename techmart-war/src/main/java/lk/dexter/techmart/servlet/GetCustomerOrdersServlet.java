package lk.dexter.techmart.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dexter.techmart.entity.Orders;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.service.OrderServiceRemote;
import com.google.gson.Gson;

import javax.naming.InitialContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/customer-orders")
public class GetCustomerOrdersServlet extends HttpServlet {

    @EJB
    private OrderServiceRemote orderService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("LOGGED_IN_USER") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"User not logged in\"}");
            return;
        }

        User user = (User) session.getAttribute("LOGGED_IN_USER");

        try {
            List<Orders> ordersList = orderService.getOrdersByUser(user.getId());

            List<Map<String, Object>> serializedOrders = new ArrayList<>();

            for (Orders order : ordersList) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("orderId", order.getOrderId());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("status", order.getStatus());
                orderMap.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");

                List<Map<String, Object>> serializedItems = orderService.getSerializableOrderItemsByOrderId(order.getOrderId());

                orderMap.put("items", serializedItems);
                serializedOrders.add(orderMap);
            }

            resp.getWriter().write(new Gson().toJson(serializedOrders));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Failed to load orders with items\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("LOGGED_IN_USER") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            Orders order = orderService.getOrderById(orderId);

            if (order != null) {
                if (order.getStatus() == 1 || order.getStatus() == 2) {
                    boolean success = orderService.updateOrderStatus(orderId, 5); // 5 = Cancelled
                    if (success) {
                        resp.getWriter().write("{\"success\":true, \"message\":\"Order cancelled successfully!\"}");
                        return;
                    }
                } else {
                    resp.getWriter().write("{\"success\":false, \"message\":\"Cannot cancel order. It is already packed or shipped!\"}");
                    return;
                }
            }
            resp.getWriter().write("{\"success\":false, \"message\":\"Order not found\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false, \"message\":\"Error occurred\"}");
        }
    }
}