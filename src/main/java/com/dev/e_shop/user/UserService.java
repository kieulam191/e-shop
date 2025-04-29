package com.dev.e_shop.user;

import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.user.profile.Profile;
import com.dev.e_shop.user.profile.ProfileRepository;
import com.dev.e_shop.user.profile.dto.ProfileRequest;
import com.dev.e_shop.user.profile.dto.ProfileResponse;
import com.dev.e_shop.user.profile.mapper.ProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public UserService(ProfileRepository profileRepository, ProfileMapper profileMapper) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
    }

    public ProfileResponse getProfile(UserDetail userDetail) {
        Profile profile = profileRepository.findByUserId(userDetail.getId())
                .orElseGet(() -> creatProfile(userDetail.getId()));

        return profileMapper.toProfileResponse(profile);
    }

    @Transactional
    private Profile creatProfile(long userId) {
        Profile profile = new Profile();
        profile.setUserId(userId);

        return profileRepository.save(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(UserDetail userDetail, ProfileRequest body) {
        Profile updatedProfile = profileRepository.findByUserId(userDetail.getId())
                .map(userProfile -> {
                    userProfile.setAddress(body.address());
                    userProfile.setPhone(body.phone());

                    return profileRepository.save(userProfile);
                })
                .orElseThrow(() -> new NotFoundException("User not found"));

        return profileMapper.toProfileResponse(updatedProfile);
    }
}
