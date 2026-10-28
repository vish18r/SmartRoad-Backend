package com.smartroad.services.api.error;

import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.SmartRoadResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(SmartRoadException.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleSmartRoadException(SmartRoadException ex) {
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
                .body(SmartRoadResponseDTO.error(resolveMessage(ex.getMessageKey(), ex.getArguments())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SmartRoadResponseDTO<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SmartRoadResponseDTO.success("Validation failed", fieldErrors));
    }

    /**
     * Maps a path/query parameter that cannot be converted to its declared type (for example a
     * malformed UUID or date) to 400 Bad Request instead of the generic 500 fallback.
     *
     * @param ex the conversion failure
     * @return a 400 response naming the offending parameter
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SmartRoadResponseDTO.error("Invalid value for parameter '" + ex.getName() + "'"));
    }

    /**
     * Maps an unreadable/malformed JSON request body (for example an unknown enum value or an
     * unparseable timestamp) to 400 Bad Request instead of the generic 500 fallback.
     *
     * @param ex the body-parsing failure
     * @return a 400 response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        logger.warn("Unreadable request body: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SmartRoadResponseDTO.error("Malformed request body"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(SmartRoadResponseDTO.error("Invalid credentials"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(SmartRoadResponseDTO.error("Access denied"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        logger.error("Data integrity violation", ex);
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg != null && msg.contains("uq_")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(SmartRoadResponseDTO.error("A record with those details already exists."));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(SmartRoadResponseDTO.error("Data constraint violation: " + (msg != null ? msg : "unknown")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleGenericException(Exception ex) {
        logger.error("Unhandled exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        String detail = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SmartRoadResponseDTO.error("Something went wrong: " + detail));
    }

    private String resolveMessage(String messageKey, java.util.List<String> arguments) {
        try {
            return messageSource.getMessage(messageKey,
                    arguments == null ? null : arguments.toArray(), LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException ex) {
            return messageKey;
        }
    }
}
