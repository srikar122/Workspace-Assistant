package com.org.workspace.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2AuthorizedClientService clientService;
    private final GoogleTokenHolder tokenHolder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .successHandler((request, response, authentication) -> {
                    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                    OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                            oauthToken.getAuthorizedClientRegistrationId(),
                            oauthToken.getName()
                    );

                    String email = oauthToken.getPrincipal().getAttribute("email");
                    String name = oauthToken.getPrincipal().getAttribute("name");
                    if (email == null) email = oauthToken.getName();
                    if (name == null) name = email;

                    tokenHolder.setAccessToken(client.getAccessToken().getTokenValue());
                    tokenHolder.setUserEmail(email);
                    tokenHolder.setUserName(name);

                    log.info("Google OAuth2 connected for user: {} ({})", name, email);

                    String redirectUrl = String.format(
                            "http://localhost:5173?connected=true&userId=%s&name=%s",
                            URLEncoder.encode(email, StandardCharsets.UTF_8),
                            URLEncoder.encode(name, StandardCharsets.UTF_8)
                    );
                    response.sendRedirect(redirectUrl);
                })
            );

        return http.build();
    }
}