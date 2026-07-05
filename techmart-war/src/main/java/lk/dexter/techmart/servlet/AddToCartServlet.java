package lk.dexter.techmart.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dexter.techmart.service.CartServiceRemote;
import javax.naming.InitialContext;
import java.io.IOException;

@WebServlet("/add-to-cart")
public class AddToCartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);

        try {
            Integer productId = Integer.parseInt(req.getParameter("pid"));
            Integer warehouseId = Integer.parseInt(req.getParameter("wid"));
            int qty = Integer.parseInt(req.getParameter("qty"));

            CartServiceRemote cartService = (CartServiceRemote) session.getAttribute("USER_CART_SERVICE");

            if (cartService == null) {
                InitialContext ctx = new InitialContext();
                cartService = (CartServiceRemote) ctx.lookup("java:global/techmart-ear/lk.dexter.techmart-techmart-ejb-1.0/CartService!lk.dexter.techmart.service.CartServiceRemote");
                session.setAttribute("USER_CART_SERVICE", cartService);
            }

            cartService.addItem(productId, warehouseId, qty);
            resp.getWriter().write("{\"success\": true}");

        } catch (IllegalArgumentException e) {
            resp.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("{\"success\": false, \"message\": \"Server error occurred.\"}");
        }
    }
}