package com.francislalonde;

import java.util.ArrayList;

public class AddComments {

    private static ArrayList<String> comments = new ArrayList<>();

    public static void CustomComment(String comment){
        comments.add(comment);
    }

    public static void UNCLOSED_COMMENT(int atLine){
        comments.add("Unclosed multi-line comment starting at line " + atLine +".");
    }

    public static void UNBALANCED_BRACKETS(int atLine){
        comments.add("Unbalanced brackets starting at line " + atLine +".");
    }

    public static void WARNING_VERY_LONG_LINE(int atLine, int length){
        comments.add("Very long line at " + atLine + ". (line is " + length +" characters long");
    }

    public static void RESET_COMMENTS(){
        comments = new ArrayList<>();
    }

    public static ArrayList<String> getComments() {
        return comments;
    }

}
