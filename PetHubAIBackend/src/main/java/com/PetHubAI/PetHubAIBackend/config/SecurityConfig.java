package com.PetHubAI.PetHubAIBackend.config;

import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.repository.UserRepository;
import com.PetHubAI.PetHubAIBackend.security.CustomUserDetailsService;
import com.PetHubAI.PetHubAIBackend.security.JwtAuthenticationEntryPoint;
import com.PetHubAI.PetHubAIBackend.security.JwtAuthenticationFilter;
import com.PetHubAI.PetHubAIBackend.service.JwtService;
import com.PetHubAI.PetHubAIBackend.service.OAuth2UserService;
import com.PetHubAI.PetHubAIBackend.service.UserService;

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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import java.io.IOException;
import java.net.URLEncoder;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private OAuth2UserService oauth2UserService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/auth/**", "/oauth2/**", "/login/oauth2/code/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService)
                        )
                        .successHandler(new SimpleUrlAuthenticationSuccessHandler() {
                            @Override
                            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                                Authentication authentication) throws IOException {
                                try {
                                    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                                    String email = oauth2User.getAttribute("email");
                                    String login = oauth2User.getAttribute("login");
                                    String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

                                    // FIXED: Proper handling of different ID types
                                    Object idAttribute = oauth2User.getAttribute("id");
                                    String oauthId = null;

                                    if (idAttribute != null) {
                                        if (idAttribute instanceof String) {
                                            oauthId = (String) idAttribute;
                                        } else if (idAttribute instanceof Integer) {
                                            oauthId = String.valueOf((Integer) idAttribute);
                                        } else if (idAttribute instanceof Long) {
                                            oauthId = String.valueOf((Long) idAttribute);
                                        } else {
                                            oauthId = String.valueOf(idAttribute);
                                        }
                                    }

                                    System.out.println("OAuth2 Success Handler - Email: " + email);
                                    System.out.println("OAuth2 Success Handler - Login: " + login);
                                    System.out.println("OAuth2 Success Handler - Provider: " + registrationId);
                                    System.out.println("OAuth2 Success Handler - OAuth ID: " + oauthId + " (type: " +
                                            (idAttribute != null ? idAttribute.getClass().getSimpleName() : "null") + ")");

                                    User user = null;

                                    // Try to find user by OAuth provider and ID first
                                    if (oauthId != null) {
                                        try {
                                            user = userRepository.findByOauthProviderAndOauthId(registrationId, oauthId)
                                                    .orElse(null);
                                            System.out.println("Found user by OAuth ID: " + (user != null ? user.getId() : "null"));
                                        } catch (Exception e) {
                                            System.err.println("Error finding user by OAuth ID: " + e.getMessage());
                                        }
                                    }

                                    // If not found by OAuth ID, try by email
                                    if (user == null) {
                                        String searchEmail = email;

                                        // For GitHub, if no email, create placeholder email
                                        if (searchEmail == null && "github".equals(registrationId)) {
                                            searchEmail = login + "@github.users.noreply.com";
                                        }

                                        if (searchEmail != null) {
                                            System.out.println("Searching for user by email: " + searchEmail);

                                            try {
                                                user = userService.findByEmail(searchEmail);
                                                System.out.println("Found user by email: " + user.getId());
                                            } catch (Exception e) {
                                                System.err.println("Error finding user by email: " + e.getMessage());

                                                // Wait a moment and try again (handle race condition)
                                                Thread.sleep(100);
                                                try {
                                                    user = userService.findByEmail(searchEmail);
                                                    System.out.println("Found user by email on retry: " + user.getId());
                                                } catch (Exception retryException) {
                                                    System.err.println("User still not found after retry: " + retryException.getMessage());
                                                }
                                            }
                                        }
                                    }

                                    if (user != null) {
                                        String token = jwtService.generateToken(user);
                                        System.out.println("Generated token for user: " + user.getId());

                                        String redirectUrl = String.format(
                                                "http://localhost:5173/oauth2/redirect?token=%s&type=Bearer",
                                                URLEncoder.encode(token, "UTF-8")
                                        );

                                        System.out.println("Redirecting to: " + redirectUrl);
                                        response.sendRedirect(redirectUrl);
                                    } else {
                                        System.err.println("User not found after all attempts");
                                        String errorUrl = "http://localhost:5173/login?error=oauth2_processing_error";
                                        response.sendRedirect(errorUrl);
                                    }

                                } catch (Exception e) {
                                    System.err.println("OAuth2 Success Handler Error: " + e.getMessage());
                                    e.printStackTrace();
                                    String errorUrl = "http://localhost:5173/login?error=oauth2_processing_error";
                                    response.sendRedirect(errorUrl);
                                }
                            }
                        })


                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
