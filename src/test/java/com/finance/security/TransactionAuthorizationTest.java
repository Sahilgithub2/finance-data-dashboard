package com.finance.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.finance.enums.Role;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.repository.TransactionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class TransactionAuthorizationTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionAuthorization authorization;

    @Test
    void adminCanModifyAny() {
        AuthUserDetails admin = new AuthUserDetails(1L, "a@b.com", "x", Role.ADMIN, true);
        var auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
        assertThat(authorization.canModifyTransaction(5L, auth)).isTrue();
    }

    @Test
    void ownerCanModifyOwn() {
        AuthUserDetails viewer = new AuthUserDetails(2L, "v@b.com", "x", Role.VIEWER, true);
        var auth = new UsernamePasswordAuthenticationToken(viewer, null, viewer.getAuthorities());
        User owner = new User();
        owner.setId(2L);
        Transaction tx = new Transaction();
        tx.setUser(owner);
        when(transactionRepository.findActiveById(10L)).thenReturn(Optional.of(tx));
        assertThat(authorization.canModifyTransaction(10L, auth)).isTrue();
    }

    @Test
    void otherUserCannotModify() {
        AuthUserDetails viewer = new AuthUserDetails(3L, "v@b.com", "x", Role.VIEWER, true);
        var auth = new UsernamePasswordAuthenticationToken(viewer, null, viewer.getAuthorities());
        User owner = new User();
        owner.setId(2L);
        Transaction tx = new Transaction();
        tx.setUser(owner);
        when(transactionRepository.findActiveById(10L)).thenReturn(Optional.of(tx));
        assertThat(authorization.canModifyTransaction(10L, auth)).isFalse();
    }
}
