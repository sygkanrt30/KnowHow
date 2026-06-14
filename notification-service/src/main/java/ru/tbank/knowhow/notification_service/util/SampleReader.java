package ru.tbank.knowhow.notification_service.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public final class SampleReader {

    private static final String SAMPLE_DIRECTORY = "/email";

    public String readSample(String path) {
        try (var is = SampleReader.class.getResourceAsStream(SAMPLE_DIRECTORY + path)) {
            if (Objects.isNull(is)) {
                throw new FileNotFoundException("File not found in classpath: " + SAMPLE_DIRECTORY + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Unable to read sample file " + path, e);
        }
    }
}
