package ru.tbank.knowhow.core_service.service.courses.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.core_service.service.courses.GetCourseService;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final GetCourseService getCourseService;

    @Override
    public Set<String> findAllTags() {
        return getCourseService.findAllTags()
                .flatMap(Arrays::stream)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
