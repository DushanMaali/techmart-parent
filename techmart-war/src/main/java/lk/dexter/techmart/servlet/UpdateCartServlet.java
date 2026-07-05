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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/update-cart")
public class UpdateCartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);
        User user = (User) session.getAttribute("LOGGED_IN_USER");
        String action = req.getParameter("action");
        Map<String, Object> jsonResponse = new HashMap<>();

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
                resp.getWriter().write("{\"success\": false, \"message\": \"EJB Lookup Failed.\"}");
                return;
            }
        }

        try {
            if ("update_qty".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("pid"));
                int warehouseId = Integer.parseInt(req.getParameter("wid"));
                int qtyChange = Integer.parseInt(req.getParameter("qtyChange"));

                cartService.addItem(productId, warehouseId, qtyChange);

                List<Map<String, Object>> items = cartService.getSerializableCartItems();
                boolean itemStillExists = false;

                for (Map<String, Object> itemMap : items) {
                    int itemProductId = (int) itemMap.get("productId");
                    int itemWarehouseId = (int) itemMap.get("warehouseId");

                    if (itemProductId == productId && itemWarehouseId == warehouseId) {
                        jsonResponse.put("updatedQty", itemMap.get("qty"));
                        jsonResponse.put("updatedSubTotal", itemMap.get("subTotal"));
                        itemStillExists = true;
                        break;
                    }
                }

                jsonResponse.put("success", true);
                jsonResponse.put("itemRemoved", !itemStillExists);
                jsonResponse.put("grandTotal", cartService.getTotal());

            } else if ("remove".equals(action)) {
                int inventoryId = Integer.parseInt(req.getParameter("inventoryId"));
                cartService.removeItem(inventoryId);

                jsonResponse.put("success", true);
                jsonResponse.put("grandTotal", cartService.getTotal());
            }

            resp.getWriter().write(new Gson().toJson(jsonResponse));

        } catch (IllegalArgumentException e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", e.getMessage());
            resp.getWriter().write(new Gson().toJson(jsonResponse));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("{\"success\": false, \"message\": \"Server error occurred.\"}");
        }
    }
}