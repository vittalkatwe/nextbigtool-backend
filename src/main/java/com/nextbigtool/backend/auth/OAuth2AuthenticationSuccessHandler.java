package com.nextbigtool.backend.auth;

import com.nextbigtool.backend.entity.user.RefreshToken;
import com.nextbigtool.backend.repository.UserRepository;
import com.nextbigtool.backend.service.auth.CustomUserDetails;
import com.nextbigtool.backend.service.auth.JwtService;
import com.nextbigtool.backend.service.auth.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;  // ← ADD

    @Autowired
    private UserRepository userRepository;                          // ← ADD

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // ── Extract Google refresh token from OAuth2AuthorizedClient ──────────
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            if (authorizedClient != null) {
                // Access token — always present
                String googleAccessToken = authorizedClient.getAccessToken().getTokenValue();

                // Refresh token — present on first consent or after revoke+re-consent
                OAuth2RefreshToken refreshTokenObj = authorizedClient.getRefreshToken();

                userRepository.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                    user.setGoogleAccessToken(googleAccessToken);
                    if (refreshTokenObj != null) {
                        user.setGoogleRefreshToken(refreshTokenObj.getTokenValue());
                        log.info("Saved Google refresh token for user: {}", user.getEmail());
                    } else {
                        log.warn("No refresh token returned for user: {} — may need to revoke app access", user.getEmail());
                    }
                    userRepository.save(user);
                });
            }
        }

        // Build JWT claims (unchanged)
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getUserId());
        claims.put("profileComplete", userDetails.isProfileComplete());
        userDetails.getAuthorities().stream()
                .findFirst()
                .ifPresent(a -> claims.put("role", a.getAuthority()));

        String jwt = jwtService.generateToken(userDetails.getUsername(), claims);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", jwt)
                .queryParam("refreshToken", refreshToken.getToken())
                .queryParam("isNewUser", userDetails.isNewUser())
                .queryParam("profileComplete", userDetails.isProfileComplete())
                .queryParam("expiresIn", jwtService.getExpirationTime())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}