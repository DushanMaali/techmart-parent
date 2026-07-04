package lk.dexter.techmart.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.dexter.techmart.service.ProductServiceRemote;
import com.google.gson.Gson;

import java.io.IOException;

@WebServlet("/get-products-by-category")
public class GetProductsServlet extends HttpServlet {

    @EJB
    private ProductServiceRemote productService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String json = new Gson().toJson(productService.getProductsGroupedByCategory());
        resp.getWriter().write(json);
    }
}