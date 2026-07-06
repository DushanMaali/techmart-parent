package lk.dexter.techmart.admin.servlet;

import jakarta.ejb.EJB;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.dexter.techmart.entity.Warehouse;
import lk.dexter.techmart.service.WarehouseServiceRemote;
import java.io.IOException;

@WebServlet(urlPatterns = {"/admin/warehouse-action", "/admin/warehouse-list"})
public class WarehouseServlet extends HttpServlet {
    @EJB
    private WarehouseServiceRemote warehouseService;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(req.getServletPath().equals("/admin/warehouse-list")) {
            resp.setContentType("application/json");
            resp.getWriter().write(new com.google.gson.Gson().toJson(warehouseService.getAllWarehouses()));
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if ("add".equals(action)) {
            Warehouse w = new Warehouse();
            w.setName(req.getParameter("name"));
            warehouseService.addWarehouse(w);
        } else if ("update".equals(action)) {
            Warehouse w = warehouseService.getWarehouseById(Integer.parseInt(req.getParameter("id")));
            w.setName(req.getParameter("name"));
            warehouseService.updateWarehouse(w);
        }
        resp.sendRedirect("manage-warehouse.html");
    }
}