package com.tech.emailvalidation.offerletter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.tech.emailvalidation.offerletter.config.OfferLetterProperties;
import com.tech.emailvalidation.offerletter.dto.EmailDispatchResult;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSenderServiceImpl implements EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final OfferLetterProperties properties;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public EmailSenderServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider, OfferLetterProperties properties) {
        this.mailSenderProvider = mailSenderProvider;
        this.properties = properties;
    }

    @Override
    public EmailDispatchResult sendOfferLetterEmail(
            String recipientEmail,
            String recipientName,
            String designation,
            String department,
            String packageAmount,
            String joiningDate,
            String address,
            String companyName,
            byte[] pdfBytes,
            String pdfFileName) {

        log.info("Preparing to send Offer Letter email to {} ({})", recipientName, recipientEmail);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("JavaMailSender bean is not available. Skipping real email dispatch for {}", recipientEmail);
            return EmailDispatchResult.failed(
                    "Mail service is not configured (missing JavaMailSender bean)",
                    "Configure spring.mail properties in application.yml to enable SMTP delivery."
            );
        }

        // Check if username/password are still default placeholders
        if (mailUsername == null || mailUsername.isBlank() || mailUsername.contains("your-email@gmail.com")) {
            log.warn("Spring Mail username is not configured or using placeholder '{}'. Skipping live SMTP delivery.", mailUsername);
            return EmailDispatchResult.failed(
                    "Mail delivery skipped: SMTP credentials not configured",
                    "Set SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD to your email provider credentials in application.yml."
            );
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String senderEmail = (mailUsername != null && !mailUsername.isBlank()) ? mailUsername : properties.getHr().getSenderEmail();
            String senderPersonal = properties.getHr().getSenderName() + " - " + companyName;

            helper.setFrom(senderEmail, senderPersonal);
            if (properties.getHr().getSenderEmail() != null && !properties.getHr().getSenderEmail().isBlank()) {
                helper.setReplyTo(properties.getHr().getSenderEmail());
            }
            helper.setTo(recipientEmail);
            helper.setSubject("Congratulations! You Have Been Selected – " + designation);

            String htmlBody = buildHtmlEmailBody(recipientName, designation, department, packageAmount, joiningDate, address, companyName);
            String textBody = buildPlainTextEmailBody(recipientName, designation, department, packageAmount, joiningDate, address, companyName);

            helper.setText(textBody, htmlBody);

            // Attach the generated PDF
            if (pdfBytes != null && pdfBytes.length > 0) {
                ByteArrayResource attachment = new ByteArrayResource(pdfBytes);
                helper.addAttachment(pdfFileName, attachment, "application/pdf");
            }

            mailSender.send(message);
            log.info("Offer letter email sent successfully to {}", recipientEmail);

            return EmailDispatchResult.ok("Offer letter email successfully delivered to " + recipientEmail);
        } catch (Exception e) {
            log.error("Failed to deliver offer letter email to {}: {}", recipientEmail, e.getMessage(), e);
            return EmailDispatchResult.failed(
                    "Failed to deliver email: " + e.getMessage(),
                    e.getClass().getSimpleName() + ": " + e.getMessage()
            );
        }
    }

    private String buildHtmlEmailBody(
            String recipientName,
            String designation,
            String department,
            String packageAmount,
            String joiningDate,
            String address,
            String companyName) {

        String hrEmail = properties.getHr().getSenderEmail();

        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f4f7fa; margin: 0; padding: 24px; color: #2d3748; line-height: 1.6; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 14px rgba(0,0,0,0.06); }" +
                ".header { background: linear-gradient(135deg, #0A3D91 0%, #1976D2 100%); color: #ffffff; padding: 32px 28px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 22px; font-weight: 700; }" +
                ".header p { margin: 6px 0 0 0; font-size: 14px; opacity: 0.9; }" +
                ".content { padding: 30px 28px; font-size: 15px; color: #2d3748; }" +
                ".card { background-color: #f8fafc; border-left: 4px solid #1976D2; border-radius: 4px; padding: 18px 20px; margin: 22px 0; }" +
                ".card-title { font-size: 14px; font-weight: 700; color: #0A3D91; margin-bottom: 10px; }" +
                ".card-list { list-style: none; padding: 0; margin: 0; }" +
                ".card-list li { padding: 5px 0; font-size: 14px; }" +
                ".label { font-weight: 600; color: #4a5568; display: inline-block; width: 140px; }" +
                ".value { font-weight: 600; color: #1a202c; }" +
                ".attachment-box { background-color: #ebf5fb; border: 1px dashed #90caf9; border-radius: 6px; padding: 14px; margin: 20px 0; text-align: center; color: #0A3D91; font-weight: 600; font-size: 14px; }" +
                ".signoff { margin-top: 24px; border-top: 1px solid #edf2f7; padding-top: 20px; font-size: 14px; color: #4a5568; }" +
                ".footer { background-color: #f7fafc; padding: 16px 28px; text-align: center; font-size: 12px; color: #a0aec0; border-top: 1px solid #edf2f7; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "  <div class='header'>" +
                "    <h1>Congratulations! 🎉</h1>" +
                "    <p>You Have Been Selected – " + escape(designation) + "</p>" +
                "  </div>" +
                "  <div class='content'>" +
                "    <p>Dear <strong>" + escape(recipientName) + "</strong>,</p>" +
                "    <p><strong>Congratulations! 🎉</strong></p>" +
                "    <p>We are delighted to inform you that you have been selected for the position of <strong>" + escape(designation) + "</strong> at <strong>" + escape(companyName) + "</strong>.</p>" +
                "    <p>Please find your official offer letter attached to this email. It contains your employment details, annual compensation package, joining date, and other applicable terms and conditions.</p>" +
                "    <div class='card'>" +
                "      <div class='card-title'>💼 Your Offer Summary</div>" +
                "      <ul class='card-list'>" +
                "        <li><span class='label'>• Designation:</span> <span class='value'>" + escape(designation) + "</span></li>" +
                "        <li><span class='label'>• Annual Package:</span> <span class='value'>" + escape(packageAmount) + "</span></li>" +
                "        <li><span class='label'>• Joining Date:</span> <span class='value'>" + escape(joiningDate) + "</span></li>" +
                "        <li><span class='label'>• Work Location:</span> <span class='value'>" + escape(address) + "</span></li>" +
                "      </ul>" +
                "    </div>" +
                "    <div class='attachment-box'>📎 Official Offer Letter PDF Attached</div>" +
                "    <p>Kindly review the attached offer letter and confirm your acceptance by replying to this email or contacting our HR department.</p>" +
                "    <p>We congratulate you on this achievement and look forward to having you as part of our team.</p>" +
                "    <div class='signoff'>" +
                "      Best regards,<br/><br/>" +
                "      <strong>Human Resources Department</strong><br/>" +
                "      " + escape(companyName) + "<br/>" +
                "      <a href='mailto:" + escape(hrEmail) + "' style='color: #1976D2; text-decoration: none;'>" + escape(hrEmail) + "</a>" +
                "    </div>" +
                "  </div>" +
                "  <div class='footer'>" +
                "    CONFIDENTIAL EMPLOYMENT DOCUMENT • " + escape(companyName) + "<br/>" +
                "  </div>" +
                "</div></body></html>";
    }

    private String buildPlainTextEmailBody(
            String recipientName,
            String designation,
            String department,
            String packageAmount,
            String joiningDate,
            String address,
            String companyName) {

        String hrEmail = properties.getHr().getSenderEmail();

        return "Dear " + recipientName + ",\n\n" +
                "Congratulations! 🎉\n\n" +
                "We are delighted to inform you that you have been selected for the position of " + designation + " at " + companyName + ".\n\n" +
                "Please find your official offer letter attached to this email. It contains your employment details, annual compensation package, joining date, and other applicable terms and conditions.\n\n" +
                "Your Offer Summary\n" +
                "- Designation: " + designation + "\n" +
                "- Annual Package: " + packageAmount + "\n" +
                "- Joining Date: " + joiningDate + "\n" +
                "- Work Location: " + address + "\n\n" +
                "Kindly review the attached offer letter and confirm your acceptance by replying to this email or contacting our HR department.\n\n" +
                "We congratulate you on this achievement and look forward to having you as part of our team.\n\n" +
                "Best regards,\n\n" +
                "Human Resources Department\n\n" +
                companyName + "\n\n" +
                hrEmail + "\n";
    }

    private String escape(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
