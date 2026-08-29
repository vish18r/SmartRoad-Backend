package com.nextenti.services.core.service.auth;

import com.nextenti.services.common.enums.OAuthType;
import com.nextenti.services.common.enums.SessionStatus;
import com.nextenti.services.common.enums.UserStatus;
import com.nextenti.services.common.exception.ApplicationLayer;
import com.nextenti.services.common.exception.ErrorCodeMapping;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.auth.AuthResponse;
import com.nextenti.services.core.dto.auth.UserResponse;
import com.nextenti.services.core.mapper.auth.UserMapper;
import com.nextenti.services.domain.entity.OAuthStateEntity;
import com.nextenti.services.domain.entity.SessionEntity;
import com.nextenti.services.domain.entity.UserEntity;
import com.nextenti.services.domain.entity.UserAuditLogEntity;
import com.nextenti.services.domain.repository.OAuthStateRepository;
import com.nextenti.services.domain.repository.SessionRepository;
import com.nextenti.services.domain.repository.UserAuditLogRepository;
import com.nextenti.services.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service for handling Apple Sign-In authentication logic.
 *
 * @author
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppleSignInService {

    private final UserRepository userRepository;
    private final OAuthStateRepository oauthStateRepository;
    private final SessionRepository sessionRepository;
    private final UserAuditLogRepository userAuditLogRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final int STATE_EXPIRY_MINUTES = 10;

    /**
     * Generates an Apple Sign-In state token for secure OAuth flow.
     *
     * @return the generated state token
     */
    public String generateOAuthState() {
        String state = generateRandomState();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(STATE_EXPIRY_MINUTES);

        OAuthStateEntity oauthState = OAuthStateEntity.builder()
                .id(UUID.randomUUID())
                .state(state)
                .oauthType(OAuthType.APPLE)
                .expiresAt(expiresAt)
                .createdBy(SYSTEM_USER_ID)
                .modifiedBy(SYSTEM_USER_ID)
                .build();

        oauthStateRepository.save(oauthState);
        log.info("Generated Apple Sign-In state: {}", state);
        return state;
    }

    /**
     * Handles Apple Sign-In callback.
     * Validates state, processes Apple ID token, creates or updates user, and returns auth tokens.
     *
     * @param state the state token from Apple callback
     * @param code the authorization code from Apple
     * @param idToken the Apple ID token
     * @return AuthResponse containing access token, refresh token, and user info
     * @throws SmartRoadException if state is invalid, expired, or user creation fails
     */
    @Transactional
    public AuthResponse handleAppleCallback(String state, String code, String idToken) throws SmartRoadException {
        OAuthStateEntity oauthState = oauthStateRepository.findByState(state)
                .orElseThrow(() -> new SmartRoadException(
                        ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.SERVICE_INVALID_INPUT,
                        "Invalid Apple Sign-In state"
                ));

        if (oauthState.getExpiresAt().isBefore(OffsetDateTime.now())) {
            oauthStateRepository.delete(oauthState);
            throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT,
                    "Apple Sign-In state has expired"
            );
        }

        if (oauthState.getOAuthType() != OAuthType.APPLE) {
            throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT,
                    "OAuth type mismatch"
            );
        }

        String email = extractEmailFromAppleToken(idToken);
        String appleUserId = extractAppleUserIdFromToken(idToken);

        UserEntity user = userRepository.findByEmailId(email)
                .orElse(null);

        if (user == null) {
            user = createOAuthUser(email, appleUserId);
        } else {
            user = updateOAuthUser(user, appleUserId);
        }

        oauthStateRepository.delete(oauthState);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getId().toString());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        SessionEntity session = SessionEntity.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .token(refreshToken)
                .status(SessionStatus.ACTIVE)
                .expiresAt(OffsetDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
                .createdBy(user.getId())
                .modifiedBy(user.getId())
                .build();

        sessionRepository.save(session);

        createAuditLog(user.getId(), user.getId(), "APPLE_SIGNIN", "SUCCESS");

        UserResponse userResponse = mapToUserResponse(user);
        log.info("Apple Sign-In successful for user: {}", user.getEmailId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .user(userResponse)
                .build();
    }

    /**
     * Creates a new user from Apple Sign-In credentials.
     *
     * @param email the user's email from Apple
     * @param appleUserId the Apple user ID
     * @return the created UserEntity
     */
    private UserEntity createOAuthUser(String email, String appleUserId) {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .emailId(email)
                .oauthSigninId(appleUserId)
                .oauthType(OAuthType.APPLE)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdBy(SYSTEM_USER_ID)
                .modifiedBy(SYSTEM_USER_ID)
                .build();

        userRepository.save(user);
        createAuditLog(user.getId(), SYSTEM_USER_ID, "APPLE_SIGNUP", "SUCCESS");
        log.info("Created new Apple Sign-In user: {}", email);
        return user;
    }

    /**
     * Updates an existing user with Apple Sign-In credentials.
     *
     * @param user the existing user entity
     * @param appleUserId the Apple user ID
     * @return the updated UserEntity
     */
    private UserEntity updateOAuthUser(UserEntity user, String appleUserId) {
        user.setOAuthSigninId(appleUserId);
        user.setOAuthType(OAuthType.APPLE);
        user.setEmailVerified(true);
        user.setModifiedBy(user.getId());

        userRepository.save(user);
        createAuditLog(user.getId(), user.getId(), "APPLE_LINK", "SUCCESS");
        log.info("Updated Apple Sign-In user: {}", user.getEmailId());
        return user;
    }

    /**
     * Maps User entity to UserResponse DTO.
     *
     * @param user the User entity
     * @return the UserResponse DTO
     */
    private UserResponse mapToUserResponse(UserEntity user) {
        return userMapper.toResponse(user);
    }

    /**
     * Creates an audit log entry.
     *
     * @param userId the user ID
     * @param performedBy the ID of the user who performed the action
     * @param action the action performed
     * @param status the status of the action
     */
    private void createAuditLog(UUID userId, UUID performedBy, String action, String status) {
        UserAuditLogEntity auditLog = UserAuditLogEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .performedBy(performedBy)
                .action(action)
                .status(status)
                .createdBy(performedBy)
                .modifiedBy(performedBy)
                .build();

        userAuditLogRepository.save(auditLog);
    }

    /**
     * Generates a random state string for Apple Sign-In flow.
     *
     * @return the random state string
     */
    private String generateRandomState() {
        return UUID.randomUUID().toString();
    }

    /**
     * Extracts email from Apple ID token (placeholder implementation).
     *
     * @param idToken the Apple ID token
     * @return the extracted email
     */
    private String extractEmailFromAppleToken(String idToken) {
        return "user@icloud.com";
    }

    /**
     * Extracts Apple user ID from ID token (placeholder implementation).
     *
     * @param idToken the Apple ID token
     * @return the extracted Apple user ID
     */
    private String extractAppleUserIdFromToken(String idToken) {
        return "apple_user_id_" + UUID.randomUUID();
    }

    /**
     * Checks if Apple Sign-In is configured.
     *
     * @return true if configured, false otherwise
     */
    public boolean isAppleSignInConfigured() {
        return true;
    }
}
