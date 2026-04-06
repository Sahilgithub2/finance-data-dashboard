package com.finance.controller;

import com.finance.dto.user.UpdateRoleRequest;
import com.finance.dto.user.UpdateStatusRequest;
import com.finance.dto.user.UserResponse;
import com.finance.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Admin only — list users, change role (ADMIN / ANALYST / VIEWER), activate or deactivate.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "List users")
    @GetMapping
    public List<UserResponse> list() {
        return userService.listUsers();
    }

    @Operation(summary = "Update user role")
    @PatchMapping("/{id}/role")
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return userService.updateRole(id, request);
    }

    @Operation(summary = "Update user active status")
    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return userService.updateStatus(id, request);
    }
}
