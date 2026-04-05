package com.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finance.dto.auth.LoginRequest;
import com.finance.dto.auth.RegisterRequest;
import com.finance.enums.Role;
import com.finance.model.User;
import com.finance.repository.UserRepository;
import com.finance.security.AuthUserDetails;
import com.finance.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@b.com");
        req.setPassword("password12");
        assertThatThrownBy(() -> authService.register(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_createsViewerAndReturnsToken() {
        when(userRepository.existsByEmailIgnoreCase("new@b.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> {
                    User u = inv.getArgument(0);
                    u.setId(99L);
                    return u;
                });

        RegisterRequest req = new RegisterRequest();
        req.setEmail("New@B.com");
        req.setPassword("password12");
        var res = authService.register(req);
        assertThat(res.getToken()).isEqualTo("jwt-token");
        assertThat(res.getRole()).isEqualTo(Role.VIEWER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_returnsToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("u@b.com");
        req.setPassword("password12");
        AuthUserDetails principal = new AuthUserDetails(1L, "u@b.com", "hash", Role.VIEWER, true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        User u = new User();
        u.setId(1L);
        u.setEmail("u@b.com");
        u.setRole(Role.VIEWER);
        when(userRepository.findByEmailIgnoreCase("u@b.com")).thenReturn(Optional.of(u));
        when(jwtService.generateToken(u)).thenReturn("tok");

        var res = authService.login(req);
        assertThat(res.getToken()).isEqualTo("tok");
    }
}
