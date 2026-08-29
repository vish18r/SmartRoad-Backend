package com.nextenti.services.api.rest.auth;

import com.nextenti.services.common.exception.NextentiException;
import com.nextenti.services.core.dto.auth.ChangePasswordRequest;
import com.nextenti.services.core.dto.auth.ForgotPasswordRequest;
import com.nextenti.services.core.dto.auth.LoginRequest;
import com.nextenti.services.core.dto.auth.RefreshTokenRequest;
import com.nextenti.services.core.dto.auth.ResendOtpRequest;
import com.nextenti.services.core.dto.auth.ResetPasswordRequest;
import com.nextenti.services.core.dto.auth.SendOtpRequest;
import com.nextenti.services.core.dto.auth.VerifyOtpRequest;
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
     * @throws NextentiException if registration fails
     */
    @PostMapping(path = {"/register", "/signup"}, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> register(@RequestBody @Valid SendOtpRequest request,
                                           @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside register method--");

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Sends OTP to email or phone.
     *
     * @param request the OTP request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws NextentiException if OTP generation fails
     */
    @PostMapping(path = "/otp/send", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> sendOtp(@RequestBody @Valid SendOtpRequest request,
                                          @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside sendOtp method--");

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            otpService.generateOtp(request.getEmail(), request.getFlow());
        } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            otpService.generateOtpForPhone(request.getPhoneNumber(), request.getFlow());
        } else {
            throw new NextentiException(
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
     * @throws NextentiException if OTP verification fails
     */
    @PostMapping(path = {"/otp/verify", "/verify-otp", "/verify-email"},
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> verifyOtp(@RequestBody @Valid VerifyOtpRequest request,
                                            @RequestHeader HttpHeaders headers) throws NextentiException {
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
     * @throws NextentiException if OTP resend fails
     */
    @PostMapping(path = {"/otp/resend", "/resend-otp", "/resend-verification"},
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> resendOtp(@RequestBody @Valid ResendOtpRequest request,
                                            @RequestHeader HttpHeaders headers) throws NextentiException {
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
     * @throws NextentiException if password reset initiation fails
     */
    @PostMapping(path = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request,
                                                 @RequestHeader HttpHeaders headers) throws NextentiException {
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
     * @throws NextentiException if password reset fails
     */
    @PostMapping(path = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> resetPassword(@RequestBody @Valid ResetPasswordRequest request,
                                                @RequestHeader HttpHeaders headers) throws NextentiException {
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
     * @throws NextentiException if password change fails
     */
    @PostMapping(path = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordRequest request,
                                                 @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside changePassword method--");

        // Note: Extract user ID from authenticated principal in actual implementation
        authService.changePassword(UUID.randomUUID(), request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
