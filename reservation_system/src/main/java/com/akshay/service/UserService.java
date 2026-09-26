package com.akshay.service;

import com.akshay.dto.user.UpdateUserRoleRequest;
import com.akshay.dto.user.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUserRole(Long id, UpdateUserRoleRequest request);
}
