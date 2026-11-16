package com.emergency.alert.service;

import com.emergency.alert.dto.AuthRequest;
import com.emergency.alert.dto.AuthResponse;
import com.emergency.alert.dto.RegisterRequest;
import com.emergency.alert.dto.UserProfileResponse;
import com.emergency.alert.model.AlertCategory;
import com.emergency.alert.model.Role;
import com.emergency.alert.model.User;
import com.emergency.alert.model.UserSubscription;
import com.emergency.alert.repository.UserRepository;
import com.emergency.alert.repository.UserSubscriptionRepository;
import com.emergency.alert.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository,
                       UserSubscriptionRepository subscriptionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered");
        }

        // Default role is ROLE_USER
        Role userRole = Role.ROLE_USER;

        // Hash password securely with BCrypt
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getUsername().trim(),
                request.getEmail().trim().toLowerCase(),
                encodedPassword,
                request.getFullName().trim(),
                request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null,
                userRole
        );

        User savedUser = userRepository.save(user);

        // Add category subscriptions if provided (or default to all categories for demo)
        Set<AlertCategory> subs = request.getSubscriptions();
        if (subs == null || subs.isEmpty()) {
            subs = Set.of(AlertCategory.FIRE, AlertCategory.FLOOD, AlertCategory.EARTHQUAKE, AlertCategory.SECURITY);
        }

        for (AlertCategory cat : subs) {
            subscriptionRepository.save(new UserSubscription(savedUser, cat));
        }

        String token = tokenProvider.generateTokenFromUsername(savedUser.getUsername(), savedUser.getRole().name());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name(),
                subs
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticateUser(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = tokenProvider.generateToken(authentication);

        Set<AlertCategory> subscriptions = subscriptionRepository.findByUserId(user.getId()).stream()
                .map(UserSubscription::getCategory)
                .collect(Collectors.toSet());

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                subscriptions
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Set<AlertCategory> subscriptions = subscriptionRepository.findByUserId(user.getId()).stream()
                .map(UserSubscription::getCategory)
                .collect(Collectors.toSet());

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole().name());
        response.setEnabled(user.isEnabled());
        response.setCreatedAt(user.getCreatedAt());
        response.setSubscriptions(subscriptions);

        return response;
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }
}
