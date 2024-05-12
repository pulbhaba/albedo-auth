package com.akbo.auth.api.service.impl;

import java.util.Objects;

import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.akbo.auth.api.service.UserService;
import com.akbo.auth.dao.entity.User;
import com.akbo.auth.dao.repository.UserRepository;
import com.akbo.auth.dto.UserDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserDto createUser(final UserDto user) {
        final User existingUser;
        // early exit
        if (Objects.nonNull(user.getId()))
            existingUser = userRepository.findById(user.getId()).orElse(null);
        else
            existingUser = userRepository.findByUsername(user.getUsername()).orElse(null);

        if (Objects.nonNull(existingUser))
            return modelMapper.map(existingUser, UserDto.class);
        ;

        final User newUser = modelMapper.map(user, User.class);
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        final User savedUser = userRepository.save(newUser);

        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public void createUser(final UserDetails user) {
        createUser(modelMapper.map(user, UserDto.class));
    }

    @Override
    public void updateUser(final UserDetails user) {
        userRepository.save((User) user);
    }

    @Override
    public void deleteUser(final String username) {
        userRepository.findByUsername(username)
                .ifPresentOrElse(user -> userRepository.deleteById(user.getId()),
                        () -> new BadRequestException("The user does not exist in the system"));
    }

    @Override
    public void changePassword(final String oldPassword, final String newPassword) {
    }

    @Override
    public boolean userExists(final String username) {
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("The username was not found in the system"));
    }

    @Override
    public UserDto getUser(String username) {
        final UserDto user = modelMapper.map((User) loadUserByUsername(username), UserDto.class);
        return user;
    }
}
