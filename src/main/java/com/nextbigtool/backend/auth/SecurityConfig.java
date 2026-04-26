package com.nextbigtool.backend.auth;

import com.nextbigtool.backend.auth.CustomOAuth2UserService;
import com.nextbigtool.backend.service.auth.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // ── Auth ─────────────────────────────────────
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/oauth/**",
                                "/api/v1/community/linkedin/auth",
                                "/api/v1/community/linkedin/callback",
                                "/api/v1/community/all",
                                "/api/v1/analytics/**",
                                // ── OAuth2 spring endpoints ───────────────────
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/v1/domain/all",
                                "/api/v1/cocreation/all",   // add after "/api/v1/domain/all"
                                // ── Public listings ───────────────────────────
                                "/api/v1/venture/all",
                                "/api/v1/community/all",
                                "/api/v1/community/linkedin/auth",
                                "/api/v1/community/linkedin/callback",
                                "/api/v1/auction/domain/**",  // public auction viewing
                                "/api/v1/auction/*/bids",     // public bid history
                                "/ws/**",
                                // ── Infra ─────────────────────────────────────
                                "/api/v1/services/public/**",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/auction/active",
                                "/error",
                                "/public/api/v1/**",
                                "/api/v1/feedback",
                                "/api/v1/becobrother",
                                // ── Insights (all authenticated via JWT) ──────
                                // No public endpoints needed here
                                // ── Community auctions (public browsing) ─────
                                "/api/v1/community-auction/active",
                                "/api/v1/community-auction/{id}",
                                "/api/v1/community-auction/community/**",
                                // ── Tools (public listing/detail) ─────────────
                                "/api/v1/tools",
                                "/api/v1/tools/featured",
                                "/api/v1/tools/{id}",
                                "/api/v1/tools/{id}/comments",
                                "/api/v1/tools/{id}/upvote/count",
                                "/api/v1/tools/{id}/analytics/view",
                                "/api/v1/pricing",
                                "/api/v1/bip/feed",
                                "/api/v1/hall-of-fame",
                                "/api/v1/webhooks/razorpay",
                                "/api/v1/newsletter/issues/*/read"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(authorizationRequestResolver())  // ← ADD
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",   // Vite default port
                "http://localhost:3000",   // Vite if configured to 3000
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
                return req != null ? customize(req) : null;
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request,
                                                      String clientRegistrationId) {
                OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
                return req != null ? customize(req) : null;
            }

            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req) {
                Map<String, Object> params = new HashMap<>(req.getAdditionalParameters());
                params.put("access_type", "offline");
                params.put("prompt", "consent");
                System.out.println("=== OAuth2 Resolver called — params being sent: "+params); // ← ADD

                return OAuth2AuthorizationRequest.from(req)
                        .additionalParameters(params)
                        .build();
            }
        };
    }
}