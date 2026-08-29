package com.nextenti.services.api.interceptor;

import com.nextenti.services.common.util.NextentiConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class RequestHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = request.getHeader(NextentiConstants.CORRELATION_ID) != null
                ? request.getHeader(NextentiConstants.CORRELATION_ID)
                : UUID.randomUUID().toString();
        MDC.put(NextentiConstants.CORRELATION_ID, correlationId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(NextentiConstants.CORRELATION_ID);
    }
}
