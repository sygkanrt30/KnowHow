package ru.tbank.knowhow.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.response.BalanceDto;
import ru.tbank.knowhow.model.dto.response.UsernameAndBalanceResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsernameAndBalanceResponseMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "balance", expression = "java(toBalanceDto(user))")
    UsernameAndBalanceResponse toUsernameAndBalanceResponse(User user);

    default BalanceDto toBalanceDto(User user) {
        return new BalanceDto(
                user.getId(),
                user.getBalance().getId(),
                user.getBalance().getCoins()
        );
    }
}
