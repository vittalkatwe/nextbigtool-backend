package com.nextbigtool.backend.auth;

import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.AuthProvider;
import com.nextbigtool.backend.entity.user.UserRole;
import com.nextbigtool.backend.repository.UserRepository;
import com.nextbigtool.backend.service.auth.CustomUserDetails;
import com.nextbigtool.backend.service.auth.OAuth2UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends OidcUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        System.out.println("=== OAuth2 Token Debug ===");
        System.out.println("Access token: "+ (userRequest.getAccessToken().getTokenValue() != null ? "PRESENT" : "NULL"));
        System.out.println("Additional params: "+ userRequest.getAdditionalParameters());
        System.out.println("Token scopes: "+ userRequest.getAccessToken().getScopes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, oidcUser.getAttributes());

        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not returned by OAuth2 provider");
        }

        // ── Extract Google tokens ─────────────────────────────────────────────
        String accessToken = userRequest.getAccessToken().getTokenValue();

        // Refresh token is only present when access_type=offline + prompt=consent
        // (which we now force in SecurityConfig)
        String refreshToken = null;
        Object rt = userRequest.getAdditionalParameters().get("refresh_token");
        if (rt != null) refreshToken = rt.toString();

        Optional<AppUser> existingUser = userRepository.findByEmail(email.toLowerCase().trim());

        AppUser user;
        if (existingUser.isPresent()) {
            user = updateExistingOAuthUser(existingUser.get(), userInfo, registrationId,
                    accessToken, refreshToken);
        } else {
            user = createNewOAuthUser(userInfo, registrationId, accessToken, refreshToken);
        }

        return new CustomUserDetails(user, oidcUser.getAttributes(),
                oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    private AppUser createNewOAuthUser(OAuth2UserInfo userInfo, String registrationId,
                                       String accessToken, String refreshToken) {
        AppUser user = new AppUser();
        user.setEmail(userInfo.getEmail().toLowerCase().trim());
        user.setEmailVerified(true);
        user.setActive(true);
        user.setRole(UserRole.GUEST);
        user.setAuthProvider(AuthProvider.OAUTH);
        user.setOauthProvider(registrationId);
        user.setOauthProviderId(userInfo.getId());
        user.setProfileComplete(false);
        user.setGoogleAccessToken(accessToken);
        if (refreshToken != null) user.setGoogleRefreshToken(refreshToken);
        return userRepository.save(user);
    }

    private AppUser updateExistingOAuthUser(AppUser user, OAuth2UserInfo userInfo,
                                            String registrationId,
                                            String accessToken, String refreshToken) {
        user.setOauthProvider(registrationId);
        user.setOauthProviderId(userInfo.getId());
        user.setEmailVerified(true);
        user.setGoogleAccessToken(accessToken);
        // Only overwrite refresh token if Google issued a new one — don't null out old one
        if (refreshToken != null) user.setGoogleRefreshToken(refreshToken);
        return userRepository.save(user);
    }
}