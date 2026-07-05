package lk.dexter.techmart.admin.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin-login")
public class AdminLoginServlet extends HttpServlet {
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = req.getParameter("username");
        String pass = req.getParameter("password");
        resp.setContentType("application/json");

        if (ADMIN_USER.equals(user) && ADMIN_PASS.equals(pass)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("ADMIN_LOGGED", true);
            resp.getWriter().write("{\"success\": true}");
        } else {
            resp.getWriter().write("{\"success\": false, \"message\": \"Access Denied!\"}");
        }
    }
}