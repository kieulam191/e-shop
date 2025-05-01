package com.dev.e_shop.user.mapper;

import com.dev.e_shop.user.User;
import com.dev.e_shop.user.dto.RegisterRequest;
import com.dev.e_shop.user.dto.RegisterResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    RegisterResponse toUserResponse(User source);
    User toUser(RegisterRequest source);
}
