package lk.dexter.techmart.admin.servlet;

import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.dexter.techmart.service.UserServiceRemote;
import java.io.IOException;

@WebServlet("/admin/user-list")
public class UserServlet extends HttpServlet {

    @EJB
    private UserServiceRemote userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String jsonUsers = new Gson().toJson(userService.getAllUsers());

        resp.getWriter().write(jsonUsers);
    }
}