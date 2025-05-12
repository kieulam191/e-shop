package com.dev.e_shop.user;

import com.dev.e_shop.exception.custom.NotFoundException;
import com.dev.e_shop.user.profile.Profile;
import com.dev.e_shop.user.profile.ProfileRepository;
import com.dev.e_shop.user.profile.dto.ProfileRequest;
import com.dev.e_shop.user.profile.dto.ProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceCacheIntegrationTest {

    @Autowired
    UserService userService;

    @MockitoSpyBean
    ProfileRepository profileRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private UserDetail userDetail;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setId(1L);

        userDetail = new UserDetail(user);
    }

    @Test
    void getProfile_whenCacheHit_returnsProfileFromCache() {
        //given
        String cacheKey = "user::1";
        redisTemplate.opsForValue().set(cacheKey, new ProfileResponse("HCM city", "1234567890"));

        //when
        userService.getProfile(userDetail);

        //then
        ProfileResponse cache =(ProfileResponse) redisTemplate.opsForValue().get(cacheKey);

        assertThat(cache).isNotNull();
        assertThat(cache.address()).isEqualTo("HCM city");
        assertThat(cache.phone()).isEqualTo("1234567890");

        verify(profileRepository, never()).findByUserId(1L);
    }

    @Test
    void getProfile_whenCacheMiss_returnsProfileFromDB() {
        //given
        String cacheKey = "user::1";
        redisTemplate.delete(cacheKey);

        //when
        ProfileResponse profile = userService.getProfile(userDetail);

        //then
        Boolean exitsCacheKey = redisTemplate.hasKey(cacheKey);
        assertTrue(exitsCacheKey, "Should create cache key after getting profile");

        ProfileResponse cacheProfile = (ProfileResponse) redisTemplate.opsForValue().get(cacheKey);

        assertThat(cacheProfile).isNotNull();
        assertThat(cacheProfile).isEqualTo(profile);

        verify(profileRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getProfile_whenCacheExpires_returnsProfileFromDb() {
        //given
        String cacheKey = "user::1";
        redisTemplate.delete(cacheKey);

        // when
        ProfileResponse profile = userService.getProfile(userDetail);

        //then
        Boolean existsCacheKey = redisTemplate.hasKey(cacheKey);
        assertTrue(existsCacheKey);

        ProfileResponse cacheProfile = (ProfileResponse) redisTemplate.opsForValue().get(cacheKey);

        assertThat(cacheProfile).isNotNull();
        assertThat(cacheProfile).isEqualTo(profile);

        verify(profileRepository, times(1)).findByUserId(1L);
    }


    @Test
    void updateProfile_whenUpdated_refreshesCacheWithLatestProfile() {
        //given
        ProfileRequest body = new ProfileRequest("hcm", "1234567895");
        Profile profile = new Profile();
        profile.setAddress("hcm");
        profile.setPhone("1234567890");
        String cacheKey = "user::1";

        given(this.profileRepository.findByUserId(1L))
                .willReturn(Optional.of(profile));

        redisTemplate.opsForValue().set(cacheKey, new ProfileResponse("hcm", "1234567890"));

        //when
        ProfileResponse profileResponse = userService.updateProfile(userDetail, body);

        //then
        ProfileResponse cachedProfile =(ProfileResponse) redisTemplate.opsForValue().get(cacheKey);
        assertThat(cachedProfile).isNotNull();
        assertEquals("hcm", cachedProfile.address());
        assertEquals("1234567895", cachedProfile.phone());
        assertThat(profileResponse).isEqualTo(cachedProfile);
    }

    @Test
    void updateProfile_whenUserNotFound_shouldThrowAndNotTouchCache() {
        //given
        ProfileRequest body = new ProfileRequest("hcm", "1234567895");
        Profile profile = new Profile();
        profile.setAddress("hcm");
        profile.setPhone("1234567890");
        String cacheKey = "user::1";

        given(this.profileRepository.findByUserId(1L))
                .willReturn(Optional.empty());

        redisTemplate.opsForValue().set(cacheKey, new ProfileResponse("hcm", "1234567890"));

        //when and then
        assertThrows(NotFoundException.class, () -> {
            userService.updateProfile(userDetail, body);
        });

        Boolean existsCacheKey = redisTemplate.hasKey(cacheKey);
        assertTrue(existsCacheKey, "Should existing cache key before updating profile");

        ProfileResponse afterCache =(ProfileResponse) redisTemplate.opsForValue().get(cacheKey);
        assertThat(afterCache).isNotNull();
        assertEquals("hcm", afterCache.address());
        assertEquals("1234567890", afterCache.phone());
    }
}
