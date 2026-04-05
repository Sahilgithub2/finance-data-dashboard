package com.finance.graphql.dto;

public class DashboardSummaryGql {

    private String totalIncome;
    private String totalExpense;
    private String netBalance;

    public DashboardSummaryGql() {}

    public DashboardSummaryGql(String totalIncome, String totalExpense, String netBalance) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
    }

    public String getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(String totalIncome) {
        this.totalIncome = totalIncome;
    }

    public String getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(String totalExpense) {
        this.totalExpense = totalExpense;
    }

    public String getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(String netBalance) {
        this.netBalance = netBalance;
    }
}
