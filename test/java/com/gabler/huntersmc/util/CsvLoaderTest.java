package com.gabler.huntersmc.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CsvLoaderTest {

    private File getFile(String name) {
        return new File(CsvLoaderTest.class.getClassLoader().getResource(name).getPath());
    }

    @Test
    public void testLoader_basicRun() throws FileNotFoundException {
        final CsvLoader objectUnderTest = new CsvLoader("a");
        final File csvFile = getFile("csv/apple-farm.csv");

        objectUnderTest.load(csvFile);

        Assertions.assertEquals("Spicer's", objectUnderTest.getValue(0, 0));
        Assertions.assertEquals("Spicer's", objectUnderTest.getValue(0, "name"));
        Assertions.assertEquals("1400", objectUnderTest.getValue(0, 1));
        Assertions.assertEquals("1400", objectUnderTest.getValue(0, "treecount"));
        Assertions.assertEquals("true", objectUnderTest.getValue(0, 2));
        Assertions.assertEquals("true", objectUnderTest.getValue(0, "profitable"));

        Assertions.assertEquals("Hollow Farm", objectUnderTest.getValue(1, 0));
        Assertions.assertEquals("3", objectUnderTest.getValue(1, 1));
        Assertions.assertEquals("false", objectUnderTest.getValue(1, 2));

        Assertions.assertThrows(CsvDataIntegrityException.class, () -> objectUnderTest.getValue(0, "hfdhasfkjdhskjdfas"));
    }

    @Test
    public void testLoader_failureToLoad() throws FileNotFoundException {
        final CsvLoader objectUnderTest = new CsvLoader("a");
        final File csvFile = getFile("csv/bad-apple-farm.csv");

        Assertions.assertThrows(CsvDataIntegrityException.class, () -> objectUnderTest.load(csvFile));
    }

    @Test
    public void testLoader_nullsAndSpecialCharacterReplacement() throws IOException {
        final CsvLoader objectUnderTest = new CsvLoader("a");
        final File csvFile = getFile("csv/apple-farm.csv");

        objectUnderTest.load(csvFile);

        // Test value set
        objectUnderTest.setValue(0, 0, "Spicers");
        Assertions.assertEquals("Spicers", objectUnderTest.getValue(0, 0));

        // Okay, enough playing around, let's see the mapping work
        objectUnderTest.setValue(0, 0, "Betty,White, and Cow|s");
        Assertions.assertEquals("Betty,White, and Cow|s", objectUnderTest.getValue(0, 0));

        // Let's see how a null shows up
        objectUnderTest.setValue(1, 0, null);
        Assertions.assertNull(objectUnderTest.getValue(1, 0));

        BufferedWriter writer = Mockito.mock(BufferedWriter.class);
        objectUnderTest.save(writer);

        Mockito.verify(writer, Mockito.times(1)).write(
            "name,treecount,profitable\nBetty\u00AEWhite\u00AE and Cow\u00B0s,1400,true\n\u00AF,3,false"
        );
    }

    @Test
    public void testLoader_addColumns() throws FileNotFoundException {
        final CsvLoader objectUnderTest = new CsvLoader("a");
        final File csvFile = getFile("csv/apple-farm.csv");

        objectUnderTest.load(csvFile);
        objectUnderTest.setMetaDataRow("a", "b", "c", "d", "e");

        Assertions.assertEquals("Spicer's", objectUnderTest.getValue(0, 0));
        Assertions.assertEquals("Spicer's", objectUnderTest.getValue(0, "a"));
        Assertions.assertEquals("1400", objectUnderTest.getValue(0, 1));
        Assertions.assertEquals("1400", objectUnderTest.getValue(0, "b"));
        Assertions.assertEquals("true", objectUnderTest.getValue(0, 2));
        Assertions.assertEquals("true", objectUnderTest.getValue(0, "c"));
        Assertions.assertNull(objectUnderTest.getValue(0, 3));
        Assertions.assertNull(objectUnderTest.getValue(0, "d"));
        Assertions.assertNull(objectUnderTest.getValue(0, 4));
        Assertions.assertNull(objectUnderTest.getValue(0, "e"));

        Assertions.assertEquals("Hollow Farm", objectUnderTest.getValue(1, 0));
        Assertions.assertEquals("3", objectUnderTest.getValue(1, 1));
        Assertions.assertEquals("false", objectUnderTest.getValue(1, 2));
        objectUnderTest.setValue(1, 3, "Imma mutate this");
        Assertions.assertEquals("Imma mutate this", objectUnderTest.getValue(1, 3));
        Assertions.assertNull(objectUnderTest.getValue(1, 4));

        Assertions.assertThrows(CsvDataIntegrityException.class, () -> objectUnderTest.getValue(0, "hfdhasfkjdhskjdfas"));
    }
}
