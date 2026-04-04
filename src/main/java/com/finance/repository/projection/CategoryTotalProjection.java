package com.finance.repository.projection;

import java.math.BigDecimal;

public interface CategoryTotalProjection {

    String getCategory();

    String getTransactionType();

    BigDecimal getTotalAmount();
}
