package com.erpapplication.InvoiceInfo;

public class Products {
    protected final String item;
    protected final int quantity;
    protected final double unit_Price;
    protected final double subtotal;

    public Products(String item, int quantity, double unit_Price, double subtotal) {
        this.item = item;
        this.quantity = quantity;
        this.unit_Price = unit_Price;
        this.subtotal = subtotal;
    }

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
