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
    @Mapping(target = "title", source = "course.businessDetails.title")
    @Mapping(target = "price", source = "course.businessDetails.price")
    @Mapping(target = "description", source = "course.businessDetails.description")
    @Mapping(target = "rating", source = "course.businessDetails.rating")
    @Mapping(target = "tags", source = "course.businessDetails.tags")
    @Mapping(target = "courseText", source = "course.businessDetails.courseText")
    CourseDto toDto(Course course);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "moderator", source = "moderator")
    @Mapping(target = "status", constant = "ON_MODERATION")
    @Mapping(target = "businessDetails.price", source = "price")
    @Mapping(target = "businessDetails.rating", constant = "0")
    @Mapping(target = "businessDetails.title", source = "request.title")
    @Mapping(target = "businessDetails.description", source = "request.description")
    @Mapping(target = "businessDetails.tags", source = "request.tags")
    @Mapping(target = "businessDetails.courseText", source = "request.courseText")
    Course toEntity(CreateCourseRequest request, User author, User moderator, Integer price);
}
