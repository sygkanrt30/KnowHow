package ru.tbank.knowhow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.knowhow.service.tag.TagService;

import java.util.Set;

@RestController
@RequestMapping("${server.base-url.tag}")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<Set<String>> getTags() {
        return ResponseEntity.ok(tagService.findAllTags());
    }
}
