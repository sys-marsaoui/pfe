package com.example.hiring.services;

import com.example.hiring.entities.Candidature;
import com.example.hiring.entities.Users;
import com.example.hiring.entities.Post;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl {
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendApplicationReceivedEmail(Users user, Post post) {
        sendEmail(user.getEmail(), "Candidature reçue - " + post.getTitle(),
                "Bonjour " + user.getFirstName() + ",\n\nNous avons bien reçu votre candidature au poste « " +
                        post.getTitle() + " ». Vous pouvez suivre son avancement depuis votre profil.\n\nÉquipe recrutement");
    }

    public void sendCvVerifiedEmail(Users user) {
        sendEmail(user.getEmail(), "Votre CV a été vérifié",
                "Bonjour " + user.getFirstName() + ",\n\nVotre CV a été vérifié. Votre dossier poursuit son parcours de recrutement. Nous vous informerons si une action est attendue de votre part.\n\nÉquipe recrutement");
    }

    public void sendQuizResultEmail(Users user, String postTitle, int score, int total, boolean passed) {
        String outcome = passed ? "Vous avez réussi le quiz." : "Vous n’avez pas atteint le seuil de réussite.";
        sendEmail(user.getEmail(), "Résultat du quiz - " + postTitle,
                "Bonjour " + user.getFirstName() + ",\n\n" + outcome + "\nVotre score : " + score + " / " + total +
                        ".\nConsultez votre profil pour suivre la suite de votre candidature.\n\nÉquipe recrutement");
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);}
    public void sendEmailContact(String to, String subject, String text,String email,String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText("Je suis "+name +",\n"+"Mon Mail:"+email+",\n"+text);
        javaMailSender.send(message);
    }
    public void sendInterviewEmail(Users user, Candidature c) throws MessagingException {
        MimeMessage msg = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        String date = c.getInterviewDateTime().format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a"));
        helper.setTo(user.getEmail());
        helper.setSubject("Interview Invitation - RGI Group");

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }" +
                ".header { background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%); padding: 40px 30px; text-align: center; }" +
                ".logo { max-width: 180px; height: auto; }" +
                ".content { padding: 40px 30px; color: #333333; }" +
                ".title { color: #1e3a8a; font-size: 28px; margin-bottom: 20px; font-weight: 600; }" +
                ".message { font-size: 16px; line-height: 1.6; color: #555555; margin-bottom: 30px; }" +
                ".score-card { background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%); border-left: 4px solid #3b82f6; padding: 20px; margin: 25px 0; border-radius: 8px; }" +
                ".score-item { display: flex; justify-content: space-between; margin: 12px 0; font-size: 15px; }" +
                ".score-label { color: #666666; font-weight: 500; }" +
                ".score-value { color: #1e3a8a; font-weight: 700; font-size: 16px; }" +
                ".interview-box { background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); border-left: 4px solid #f59e0b; padding: 20px; margin: 25px 0; border-radius: 8px; text-align: center; }" +
                ".interview-date { color: #92400e; font-size: 20px; font-weight: 700; margin: 10px 0; }" +
                ".location-section { margin: 25px 0; text-align: center; }" +
                ".location-title { color: #1e3a8a; font-weight: 600; font-size: 16px; margin-bottom: 15px; }" +
                ".map-link { display: inline-block; background-color: #3b82f6; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; font-weight: 600; margin-top: 10px; }" +
                ".map-link:hover { background-color: #2563eb; }" +
                ".location-text { color: #666666; font-size: 14px; line-height: 1.5; }" +
                ".footer { background-color: #f8fafc; padding: 30px; text-align: center; color: #64748b; font-size: 13px; border-top: 1px solid #e2e8f0; }" +
                ".congratulations { background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; padding: 15px; border-radius: 8px; text-align: center; font-size: 18px; font-weight: 600; margin-bottom: 25px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<img src='https://www.rgigroup.com/wp-content/uploads/2021/03/rgi-group-logo.png' alt='RGI Group' class='logo'/>" +
                "</div>" +
                "<div class='content'>" +
                "<div class='congratulations'>🎉 Congratulations, " + user.getFirstName() + "!</div>" +
                "<div class='title'>You're Invited to an Interview</div>" +
                "<div class='message'>" +
                "We are impressed with your application and performance. Based on your exceptional results, we would like to invite you to an interview." +
                "</div>" +
                "<div class='score-card'>" +
                "<div class='score-item'>" +
                "<span class='score-label'>📝 Quiz Score:</span>" +
                "<span class='score-value'>" + c.getQuizScore() + "/" + c.getTotalQuizScore() + "</span>" +
                "</div>" +
                "<div class='score-item'>" +
                "<span class='score-label'>✅ ATS Match:</span>" +
                "<span class='score-value'>" + String.format("%.2f", c.getAtsScore()) + "%</span>" +
                "</div>" +
                "</div>" +
                "<div class='interview-box'>" +
                "<div style='color: #92400e; font-weight: 600; font-size: 14px; margin-bottom: 5px;'>📅 INTERVIEW SCHEDULED</div>" +
                "<div class='interview-date'>" + date + "</div>" +
                "</div>" +
                "<div class='location-section'>" +
                "<div class='location-title'>📍 Interview Location</div>" +
                "<div class='location-text'>" +
                "RGI Group Office<br/>" +
                "Please arrive 10 minutes early" +
                "</div>" +
                "<a href='https://maps.app.goo.gl/HD68Q45fTypD6YSC7' class='map-link' target='_blank'>View on Google Maps</a>" +
                "</div>" +
                "<div class='message' style='margin-top: 30px;'>" +
                "We look forward to meeting you. If you have any questions or need to reschedule, please don't hesitate to contact us." +
                "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "<strong>RGI Group</strong><br/>" +
                "© 2025 RGI Group. All rights reserved.<br/>" +
                "This is an automated message, please do not reply directly to this email." +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(htmlContent, true);
        javaMailSender.send(msg);
    }

    public void sendRejectionEmail(Users user, Candidature c) throws MessagingException {
        MimeMessage msg = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setTo(user.getEmail());
        helper.setSubject("Application Status Update - RGI Group");

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }" +
                ".header { background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%); padding: 40px 30px; text-align: center; }" +
                ".logo { max-width: 180px; height: auto; }" +
                ".content { padding: 40px 30px; color: #333333; }" +
                ".title { color: #1e3a8a; font-size: 24px; margin-bottom: 25px; font-weight: 600; }" +
                ".message { font-size: 16px; line-height: 1.8; color: #555555; margin-bottom: 20px; }" +
                ".info-box { background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%); border-left: 4px solid #3b82f6; padding: 20px; margin: 25px 0; border-radius: 8px; }" +
                ".footer { background-color: #f8fafc; padding: 30px; text-align: center; color: #64748b; font-size: 13px; border-top: 1px solid #e2e8f0; }" +
                ".highlight { color: #1e3a8a; font-weight: 600; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<img src='https://www.rgigroup.com/assets/img/rgi_logo.svg' alt='RGI Group' class='logo'/>" +
                "</div>" +
                "<div class='content'>" +
                "<div class='title'>Application Status Update</div>" +
                "<div class='message'>" +
                "Dear " + user.getFirstName() + "," +
                "</div>" +
                "<div class='message'>" +
                "Thank you for taking the time to complete our assessment and for your interest in joining RGI Group. We truly appreciate the effort you put into your application." +
                "</div>" +
                "<div class='message'>" +
                "After careful consideration of all candidates, we regret to inform you that we will not be moving forward with your application at this time. This decision was difficult, as we received many qualified applications for this position." +
                "</div>" +
                "<div class='info-box'>" +
                "<div class='message' style='margin: 0;'>" +
                "We encourage you to apply for future opportunities that match your skills and experience. We will keep your application on file and may contact you if a suitable position becomes available." +
                "</div>" +
                "</div>" +
                "<div class='message'>" +
                "We wish you all the best in your job search and future career endeavors." +
                "</div>" +
                "<div class='message'>" +
                "Best regards,<br/>" +
                "<span class='highlight'>RGI Group Recruitment Team</span>" +
                "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "<strong>RGI Group</strong><br/>" +
                "© 2025 RGI Group. All rights reserved.<br/>" +
                "This is an automated message, please do not reply directly to this email." +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(htmlContent, true);
        javaMailSender.send(msg);
    }

}
