package com.akbo.auth.api.service.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    private final ModelMapper modelMapper;

    @Override
    public UserDto createUser(final UserDto user) {
        final UserDto createdUser = modelMapper.map(
                userRepository.save(modelMapper.map(user, User.class)),
                UserDto.class);
        return createdUser;
    }

    @Override
    public void createUser(UserDetails user) {
        createUser(modelMapper.map(user, UserDto.class));
    }

    @Override
    public void updateUser(UserDetails user) {
        userRepository.save((User)user);
    }

    @Override
    public void deleteUser(String username) {
        final User userFound = userRepository.findByUsername(username);
       userRepository.deleteById(userFound.getId());
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
    }

    @Override
    public boolean userExists(String username) {
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<String> findAllGroups() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllGroups'");
    }

    @Override
    public List<String> findUsersInGroup(String groupName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findUsersInGroup'");
    }

    @Override
    public void createGroup(String groupName, List<GrantedAuthority> authorities) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createGroup'");
    }

    @Override
    public void deleteGroup(String groupName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteGroup'");
    }

    @Override
    public void renameGroup(String oldName, String newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'renameGroup'");
    }

    @Override
    public void addUserToGroup(String username, String group) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addUserToGroup'");
    }

    @Override
    public void removeUserFromGroup(String username, String groupName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeUserFromGroup'");
    }

    @Override
    public List<GrantedAuthority> findGroupAuthorities(String groupName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findGroupAuthorities'");
    }

    @Override
    public void addGroupAuthority(String groupName, GrantedAuthority authority) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addGroupAuthority'");
    }

    @Override
    public void removeGroupAuthority(String groupName, GrantedAuthority authority) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeGroupAuthority'");
    }

    @Override
    public UserDto getUser(String username) {
        final UserDto user = modelMapper.map((User)loadUserByUsername(username), UserDto.class);
        return user;
    }
}
