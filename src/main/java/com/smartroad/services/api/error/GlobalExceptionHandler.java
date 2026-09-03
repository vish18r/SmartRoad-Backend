package com.smartroad.services.api.error;

import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.SmartRoadResponseDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(SmartRoadResponseDTO.error("Invalid credentials"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(SmartRoadResponseDTO.error("Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SmartRoadResponseDTO<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SmartRoadResponseDTO.error("Something went wrong: " + ex.getMessage()));
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
