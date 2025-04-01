package com.ndifreke.core_banking_api.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTransactionEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom("test@example.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error sending email to: " + to + ": " + e.getMessage());
        }
    }

//    public void sendVerificationEmail(String to, String verificationCode, String verificationUrl) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8"); // true = multipart
//            helper.setFrom("your-email@example.com");
//            helper.setTo(to);
//            helper.setSubject("Please Verify Your Account");
//            helper.setText(
//                    "<html><body>" +
//                            "<p>Dear User,</p>" +
//                            "<p>Please click the following link to verify your account:</p>" +
//                            "<a href='" + verificationUrl + "?code=" + verificationCode + "'>Verify Account</a>" +
//                            "<p>Or, you can manually enter the following code: <b>" + verificationCode + "</b></p>" +
//                            "</body></html>",
//                    true // true = isHtml
//            );
//
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            // Handle exception (e.g., log error)
//        }
//    }

    public void sendLoginEmail(String to, String firstName, String lastName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom("test@example.com");
            helper.setTo(to);
            helper.setSubject("Successful Login");
            helper.setText(
                    "<html><body>" +
                            "<p>Dear " + firstName + " " + lastName + ",</p>" +
                            "<p>You have successfully logged in to your account.</p>" +
                            "</body></html>",
                    true
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Handle exception
            System.err.println("Error sending email to: " + to + ": " + e.getMessage());
        }
    }
}