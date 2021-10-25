package com.erpapplication.VatChecker;

abstract class BaseFault<T extends Enum<T>> {

    protected BaseFault(String fault, T defaultValue) {
    }
}
