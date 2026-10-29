package com.eduguard.controller;

import com.eduguard.model.User;
import com.eduguard.util.HibernateUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        System.out.println("LOGIN REQUEST");
        System.out.println("Username: " + username);

        if (username == null || password == null ||
                username.trim().isEmpty() || password.isEmpty()) {

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            resp.getWriter().write(
                    "{\"success\":false,\"message\":\"Username and password are required\"}"
            );
            return;
        }

        username = username.trim();

        try (Session session = HibernateUtil.getFactory().openSession()) {

            User user = session.createQuery(
                            "FROM User WHERE username = :username",
                            User.class
                    )
                    .setParameter("username", username)
                    .uniqueResult();

            if (user == null) {

                System.out.println("LOGIN FAILED: User not found");

                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                resp.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid username or password\"}"
                );
                return;
            }

            System.out.println("User found: " + user.getUsername());
            System.out.println("Role: " + user.getRole());

            boolean passwordCorrect;

            try {
                passwordCorrect =
                        BCrypt.checkpw(password, user.getPasswordHash());
            } catch (Exception e) {
                e.printStackTrace();

                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                resp.getWriter().write(
                        "{\"success\":false,\"message\":\"Password verification error\"}"
                );
                return;
            }

            if (!passwordCorrect) {

                System.out.println("LOGIN FAILED: Wrong password");

                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                resp.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid username or password\"}"
                );
                return;
            }

            HttpSession httpSession = req.getSession(true);

            httpSession.setAttribute("userId", user.getId());
            httpSession.setAttribute("username", user.getUsername());
            httpSession.setAttribute("role", user.getRole());

            System.out.println("LOGIN SUCCESS");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Role: " + user.getRole());

            resp.getWriter().write(
                    "{\"success\":true,\"role\":\""
                            + user.getRole()
                            + "\",\"username\":\""
                            + user.getUsername()
                            + "\"}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            resp.getWriter().write(
                    "{\"success\":false,\"message\":\"Login server error\"}"
            );
        }
    }
}