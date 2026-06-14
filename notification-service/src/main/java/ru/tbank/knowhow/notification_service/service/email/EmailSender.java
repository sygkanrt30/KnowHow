package ru.tbank.knowhow.notification_service.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender implements Sender {

    private final JavaMailSender mailSender;

    @Override
    public void send(String contact, String body, String username) {

    }
}


//<!DOCTYPE html>
//<html lang="ru">
//<head>
//    <meta charset="UTF-8">
//    <meta name="viewport" content="width=device-width, initial-scale=1.0">
//    <title>Подтверждение Email</title>
//    <style>
//        body {
//            font-family: Arial, sans-serif;
//        }
//        h1 {
//            font-family: Verdana, Geneva, sans-serif;
//            font-size: 24px;
//            font-weight: 700;
//            line-height: 26.4px;
//        }
//        p {
//            font-family: Verdana, Geneva, sans-serif;
//            font-size: 24px;
//            font-weight: 700;
//            line-height: 26.4px;
//        }
//    </style>
//</head>
//<body>
//<h1>Ваш код для подтверждения email в приложение ToDoList:</h1>
//<p>{{code}}</p>
//</body>
//</html>
