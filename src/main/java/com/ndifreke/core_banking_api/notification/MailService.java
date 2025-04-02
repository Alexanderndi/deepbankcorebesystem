package com.ndifreke.core_banking_api.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * The type Mail service.
 */
@Service
public class MailService {

    private final JavaMailSender mailSender;

    /**
     * Instantiates a new Mail service.
     *
     * @param mailSender the mail sender
     */
    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Generic method to send an email with HTML content.
     *
     * @param to      the to
     * @param subject the subject
     * @param text    the text
     */
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

    /**
     * Email for when funds are debited from the sender's account in a transfer.
     *
     * @param to                the to
     * @param firstName         the first name
     * @param amount            the amount
     * @param fromAccountNumber the from account number
     * @param toAccountNumber   the to account number
     * @param description       the description
     */
    public void sendTransferDebitEmail(String to, String firstName, BigDecimal amount,
                                       String fromAccountNumber, String toAccountNumber, String description) {
        String subject = "CBA DEBIT Transaction Notification";
        String text = String.format(
                "<html><body>" +
                        "<p>Dear %s,</p>" +
                        "<p>A transfer of <b>%.2f</b> has been debited from your account (%s) to account (%s).</p>" +
                        "<p>Description: %s</p>" +
                        "</body></html>",
                firstName, amount, fromAccountNumber, toAccountNumber, description
        );
        sendTransactionEmail(to, subject, text);
    }

    /**
     * Email for when funds are credited to the receiver's account in a transfer.
     *
     * @param to                the to
     * @param firstName         the first name
     * @param amount            the amount
     * @param toAccountNumber   the to account number
     * @param fromAccountNumber the from account number
     * @param description       the description
     */
    public void sendTransferCreditEmail(String to, String firstName, BigDecimal amount,
                                        String toAccountNumber, String fromAccountNumber, String description) {
        String subject = "CBA CREDIT Transaction Notification\"";
        String text = String.format(
                "<html><body>" +
                        "<p>Dear %s,</p>" +
                        "<p>A transfer of <b>%.2f</b> has been credited to your account (%s) from account (%s).</p>" +
                        "<p>Description: %s</p>" +
                        "</body></html>",
                firstName, amount, toAccountNumber, fromAccountNumber, description
        );
        sendTransactionEmail(to, subject, text);
    }

    /**
     * Email for a successful deposit.
     *
     * @param to            the to
     * @param firstName     the first name
     * @param amount        the amount
     * @param accountNumber the account number
     */
    public void sendDepositEmail(String to, String firstName, BigDecimal amount, String accountNumber) {
        String subject = "CBA CREDIT Transaction Notification\"";
        String text = String.format(
                "<html><body>" +
                        "<p>Dear %s,</p>" +
                        "<p>A deposit of <b>%.2f</b> has been credited to your account (%s).</p>" +
                        "</body></html>",
                firstName, amount, accountNumber
        );
        sendTransactionEmail(to, subject, text);
    }

    /**
     * Email for a successful withdrawal.
     *
     * @param to            the to
     * @param firstName     the first name
     * @param amount        the amount
     * @param accountNumber the account number
     */
    public void sendWithdrawalEmail(String to, String firstName, BigDecimal amount, String accountNumber) {
        String subject = "CBA CREDIT Transaction Notification\"";
        String text = String.format(
                "<html><body>" +
                        "<p>Dear %s,</p>" +
                        "<p>A withdrawal of <b>%.2f</b> has been debited from your account (%s).</p>" +
                        "</body></html>",
                firstName, amount, accountNumber
        );
        sendTransactionEmail(to, subject, text);
    }

    /**
     * Email for fraud alerts, specifying the rule broken and optionally the amount.
     *
     * @param to     the to
     * @param reason the reason
     * @param amount the amount
     */
    public void sendFraudAlertEmail(String to, String reason, BigDecimal amount) {
        String subject = "Fraud Alert: Transaction Blocked";
        String text;
        if ("Large transfer amount".equals(reason) && amount != null) {
            text = String.format(
                    "<html><body>" +
                            "<p>Dear User,</p>" +
                            "<p>Your transaction attempt was blocked due to a large transfer amount: %.2f</p>" +
                            "<p>Please review your transaction and try again later.</p>" +
                            "</body></html>",
                    amount
            );
        } else {
            text = String.format(
                    "<html><body>" +
                            "<p>Dear User,</p>" +
                            "<p>Your transaction attempt was blocked due to: %s</p>" +
                            "<p>Please wait and try again later.</p>" +
                            "</body></html>",
                    reason
            );
        }
        sendTransactionEmail(to, subject, text);
    }

    /**
     * Email for successful login (unchanged from original).
     *
     * @param to        the to
     * @param firstName the first name
     * @param lastName  the last name
     */
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
}