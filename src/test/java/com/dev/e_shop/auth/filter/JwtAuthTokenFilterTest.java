package com.dev.e_shop.auth.filter;

import com.dev.e_shop.auth.JwtService;
import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JwtAuthTokenFilterTest {
    @InjectMocks
    JwtAuthTokenFilter authTokenJwtFilter;

    @Mock
    JwtService jwtService;

    @Mock
    UserDetailsService userDetailsService;

    private User user;

    private UserDetail userDetail;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("test123");

        userDetail = new UserDetail(user);
    }

    @Test
    void doFilterInternal_withValidToken_passesRequestToFilterChain() throws ServletException, IOException {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        given(jwtService.extractUsername("valid-token")).willReturn("test@gmail.com");
        given(userDetailsService.loadUserByUsername("test@gmail.com")).willReturn(userDetail);

        //when
        authTokenJwtFilter.doFilter(request, response, filterChain);

        //then
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInValidToken_throwsJwtException() throws ServletException, IOException {
        //given
        given(jwtService.extractUsername("invalid-token")).willThrow(JwtException.class);
        //when
        assertThrows(JwtException.class, () -> {
            jwtService.extractUsername("invalid-token");
        });
    }

}