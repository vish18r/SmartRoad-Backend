package com.smartroad.services.app.config;

import com.smartroad.services.common.enums.OAuthType;
import com.smartroad.services.core.dto.auth.AuthResponseDTO;
import com.smartroad.services.core.service.auth.OAuth2Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handles a successful Spring Security OAuth2 login by minting application JWTs
 * for the authenticated user and redirecting back to the frontend with the tokens.
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Service oauth2Service;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
    }

    /**
     * Extracts the authenticated OAuth2 user's email and provider subject id,
     * issues application JWTs, and redirects the browser to the frontend
     * OAuth callback route with the tokens appended as query parameters.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authentication the successful OAuth2 authentication
     * @throws IOException if the redirect cannot be written
     * @throws ServletException if the authentication cannot be processed
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        String email = oAuth2User.getAttribute("email");
        String subject = oAuth2User.getAttribute("sub");

        AuthResponseDTO authResponse = oauth2Service.processOAuth2Login(email, subject, OAuthType.GOOGLE);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                .queryParam("accessToken", authResponse.getAccessToken())
                .queryParam("refreshToken", authResponse.getRefreshToken())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
