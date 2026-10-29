package com.eduguard.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter("/api/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;

        boolean loginCall = req.getRequestURI().endsWith("/api/login");
        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;

        if (loginCall || loggedIn) {
            chain.doFilter(rq, rs);
        } else {
            resp.setStatus(401);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Not logged in\"}");
        }
    }
}