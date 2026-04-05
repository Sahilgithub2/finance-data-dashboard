package com.finance.service;

import com.finance.dto.user.UpdateRoleRequest;
import com.finance.dto.user.UpdateStatusRequest;
import com.finance.dto.user.UserResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.mapper.EntityMapper;
import com.finance.model.User;
import com.finance.repository.UserRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    public UserService(UserRepository userRepository, EntityMapper entityMapper) {
        this.userRepository = userRepository;
        this.entityMapper = entityMapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(entityMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateRole(Long id, UpdateRoleRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(request.getRole());
        return entityMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateStatus(Long id, UpdateStatusRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(Boolean.TRUE.equals(request.getActive()));
        return entityMapper.toUserResponse(userRepository.save(user));
    }
}
