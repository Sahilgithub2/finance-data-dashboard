package com.finance.mapper;

import com.finance.dto.transaction.TransactionResponse;
import com.finance.dto.user.UserResponse;
import com.finance.model.Transaction;
import com.finance.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    UserResponse toUserResponse(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
