package com.dev.e_shop.user;

import com.dev.e_shop.user.profile.Profile;
import com.dev.e_shop.user.profile.ProfileRepository;
import com.dev.e_shop.user.profile.dto.ProfileRequest;
import com.dev.e_shop.user.profile.dto.ProfileResponse;
import com.dev.e_shop.user.profile.mapper.ProfileMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Mock
    ProfileRepository profileRepository;

    @Mock
    ProfileMapper profileMapper;


    @Test
    void getUserInfo_withAuthenticated_returnsProfileResponse() {
        //given
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUserId(1L);

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setId(1L);

        UserDetail userDetail = new UserDetail(user);

        ProfileResponse profileResponse = new ProfileResponse(null, null);

        given(profileRepository.findByUserId(1L)).willReturn(Optional.of(profile));
        given(profileMapper.toProfileResponse(any(Profile.class))).willReturn(profileResponse);
        //when
        ProfileResponse actual = userService.getProfile(userDetail);

        //then
        assertNotNull(actual);
        assertNull(actual.address());
        assertNull(actual.phone());
    }


    @Test
    void updateUserInfo_withAuthenticated_returnsUpdatedProfileResponse() {
        //given
        ProfileRequest body = new ProfileRequest("HCM city", "1234567890");

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setId(1L);

        UserDetail userDetail = new UserDetail(user);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUserId(1L);

        Profile updatedProfile = new Profile();
        updatedProfile.setId(1L);
        updatedProfile.setAddress(body.address());
        updatedProfile.setPhone(body.phone());
        updatedProfile.setUserId(1L);

        ProfileResponse profileResponse = new ProfileResponse(updatedProfile.getAddress(), updatedProfile.getPhone());

        given(profileRepository.findByUserId(1L)).willReturn(Optional.of(profile));
        given(profileRepository.save(any(Profile.class))).willReturn(updatedProfile);
        given(profileMapper.toProfileResponse(any(Profile.class))).willReturn(profileResponse);
        //when
        ProfileResponse actual = userService.updateProfile(userDetail, body);

        //then
        assertNotNull(actual);
        assertEquals("HCM city", actual.address());
        assertEquals("1234567890", actual.phone());
    }

}