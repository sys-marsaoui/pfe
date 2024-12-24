package com.example.hiring.authModule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {
 /*   public void sendSimpleEmail(String toEmail,
                                String subject,
                                String body
    ) {
        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");{
            Properties props = new Properties();

            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");


            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {

                    return new PasswordAuthentication("shopify.4se5@gmail.com", "uemzwprxpdyzqpao");
                }
            });


            try {
                Message message = new MimeMessage(session);

                message.setFrom(new InternetAddress("noreplyshopify23@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);

                System.out.println("Sent");

            } catch (MessagingException e) {
                System.out.println("ERROR: " + e.getMessage());
            }


        }*/

    @Autowired
    private JavaMailSender javaMailSender;
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
        javaMailSender.send(message);}

}
