package lk.dexter.techmart.admin.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.dexter.techmart.service.OrderServiceRemote;

import java.io.IOException;
import java.util.Map;

@WebServlet("/admin/stats")
public class AdminStatsServlet extends HttpServlet {
    @EJB
    private OrderServiceRemote orderService;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Map<String, Object> stats = orderService.getDashboardStats();

        String json = new com.google.gson.Gson().toJson(stats);
        resp.getWriter().write(json);
    }
}