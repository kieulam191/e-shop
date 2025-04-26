package com.dev.e_shop.auth;

import com.dev.e_shop.exception.AlreadyResourceException;
import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
import com.dev.e_shop.user.UserRepository;
import com.dev.e_shop.user.dto.RegisterRequest;
import com.dev.e_shop.user.dto.LoginRequest;
import com.dev.e_shop.user.dto.LoginResponse;
import com.dev.e_shop.user.dto.RegisterResponse;
import com.dev.e_shop.user.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest body) {
        if(this.userRepository.existsByEmail(body.email())) {
            throw new AlreadyResourceException("Email already exists in system");
        }

        User user  = userMapper.toUser(body);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = this.userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest body) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(body.email(), body.password());
        Authentication authenticated = authenticationManager.authenticate(authentication);

        UserDetail userDetail = (UserDetail) authenticated.getPrincipal();
        String roles = getAuthority(authenticated);
        String token = jwtService.generateToken(userDetail);

        SecurityContextHolder.getContext().setAuthentication(authenticated);

        return new LoginResponse(
                userDetail.getUsername(),
                roles,
                token);
    }

    private String getAuthority(Authentication authenticated) {
        return authenticated.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}
