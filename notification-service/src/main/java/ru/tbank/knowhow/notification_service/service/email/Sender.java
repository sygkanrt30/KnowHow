package ru.tbank.knowhow.notification_service.service.email;

import jakarta.annotation.Nullable;

public interface Sender {

    void send(String contact, String body, @Nullable String username, String subject);
}