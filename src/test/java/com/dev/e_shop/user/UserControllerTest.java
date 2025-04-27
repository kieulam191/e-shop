package com.dev.e_shop.user;

import com.dev.e_shop.user.profile.Profile;
import com.dev.e_shop.user.profile.dto.ProfileRequest;
import com.dev.e_shop.user.profile.dto.ProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @MockitoBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(value = "user", username = "test@gmail.com", roles = {"USER"})
    void getUserInfo_withAuthenticated_returnsProfileResponse() throws Exception {
        //given
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUserId(1L);

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setId(1L);

        UserDetail userDetail = new UserDetail(user);

        ProfileResponse profileResponse = new ProfileResponse(null, null);
        given(userService.getProfile(userDetail)).willReturn(profileResponse);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetail, null,
                userDetail.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //when and then
        this.mockMvc.perform(get("/api/user/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get profile of the user success"))
                .andExpect(jsonPath("$.data.address").isEmpty())
                .andExpect(jsonPath("$.data.phone").isEmpty());
    }

    @Test
    void getUserInfo_withoutAuth_throwsUnauthorizedException() throws Exception {
        //given
        SecurityContextHolder.clearContext();

        //when and then
        this.mockMvc.perform(get("/api/user/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.errors").value("Access denied: Please provide a valid authentication token"))
                .andExpect(jsonPath("$.path").value("/api/user/me"));
    }

    @Test
    @WithMockUser(value = "user", username = "test@gmail.com", roles = {"USER"})
    void updateUserInfo_withValidInput_returnsUpdatedProfileResponse() throws Exception {
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

        String json = objectMapper.writeValueAsString(body);

        given(this.userService.updateProfile(any(UserDetail.class), any(ProfileRequest.class)))
                .willReturn(profileResponse);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetail, null,
                userDetail.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //when and then
        this.mockMvc.perform(patch("/api/user/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Update profile of the user success"))
                .andExpect(jsonPath("$.data.address").value("HCM city"))
                .andExpect(jsonPath("$.data.phone").value("1234567890"));
    }

    @Test
    void updateUserInfo_withoutAuth_throwsUnauthorizedException() throws Exception {
        //given
        ProfileRequest body = new ProfileRequest("HCM city", "1234567890");
        String json = objectMapper.writeValueAsString(body);

        SecurityContextHolder.clearContext();

        //when and then
        this.mockMvc.perform(patch("/api/user/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.errors").value("Access denied: Please provide a valid authentication token"))
                .andExpect(jsonPath("$.path").value("/api/user/me"));
    }

    @Test
    @WithMockUser(value = "user", username = "test@gmail.com", roles = {"USER"})
    void updateUserInfo_withPhoneLess10_throwsBadRequestException() throws Exception {
        //given
        ProfileRequest body = new ProfileRequest("", "1234567");

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

        String json = objectMapper.writeValueAsString(body);

        given(this.userService.updateProfile(any(UserDetail.class), any(ProfileRequest.class)))
                .willThrow(DataIntegrityViolationException.class);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetail, null,
                userDetail.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //when and then
        this.mockMvc.perform(patch("/api/user/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.errors.phone").value("Phone must be least at 10 number"))
                .andExpect(jsonPath("$.path").value("/api/user/me"));
    }
}