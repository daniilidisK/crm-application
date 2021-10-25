package com.erpapplication.VatChecker;

public class GRVatCheckResponse {

    private final boolean validSyntax;
    private final boolean validStructure;

    private final boolean error;
    private final Fault fault;

    GRVatCheckResponse(boolean validSyntax, boolean validStructure, boolean error, Fault fault) {
        this.validSyntax = validSyntax;
        this.validStructure = validStructure;
        this.error = error;
        this.fault = fault;
    }

    public boolean isValidSyntax() {
        return validSyntax;
    }

    public boolean isValidStructure() {
        return validStructure;
    }

    public boolean isError() {
        return error;
    }

    public Fault getFault() {
        return fault;
    }

    public static class Fault extends BaseFault<FaultType> {
        Fault(String faultCode, String fault) {
            super(fault, FaultType.OTHER);
        }
    }

    public enum FaultType {
        INVALID_INPUT,
        NO_INFORMATION,
        SERVICE_UNAVAILABLE,
        SERVER_BUSY,
        OTHER
    }
}
