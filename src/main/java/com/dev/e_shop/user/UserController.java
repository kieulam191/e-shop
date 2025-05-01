package com.dev.e_shop.user;

import com.dev.e_shop.dto.ApiResponse;
import com.dev.e_shop.user.profile.dto.ProfileRequest;
import com.dev.e_shop.user.profile.dto.ProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/me")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getUserInfo(@AuthenticationPrincipal UserDetail userDetail) {
        ProfileResponse profile = userService.getProfile(userDetail);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get profile of the user success",
                        profile
                ));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<ProfileResponse>>  updateUserInfo(
            @AuthenticationPrincipal UserDetail userDetail,
            @Valid @RequestBody ProfileRequest profileRequest) {
        ProfileResponse profile = userService.updateProfile(userDetail, profileRequest);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Update profile of the user success",
                        profile
                ));
    }
}
