package com.peti.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("PETI Password Reset OTP");

        // HTML email template
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Arial', sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background-color: #4CAF50;
                        padding: 20px;
                        text-align: center;
                        color: white;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        padding: 30px;
                        text-align: center;
                    }
                    .otp-box {
                        display: inline-block;
                        background-color: #e8f5e9;
                        padding: 15px 30px;
                        border-radius: 5px;
                        font-size: 24px;
                        font-weight: bold;
                        color: #2e7d32;
                        margin: 20px 0;
                        letter-spacing: 5px;
                    }
                    .content p {
                        color: #333333;
                        font-size: 16px;
                        line-height: 1.6;
                    }
                    .footer {
                        background-color: #f4f4f4;
                        padding: 20px;
                        text-align: center;
                        font-size: 14px;
                        color: #777777;
                    }
                    .footer a {
                        color: #4CAF50;
                        text-decoration: none;
                    }
                    @media only screen and (max-width: 600px) {
                        .container {
                            width: 100%%;
                            margin: 10px;
                        }
                        .content {
                            padding: 20px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>PETI Password Reset</h1>
                    </div>
                    <div class="content">
                        <h2>Your One-Time Password (OTP)</h2>
                        <p>Use the following OTP to reset your password. This code is valid for <strong>10 minutes</strong>.</p>
                        <div class="otp-box">%s</div>
                        <p>If you did not request a password reset, please ignore this email or contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 PETI. All rights reserved.</p>
                        <p><a href="mailto:support@peti.com">Contact Support</a></p>
                    </div>
                </div>
            </body>
            </html>
            """, otp);

        helper.setText(htmlContent, true); // true indicates HTML content
        mailSender.send(message);
    }
}