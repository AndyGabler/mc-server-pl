package com.gabler.huntersmc.util;

public class CsvRow {

    private final CsvLoader loader;
    private int originalIndex;

    public CsvRow(CsvLoader aLoader, int anOriginalIndex) {
        loader = aLoader;
        this.originalIndex = anOriginalIndex;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public String getValue(int columnIndex) {
        return loader.getRowValue(this, columnIndex);
    }

    public void setValue(int columnIndex, String value) {
        loader.setRowValue(this, columnIndex, value);
    }

    public String getValue(String columnName) {
        return loader.getRowValue(this, columnName);
    }

    public void setValue(String columnName, String value) {
        loader.setRowValue(this, columnName, value);
    }

    public void delete() {
        loader.deleteRow(this);
    }
}
