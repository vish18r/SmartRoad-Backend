package com.nextenti.services.api.rest.auth;

import com.nextenti.services.api.utils.RequestUtil;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.auth.ChangePasswordRequestDTO;
import com.nextenti.services.core.dto.auth.ForgotPasswordRequestDTO;
import com.nextenti.services.core.dto.auth.LoginRequestDTO;
import com.nextenti.services.core.dto.auth.RefreshTokenRequestDTO;
import com.nextenti.services.core.dto.auth.ResendOtpRequestDTO;
import com.nextenti.services.core.dto.auth.ResetPasswordRequestDTO;
import com.nextenti.services.core.dto.auth.SendOtpRequestDTO;
import com.nextenti.services.core.dto.auth.SignupRequestDTO;
import com.nextenti.services.core.dto.auth.VerifyOtpRequestDTO;
import com.nextenti.services.core.service.auth.AuthService;
import com.nextenti.services.core.service.auth.OtpService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for legacy authentication endpoints
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final OtpService otpService;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param authService the authentication service
     * @param otpService  the OTP service
     */
    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    /**
     * Registers a new user.
     *
     * @param request the signup request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 201 Created
     * @throws SmartRoadException if registration fails
     */
    @PostMapping(path = {"/register", "/signup"}, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> register(@RequestBody @Valid SignupRequestDTO request,
                                           @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside register method--");

        authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Sends OTP to email or phone.
     *
     * @param request the OTP request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if OTP generation fails
     */
    @PostMapping(path = "/otp/send", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> sendOtp(@RequestBody @Valid SendOtpRequestDTO request,
                                          @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside sendOtp method--");

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            otpService.generateOtp(request.getEmail(), request.getFlow());
        } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            otpService.generateOtpForPhone(request.getPhoneNumber(), request.getFlow());
        } else {
            throw new SmartRoadException(
                    com.nextenti.services.common.exception.ApplicationLayer.SERVICE_LAYER,
                    com.nextenti.services.common.exception.ErrorCodeMapping.SERVICE_INVALID_INPUT,
                    "email.or.phone.required"
            );
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Verifies an OTP.
     *
     * @param request the OTP verification request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if OTP verification fails
     */
    @PostMapping(path = {"/otp/verify", "/verify-otp", "/verify-email"},
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> verifyOtp(@RequestBody @Valid VerifyOtpRequestDTO request,
                                            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside verifyOtp method--");

        authService.verifyOtp(request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Resends an OTP.
     *
     * @param request the OTP resend request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if OTP resend fails
     */
    @PostMapping(path = {"/otp/resend", "/resend-otp", "/resend-verification"},
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> resendOtp(@RequestBody @Valid ResendOtpRequestDTO request,
                                            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside resendOtp method--");

        authService.resendOtp(request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Initiates password reset flow.
     *
     * @param request the forgot password request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if password reset initiation fails
     */
    @PostMapping(path = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO request,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside forgotPassword method--");

        authService.forgotPassword(request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Resets password using OTP.
     *
     * @param request the reset password request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if password reset fails
     */
    @PostMapping(path = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO request,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside resetPassword method--");

        authService.resetPassword(request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Changes password for authenticated user.
     *
     * @param request the change password request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if password change fails
     */
    @PostMapping(path = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordRequestDTO request,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside changePassword method--");

        UUID userId = RequestUtil.extractUserId();
        authService.changePassword(userId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Authenticates a user and returns tokens.
     *
     * @param request the login request containing identifier and password
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK containing {@link AuthResponseDTO}
     * @throws SmartRoadException if authentication fails
     */
    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> login(@RequestBody @Valid LoginRequestDTO request,
                                        @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside login method--");

        var response = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param request the refresh token request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK containing {@link TokenResponseDTO}
     * @throws SmartRoadException if token refresh fails
     */
    @PostMapping(path = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> refresh(@RequestBody @Valid RefreshTokenRequestDTO request,
                                          @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside refresh method--");

        var response = authService.refreshToken(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves the current authenticated user's profile.
     *
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK containing {@link UserResponseDTO}
     * @throws SmartRoadException if user not found
     */
    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Object> getCurrentUser(@RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getCurrentUser method--");

        UUID userId = RequestUtil.extractUserId();
        var response = authService.getCurrentUser(userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Logs out the current user by revoking their session.
     *
     * @param request the logout request containing refresh token
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if logout fails
     */
    @PostMapping(path = "/logout", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Object> logout(@RequestBody RefreshTokenRequestDTO request,
                                         @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside logout method--");

        UUID userId = RequestUtil.extractUserId();
        authService.logout(userId, request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Logs out the current user from all devices by revoking all sessions.
     *
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if logout fails
     */
    @PostMapping(path = "/logout-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Object> logoutAll(@RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside logoutAll method--");

        UUID userId = RequestUtil.extractUserId();
        authService.logoutAll(userId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
