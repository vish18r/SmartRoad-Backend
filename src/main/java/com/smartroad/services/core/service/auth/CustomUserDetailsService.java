package com.smartroad.services.core.service.auth;

import com.smartroad.services.common.enums.auth.UserStatusEnum;
import com.smartroad.services.domain.entity.UserEntity;
import com.smartroad.services.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByIdentifier(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + username));

        if (user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new UsernameNotFoundException("User account is not active: " + username);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString())
                .password(user.getPassword())
                .authorities(new ArrayList<>())
                .accountExpired(false)
                .accountLocked(user.getStatus() == UserStatusEnum.BLOCKED)
                .credentialsExpired(false)
                .disabled(user.getStatus() == UserStatusEnum.INACTIVE)
                .build();
    }
}
