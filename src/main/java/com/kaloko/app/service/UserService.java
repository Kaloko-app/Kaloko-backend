package com.kaloko.app.service;

import com.kaloko.app.dto.*;
import com.kaloko.app.entity.User;
import com.kaloko.app.entity.Gender;
import com.kaloko.app.exception.*;
import com.kaloko.app.repository.UserRepository;
import com.kaloko.app.security.JwtService;
import com.kaloko.app.security.RefreshTokenService;
import com.kaloko.app.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthenticationResponseDTO register(UserRegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setGender(request.gender());
        user.setPassword(hashPassword(request.password()));

        User savedUser = userRepository.save(user);
        
        String token = jwtService.generateToken(savedUser.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());
        
        return new AuthenticationResponseDTO(token, refreshToken.getToken(), convertToResponseDTO(savedUser));
    }

    @Transactional
    public AuthenticationResponseDTO login(UserLoginRequestDTO request) {
        User user = userRepository.findByUsername(request.username())
                .or(() -> userRepository.findByEmail(request.username()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!verifyPassword(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthenticationResponseDTO(token, refreshToken.getToken(), convertToResponseDTO(user));
    }

    @Transactional
    public TokenRefreshResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user.getUsername());
                    return new TokenRefreshResponseDTO(token, request.refreshToken());
                })
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));
    }

    @Transactional
    public UserResponseDTO updateMetrics(UserMetricsUpdateRequestDTO request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));

        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (!user.getUsername().equals(currentUsername)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update these metrics");
        }

        user.setCurrentWeight(request.currentWeight());
        user.setWeightGoal(request.weightGoal());
        user.setHeight(request.height());
        user.setAge(request.age());
        user.setActivityLevel(request.activityLevel());
        user.setGender(request.gender());
        user.setBodyFatPercentage(request.bodyFatPercentage());

        calculateAndSetGoals(user);

        User updatedUser = userRepository.save(user);
        return convertToResponseDTO(updatedUser);
    }

    private void calculateAndSetGoals(User user) {
        if (user.getCurrentWeight() == null || user.getHeight() == null || user.getAge() == null || user.getActivityLevel() == null) {
            return;
        }

        // BMR calculation using Mifflin-St Jeor (constant: female = -161, male/unspecified = +5)
        double genderConstant = (user.getGender() == Gender.FEMALE) ? -161 : 5;
        double bmr = (10 * user.getCurrentWeight()) + (6.25 * user.getHeight()) - (5 * user.getAge()) + genderConstant;

        double activityMultiplier = switch (user.getActivityLevel()) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
        };

        double tdee = bmr * activityMultiplier;
        int dailyCalories = (int) Math.round(tdee);

        // Hypertrophy oriented macros:
        // Protein: 2.2g per kg of current weight
        int proteinGoal = (int) Math.round(user.getCurrentWeight() * 2.2);

        // Fat: 25% of calories (range is 20-25%)
        double fatCalories = dailyCalories * 0.25;
        int fatGoal = (int) Math.round(fatCalories / 9.0);

        // Carbs: Remaining calories
        int proteinCalories = proteinGoal * 4;
        int fatCaloriesActual = fatGoal * 9;
        int remainingCalories = dailyCalories - proteinCalories - fatCaloriesActual;
        int carbGoal = remainingCalories > 0 ? (int) Math.round(remainingCalories / 4.0) : 0;

        user.setDailyCalories(dailyCalories);
        user.setProteinGoal(proteinGoal);
        user.setFatGoal(fatGoal);
        user.setCarbGoal(carbGoal);
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCurrentWeight(),
                user.getWeightGoal(),
                user.getHeight(),
                user.getAge(),
                user.getActivityLevel(),
                user.getGender(),
                user.getBodyFatPercentage(),
                user.getDailyCalories(),
                user.getProteinGoal(),
                user.getCarbGoal(),
                user.getFatGoal()
        );
    }

    private String hashPassword(String password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt, 600000, 256);
        
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        
        return "600000:" + saltBase64 + ":" + hashBase64;
    }

    private boolean verifyPassword(String password, String storedPassword) {
        String[] parts = storedPassword.split(":");
        if (parts.length != 3) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hash = Base64.getDecoder().decode(parts[2]);

            byte[] testHash = pbkdf2(password.toCharArray(), salt, iterations, hash.length * 8);

            return MessageDigest.isEqual(hash, testHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error computing PBKDF2 hash", e);
        }
    }
}
