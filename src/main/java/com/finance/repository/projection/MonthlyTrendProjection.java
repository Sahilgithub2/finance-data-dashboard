package com.finance.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlyTrendProjection {

    LocalDate getMonth();

    BigDecimal getIncome();

    BigDecimal getExpense();
}
