package com.finance.controller;

import com.finance.dto.transaction.PagedTransactionsResponse;
import com.finance.dto.transaction.TransactionCreateRequest;
import com.finance.dto.transaction.TransactionResponse;
import com.finance.dto.transaction.TransactionUpdateRequest;
import com.finance.enums.TransactionType;
import com.finance.service.TransactionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public PagedTransactionsResponse list(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return transactionService.findAll(type, category, from, to, page, size);
    }

    @GetMapping("/mine")
    public PagedTransactionsResponse mine(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        return transactionService.findMine(type, category, from, to, page, size, authentication);
    }

    @PostMapping
    public TransactionResponse create(
            @Valid @RequestBody TransactionCreateRequest request, Authentication authentication) {
        return transactionService.create(request, authentication);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request,
            Authentication authentication) {
        return transactionService.update(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        transactionService.softDelete(id, authentication);
    }
}
