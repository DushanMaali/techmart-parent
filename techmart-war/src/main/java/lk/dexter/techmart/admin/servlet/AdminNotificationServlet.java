package lk.dexter.techmart.admin.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dexter.techmart.entity.Notification;
import lk.dexter.techmart.service.NotificationServiceRemote;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin-notifications")
public class AdminNotificationServlet extends HttpServlet {

    @EJB
    private NotificationServiceRemote notificationService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("ADMIN_LOGGED") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        List<Notification> notifications = notificationService.getAdminNotifications();
        notifications.sort((n1, n2) -> n2.getId().compareTo(n1.getId()));

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < notifications.size(); i++) {
            Notification n = notifications.get(i);
            String safeMessage = n.getMessage().replace("\"", "\\\"");
            json.append("{\"id\":").append(n.getId()).append(", \"message\":\"").append(safeMessage).append("\"}");
            if (i < notifications.size() - 1) json.append(",");
        }
        json.append("]");
        resp.getWriter().write(json.toString());
    }
}