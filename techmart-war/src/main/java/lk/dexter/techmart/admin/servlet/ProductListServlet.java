package lk.dexter.techmart.servlet;

import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.dexter.techmart.service.ProductServiceRemote;
import java.io.IOException;

@WebServlet("/products")
public class ProductListServlet extends HttpServlet {
    @EJB
    private ProductServiceRemote productService;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String json = new Gson().toJson(productService.getAllProducts());
        resp.getWriter().write(json);
    }
}