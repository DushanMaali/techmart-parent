package lk.dexter.techmart.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.dexter.techmart.entity.Warehouse;
import lk.dexter.techmart.service.ProductServiceRemote;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/get-stock")
public class StockServlet extends HttpServlet {

    @EJB
    private ProductServiceRemote productService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pidParam = req.getParameter("pid");
        if (pidParam != null) {
            Integer productId = Integer.parseInt(pidParam);
            Map<Warehouse, Integer> stockMap = productService.getAvailableStockByProduct(productId);

            Map<String, Map<String, Object>> customStockMap = new HashMap<>();

            stockMap.forEach((warehouse, qty) -> {
                Map<String, Object> details = new HashMap<>();
                details.put("id", warehouse.getWarehouse_id());
                details.put("qty", qty);
                customStockMap.put(warehouse.getName(), details);
            });

            String json = new Gson().toJson(customStockMap);
            resp.getWriter().write(json);
        } else {
            resp.getWriter().write("{}");
        }
    }
}