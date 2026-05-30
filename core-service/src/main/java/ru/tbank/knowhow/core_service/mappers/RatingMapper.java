package ru.tbank.knowhow.core_service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.tbank.knowhow.core_service.model.ratings.Rating;
import ru.tbank.knowhow.core_service.model.dto.rating.response.RatingDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RatingMapper {

    @Mapping(target = "userId", source = "rating.user.id")
    RatingDto toDto(Rating rating);
}
