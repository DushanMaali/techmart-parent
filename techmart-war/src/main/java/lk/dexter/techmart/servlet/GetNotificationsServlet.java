package lk.dexter.techmart.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dexter.techmart.entity.Notification;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.service.NotificationServiceRemote;
import java.io.IOException;
import java.util.List;

@WebServlet("/get-notifications")
public class GetNotificationsServlet extends HttpServlet {
    @EJB
    private NotificationServiceRemote notificationService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("LOGGED_IN_USER") != null) {
            User user = (User) session.getAttribute("LOGGED_IN_USER");
            List<Notification> notifications = notificationService.getUnreadNotifications(user.getId());
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < notifications.size(); i++) {
                Notification n = notifications.get(i);
                String safeMessage = n.getMessage().replace("\"", "\\\"");
                json.append("{\"id\":").append(n.getId()).append(", \"message\":\"").append(safeMessage).append("\"}");
                if (i < notifications.size() - 1) json.append(",");
            }
            json.append("]");
            resp.getWriter().write(json.toString());
        } else {
            resp.getWriter().write("[]");
        }
    }
}