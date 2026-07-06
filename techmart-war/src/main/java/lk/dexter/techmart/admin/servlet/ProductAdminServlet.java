package lk.dexter.techmart.admin.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.dexter.techmart.entity.Category;
import lk.dexter.techmart.entity.Product;
import lk.dexter.techmart.service.ProductServiceRemote;
import java.io.IOException;

@WebServlet("/admin/product-action")
public class ProductAdminServlet extends HttpServlet {
    @EJB
    private ProductServiceRemote productService;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");

        if ("add".equals(action)) {
            Integer catId = Integer.parseInt(req.getParameter("categoryId"));
            Category category = productService.getAllCategories().stream()
                    .filter(c -> c.getCategoryId().equals(catId))
                    .findFirst().orElse(null);

            Product p = new Product();
            p.setProductName(req.getParameter("name"));
            p.setPrice(Double.parseDouble(req.getParameter("price")));
            p.setDescription(req.getParameter("description"));
            p.setProductImg(req.getParameter("img"));
            p.setCategory(category);
            p.setStatus(1);
            productService.addProduct(p); //
            resp.sendRedirect("../admin/manage-products.html");

        } else if ("update".equals(action)) {
            Integer id = Integer.parseInt(req.getParameter("id"));
            Product p = productService.getProductById(id);
            p.setProductName(req.getParameter("name"));
            p.setPrice(Double.parseDouble(req.getParameter("price")));
            p.setDescription(req.getParameter("description"));
            productService.updateProduct(p);
            resp.sendRedirect("../admin/manage-products.html");

        } else if ("status".equals(action)) {
            Integer id = Integer.parseInt(req.getParameter("id"));
            Integer newStatus = Integer.parseInt(req.getParameter("status"));
            productService.updateProductStatus(id, newStatus);
            resp.getWriter().write("{\"success\":true}");
        }
    }
}