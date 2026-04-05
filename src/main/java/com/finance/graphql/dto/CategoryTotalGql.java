package com.finance.graphql.dto;

public class CategoryTotalGql {

    private String category;
    private String totalAmount;
    private String type;

    public CategoryTotalGql() {}

    public CategoryTotalGql(String category, String totalAmount, String type) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
