package com.smartroad.services.core.service.auth;

import com.smartroad.services.common.enums.OtpFlow;
import com.smartroad.services.common.enums.SessionStatus;
import com.smartroad.services.common.enums.auth.UserStatusEnum;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.core.dto.auth.SignupRequestDTO;
import com.smartroad.services.core.dto.auth.LoginRequestDTO;
import com.smartroad.services.core.dto.auth.AuthResponseDTO;
import com.smartroad.services.core.dto.auth.RefreshTokenRequestDTO;
import com.smartroad.services.core.dto.auth.TokenResponseDTO;
import com.smartroad.services.core.dto.auth.UserResponseDTO;
import com.smartroad.services.core.dto.auth.ForgotPasswordRequestDTO;
import com.smartroad.services.core.dto.auth.ResetPasswordRequestDTO;
import com.smartroad.services.core.dto.auth.ChangePasswordRequestDTO;
import com.smartroad.services.core.dto.auth.VerifyOtpRequestDTO;
import com.smartroad.services.core.dto.auth.ResendOtpRequestDTO;
import com.smartroad.services.domain.entity.SessionEntity;
import com.smartroad.services.domain.entity.UserEntity;
import com.smartroad.services.domain.entity.UserAuditLogEntity;
import com.smartroad.services.domain.repository.SessionRepository;
import com.smartroad.services.domain.repository.UserAuditLogRepository;
import com.smartroad.services.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Vishal
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final UserAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /**
     * Registers a new user account with email/phone verification.
     * Validates password confirmation, checks email/phone uniqueness, creates user record with PENDING status,
     * generates OTP for email verification, and creates audit log entry.
     *
     * @param request the signup request containing user details and password
     * @throws SmartRoadException if password mismatch, email/phone already exists, or database operation fails
     */
    @Transactional
    public void signup(SignupRequestDTO request) throws SmartRoadException {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "passwords.do.not.match");
        }

        if (request.getEmail() != null && userRepository.existsByEmailId(request.getEmail())) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "email.already.registered");
        }

        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "phone.number.already.registered");
        }

        UserEntity user = new UserEntity();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmailId(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCountryCode(request.getCountryCode());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatusEnum.PENDING);
        user.setEmailVerifiedYn(false);
        user.setCreatedBy(SYSTEM_USER_ID);
        user.setModifiedBy(SYSTEM_USER_ID);

        userRepository.save(user);

        createAuditLog(user.getId(), SYSTEM_USER_ID, "SIGNUP", "SUCCESS");

        if (user.getPhoneNumber() != null) {
            otpService.generateOtpForPhone(user.getPhoneNumber(), OtpFlow.SIGNUP_VERIFICATION);
            log.info("OTP sent to phone: {}", user.getPhoneNumber());
        }

        if (user.getEmailId() != null) {
            otpService.generateOtp(user.getEmailId(), OtpFlow.SIGNUP_VERIFICATION);
            log.info("OTP sent to email: {}", user.getEmailId());
        }

        log.info("User signed up successfully: {}", user.getEmailId());
    }

    /**
     * Authenticates a user with email/phone and password.
     * Validates credentials, checks user status, generates JWT access and refresh tokens,
     * creates session record, and logs the login action.
     *
     * @param request the login request containing identifier and password
     * @return AuthResponseDTO containing access token, refresh token, and user details
     * @throws SmartRoadException if authentication fails, user not found, or account not active
     */
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) throws SmartRoadException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
        );

        UserEntity user = userRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "user.not.found"));

        if (user.getStatus() == UserStatusEnum.BLOCKED) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_ACCESS_DENIED, "account.blocked");
        }

        if (user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_ACCESS_DENIED, "account.not.active");
        }

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

        createAuditLog(user.getId(), user.getId(), "LOGIN", "SUCCESS");

        UserResponseDTO userResponse = mapToUserResponse(user);

        log.info("User logged in successfully: {}", user.getEmailId());

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .user(userResponse)
                .build();
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * Validates the refresh token, checks session status and expiration,
     * generates new access token, and updates session last used timestamp.
     *
     * @param request the refresh token request containing the refresh token
     * @return TokenResponseDTO containing new access token and expiration time
     * @throws SmartRoadException if refresh token is invalid, expired, or session not active
     */
    @Transactional
    public TokenResponseDTO refreshToken(RefreshTokenRequestDTO request) throws SmartRoadException {
        SessionEntity session = sessionRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_UNAUTHORIZED, "invalid.refresh.token"));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_UNAUTHORIZED, "session.not.active");
        }

        if (session.getExpiresAt().isBefore(OffsetDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_UNAUTHORIZED, "refresh.token.expired");
        }

        UserEntity user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "user.not.found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getId().toString());

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        session.setLastUsedAt(OffsetDateTime.now());
        sessionRepository.save(session);

        log.info("Token refreshed for user: {}", user.getEmailId());

        return TokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .build();
    }

    /**
     * Logs out a user by revoking their current session.
     * Validates the refresh token belongs to the user, marks session as REVOKED,
     * records revocation timestamp, and creates audit log entry.
     *
     * @param userId the UUID of the user to logout
     * @param refreshToken the refresh token to revoke
     * @throws SmartRoadException if refresh token is invalid or does not belong to user
     */
    @Transactional
    public void logout(UUID userId, String refreshToken) throws SmartRoadException {
        SessionEntity session = sessionRepository.findByToken(refreshToken)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_UNAUTHORIZED, "invalid.session"));

        if (!session.getUserId().equals(userId)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_ACCESS_DENIED, "session.does.not.belong.to.user");
        }

        session.setStatus(SessionStatus.REVOKED);
        session.setRevokedAt(OffsetDateTime.now());
        sessionRepository.save(session);

        createAuditLog(userId, userId, "LOGOUT", "SUCCESS");

        log.info("User logged out: {}", userId);
    }

    /**
     * Logs out a user from all devices by revoking all their active sessions.
     * Marks all sessions as REVOKED, records revocation timestamps,
     * and creates audit log entry.
     *
     * @param userId the UUID of the user to logout from all devices
     * @throws SmartRoadException if database operation fails
     */
    @Transactional
    public void logoutAll(UUID userId) throws SmartRoadException {
        sessionRepository.findByUserId(userId).forEach(session -> {
            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(OffsetDateTime.now());
            sessionRepository.save(session);
        });

        createAuditLog(userId, userId, "LOGOUT_ALL", "SUCCESS");

        log.info("User logged out from all devices: {}", userId);
    }

    /**
     * Retrieves the current user's profile information.
     * Fetches user details by ID and maps to response DTO.
     *
     * @param userId the UUID of the user to retrieve
     * @return UserResponseDTO containing user profile details
     * @throws SmartRoadException if user not found
     */
    public UserResponseDTO getCurrentUser(UUID userId) throws SmartRoadException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "user.not.found"));

        return mapToUserResponse(user);
    }

    /**
     * Initiates password reset by sending OTP to user's email.
     * Generates OTP for password reset flow if user exists.
     * Does not reveal whether user exists for security.
     *
     * @param request the forgot password request containing email
     * @throws SmartRoadException if OTP generation fails
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) throws SmartRoadException {
        UserEntity user = userRepository.findByEmailId(request.getEmail())
                .orElse(null);

        if (user != null) {
            otpService.generateOtp(user.getEmailId(), OtpFlow.PASSWORD_RESET);
            log.info("Password reset OTP generated for: {}", user.getEmailId());
        }

        log.info("Password reset requested for: {} (user may not exist)", request.getEmail());
    }

    /**
     * Resets user password after OTP verification.
     * Validates password confirmation, verifies OTP, updates password,
     * invalidates all sessions, and creates audit log entry.
     *
     * @param request the reset password request containing email, OTP, and new password
     * @throws SmartRoadException if password mismatch, OTP invalid, or user not found
     */
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) throws SmartRoadException {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "passwords.do.not.match");
        }

        otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpFlow.PASSWORD_RESET);

        UserEntity user = userRepository.findByEmailId(request.getEmail())
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "user.not.found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        sessionRepository.deleteByUserId(user.getId());

        createAuditLog(user.getId(), user.getId(), "PASSWORD_RESET", "SUCCESS");

        log.info("Password reset successful for: {}", user.getEmailId());
    }

    /**
     * Changes user password for authenticated user.
     * Validates current password, checks new password confirmation,
     * ensures new password is different, updates password,
     * invalidates all sessions, and creates audit log entry.
     *
     * @param userId the UUID of the user changing password
     * @param request the change password request containing current and new passwords
     * @throws SmartRoadException if current password incorrect, password mismatch, or user not found
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequestDTO request) throws SmartRoadException {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "passwords.do.not.match");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "user.not.found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_UNAUTHORIZED, "current.password.incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "new.password.must.be.different");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        sessionRepository.deleteByUserId(user.getId());

        createAuditLog(userId, userId, "PASSWORD_CHANGED", "SUCCESS");

        log.info("Password changed for user: {}", user.getEmailId());
    }

    /**
     * Verifies OTP for email or phone verification.
     * Validates OTP, marks email/phone as verified, activates user account
     * if status is PENDING, and creates audit log entry.
     *
     * @param request the verify OTP request containing email/phone, OTP, and flow type
     * @throws SmartRoadException if OTP invalid or user not found
     */
    @Transactional
    public void verifyOtp(VerifyOtpRequestDTO request) throws SmartRoadException {
        UserEntity user = null;

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            otpService.verifyOtp(request.getEmail(), request.getOtp(), request.getFlow());
            user = userRepository.findByEmailId(request.getEmail())
                    .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "user.not.found"));
            user.setEmailVerifiedYn(true);
        } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            otpService.verifyPhoneOtp(request.getPhoneNumber(), request.getOtp(), request.getFlow());
            user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "user.not.found"));
        }

        if (user != null && (request.getFlow() == OtpFlow.EMAIL_VERIFICATION || request.getFlow() == OtpFlow.SIGNUP_VERIFICATION)) {
            if (user.getStatus() == UserStatusEnum.PENDING) {
                user.setStatus(UserStatusEnum.ACTIVE);
            }
            try {
                userRepository.saveAndFlush(user);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Optimistic lock conflict, retrying: {}", e.getMessage());
                user = userRepository.findById(user.getId()).orElseThrow();
                if (user.getStatus() == UserStatusEnum.PENDING) {
                    user.setStatus(UserStatusEnum.ACTIVE);
                }
                userRepository.saveAndFlush(user);
            }

            createAuditLog(user.getId(), user.getId(), "OTP_VERIFIED", "SUCCESS");

            log.info("OTP verified for user: {}", user.getEmailId() != null ? user.getEmailId() : user.getPhoneNumber());
        }
    }

    /**
     * Resends OTP to user's email or phone for verification.
     * Checks resend cooldown, invalidates previous OTPs,
     * generates new OTP, and logs the action.
     *
     * @param request the resend OTP request containing email/phone and flow type
     * @throws SmartRoadException if resend cooldown not elapsed
     */
    @Transactional
    public void resendOtp(ResendOtpRequestDTO request) throws SmartRoadException {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!otpService.canResendOtp(request.getEmail(), request.getFlow())) {
                throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "please.wait.before.requesting.another.otp");
            }
            otpService.invalidatePreviousOtps(request.getEmail(), request.getFlow());
            String otp = otpService.generateOtp(request.getEmail(), request.getFlow());
            log.info("OTP resent to email: {} with flow: {}", request.getEmail(), request.getFlow());
        } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (!otpService.canResendOtp(request.getPhoneNumber(), request.getFlow())) {
                throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "please.wait.before.requesting.another.otp");
            }
            otpService.invalidatePreviousPhoneOtps(request.getPhoneNumber(), request.getFlow());
            String otp = otpService.generateOtpForPhone(request.getPhoneNumber(), request.getFlow());
            log.info("OTP resent to phone: {} with flow: {}", request.getPhoneNumber(), request.getFlow());
        }
    }

    /**
     * Maps User entity to UserResponseDTO DTO.
     * Converts user entity fields to response format.
     *
     * @param user the User entity to map
     * @return UserResponseDTO DTO containing user details
     */
    private UserResponseDTO mapToUserResponse(UserEntity user) throws SmartRoadException {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmailId())
                .phoneNumber(user.getPhoneNumber())
                .countryCode(user.getCountryCode())
                .role(user.getRole())
                .userType(user.getUserType())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerifiedYn())
                .build();
    }

    /**
     * Creates an audit log entry for user actions.
     * Records action performed, target user, requester, and status.
     *
     * @param actionForUserId the UUID of the user the action was performed on
     * @param requestedBy the UUID of the user who requested the action
     * @param action the action performed (e.g., LOGIN, LOGOUT, SIGNUP)
     * @param status the status of the action (e.g., SUCCESS, FAILED)
     */
    private void createAuditLog(UUID actionForUserId, UUID requestedBy, String action, String status) throws SmartRoadException {
        UserAuditLogEntity auditLog = UserAuditLogEntity.builder()
                .userId(actionForUserId)
                .requestedBy(requestedBy.toString())
                .actionDone(action)
                .ntStatus(status)
                .build();

        auditLog.setCreatedBy(SYSTEM_USER_ID);
        auditLog.setModifiedBy(SYSTEM_USER_ID);

        auditLogRepository.save(auditLog);
    }
}
