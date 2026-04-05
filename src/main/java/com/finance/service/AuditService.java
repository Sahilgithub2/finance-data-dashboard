package com.finance.service;

import com.finance.enums.AuditAction;
import com.finance.model.AuditLog;
import com.finance.model.User;
import com.finance.repository.AuditLogRepository;
import com.finance.repository.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, AuditAction action, String entity, Long entityId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setDetails(details);
        if (userId != null) {
            Optional<User> u = userRepository.findById(userId);
            u.ifPresent(log::setUser);
        }
        auditLogRepository.save(log);
    }
}
