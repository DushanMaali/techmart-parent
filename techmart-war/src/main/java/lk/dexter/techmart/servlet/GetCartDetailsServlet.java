package lk.dexter.techmart.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.service.CartServiceRemote;
import com.google.gson.Gson;
import javax.naming.InitialContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/get-cart-details")
public class GetCartDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);
        User user = (User) session.getAttribute("LOGGED_IN_USER");

        CartServiceRemote cartService = (CartServiceRemote) session.getAttribute("USER_CART_SERVICE");
        if (cartService == null) {
            try {
                InitialContext ctx = new InitialContext();
                cartService = (CartServiceRemote) ctx.lookup("java:global/techmart-ear/lk.dexter.techmart-techmart-ejb-1.0/CartService!lk.dexter.techmart.service.CartServiceRemote");

                if (user != null) {
                    cartService.initializeCartForUser(user);
                }
                session.setAttribute("USER_CART_SERVICE", cartService);
            } catch (Exception e) {
                e.printStackTrace();
                resp.getWriter().write("{\"items\":[], \"total\":0.0, \"error\":\"EJB Lookup Failed\"}");
                return;
            }
        }

        Map<String, Object> jsonResponse = new HashMap<>();

        List<Map<String, Object>> itemsList = cartService.getSerializableCartItems();

        jsonResponse.put("total", cartService.getTotal());
        jsonResponse.put("items", itemsList);

        resp.getWriter().write(new Gson().toJson(jsonResponse));
    }
}