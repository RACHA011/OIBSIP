package com.oibsip.library.web;

import com.oibsip.library.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    private final User.Role requiredRole;

    public AuthInterceptor(User.Role requiredRole) {
        this.requiredRole = requiredRole;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            response.sendRedirect("/login");
            return false;
        }
        if (requiredRole != null && currentUser.getRole() != requiredRole) {
            response.sendRedirect("/login?error=unauthorized");
            return false;
        }
        return true;
    }
}