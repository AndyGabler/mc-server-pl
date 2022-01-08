package com.gabler.huntersmc.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;

public class CsvLoader {

    public static final Pattern ACCEPTABLE_CHARACTERS_REGEX =
        Pattern.compile("[\\dA-Za-z\\-.,!|&'%$#^()=<>\\/\\\\ ]*");

    public static final HashMap<Character, Character> REPLACEMENT_CHARACTERS;
    public static final HashMap<Character, Character> READ_CONVERSION_CHARACTERS;
    private static final Character NULL_CHARACTER = (char)175;
    static {
        REPLACEMENT_CHARACTERS = new HashMap<>();
        REPLACEMENT_CHARACTERS.put(',', (char)174);
        REPLACEMENT_CHARACTERS.put('|', (char)176);

        READ_CONVERSION_CHARACTERS = new HashMap<>();
        REPLACEMENT_CHARACTERS.keySet().forEach(key -> {
            Character value = REPLACEMENT_CHARACTERS.get(key);
            READ_CONVERSION_CHARACTERS.put(value, key);
        });
    }

    private final String path;
    private String metaRow = "";
    private HashMap<String, Integer> columnToIndexMap = new HashMap<>();
    private ArrayList<ArrayList<String>> data = new ArrayList<>();

    public CsvLoader(String aPath) {
        path = "plugins/HuntersMC/" + aPath; // TODO don't hard-code me
    }

    public void setMetaDataRow(String... columnList) {
        // Just adds the values and null values
        if (columnList.length < columnToIndexMap.size()) {
            throw new CsvDataIntegrityException("CSV loader will not prune data. Can only add/rename columns.");
        }

        final int oldLength = columnToIndexMap.size();
        final int newLength = columnList.length;

        columnToIndexMap.clear();
        metaRow = "";
        for (int index = 0; index < columnList.length; index++) {
            columnToIndexMap.put(columnList[index], index);
            metaRow += columnList[index];
            if (index != columnList.length - 1) {
                metaRow += ",";
            }
        }
        if (oldLength == newLength) {
            return;
        }

        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            for (int columnIndex = oldLength; columnIndex < newLength; columnIndex++) {
                data.get(rowIndex).add(null);
                setValue(rowIndex, columnIndex, null);
            }
        }
    }

    public CsvRow newRow() {
        final ArrayList<String> rowData = new ArrayList<>();
        for (int index = 0; index < columnToIndexMap.size(); index++) {
            rowData.add(null);
        }
        data.add(rowData);
        return new CsvRow(this, data.size() - 1);
    }

    public void deleteRow(int rowIndex) {
        data.remove(rowIndex);
    }

    public void deleteRow(CsvRow row) {
        deleteRow(row.getOriginalIndex());
    }

    public void ensureFileExists() throws IOException {
        final File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public void load() throws FileNotFoundException, CsvDataIntegrityException {
        load(new File(path));
    }

    public void load(File csvFile) throws FileNotFoundException, CsvDataIntegrityException {
        final Scanner scanner = new Scanner(csvFile);

        columnToIndexMap = new HashMap<>();
        data = new ArrayList<>();

        Integer columnCount = null;

        int lineNumber = 0;
        if (scanner.hasNextLine()) {
            lineNumber++;
            final String metaRow = scanner.nextLine();
            final String[] columnNames = metaRow.split(",");
            columnCount = columnNames.length;
            this.metaRow = metaRow;

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                columnToIndexMap.put(columnNames[columnIndex], columnIndex);
            }
        }

        while (scanner.hasNextLine()) {
            lineNumber++;
            final String line = scanner.nextLine();
            String[] data = line.split(",");

            if (data.length != columnCount) {
                throw new CsvDataIntegrityException("Incorrect column count on line " + lineNumber + ". Had " + data.length + " columns. Required " + columnCount + ".");
            }

            this.data.add(new ArrayList<>(Arrays.asList(data)));
        }
    }

    public void save() throws IOException {
        save(new BufferedWriter(new FileWriter(path)));
    }

    public void save(BufferedWriter writer) throws IOException {
        final StringBuilder builder = new StringBuilder(metaRow);

        if (data.size() != 0) {
            builder.append("\n");
        }

        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            ArrayList<String> row = data.get(rowIndex);
            for (int cellIndex = 0; cellIndex < row.size(); cellIndex++) {
                builder.append(row.get(cellIndex));

                if (cellIndex != row.size() - 1) {
                    builder.append(",");
                }
            }

            if (rowIndex != data.size() - 1) {
                builder.append("\n");
            }
        }

        writer.write(builder.toString());
        writer.flush();
        writer.close();
    }

    public List<CsvRow> getRows() {
        return getRowsByCriteria(r -> true);
    }

    public List<CsvRow> getRowsByCriteria(Function<CsvRow, Boolean> criteria) {
        final ArrayList<CsvRow> rows = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            final CsvRow candidateRow = new CsvRow(this, rowIndex);
            if (criteria.apply(candidateRow)) {
                rows.add(candidateRow);
            }
        }
        return rows;
    }

    public CsvRow getRowByCriteria(Function<CsvRow, Boolean> criteria) {
        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            final CsvRow candidateRow = new CsvRow(this, rowIndex);
            if (criteria.apply(candidateRow)) {
                return candidateRow;
            }
        }
        return null;
    }

    public String getValue(int rowIndex, int columnIndex) {
        String value = data.get(rowIndex).get(columnIndex);
        // Doing a little something special for null
        if (value.equals(NULL_CHARACTER + "")) {
            return null;
        }

        for (Character key : READ_CONVERSION_CHARACTERS.keySet()) {
            value = value.replace(key, READ_CONVERSION_CHARACTERS.get(key));
        }
        return value;
    }

    public void setValue(int rowIndex, int columnIndex, String value) {

        if (value != null) {
            if (!ACCEPTABLE_CHARACTERS_REGEX.matcher(value).matches()) {
                throw new CsvDataIntegrityException("Illegal character in CSV value.");
            }

            for (Character key : REPLACEMENT_CHARACTERS.keySet()) {
                value = value.replace(key, REPLACEMENT_CHARACTERS.get(key));
            }
        } else {
            value = NULL_CHARACTER + "";
        }
        data.get(rowIndex).set(columnIndex, value);
    }

    public String getValue(int rowIndex, String columnName) {
        Integer columnIndex = columnToIndexMap.get(columnName);
        if (columnIndex == null) {
            throw new CsvDataIntegrityException("No column with name \"" + columnName + "\".");
        }
        return getValue(rowIndex, columnIndex);
    }

    public void setValue(int rowIndex, String columnName, String value) {
        Integer columnIndex = columnToIndexMap.get(columnName);
        if (columnIndex == null) {
            throw new CsvDataIntegrityException("No column with name \"" + columnName + "\".");
        }
        setValue(rowIndex, columnIndex, value);
    }

    public String getRowValue(CsvRow row, int columnIndex) {
        return getValue(row.getOriginalIndex(), columnIndex);
    }

    public void setRowValue(CsvRow row, int columnIndex, String value) {
        setValue(row.getOriginalIndex(), columnIndex, value);
    }

    public String getRowValue(CsvRow row, String columnName) {
        Integer columnIndex = columnToIndexMap.get(columnName);
        if (columnIndex == null) {
            throw new CsvDataIntegrityException("No column with name \"" + columnName + "\".");
        }
        return getValue(row.getOriginalIndex(), columnIndex);
    }

    public void setRowValue(CsvRow row, String columnName, String value) {
        Integer columnIndex = columnToIndexMap.get(columnName);
        if (columnIndex == null) {
            throw new CsvDataIntegrityException("No column with name \"" + columnName + "\".");
        }
        setValue(row.getOriginalIndex(), columnIndex, value);
    }
}
