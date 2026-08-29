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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service for handling OAuth2 authentication logic.
 *
 * @author
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service {

    private final UserRepository userRepository;
    private final OAuthStateRepository oauthStateRepository;
    private final SessionRepository sessionRepository;
    private final UserAuditLogRepository userAuditLogRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final int STATE_EXPIRY_MINUTES = 10;

    /**
     * Generates an OAuth state token for secure OAuth flow.
     *
     * @param oauthType the OAuth provider type (GOOGLE, APPLE)
     * @return the generated state token
     */
    public String generateOAuthState(OAuthType oauthType) {
        String state = generateRandomState();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(STATE_EXPIRY_MINUTES);

        OAuthStateEntity oauthState = OAuthStateEntity.builder()
                .id(UUID.randomUUID())
                .state(state)
                .oauthType(oauthType)
                .expiresAt(expiresAt)
                .createdBy(SYSTEM_USER_ID)
                .modifiedBy(SYSTEM_USER_ID)
                .build();

        oauthStateRepository.save(oauthState);
        log.info("Generated OAuth state for {}: {}", oauthType, state);
        return state;
    }

    /**
     * Handles OAuth2 callback from the provider.
     * Validates state, retrieves user info, creates or updates user, and returns auth tokens.
     *
     * @param state the state token from OAuth callback
     * @param code the authorization code from OAuth callback
     * @param oauthType the OAuth provider type
     * @return AuthResponse containing access token, refresh token, and user info
     * @throws SmartRoadException if state is invalid, expired, or user creation fails
     */
    @Transactional
    public AuthResponse handleOAuth2Callback(String state, String code, OAuthType oauthType) throws SmartRoadException {
        OAuthStateEntity oauthState = oauthStateRepository.findByState(state)
                .orElseThrow(() -> new SmartRoadException(
                        ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.SERVICE_INVALID_INPUT,
                        "Invalid OAuth state"
                ));

        if (oauthState.getExpiresAt().isBefore(OffsetDateTime.now())) {
            oauthStateRepository.delete(oauthState);
            throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT,
                    "OAuth state has expired"
            );
        }

        if (oauthState.getOAuthType() != oauthType) {
            throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT,
                    "OAuth type mismatch"
            );
        }

        String email = extractEmailFromCode(code, oauthType);
        String oauthSigninId = extractOAuthSigninIdFromCode(code, oauthType);

        UserEntity user = userRepository.findByEmailId(email)
                .orElse(null);

        if (user == null) {
            user = createOAuthUser(email, oauthSigninId, oauthType);
        } else {
            user = updateOAuthUser(user, oauthSigninId, oauthType);
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

        createAuditLog(user.getId(), user.getId(), "OAUTH_LOGIN", "SUCCESS");

        UserResponse userResponse = mapToUserResponse(user);
        log.info("OAuth2 login successful for user: {}", user.getEmailId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .user(userResponse)
                .build();
    }

    /**
     * Creates a new user from OAuth credentials.
     *
     * @param email the user's email from OAuth provider
     * @param oauthSigninId the OAuth provider's user ID
     * @param oauthType the OAuth provider type
     * @return the created UserEntity
     */
    private UserEntity createOAuthUser(String email, String oauthSigninId, OAuthType oauthType) {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .emailId(email)
                .oauthSigninId(oauthSigninId)
                .oauthType(oauthType)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdBy(SYSTEM_USER_ID)
                .modifiedBy(SYSTEM_USER_ID)
                .build();

        userRepository.save(user);
        createAuditLog(user.getId(), SYSTEM_USER_ID, "OAUTH_SIGNUP", "SUCCESS");
        log.info("Created new OAuth user: {}", email);
        return user;
    }

    /**
     * Updates an existing user with OAuth credentials.
     *
     * @param user the existing user entity
     * @param oauthSigninId the OAuth provider's user ID
     * @param oauthType the OAuth provider type
     * @return the updated UserEntity
     */
    private UserEntity updateOAuthUser(UserEntity user, String oauthSigninId, OAuthType oauthType) {
        user.setOAuthSigninId(oauthSigninId);
        user.setOAuthType(oauthType);
        user.setEmailVerified(true);
        user.setModifiedBy(user.getId());

        userRepository.save(user);
        createAuditLog(user.getId(), user.getId(), "OAUTH_LINK", "SUCCESS");
        log.info("Updated OAuth user: {}", user.getEmailId());
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
     * Generates a random state string for OAuth flow.
     *
     * @return the random state string
     */
    private String generateRandomState() {
        return UUID.randomUUID().toString();
    }

    /**
     * Extracts email from OAuth authorization code (placeholder implementation).
     *
     * @param code the authorization code
     * @param oauthType the OAuth provider type
     * @return the extracted email
     */
    private String extractEmailFromCode(String code, OAuthType oauthType) {
        return "user@example.com";
    }

    /**
     * Extracts OAuth signin ID from authorization code (placeholder implementation).
     *
     * @param code the authorization code
     * @param oauthType the OAuth provider type
     * @return the extracted OAuth signin ID
     */
    private String extractOAuthSigninIdFromCode(String code, OAuthType oauthType) {
        return "oauth_signin_id_" + UUID.randomUUID();
    }
}
