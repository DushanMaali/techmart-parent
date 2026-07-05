package lk.dexter.techmart.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.service.UserServiceRemote;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    @EJB
    private UserServiceRemote userService;


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String fname = request.getParameter("fname");
            String lname = request.getParameter("lname");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String mobile = request.getParameter("mobile");

            if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                out.println("<script>alert('Invalid Email format!'); window.location='register.html';</script>");
                return;
            }

            if (password == null || password.length() != 8) {
                out.println("<script>alert('Password must be exactly 8 characters long!'); window.location='register.html';</script>");
                return;
            }

            if (mobile == null || !mobile.matches("^07[01245678]\\d{7}$")) {
                out.println("<script>alert('Invalid Mobile number! Must be 10 digits.'); window.location='register.html';</script>");
                return;
            }

            String userId =userService.generateNextUserId();

            User newUser = new User();
            newUser.setId(userId);
            newUser.setFname(fname);
            newUser.setLname(lname);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setMobile(mobile);

            boolean success = userService.registerUser(newUser);

            if (success) {
                response.sendRedirect("login.html");
            } else {
                out.println("<script>alert('Registration Failed! User ID already exists.'); window.location='register.html';</script>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h3 style='color:red;'>Error occurred: " + e.getMessage() + "</h3>");
        }
    }
}