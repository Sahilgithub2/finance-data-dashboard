package com.finance.graphql.dto;

public class MonthlyTrendGql {

    private String month;
    private String income;
    private String expense;

    public MonthlyTrendGql() {}

    public MonthlyTrendGql(String month, String income, String expense) {
        this.month = month;
        this.income = income;
        this.expense = expense;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getIncome() {
        return income;
    }

    public void setIncome(String income) {
        this.income = income;
    }

    public String getExpense() {
        return expense;
    }

    public void setExpense(String expense) {
        this.expense = expense;
    }
}
