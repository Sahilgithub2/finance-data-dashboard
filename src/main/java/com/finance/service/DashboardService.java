package com.finance.service;

import com.finance.enums.Role;
import com.finance.enums.TransactionType;
import com.finance.graphql.dto.CategoryTotalGql;
import com.finance.graphql.dto.DashboardSummaryGql;
import com.finance.graphql.dto.MonthlyTrendGql;
import com.finance.graphql.dto.RecentTransactionGql;
import com.finance.model.Transaction;
import com.finance.repository.TransactionRepository;
import com.finance.repository.projection.CategoryTotalProjection;
import com.finance.repository.projection.MonthlyTrendProjection;
import com.finance.security.AuthUserDetails;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public DashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public DashboardSummaryGql summary(LocalDate dateFrom, LocalDate dateTo, Authentication authentication) {
        LocalDate[] range = normalizeRange(dateFrom, dateTo);
        Long scope = scopeUserId(authentication);
        BigDecimal income = transactionRepository.sumAmountByTypeScoped(
                TransactionType.INCOME.name(), scope, range[0], range[1]);
        BigDecimal expense = transactionRepository.sumAmountByTypeScoped(
                TransactionType.EXPENSE.name(), scope, range[0], range[1]);
        BigDecimal net = income.subtract(expense);
        return new DashboardSummaryGql(fmt(income), fmt(expense), fmt(net));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<CategoryTotalGql> categoryBreakdown(LocalDate dateFrom, LocalDate dateTo, Authentication authentication) {
        LocalDate[] range = normalizeRange(dateFrom, dateTo);
        Long scope = scopeUserId(authentication);
        List<CategoryTotalProjection> rows =
                transactionRepository.sumByCategoryScoped(scope, range[0], range[1]);
        return rows.stream()
                .map(r -> new CategoryTotalGql(
                        r.getCategory(), fmt(r.getTotalAmount()), r.getTransactionType()))
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<MonthlyTrendGql> monthlyTrends(LocalDate dateFrom, LocalDate dateTo, Authentication authentication) {
        LocalDate[] range = normalizeRange(dateFrom, dateTo);
        Long scope = scopeUserId(authentication);
        List<MonthlyTrendProjection> rows =
                transactionRepository.monthlyTrendsScoped(scope, range[0], range[1]);
        return rows.stream()
                .map(r -> new MonthlyTrendGql(r.getMonth().toString(), fmt(r.getIncome()), fmt(r.getExpense())))
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<RecentTransactionGql> recentTransactions(
            Integer limit, LocalDate dateFrom, LocalDate dateTo, Authentication authentication) {
        int lim = limit == null || limit < 1 ? 10 : Math.min(limit, 100);
        LocalDate[] range = normalizeRange(dateFrom, dateTo);
        Long scope = scopeUserId(authentication);
        List<Transaction> rows = transactionRepository.findRecentScoped(
                scope, range[0], range[1], PageRequest.of(0, lim));
        return rows.stream()
                .map(t -> new RecentTransactionGql(
                        t.getId().toString(),
                        fmt(t.getAmount()),
                        t.getType().name(),
                        t.getCategory(),
                        t.getDate().toString()))
                .toList();
    }

    private static Long scopeUserId(Authentication authentication) {
        AuthUserDetails u = (AuthUserDetails) authentication.getPrincipal();
        if (u.getRole() == Role.VIEWER) {
            return u.getId();
        }
        return null;
    }

    private static LocalDate[] normalizeRange(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusMonths(12);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("dateFrom must be on or before dateTo");
        }
        return new LocalDate[] {start, end};
    }

    private static String fmt(BigDecimal v) {
        BigDecimal n = v == null ? BigDecimal.ZERO : v;
        return n.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
