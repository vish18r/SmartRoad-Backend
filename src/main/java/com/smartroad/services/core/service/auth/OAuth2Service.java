package com.smartroad.services.core.service.auth;

import com.smartroad.services.common.enums.OAuthType;
import com.smartroad.services.common.enums.SessionStatus;
import com.smartroad.services.common.enums.auth.UserStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.auth.AuthResponseDTO;
import com.smartroad.services.core.dto.auth.UserResponseDTO;
import com.smartroad.services.core.mapper.auth.UserMapper;
import com.smartroad.services.domain.entity.OAuthStateEntity;
import com.smartroad.services.domain.entity.SessionEntity;
import com.smartroad.services.domain.entity.UserEntity;
import com.smartroad.services.domain.entity.UserAuditLogEntity;
import com.smartroad.services.domain.repository.OAuthStateRepository;
import com.smartroad.services.domain.repository.SessionRepository;
import com.smartroad.services.domain.repository.UserAuditLogRepository;
import com.smartroad.services.domain.repository.UserRepository;
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
                .state(state)
                .oauthType(oauthType)
                .expiresAt(expiresAt)
                .build();

        oauthState.setCreatedBy(SYSTEM_USER_ID);
        oauthState.setModifiedBy(SYSTEM_USER_ID);

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
     * @return AuthResponseDTO containing access token, refresh token, and user info
     * @throws SmartRoadException if state is invalid, expired, or user creation fails
     */
    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO handleOAuth2Callback(String state, String code, OAuthType oauthType) throws SmartRoadException {
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

        if (oauthState.getOauthType() != oauthType) {
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
                .userId(user.getId())
                .token(refreshToken)
                .status(SessionStatus.ACTIVE)
                .expiresAt(OffsetDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
                .build();

        session.setCreatedBy(user.getId());
        session.setModifiedBy(user.getId());

        sessionRepository.save(session);

        createAuditLog(user.getId(), user.getId(), "OAUTH_LOGIN", "SUCCESS");

        UserResponseDTO userResponse = mapToUserResponse(user);
        log.info("OAuth2 login successful for user: {}", user.getEmailId());

        return AuthResponseDTO.builder()
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
                .emailId(email)
                .oauthSigninId(oauthSigninId)
                .status(UserStatusEnum.ACTIVE)
                .emailVerifiedYn(true)
                .build();

        user.setOauthType(oauthType.getValue());
        user.setCreatedBy(SYSTEM_USER_ID);
        user.setModifiedBy(SYSTEM_USER_ID);

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
        user.setOauthSigninId(oauthSigninId);
        user.setOauthType(oauthType.getValue());
        user.setEmailVerifiedYn(true);
        user.setModifiedBy(user.getId());

        userRepository.save(user);
        createAuditLog(user.getId(), user.getId(), "OAUTH_LINK", "SUCCESS");
        log.info("Updated OAuth user: {}", user.getEmailId());
        return user;
    }

    /**
     * Maps User entity to UserResponseDTO DTO.
     *
     * @param user the User entity
     * @return the UserResponseDTO DTO
     */
    private UserResponseDTO mapToUserResponse(UserEntity user) {
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
                .userId(userId)
                .actionDone(action)
                .ntStatus(status)
                .requestedBy(performedBy.toString())
                .build();

        auditLog.setCreatedBy(performedBy);
        auditLog.setModifiedBy(performedBy);

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
