package com.francislalonde;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class LOC_AnalyzerTest {

    @Test
    void getListClasses() {
    }

    @Test
    void findBalancedCurlyBracket(){
        ArrayList<String> linesToAnalyze = new ArrayList<>();
        Collection<String> lines;

        linesToAnalyze.add("one bracket opening(){");
        linesToAnalyze.add("line with no bracket");
        linesToAnalyze.add("opening a second one{");
        linesToAnalyze.add("");
        linesToAnalyze.add("oh that line was empty. all is good right?. let's leave that bracket alone");
        linesToAnalyze.add("}");
        linesToAnalyze.add("One still left open, yet, I'm an outlaw(){ " +
                        "I open two brackets here(){ on the same line! three opened;} I mean two " +
                        "and frankly, I don't give a damn}");
        linesToAnalyze.add("Back to one");
        linesToAnalyze.add("Now zero. Should be ten lines total");
        linesToAnalyze.add("}");
        linesToAnalyze.add("this line should NOT be counted");
        linesToAnalyze.add("and this one neither.");

        LOC_Analyzer analyzer = new LOC_Analyzer(linesToAnalyze);

        };


    }

}