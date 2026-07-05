package lk.dexter.techmart.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.service.CartServiceRemote;
import lk.dexter.techmart.service.UserServiceRemote;
import javax.naming.InitialContext;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    @EJB
    private UserServiceRemote userService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String email = request.getParameter("email");
            String password = request.getParameter("password");

            User loggedUser = userService.loginUser(email, password);

            if (loggedUser != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("LOGGED_IN_USER", loggedUser);

                CartServiceRemote cartService = (CartServiceRemote) session.getAttribute("USER_CART_SERVICE");

                if (cartService == null) {
                    InitialContext ctx = new InitialContext();
                    cartService = (CartServiceRemote) ctx.lookup("java:global/techmart-ear/lk.dexter.techmart-techmart-ejb-1.0/CartService!lk.dexter.techmart.service.CartServiceRemote");
                    session.setAttribute("USER_CART_SERVICE", cartService);
                }

                cartService.initializeCartForUser(loggedUser);

                response.sendRedirect("home.html");
            } else {
                out.println("<script>alert('Invalid Email or Password!'); window.location='login.html';</script>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h3 style='color:red;'>Error occurred: " + e.getMessage() + "</h3>");
        }
    }
}