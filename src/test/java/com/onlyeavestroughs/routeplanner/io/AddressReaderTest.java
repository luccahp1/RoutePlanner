package com.onlyeavestroughs.routeplanner.io;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddressReaderTest {

    @Test
    void cleansBlankLinesAndDuplicates_preservesOrder() throws Exception {
        Path tmp = Files.createTempFile("addresses", ".txt");
        Files.write(tmp, List.of(
                "  1560 Hartlet Street London Ontario  ",
                "",
                "940 William Street London Ontario",
                "940 William Street London Ontario", // duplicate
                "   ",
                "767 Helmuth Street London Ontario"
        ), StandardCharsets.UTF_8);

        AddressReader.ReadResult r = AddressReader.read(tmp);

        assertEquals(6, r.rawLineCount());
        assertEquals(2, r.blankLineCount());
        assertEquals(1, r.duplicateLineCount());
        assertEquals(List.of(
                "1560 Hartlet Street London Ontario",
                "940 William Street London Ontario",
                "767 Helmuth Street London Ontario"
        ), r.addresses());
    }
}
