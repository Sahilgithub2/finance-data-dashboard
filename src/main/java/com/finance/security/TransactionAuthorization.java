package com.finance.security;

import com.finance.enums.Role;
import com.finance.repository.TransactionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authz")
public class TransactionAuthorization {

    private final TransactionRepository transactionRepository;

    public TransactionAuthorization(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public boolean canModifyTransaction(Long id, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserDetails principal)) {
            return false;
        }
        if (principal.getRole() == Role.ADMIN) {
            return true;
        }
        return transactionRepository
                .findActiveById(id)
                .map(t -> t.getUser().getId().equals(principal.getId()))
                .orElse(false);
    }
}
