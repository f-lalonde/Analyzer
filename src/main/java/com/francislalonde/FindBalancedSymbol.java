package com.francislalonde;

import java.util.ArrayList;

public class FindBalancedSymbol {

    /**
     * Returns the index of the String in the provided ArrayList in which opening and closing balance has been found.
     * Assumes that the lines provided begins ON or BEFORE the first opening character you want to work on.
     * Unfortunately, this method doesn't deliver on the promise in its name. You will be majorly undewhelmed.
     * @param startAtIndex index of the String where the analysis begins
     * @param strings ArrayList containing the strings to analyze.
     * @param openSymbol character representing the opening symbol (eg '{')
     * @param closeSymbol character representing the closing symbol (eg '}')
     * @return the index where balance was first found for the characters given.
     */
    public static int findInnerBalance(int startAtIndex, ArrayList<String> strings, char openSymbol, char closeSymbol) {

        return findBalance(startAtIndex, strings, openSymbol,closeSymbol, "code");
    }

    /**
     * @param startAtIndex index of the String where the analysis begins
     * @param strings ArrayList containing the strings to analyze.
     * @param openSymbol character representing the opening symbol (eg '{')
     * @param closeSymbol character representing the closing symbol (eg '}')
     * @param option input "text" as that parameter if you want to analyze the whole strings.
     *               Else it is considered Java code and will ignore comments and string literals.
     * @return the index where balance was first found for the characters given.
     */
    public static int findInnerBalance(int startAtIndex, ArrayList<String> strings, char openSymbol, char closeSymbol, String option) {
        return findBalance(startAtIndex, strings, openSymbol,closeSymbol, option);
    }

    private static int findBalance(int startAtIndex, ArrayList<String> strings, char openSymbol, char closeSymbol, String option){
        int count = 0;
        boolean started = false;
        for(int i = startAtIndex; i<strings.size();++i){
            String line = strings.get(i);
            if(line.isBlank()){
                continue;
            }

            if(!option.equals("text")) {
                // on évite de détecter les symboles dans les strings ou les commentaires dans du code java
                line = line.replaceAll("\".*\"", "stringReplaced");
                line = line.replaceAll("//.*", " ");
                line = line.replaceAll("/\\*.*\\*/", " ");
                line = line.replaceAll("/\\*.*", " ");
            }

            for(int j = 0; j<line.length(); j++){
                if(line.charAt(j) == openSymbol){
                    count++;
                    started = true;
                } else if(line.charAt(j) == closeSymbol){
                    count--;
                }
            }
            if(count <= 0 && started){
                return i;
            }
        }
        return -1;
    }

}
