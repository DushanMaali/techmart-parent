package lk.dexter.techmart.admin.servlet;

import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.dexter.techmart.service.InventoryManagerRemote;
import java.io.IOException;

@WebServlet("/inventory-list")
public class InventoryListServlet extends HttpServlet {

    @EJB
    private InventoryManagerRemote inventoryManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String json = new Gson().toJson(inventoryManager.getAllInventory());

        resp.getWriter().write(json);
    }
}