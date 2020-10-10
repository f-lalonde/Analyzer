package com.francislalonde;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FindBalancedSymbolTest {

    @Test
    void findInnerBalance(){
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(testCode));
        int result = FindBalancedSymbol.findInnerBalance(0, lines, '{', '}');
        assertEquals(8, result);
        int resultOption = FindBalancedSymbol.findInnerBalance(0, lines, '{', '}', "text");
        assertEquals(5, resultOption);
    }

    private final String[] testCode = {
      "Text {",
      "Second bracket {",
            "Close one } open two { {",
            "// not those two {{",
            "} and",
            "/* not those }}}}}} */ but this one yes }",
            "nothing here",
            "Should stop here \"}\"",
            "and here we stop }"
    };
}