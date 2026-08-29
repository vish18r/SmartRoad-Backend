package com.nextenti.services.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartRoadResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> SmartRoadResponseDTO<T> success(String message, T data) {
        return new SmartRoadResponseDTO<>(true, message, data);
    }

    public static <T> SmartRoadResponseDTO<T> success(T data) {
        return new SmartRoadResponseDTO<>(true, "Request completed successfully", data);
    }

    public static <T> SmartRoadResponseDTO<T> error(String message) {
        return new SmartRoadResponseDTO<>(false, message, null);
    }
}
