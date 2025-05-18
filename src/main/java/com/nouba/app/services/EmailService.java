package com.nouba.app.services;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class EmailService {
    private final JavaMailSender mailSender;



    //send info to your email
    public void sendAgencyCreationEmail(String toEmail, String agencyName,
                                        String address, String phone,
                                        String cityName, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Votre compte agence a été créé");

        String emailContent = String.format(
                "Bonjour,\n\n" +
                        "Votre compte agence a été créé avec succès.\n\n" +
                        "Détails de votre agence:\n" +
                        "Nom: %s\n" +
                        "Adresse: %s\n" +
                        "Téléphone: %s\n" +
                        "Ville: %s\n" +
                        "Email: %s\n" +
                        "Mot de passe: %s\n\n" +
                        "\n" +
                        "\n" +
                        "\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Nouba",
                agencyName, phone, address, cityName, toEmail, password
        );

        message.setText(emailContent);
        mailSender.send(message);
    }
    //end that
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String loadEmailTemplate(String templatePath, Map<String, String> values) throws Exception {
        // Charger le fichier HTML
        String content = Files.readString(new ClassPathResource(templatePath).getFile().toPath(), StandardCharsets.UTF_8);

        // Remplacer les variables dynamiques
        StrSubstitutor substitutor = new StrSubstitutor(values);
        return substitutor.replace(content);
    }

    public void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
    /**
     * Sends welcome email to newly registered client with their account details
     *
     * @param email Client's email address
     * @param name Client's full name
     * @param password Client's plain text password (for initial setup)
     * @param phone Client's phone number
     * @param address Client's physical address
     * @throws Exception if template loading or email sending fails
     */
    public void sendClientWelcomeEmail(String email, String name, String password, String phone, String address) throws Exception {
        // Prepare template variables
        Map<String, String> variables = Map.of(
                "name", name,
                "email", email,
                "password", password,
                "phone", phone,
                "address", address
        );

        // Load and populate template
        String content = loadEmailTemplate("templates.emails/welcome-client.html", variables);

        // Send email
        sendEmail(email, "Bienvenue chez Nouba", content);
    }

    /**
     * Sends login notification email when client successfully logs in
     *
     * @param email Client's email address
     * @param name Client's full name
     * @throws Exception if template loading or email sending fails
     */
    public void sendLoginNotificationEmail(String email, String name) throws Exception {
        // Format current timestamp
        String loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Prepare template variables
        Map<String, String> variables = Map.of(
                "name", name,
                "loginTime", loginTime
        );

        // Load and populate template
        String content = loadEmailTemplate("templates.emails/login-notification.html", variables);

        // Send email
        sendEmail(email, "Connexion réussie", content);
    }

  /**  public void sendTicketVerificationEmail(
            String email,
            String ticketNumber,
            String clientName,
            String agencyName,
            String city,
            String status,
            int positionInQueue,
            String estimatedWaitTime) {

        String subject = "Ticket Verification: " + ticketNumber;
        String content = String.format(
                "Dear %s,\n\n" +
                        "Your ticket verification details:\n\n" +
                        "Ticket Number: %s\n" +
                        "Agency: %s\n" +
                        "City: %s\n" +
                        "Current Status: %s\n" +
                        "Position in Queue: %d\n" +
                        "Estimated Wait Time: %s\n\n" +
                        "Thank you for using our services.",
                clientName, ticketNumber, agencyName, city,
                status, positionInQueue, estimatedWaitTime
        );

        sendSimpleEmail(email, subject, content);
    }

    private void sendSimpleEmail(String email, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
        System.out.println("Email sent successfully!");
        System.out.println("Email subject: " + subject);
        System.out.println("Email content: " + content);
        System.out.println("Email to: " + email);
    }*/

}