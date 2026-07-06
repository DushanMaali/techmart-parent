package lk.dexter.techmart.admin.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.dexter.techmart.service.OrderServiceRemote;
import java.io.IOException;

@WebServlet("/update-order-status")
public class UpdateStatusServlet extends HttpServlet {
    @EJB
    private OrderServiceRemote orderService;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int orderId = Integer.parseInt(req.getParameter("orderId"));
        int newStatus = Integer.parseInt(req.getParameter("status"));

        boolean success = orderService.updateOrderStatus(orderId, newStatus);

        if (success) {
            resp.getWriter().write("success");
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("failed");
        }
    }
}