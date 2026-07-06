package lk.dexter.techmart.admin.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.dexter.techmart.service.InventoryManagerRemote;
import java.io.IOException;

@WebServlet("/admin/inventory-action")
public class InventoryAdminServlet extends HttpServlet {
    @EJB
    private InventoryManagerRemote inventoryManager;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        Integer productId = Integer.parseInt(req.getParameter("productId"));
        Integer warehouseId = Integer.parseInt(req.getParameter("warehouseId"));
        int qty = Integer.parseInt(req.getParameter("quantity"));

        if ("add".equals(action)) {
            // නව තොග එකතු කිරීමේ ක්‍රියාවලිය[cite: 11]
            inventoryManager.addInventory(productId, qty, warehouseId);
        }
        resp.sendRedirect("../admin/manage-inventory.html");
    }
}