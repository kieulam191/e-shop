package com.dev.e_shop.user.profile.mapper;

import com.dev.e_shop.user.profile.Profile;
import com.dev.e_shop.user.profile.dto.ProfileResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileResponse toProfileResponse(Profile source);
}
