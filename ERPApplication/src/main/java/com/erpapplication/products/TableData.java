package com.erpapplication.products;

public class TableData {
    private final String desc;
    private final int quantity;
    private final Double item_price;

    public TableData(String desc, int quantity, Double item_price) {
        this.desc = desc;
        this.quantity = quantity;
        this.item_price = item_price;
    }

    public String getDesc() {
        return desc;
    }

    public int getQuantity() {
        return quantity;
    }

    public Double getItem_price() {
        return item_price;
    }
}