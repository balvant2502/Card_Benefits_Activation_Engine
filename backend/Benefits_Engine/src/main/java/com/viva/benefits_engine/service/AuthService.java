package com.viva.benefits_engine.service;

import com.viva.benefits_engine.dto.LoginRequest;
import com.viva.benefits_engine.dto.RegisterRequest;
import com.viva.benefits_engine.dto.AuthResponse;
import com.viva.benefits_engine.dto.UserProfileResponse;
import com.viva.benefits_engine.models.User;
import com.viva.benefits_engine.models.UserRole;
import com.viva.benefits_engine.repository.UserRepository;
import com.viva.benefits_engine.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private VirtualCardService virtualCardService;

    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new AuthResponse(null, null, null, null, null, "Email already registered");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(UserRole.CUSTOMER);

        User savedUser = userRepository.save(user);
        virtualCardService.createInitialCard(savedUser);

        // Generate token
        String token = jwtTokenProvider.generateTokenFromEmail(savedUser.getEmail(), "ROLE_CUSTOMER");

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRole().name(),
                token,
                "Registration successful"
        );
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String authenticatedEmail = authentication.getName();
            User user = userRepository.findByEmail(authenticatedEmail)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
            String token = jwtTokenProvider.generateToken(authentication);

            return new AuthResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole().name(),
                    token,
                    "Login successful"
            );
        } catch (AuthenticationException e) {
            return new AuthResponse(null, null, null, null, null, "Invalid email or password");
        }
    }

    public AuthResponse getCurrentUser(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            User u = user.get();
            return new AuthResponse(
                    u.getId(),
                    u.getEmail(),
                    u.getName(),
                    u.getRole().name(),
                    null,
                    "User found"
            );
        }
        return new AuthResponse(null, null, null, null, null, "User not found");
    }

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(),
                user.getPhone(), user.getAddress(), user.getRole().name());
    }
}
