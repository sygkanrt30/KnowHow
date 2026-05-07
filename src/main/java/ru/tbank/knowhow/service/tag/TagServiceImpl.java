package ru.tbank.knowhow.service.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.repository.CourseRepository;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public Set<String> findAllTags() {
        return courseRepository.getTags()
                .flatMap(Arrays::stream)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
