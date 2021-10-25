package com.erpapplication.ClientDatabase;

public class Client {
    private final String ClientName;
    private final String Address;
    private final String City;
    private final String DOY;
    private final String Occupation;
    private String BankAccount;
    private final String VATID;

    public Client(String Address, String BankAccount, String City, String ClientName, String DOY, String Occupation, String VATID){
        this.ClientName = ClientName;
        this.Address = Address;
        this.City = City;
        this.DOY = DOY;
        this.Occupation = Occupation;
        this.BankAccount = BankAccount;
        this.VATID = VATID;
    }

    public Client(String Address, String City, String ClientName, String DOY, String Occupation, String VATID){
        this.ClientName = ClientName;
        this.Address = Address;
        this.City = City;
        this.DOY = DOY;
        this.Occupation = Occupation;
        this.VATID = VATID;
    }

    public String getClientName(){
        return ClientName;
    }
    public String getAddress(){
        return Address;
    }
    public String getCity(){
        return City;
    }
    public String getDOY(){
        return DOY;
    }
    public String getOccupation(){
        return Occupation;
    }
    public String getBankAccount(){
        return BankAccount;
    }
    public String getVATID() {
        return VATID;
    }
}
