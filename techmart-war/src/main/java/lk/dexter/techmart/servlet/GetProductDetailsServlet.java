package lk.dexter.techmart.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.dexter.techmart.entity.Product;
import lk.dexter.techmart.service.ProductServiceRemote;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/get-product-details")
public class GetProductDetailsServlet extends HttpServlet {

    @EJB
    private ProductServiceRemote productService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pidParam = req.getParameter("pid");
        if (pidParam != null) {
            try {
                Integer productId = Integer.parseInt(pidParam);

                Product product = productService.getProductById(productId);

                if (product != null) {
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("productId", product.getProductId());
                    productData.put("productName", product.getProductName());
                    productData.put("price", product.getPrice());

                    String json = new Gson().toJson(productData);
                    resp.getWriter().write(json);
                } else {
                    resp.getWriter().write("{}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp.getWriter().write("{}");
            }
        } else {
            resp.getWriter().write("{}");
        }
    }
}