package com.finance.service;

import com.finance.dto.transaction.PagedTransactionsResponse;
import com.finance.dto.transaction.TransactionCreateRequest;
import com.finance.dto.transaction.TransactionResponse;
import com.finance.dto.transaction.TransactionUpdateRequest;
import com.finance.enums.TransactionType;
import com.finance.exception.ResourceNotFoundException;
import com.finance.mapper.EntityMapper;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import com.finance.security.AuthUserDetails;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    public TransactionService(
            TransactionRepository transactionRepository, UserRepository userRepository, EntityMapper entityMapper) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.entityMapper = entityMapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Transactional(readOnly = true)
    public PagedTransactionsResponse findAll(
            TransactionType type, String category, LocalDate from, LocalDate to, int page, int size) {
        LocalDate[] range = normalizeRange(from, to);
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> result =
                transactionRepository.findAllActiveFiltered(type, category, range[0], range[1], pageable);
        return toPage(result);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PagedTransactionsResponse findMine(
            TransactionType type,
            String category,
            LocalDate from,
            LocalDate to,
            int page,
            int size,
            Authentication authentication) {
        AuthUserDetails user = (AuthUserDetails) authentication.getPrincipal();
        LocalDate[] range = normalizeRange(from, to);
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> result = transactionRepository.findMine(
                user.getId(), type, category, range[0], range[1], pageable);
        return toPage(result);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TransactionResponse create(TransactionCreateRequest request, Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        User owner = userRepository.findById(principal.getId()).orElseThrow();
        Transaction tx = new Transaction();
        tx.setUser(owner);
        tx.setAmount(request.getAmount());
        tx.setType(request.getType());
        tx.setCategory(request.getCategory().trim());
        tx.setDate(request.getDate());
        tx.setNotes(request.getNotes());
        tx.setDeleted(false);
        transactionRepository.save(tx);
        return entityMapper.toTransactionResponse(tx);
    }

    @PreAuthorize("@authz.canModifyTransaction(#id, authentication)")
    @Transactional
    public TransactionResponse update(Long id, TransactionUpdateRequest request, Authentication authentication) {
        Transaction tx = transactionRepository
                .findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        tx.setAmount(request.getAmount());
        tx.setType(request.getType());
        tx.setCategory(request.getCategory().trim());
        tx.setDate(request.getDate());
        tx.setNotes(request.getNotes());
        return entityMapper.toTransactionResponse(transactionRepository.save(tx));
    }

    @PreAuthorize("@authz.canModifyTransaction(#id, authentication)")
    @Transactional
    public void softDelete(Long id, Authentication authentication) {
        Transaction tx = transactionRepository
                .findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        tx.setDeleted(true);
        transactionRepository.save(tx);
    }

    private PagedTransactionsResponse toPage(Page<Transaction> page) {
        return new PagedTransactionsResponse(
                page.getContent().stream().map(entityMapper::toTransactionResponse).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }

    private static LocalDate[] normalizeRange(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusMonths(12);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("dateFrom must be on or before dateTo");
        }
        return new LocalDate[] {start, end};
    }
}
