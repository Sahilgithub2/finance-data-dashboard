package com.finance.repository;

import com.finance.enums.TransactionType;
import com.finance.model.Transaction;
import com.finance.repository.projection.CategoryTotalProjection;
import com.finance.repository.projection.MonthlyTrendProjection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(
            countQuery =
                    """
                    SELECT count(t) FROM Transaction t
                    WHERE t.deleted = false
                    AND t.user.id = :userId
                    AND (:txType IS NULL OR t.type = :txType)
                    AND (:categoryFilter IS NULL OR t.category = :categoryFilter)
                    AND t.date >= :fromDate AND t.date <= :toDate
                    """,
            value =
                    """
                    SELECT t FROM Transaction t
                    WHERE t.deleted = false
                    AND t.user.id = :userId
                    AND (:txType IS NULL OR t.type = :txType)
                    AND (:categoryFilter IS NULL OR t.category = :categoryFilter)
                    AND t.date >= :fromDate AND t.date <= :toDate
                    ORDER BY t.date DESC, t.id DESC
                    """)
    Page<Transaction> findMine(
            @Param("userId") Long userId,
            @Param("txType") TransactionType type,
            @Param("categoryFilter") String category,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query(
            countQuery =
                    """
                    SELECT count(t) FROM Transaction t
                    WHERE t.deleted = false
                    AND (:txType IS NULL OR t.type = :txType)
                    AND (:categoryFilter IS NULL OR t.category = :categoryFilter)
                    AND t.date >= :fromDate AND t.date <= :toDate
                    """,
            value =
                    """
                    SELECT t FROM Transaction t
                    WHERE t.deleted = false
                    AND (:txType IS NULL OR t.type = :txType)
                    AND (:categoryFilter IS NULL OR t.category = :categoryFilter)
                    AND t.date >= :fromDate AND t.date <= :toDate
                    ORDER BY t.date DESC, t.id DESC
                    """)
    Page<Transaction> findAllActiveFiltered(
            @Param("txType") TransactionType type,
            @Param("categoryFilter") String category,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query(
            """
            SELECT t FROM Transaction t
            WHERE t.deleted = false AND t.id = :id
            """)
    Optional<Transaction> findActiveById(@Param("id") Long id);

    @Query(
            value =
                    """
                    SELECT COALESCE(SUM(t.amount), 0)
                    FROM transactions t
                    WHERE t.is_deleted = false
                    AND t.type = CAST(:type AS text)
                    AND (:scopeUserId IS NULL OR t.user_id = :scopeUserId)
                    AND t.date BETWEEN :fromDate AND :toDate
                    """,
            nativeQuery = true)
    BigDecimal sumAmountByTypeScoped(
            @Param("type") String type,
            @Param("scopeUserId") Long scopeUserId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query(
            nativeQuery = true,
            value =
                    """
                    SELECT t.category AS category,
                           t.type AS transaction_type,
                           COALESCE(SUM(t.amount), 0) AS total_amount
                    FROM transactions t
                    WHERE t.is_deleted = false
                    AND (:scopeUserId IS NULL OR t.user_id = :scopeUserId)
                    AND t.date BETWEEN :fromDate AND :toDate
                    GROUP BY t.category, t.type
                    ORDER BY t.category, t.type
                    """)
    List<CategoryTotalProjection> sumByCategoryScoped(
            @Param("scopeUserId") Long scopeUserId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query(
            nativeQuery = true,
            value =
                    """
                    SELECT (DATE_TRUNC('month', t.date))::date AS month,
                           COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) AS income,
                           COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS expense
                    FROM transactions t
                    WHERE t.is_deleted = false
                    AND (:scopeUserId IS NULL OR t.user_id = :scopeUserId)
                    AND t.date BETWEEN :fromDate AND :toDate
                    GROUP BY DATE_TRUNC('month', t.date)
                    ORDER BY month
                    """)
    List<MonthlyTrendProjection> monthlyTrendsScoped(
            @Param("scopeUserId") Long scopeUserId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query(
            """
            SELECT t FROM Transaction t JOIN FETCH t.user u
            WHERE t.deleted = false
            AND (:scopeUserId IS NULL OR u.id = :scopeUserId)
            AND t.date BETWEEN :fromDate AND :toDate
            ORDER BY t.date DESC, t.id DESC
            """)
    List<Transaction> findRecentScoped(
            @Param("scopeUserId") Long scopeUserId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);
}
