package com.erpapplication.InvoiceDatabase;

public class History {
    protected final int invNo;
    protected final String name;
    protected final String DOY;
    protected final String city;
    protected final String date;
    protected final String vatid;
    protected final String credit;
    protected final double totalAmount;
    protected final double Vat;
    protected final String address;

    public History(int invNo,
                   String name,
                   String address,
                   String vatid,
                   String DOY,
                   String city,
                   String date,
                   String credit,
                   double vat,
                   double totalAmount) {
        this.invNo = invNo;
        this.name = name;
        this.address = address;
        this.vatid = vatid;
        this.DOY = DOY;
        this.city = city;
        this.date = date;
        this.credit = credit;
        this.Vat = vat;
        this.totalAmount = totalAmount;
    }

    public int getInvNo() {
        return invNo;
    }

    public String getName() {
        return name;
    }

    public String getDOY() {
        return DOY;
    }

    public String getCity() {
        return city;
    }

    public String getDate() {
        return date;
    }

    public String getCredit() {
        return credit;
    }

    public String getVatid() {
        return vatid;
    }

    public double getVat() {
        return Vat;
    }

    public String getAddress() {
        return address;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
