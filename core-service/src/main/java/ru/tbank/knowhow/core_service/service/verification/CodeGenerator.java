package ru.tbank.knowhow.core_service.service.verification;

import org.jspecify.annotations.NonNull;

import java.util.Random;

final class CodeGenerator {

    @NonNull static String generateCode() {
        var random = new Random();
        int firstCodePart = 100 + random.nextInt(900);
        int secondCodePart = 100 + random.nextInt(900);
        return String.format("%d%03d", firstCodePart, secondCodePart);
    }
}