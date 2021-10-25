package com.erpapplication.VatChecker;

public class EUVatCheckResponse {
    private final boolean isValid;
    private final String name;
    private final String address;

    private final boolean error;
    private final Fault fault;

    EUVatCheckResponse(boolean isValid, String name, String address, boolean error, Fault fault) {
        this.isValid = isValid;
        this.name = name;
        this.address = address;
        this.error = error;
        this.fault = fault;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isError() {
        return error;
    }

    public Fault getFault() {
        return fault;
    }

    public static class Fault {
        private final String faultCode;
        private final FaultType faultType;
        private final String fault;

        Fault(String faultCode, String fault) {
            this.faultCode = faultCode;
            this.faultType = tryParse(fault);
            this.fault = fault;
        }

        public String getFaultCode() {
            return faultCode;
        }

        public FaultType getFaultType() {
            return faultType;
        }

        public String getFault() {
            return fault;
        }
    }

    private static FaultType tryParse(String fault) {
        try {
            return FaultType.valueOf(fault);
        } catch (IllegalArgumentException | NullPointerException e) {
            return FaultType.OTHER;
        }
    }

    public enum FaultType {
        INVALID_INPUT,
        GLOBAL_MAX_CONCURRENT_REQ,
        MS_MAX_CONCURRENT_REQ,
        SERVICE_UNAVAILABLE,
        MS_UNAVAILABLE,
        TIMEOUT,
        OTHER
    }
}
