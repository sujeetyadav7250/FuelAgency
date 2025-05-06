package com.faos.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.faos.model.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);
    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * Send a simple text email.
     * @param toEmail  Recipient address
     * @param subject  Email subject
     * @param body     Email body (plain text)
     */
    public void sendEmail(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send email: recipient address is null or blank.");
            return; // or throw an exception if you want
        }

        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(toEmail);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(body);

            javaMailSender.send(simpleMailMessage);
            log.info("Email sent successfully to {}", toEmail);

        } catch (Exception e) {
            // Catches invalid addresses or other mailing issues (SMTP not configured, etc.)
            log.error("Failed to send email to {}. Reason: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String body, 
                                    byte[] attachment, String attachmentName, 
                                    String attachmentType) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            
            // Add the attachment
            ByteArrayResource resource = new ByteArrayResource(attachment);
            helper.addAttachment(attachmentName, resource, attachmentType);
            
            javaMailSender.send(message);
        } catch (MessagingException e) {
            // Fallback to email without attachment if there's an error
            sendEmail(to, subject, body);
            // Log error
            System.err.println("Failed to send email with attachment: " + e.getMessage());
        }
    }

	public void sendRegistrationEmail(User user) {
		// TODO Auto-generated method stub
		SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Welcome to Fuel Pro!");

        // Create a formatted email template
        String emailContent = String.format(
            "Dear %s %s,\n\n"
            + "Thank you for registering with Fuel Pro Management System.\n"
            + "Here are your registration details:\n\n"
            + "📌 **User ID:** %d\n"
            + "📌 **Email:** %s\n"
            + "📌 **Phone:** %s\n"
            + "📌 **Address:** %s\n"
            + "📌 **Registration Date:** %s\n"
            + "📌 **Connection Type:** %s\n"
            + "📌 **Connection Status:** %s\n\n"
            + "If you have any questions, feel free to contact us.\n\n"
            + "Best Regards,\n"
            + "Fuel Pro Team",
            user.getFirstName(),
            user.getLastName(),
            user.getUserId(),
            user.getEmail(),
            user.getPhone(),
            user.getAddress(),
            user.getRegistrationDate(),
            user.getConnectionType(),
            user.getConnectionStatus()
        );

        message.setText(emailContent);
        javaMailSender.send(message);
	}
	
	public void sendDeactivationEmail(User user) {
		// TODO Auto-generated method stub public void sendDeactivationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Account Deactivation");
        message.setText("Dear " + user.getFirstName() + ",\n\nYour account has been deactivated. Please contact support if you need assistance.");
        javaMailSender.send(message);
    }
	
	public void sendActivationEmail(User user) {
		// TODO Auto-generated method stub public void sendDeactivationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Account Deactivation");
        message.setText("Dear " + user.getFirstName() + ",\n\n"                
        		+ "Your account has been activated successfully. You can now access all features of our platform.\n\n"
                + "Thank you for choosing Fuel Pro Management System.\n\n"
                + "Best regards,\n");
        javaMailSender.send(message);
    }

	public void sendRegistrationEmail(Long UserId,String email, String Password) {
		SimpleMailMessage message = new SimpleMailMessage();
	    message.setTo(email);
	    message.setSubject("Welcome to Fuel Pro Management System - Your Login Credentials");

	    String emailContent = String.format(
	        "Dear User,\n\n"
	         + "Your account has been successfully registered.\n"
	         + "Here are your login details:\n\n"
	         + "📌 **UserId:** %d\n"
	         + "📌 **Email:** %s\n"
	         + "📌 **Password:** %s\n\n"
	         + "Please change your password after logging in.\n\n"
	         + "Regards,\nSupport Team",
	         UserId,email, Password
	    );

	        message.setText(emailContent);
	        javaMailSender.send(message);
	}
	
	public void sendUpdateNotificationEmail(User user) {
		
		 SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(user.getEmail());
	        message.setSubject("Your Account Details Have Been Updated");
	        
	        String emailBody = String.format(
	                "Fuel Pro Management System,\n\nYour account details have been updated successfully. Here are your updated details:\n\n" +
	                "First Name: %s\nLast Name: %s\nEmail: %s\nPhone: %s\nAddress: %s\nConnection Status: %s\nConnection Type: %s\n\n" +
	                "Thank you for using our service!",
	                user.getFirstName(),
	                user.getLastName(),
	                user.getEmail(),
	                user.getPhone(),
	                user.getAddress(),
	                user.getConnectionStatus(),
	                user.getConnectionType());

	        message.setText(emailBody);
	        javaMailSender.send(message);
		
	}
	
	public static Logger getLog() {
		return log;
	}

	public JavaMailSender getJavaMailSender() {
		return javaMailSender;
	}
    
}
