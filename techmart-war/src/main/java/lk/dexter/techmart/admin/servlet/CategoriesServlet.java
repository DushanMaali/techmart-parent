package lk.dexter.techmart.admin.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.dexter.techmart.service.ProductServiceRemote;

import java.io.IOException;

@WebServlet("/categories")
public class CategoriesServlet extends HttpServlet {
    @EJB
    private ProductServiceRemote productService;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.getWriter().write(new com.google.gson.Gson().toJson(productService.getAllCategories()));
    }
}
