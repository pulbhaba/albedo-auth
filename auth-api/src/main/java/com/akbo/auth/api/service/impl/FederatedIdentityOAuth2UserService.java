package com.akbo.auth.api.service.impl;

import com.akbo.auth.api.config.SocialLoginProperties;
import com.akbo.auth.dao.entity.User;
import com.akbo.auth.dao.entity.UserRole;
import com.akbo.auth.dao.repository.UserRepository;
import com.akbo.auth.dao.repository.UserRoleRepository;
import com.akbo.auth.dto.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FederatedIdentityOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SocialLoginProperties socialLoginProperties;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        if (!socialLoginProperties.isEnabled()) {
            throw new OAuth2AuthenticationException("Social login is disabled");
        }
        
        SocialLoginProperties.ProviderProperties providerProperties = socialLoginProperties.getProviders().get(registrationId);
        if (providerProperties != null && !providerProperties.isEnabled()) {
            throw new OAuth2AuthenticationException("Social login provider " + registrationId + " is disabled");
        }

        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            email = oAuth2User.getAttribute("preferred_username");
        }
        if (email == null) {
            email = oAuth2User.getName();
        }

        Optional<User> userOptional = userRepository.findByUsername(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setAttributes(oAuth2User.getAttributes());
            return user;
        } else {
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setEmailAddress(email);
            newUser.setPassword("{noop}" + UUID.randomUUID().toString());
            newUser.setEnabled(true);
            
            newUser.setFirstName(oAuth2User.getAttribute("given_name"));
            newUser.setLastName(oAuth2User.getAttribute("family_name"));
            
            if (newUser.getFirstName() == null) {
                newUser.setFirstName(oAuth2User.getAttribute("name"));
            }

            UserRole userRole = userRoleRepository.findById(Role.ROLE_USER)
                    .orElseGet(() -> userRoleRepository.save(new UserRole(Role.ROLE_USER)));
            
            Set<UserRole> authorities = new HashSet<>();
            authorities.add(userRole);
            newUser.setAuthorities(authorities);
            
            User savedUser = userRepository.save(newUser);
            savedUser.setAttributes(oAuth2User.getAttributes());
            return savedUser;
        }
    }
}
