package com.luidmidev.template.spring.services.email;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Autowired
    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendSimpleMail(String to, String subject, String content) {
        var emailDetails = new EmailDetails(to, subject, content);
        sendSimpleMail(emailDetails);

    }

    public void sendMailWithAttachment(String to, String subject, String content, EmailAttachment... attachment) throws MessagingException {
        var emailDetails = new EmailDetails(to, subject, content);
        sendMailWithAttachment(emailDetails, attachment);
    }

    public void sendSimpleMail(EmailDetails details) {

        var mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(sender);
        mailMessage.setTo(details.getTo());
        mailMessage.setText(details.getContent());
        mailMessage.setSubject(details.getSubject());

        mailSender.send(mailMessage);

    }

    public void sendMailWithAttachment(EmailDetails details, EmailAttachment... attachment) throws MessagingException {

        var mimeMessage = mailSender.createMimeMessage();
        var mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(sender);
        mimeMessageHelper.setTo(details.getTo());
        mimeMessageHelper.setText(details.getContent());
        mimeMessageHelper.setSubject(details.getSubject());

        for (var attach : attachment) {
            mimeMessageHelper.addAttachment(attach.getName(), attach);
        }

        mailSender.send(mimeMessage);
    }

    public void sendHtmlMail(String to, String subject, String content) throws MessagingException {
        var emailDetails = new EmailDetails(to, subject, content);
        sendHtmlMail(emailDetails);
    }

    public void sendHtmlMail(EmailDetails details) throws MessagingException {
        var message = mailSender.createMimeMessage();
        message.setSubject(details.getSubject());
        var helper = new MimeMessageHelper(message, true);
        helper.setFrom(sender);
        helper.setTo(details.getTo());
        helper.setText(details.getContent(), true);
        mailSender.send(message);
    }
}
