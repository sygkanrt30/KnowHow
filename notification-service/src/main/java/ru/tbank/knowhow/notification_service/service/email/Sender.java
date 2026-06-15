package ru.tbank.knowhow.notification_service.service.email;

public interface Sender {

    void send(String contact, String body, String subject);
}