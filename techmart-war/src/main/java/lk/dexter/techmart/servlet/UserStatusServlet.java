package lk.dexter.techmart.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dexter.techmart.entity.User;

import java.io.IOException;

@WebServlet("/user-status")
public class UserStatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        User user = null;
        if (session != null) {
            user = (User) session.getAttribute("LOGGED_IN_USER");
        }

        resp.setContentType("application/json");

        if (user != null) {
            // ඔයාගේ Session එකේ තිබ්බ නම "LOGGED_IN_USER" නිසා මෙතනින් ඒක ගන්න
            resp.getWriter().write("{\"status\":\"logged\", \"name\":\"" + user.getFname() + "\"}");
        } else {
            resp.getWriter().write("{\"status\":\"guest\"}");
        }

    }
}
