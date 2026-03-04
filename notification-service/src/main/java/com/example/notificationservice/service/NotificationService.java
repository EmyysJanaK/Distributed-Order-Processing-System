package com.example.notificationservice.service;

import com.example.commonevents.OrderCreatedEvent;
import com.example.commonevents.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    public void notifyOrderCreated(OrderCreatedEvent event) {
        log.info("Sending order-created notification for orderId={}", event.getOrderId());
        sendEmail(
                "customer@example.com", // In production: lookup customer email by customerId
                "Order Confirmation - " + event.getOrderId(),
                String.format("Your order %s has been placed successfully. Total: %s",
                        event.getOrderId(), event.getTotalAmount())
        );
    }

    public void notifyPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Sending payment notification for orderId={} status={}",
                event.getOrderId(), event.getStatus());
        sendEmail(
                "customer@example.com",
                "Payment Update - Order " + event.getOrderId(),
                String.format("Payment for order %s is %s. Amount: %s",
                        event.getOrderId(), event.getStatus(), event.getAmount())
        );
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}

