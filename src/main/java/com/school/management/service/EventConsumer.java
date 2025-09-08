package com.school.management.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);


     @Autowired
    JavaMailSender mailSender;
    @KafkaListener(topics = EventPublisher.NOTIFICATION_EVENTS_TOPIC, groupId = "notification-events-group")
    public void consumeEvent(EventPublisher.SchoolEvent<EventPublisher.NotificationData> event) {
        if ("SEND_EMAIL".equals(event.getEventType())) {
            EventPublisher.NotificationData data = event.getData();
            handleSendEmail(data);
        }
    }

    private void handleSendEmail(EventPublisher.NotificationData data) {
        try {
            sendEmail(
                    data.getEmail(),
                    data.getSubject(),
                    data.getMessage()
            );
            System.out.println("Email sent successfully to: " + data.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + data.getEmail());
            e.printStackTrace();
        }
    }

    private void sendEmail(String email, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
     //   message.setFrom("your_email@gmail.com"); // optional, defaults to spring.mail.username
        mailSender.send(message);
    }
}
