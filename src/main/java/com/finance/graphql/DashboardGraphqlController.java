package com.finance.graphql;

import com.finance.graphql.dto.CategoryTotalGql;
import com.finance.graphql.dto.DashboardSummaryGql;
import com.finance.graphql.dto.MonthlyTrendGql;
import com.finance.graphql.dto.RecentTransactionGql;
import com.finance.service.DashboardService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardGraphqlController {

    private final DashboardService dashboardService;

    public DashboardGraphqlController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @QueryMapping
    public DashboardSummaryGql dashboardSummary(
            @Argument String dateFrom, @Argument String dateTo, Authentication authentication) {
        return dashboardService.summary(parseDate(dateFrom), parseDate(dateTo), authentication);
    }

    @QueryMapping
    public List<CategoryTotalGql> categoryBreakdown(
            @Argument String dateFrom, @Argument String dateTo, Authentication authentication) {
        return dashboardService.categoryBreakdown(parseDate(dateFrom), parseDate(dateTo), authentication);
    }

    @QueryMapping
    public List<MonthlyTrendGql> monthlyTrends(
            @Argument String dateFrom, @Argument String dateTo, Authentication authentication) {
        return dashboardService.monthlyTrends(parseDate(dateFrom), parseDate(dateTo), authentication);
    }

    @QueryMapping
    public List<RecentTransactionGql> recentTransactions(
            @Argument Integer limit,
            @Argument String dateFrom,
            @Argument String dateTo,
            Authentication authentication) {
        return dashboardService.recentTransactions(limit, parseDate(dateFrom), parseDate(dateTo), authentication);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }
}
