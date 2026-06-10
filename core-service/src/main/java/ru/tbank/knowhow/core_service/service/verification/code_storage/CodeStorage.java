package ru.tbank.knowhow.core_service.service.verification.code_storage;

import java.util.Optional;

public interface CodeStorage {

    void saveCode(String contact, String code);

    Optional<String> getCode(String contact);

    void deleteCode(String contact);

    boolean isBlocked(String contact);

    void blockForResend(String contact);
}
