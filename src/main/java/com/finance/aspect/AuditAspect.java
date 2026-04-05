package com.finance.aspect;

import com.finance.dto.transaction.TransactionResponse;
import com.finance.dto.user.UserResponse;
import com.finance.enums.AuditAction;
import com.finance.security.AuthUserDetails;
import com.finance.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(
            pointcut =
                    "execution(* com.finance.service.TransactionService.create(..))",
            returning = "result")
    public void auditTransactionCreate(TransactionResponse result) {
        auditService.record(
                currentUserId(), AuditAction.CREATE, "Transaction", result.getId(), "Created transaction");
    }

    @AfterReturning(
            pointcut =
                    "execution(* com.finance.service.TransactionService.update(..))",
            returning = "result")
    public void auditTransactionUpdate(JoinPoint joinPoint, TransactionResponse result) {
        Long id = (Long) joinPoint.getArgs()[0];
        auditService.record(
                currentUserId(), AuditAction.UPDATE, "Transaction", id, "Updated transaction " + result.getId());
    }

    @AfterReturning(
            pointcut =
                    "execution(* com.finance.service.TransactionService.softDelete(..))")
    public void auditTransactionDelete(JoinPoint joinPoint) {
        Long id = (Long) joinPoint.getArgs()[0];
        auditService.record(currentUserId(), AuditAction.DELETE, "Transaction", id, "Soft-deleted transaction");
    }

    @AfterReturning(
            pointcut = "execution(* com.finance.service.UserService.updateRole(..))",
            returning = "result")
    public void auditUserRole(JoinPoint joinPoint, UserResponse result) {
        Long id = (Long) joinPoint.getArgs()[0];
        auditService.record(
                currentUserId(), AuditAction.UPDATE, "User", id, "Changed role for user " + result.getEmail());
    }

    @AfterReturning(
            pointcut = "execution(* com.finance.service.UserService.updateStatus(..))",
            returning = "result")
    public void auditUserStatus(JoinPoint joinPoint, UserResponse result) {
        Long id = (Long) joinPoint.getArgs()[0];
        auditService.record(
                currentUserId(),
                AuditAction.UPDATE,
                "User",
                id,
                "Changed active status for user " + result.getEmail() + " to " + result.isActive());
    }

    private static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth.getPrincipal() instanceof AuthUserDetails u) {
            return u.getId();
        }
        return null;
    }
}
