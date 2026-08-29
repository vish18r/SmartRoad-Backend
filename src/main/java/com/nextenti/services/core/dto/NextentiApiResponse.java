package com.nextenti.services.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NextentiApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> NextentiApiResponse<T> success(String message, T data) {
        return new NextentiApiResponse<>(true, message, data);
    }

    public static <T> NextentiApiResponse<T> success(T data) {
        return new NextentiApiResponse<>(true, "Request completed successfully", data);
    }

    public static <T> NextentiApiResponse<T> error(String message) {
        return new NextentiApiResponse<>(false, message, null);
    }
}
