package com.eduguard.controller;

import com.eduguard.model.User;
import com.eduguard.util.HibernateUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

@WebServlet("/api/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");


        // CHECK EMPTY FIELDS

        if (username == null ||
                newPassword == null ||
                confirmPassword == null ||
                username.trim().isEmpty() ||
                newPassword.isEmpty() ||
                confirmPassword.isEmpty()) {

            resp.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            resp.getWriter().write(
                    "{\"success\":false,\"message\":\"All fields are required\"}"
            );

            return;
        }


        username = username.trim();


        // CHECK PASSWORD LENGTH

        if (newPassword.length() < 6) {

            resp.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            resp.getWriter().write(
                    "{\"success\":false,\"message\":\"Password must contain at least 6 characters\"}"
            );

            return;
        }


        // CHECK PASSWORD MATCH

        if (!newPassword.equals(confirmPassword)) {

            resp.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            resp.getWriter().write(
                    "{\"success\":false,\"message\":\"Passwords do not match\"}"
            );

            return;
        }


        Transaction transaction = null;


        try (Session session =
                     HibernateUtil.getFactory().openSession()) {


            // FIND USER

            User user = session.createQuery(
                            "FROM User WHERE username = :username",
                            User.class
                    )
                    .setParameter("username", username)
                    .uniqueResult();


            // USER NOT FOUND

            if (user == null) {

                resp.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                resp.getWriter().write(
                        "{\"success\":false,\"message\":\"Username not found\"}"
                );

                return;
            }


            // START TRANSACTION

            transaction = session.beginTransaction();


            // CREATE NEW PASSWORD HASH

            String newPasswordHash =
                    BCrypt.hashpw(
                            newPassword,
                            BCrypt.gensalt()
                    );


            // UPDATE PASSWORD

            user.setPasswordHash(newPasswordHash);

            session.merge(user);


            // COMMIT

            transaction.commit();


            // SUCCESS

            resp.getWriter().write(
                    "{\"success\":true,\"message\":\"Password reset successfully. You can now login with your new password.\"}"
            );


        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            e.printStackTrace();

            resp.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            resp.getWriter().write(
                    "{\"success\":false,\"message\":\"Password reset failed\"}"
            );
        }
    }
}