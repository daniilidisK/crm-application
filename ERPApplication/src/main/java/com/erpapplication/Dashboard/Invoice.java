package com.erpapplication.Dashboard;

import java.time.LocalDate;

public class Invoice {
    protected final String Client;
    protected final String Address;
    protected final String City;
    protected final String DOY;
    protected final String Occupation;
    protected final double VAT_Number;
    protected final String VAT_ID;
    protected final String Payment;
    protected double clearAmount;
    protected int invoiceNumber;
    protected LocalDate Date;

    public Invoice(int invoiceNumber, LocalDate Date, String Client, String Address, String City, String DOY, String Occupation,
                   double VAT_Number, String VAT_ID, String Payment) {
        this.invoiceNumber = invoiceNumber;
        this.Date = Date;
        this.Client = Client;
        this.Address = Address;
        this.Payment = Payment;
        this.City = City;
        this.DOY = DOY;
        this.Occupation = Occupation;
        this.VAT_Number = VAT_Number;
        this.VAT_ID = VAT_ID;
    }

    public Invoice(int invoiceNumber, LocalDate Date, String Client, String Address, String City, String DOY, String Occupation,
                   double VAT_Number, String VAT_ID, String Payment, double clearAmount) {
        this.invoiceNumber = invoiceNumber;
        this.Date = Date;
        this.Client = Client;
        this.Address = Address;
        this.Payment = Payment;
        this.City = City;
        this.DOY = DOY;
        this.Occupation = Occupation;
        this.VAT_Number = VAT_Number;
        this.VAT_ID = VAT_ID;
        this.clearAmount = clearAmount;
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDate getDate() {
        return Date;
    }

    public String getPayment() {
        return Payment;
    }

    public String getClient() {
        return Client;
    }

    public String getAddress() {
        return Address;
    }

    public String getCity() {
        return City;
    }

    public String getDOY() {
        return DOY;
    }

    public String getOccupation() {
        return Occupation;
    }

    public double getVAT_Number() {
        return VAT_Number;
    }

    public String getVAT_ID() {
        return VAT_ID;
    }

    public double getClearAmount() {
        return clearAmount;
    }

    @Override
    public String toString() {
        return ("ΕΠΩΝΥΜΙΑ: " + getClient() + "\nΔΙΕΥΘΥΝΣΗ: " + getAddress() + "\nΠΟΛΗ: " +
                getCity() + "\nΔ.Ο.Υ.: " + getDOY() + "\nΕΠΑΓΓΕΛΜΑ: " + getOccupation() + "\nΑ.Φ.Μ.: " + getVAT_ID());
    }
}