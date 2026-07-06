package lk.dexter.techmart.admin.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.dexter.techmart.entity.OrderItems;
import lk.dexter.techmart.entity.Orders;
import lk.dexter.techmart.service.OrderServiceRemote;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/all-orders")
public class OrderManagementServlet extends HttpServlet {
    @EJB
    private OrderServiceRemote orderService;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Orders> allOrders = orderService.getAllOrders();

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");

        for (int i = 0; i < allOrders.size(); i++) {
            Orders order = allOrders.get(i);
            jsonBuilder.append("{");
            jsonBuilder.append("\"orderId\":").append(order.getOrderId()).append(",");
            jsonBuilder.append("\"totalAmount\":").append(order.getTotalAmount()).append(",");
            jsonBuilder.append("\"status\":").append(order.getStatus()).append(",");
            jsonBuilder.append("\"items\": [");

            List<Map<String, Object>> items =
                    orderService.getOrderDetailsWithWarehouse(order.getOrderId());

            for (int j = 0; j < items.size(); j++) {

                Map<String, Object> item = items.get(j);

                jsonBuilder.append("{");
                jsonBuilder.append("\"productName\":\"")
                        .append(item.get("productName"))
                        .append("\",");

                jsonBuilder.append("\"qty\":")
                        .append(item.get("qty"))
                        .append(",");

                jsonBuilder.append("\"warehouseName\":\"")
                        .append(item.get("warehouseName"))
                        .append("\"");

                jsonBuilder.append("}");

                if (j < items.size() - 1) {
                    jsonBuilder.append(",");
                }
            }
            jsonBuilder.append("]");
            jsonBuilder.append("}");
            if (i < allOrders.size() - 1) jsonBuilder.append(",");
        }
        jsonBuilder.append("]");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonBuilder.toString());
    }
}