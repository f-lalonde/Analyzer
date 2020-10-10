package com.francislalonde;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LOC_AnalyzerTest {

    @Test
    void regularCodeTest(){


        ArrayList<String> linesOfCode = new ArrayList<>(Arrays.asList(normalCode));

        LOC_Analyzer analyzer = new LOC_Analyzer(linesOfCode);
        assertNotNull(analyzer.getListClasses());

        Classe thisClass = analyzer.getListClasses().get(0);
        String className = thisClass.getName();

        assertEquals("normalClass", className);
        assertEquals(20, thisClass.getCLOC());
    }

    private final String[] normalCode = {
            "/*\n",
            " * one more\n",
            " * Commentaire\n",
            " * en début\n",
            " * de code\n",
            " */\n",                            // comment count : 6
            "package com.fake.package;\n",
            "\n",
            "import some.utilities;\n",
            "import some more;\n",
            "\n",

            "import static very.sophisticated.util;\n",

            "/**\n",
            "* Some javaDoc\n",
            "* @param something something\n",
            "* @return something too\n",
            "*/\n",                            // comment count : 11
            "public class normalClass\n",
            "        implements theUsual\n",
            "{\n",
            "    // this class starts with a comment\n", // comment count : 12
            "    public static final String MEMES = \"for the lulz\";\n",
            "\n",
            "    private final List<String> chevrons = new ArrayList<>();\n",
            "    private final Map<WeDidIt, WeWentThere> dualChevrons = new ConcurrentHashMap<>();\n",
            "\n",
            "    /**\n",
            "    * Some javaDoc\n",
            "    * @param something something\n",
            "    * @return something too\n",
            "    */\n",                         // comment count : 17
            "    public simpleMethod()\n",
            "    {\n",
            "        list.add(A_BIT_OF_SPICE); // and a comment\n", // comment count : 18
            "    }\n",
            "\n",
            "    @Override\n",
            "    public List<String> listSomethingNew(Yupyup soNice)\n",
            "    {\n",
            "        return ImmutableList.copyOf(themBoys);/* multiline comment start at the end\n",
            "        but it ends on the next line */\n", // comment count : 20
            "    }\n",
            "\n",
            "    public synchronized void verySophisticated(FirstType type1, SecondType type2, ThirdType<String, Object> type3)\n",
            "    {\n",
            "        if (verySophisticated.contains(verySophisticated)) {\n",
            "            throw new megaException(ALREADY_EXISTS, format(\"verySophisticated [%s] already exists\", verySophisticated));\n",
            "        }\n",
            "        verySophisticated.add(verySophisticated);\n",
            "    }\n",
            "\n",
            "    public void methodWithLambda(Should not, Cause problem)\n",
            "    {\n",
            "        return tables.values().stream()\n",
            "                .filter(table -> schemaNameOrNull == null || table.stoleThisCode().equals(andModifiedIt))\n",
            "                .map(ItJustToTest::isThatOkay)\n",
            "                .collect(toList());\n",
            "    }\n",
            "}\n"

    };

    private final String[] difficultCode = {

    };

}