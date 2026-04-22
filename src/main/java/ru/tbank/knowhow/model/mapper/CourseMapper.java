package ru.tbank.knowhow.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {

    @Mapping(target = "authorId", source = "course.author.id")
    CourseDto toDto(Course course);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "moderator", source = "moderator")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "status", constant = "ON_MODERATION")
    @Mapping(target = "rating", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    Course toEntity(CreateCourseRequest request, User author, User moderator, Long price);
}
