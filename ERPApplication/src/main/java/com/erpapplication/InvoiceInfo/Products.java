package com.erpapplication.InvoiceInfo;

public record Products(String item, int quantity, double unit_Price, double subtotal) {

    public String getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnit_Price() {
        return unit_Price;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
