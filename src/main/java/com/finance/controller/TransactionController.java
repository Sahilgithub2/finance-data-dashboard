package com.finance.controller;

import com.finance.dto.transaction.PagedTransactionsResponse;
import com.finance.dto.transaction.TransactionCreateRequest;
import com.finance.dto.transaction.TransactionResponse;
import com.finance.dto.transaction.TransactionUpdateRequest;
import com.finance.enums.TransactionType;
import com.finance.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Transactions",
        description =
                "Admin & Analyst: list all. Viewer: use **GET /mine** only (403 on list-all). Mutations: owner or Admin.")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "List all transactions (paged)", description = "Admin and Analyst. Optional filters: type, category, from, to.")
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

    @Operation(summary = "List my transactions (paged)")
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

    @Operation(summary = "Create transaction", description = "Assigned to the authenticated user.")
    @PostMapping
    public TransactionResponse create(
            @Valid @RequestBody TransactionCreateRequest request, Authentication authentication) {
        return transactionService.create(request, authentication);
    }

    @Operation(summary = "Update transaction", description = "Admin or transaction owner.")
    @PutMapping("/{id}")
    public TransactionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request,
            Authentication authentication) {
        return transactionService.update(id, request, authentication);
    }

    @Operation(summary = "Soft-delete transaction", description = "Admin or transaction owner.")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        transactionService.softDelete(id, authentication);
    }
}
