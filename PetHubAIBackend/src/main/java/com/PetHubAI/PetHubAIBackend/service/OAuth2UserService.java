package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.entity.UserProfile;
import com.PetHubAI.PetHubAIBackend.repository.UserProfileRepository;
import com.PetHubAI.PetHubAIBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // src/main/java/com/petcare/service/OAuth2UserService.java
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String oauthId = getOAuthId(oauth2User, registrationId);
            String email = getEmail(oauth2User, registrationId);
            String firstName = getFirstName(oauth2User, registrationId);
            String lastName = getLastName(oauth2User, registrationId);

            // Debug logging
            System.out.println("OAuth2 Provider: " + registrationId);
            System.out.println("OAuth2 User ID: " + oauthId);
            System.out.println("OAuth2 Email: " + email);
            System.out.println("OAuth2 First Name: " + firstName);
            System.out.println("OAuth2 Last Name: " + lastName);

            // Validate required fields and provide defaults if needed
            if (email == null || email.isEmpty()) {
                if ("github".equals(registrationId)) {
                    String login = oauth2User.getAttribute("login");
                    email = login + "@github.users.noreply.com";
                    System.out.println("Created placeholder email for GitHub: " + email);
                } else {
                    throw new OAuth2AuthenticationException("Email not provided by " + registrationId);
                }
            }

            // Ensure firstName meets validation requirements
            if (firstName == null || firstName.trim().length() < 2) {
                firstName = "User";
            }

            // Ensure lastName meets validation requirements
            if (lastName == null || lastName.trim().length() < 2) {
                lastName = "Unknown";
            }

            // Check if user already exists
            User user = userRepository.findByOauthProviderAndOauthId(registrationId, oauthId)
                    .orElse(userRepository.findByEmail(email).orElse(null));

            if (user == null) {
                // Create new user
                user = new User();
                user.setEmail(email);
                user.setFirstName(firstName);
                user.setLastName(lastName);  // Now guaranteed to meet validation
                user.setOauthProvider(registrationId);
                user.setOauthId(oauthId);
                user.setRole(User.Role.USER);
                user.setIsVerified(true);
                user.setIsActive(true);

                User savedUser = userRepository.save(user);
                System.out.println("Created new user: " + savedUser.getId());

                // Create user profile
                UserProfile profile = new UserProfile(savedUser);
                userProfileRepository.save(profile);
            } else {
                // Update OAuth info if needed
                if (user.getOauthProvider() == null || user.getOauthId() == null) {
                    user.setOauthProvider(registrationId);
                    user.setOauthId(oauthId);
                    userRepository.save(user);
                    System.out.println("Updated existing user OAuth info: " + user.getId());
                }
            }

            return oauth2User;

        } catch (Exception e) {
            System.err.println("OAuth2 Error for " + userRequest.getClientRegistration().getRegistrationId() + ": " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("Failed to process OAuth2 login: " + e.getMessage());
        }
    }





    private String getOAuthId(OAuth2User oauth2User, String registrationId) {
        Object idAttribute = null;

        switch (registrationId) {
            case "google":
                idAttribute = oauth2User.getAttribute("sub");
                break;
            case "github":
                idAttribute = oauth2User.getAttribute("id");
                break;
            default:
                idAttribute = oauth2User.getAttribute("id");
                break;
        }

        if (idAttribute == null) {
            return null;
        }

        // Safe conversion to String regardless of type
        if (idAttribute instanceof String) {
            return (String) idAttribute;
        } else if (idAttribute instanceof Integer) {
            return String.valueOf((Integer) idAttribute);
        } else if (idAttribute instanceof Long) {
            return String.valueOf((Long) idAttribute);
        } else {
            return String.valueOf(idAttribute);
        }
    }


    private String getEmail(OAuth2User oauth2User, String registrationId) {
        String email = oauth2User.getAttribute("email");

        // GitHub might not provide email if it's private
        if ((email == null || email.isEmpty()) && "github".equals(registrationId)) {
            // Try to get primary email from GitHub API if needed
            // For now, we'll use login as fallback
//            String login = oauth2User.getAttribute("login");
//            return login != null ? login + "@github.local" : null;
            return null;
        }

        return email;
    }

    private String getFirstName(OAuth2User oauth2User, String registrationId) {
        switch (registrationId) {
            case "google":
                return oauth2User.getAttribute("given_name");
            case "github":
                String name = oauth2User.getAttribute("name");
                if (name != null && name.contains(" ")) {
                    return name.split(" ")[0];
                }
                // Fallback to login username
                String login = oauth2User.getAttribute("login");
                return login != null ? login : "GitHub User";
            default:
                return oauth2User.getAttribute("first_name");
        }
    }

    private String getLastName(OAuth2User oauth2User, String registrationId) {
        switch (registrationId) {
            case "google":
                return oauth2User.getAttribute("family_name");
            case "github":
                String name = oauth2User.getAttribute("name");
                if (name != null && name.contains(" ")) {
                    String[] parts = name.split(" ");
                    return parts.length > 1 ? parts[parts.length - 1] : "";
                }
                return ""; // GitHub often doesn't have separate last names
            default:
                return oauth2User.getAttribute("last_name");
        }
    }
}
