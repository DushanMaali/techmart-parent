package lk.dexter.techmart.admin.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/admin/*")
public class AdminAuthFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String path = request.getRequestURI();

        if (path.endsWith("admin-login.html") || path.endsWith("/admin-login")) {
            chain.doFilter(req, res);
            return;
        }

        if (session == null || session.getAttribute("ADMIN_LOGGED") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/admin-login.html");
        } else {
            chain.doFilter(req, res);
        }
    }
}