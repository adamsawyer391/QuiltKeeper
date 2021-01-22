package com.cosmicdesigns.quiltkeeper.model;

public class InvestmentModel {

    private String investment_description;
    private String investment_cost;
    private String date_selected;
    private String investmentKey;
    private String timestamp;

    public InvestmentModel() {
        //default empty constructor
    }

    public InvestmentModel(String investment_description, String investment_cost, String date_selected, String investmentKey, String timestamp) {
        this.investment_description = investment_description;
        this.investment_cost = investment_cost;
        this.date_selected = date_selected;
        this.investmentKey = investmentKey;
        this.timestamp = timestamp;
    }

    public String getInvestment_description() {
        return investment_description;
    }

    public void setInvestment_description(String investment_description) {
        this.investment_description = investment_description;
    }

    public String getInvestment_cost() {
        return investment_cost;
    }

    public void setInvestment_cost(String investment_cost) {
        this.investment_cost = investment_cost;
    }

    public String getDate_selected() {
        return date_selected;
    }

    public void setDate_selected(String date_selected) {
        this.date_selected = date_selected;
    }

    public String getInvestmentKey() {
        return investmentKey;
    }

    public void setInvestmentKey(String investmentKey) {
        this.investmentKey = investmentKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "InvestmentModel{" +
                "investment_description='" + investment_description + '\'' +
                ", investment_cost='" + investment_cost + '\'' +
                ", date_selected='" + date_selected + '\'' +
                ", investmentKey='" + investmentKey + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
