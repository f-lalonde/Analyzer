package com.francislalonde;

public class Methode {
    private final String name;
    private int LOC;
    private int CLOC;
    private double DC;

    private int CC;
    private int noeudPredicat;
    private double BC;

    private final int start;
    private final int end;

    public Methode(String name, int start, int end, int javadocLines){
        this.name = name;
        this.LOC = 0;
        this.CLOC = javadocLines;
        this.DC = 0;
        this.start = start;
        this.end = end;
        this.CC = 0;
        this.noeudPredicat = 0;
        this.BC = 0;
    }

    public void incrementLOC(){
        LOC++;
    }

    public void incrementCLOC(){
        CLOC++;
    }

    public void computeDC(){
        if(LOC == 0){
            DC = -1;
        } else {
            DC = (double)CLOC / LOC;
        }
    }

    public void incrementNoeudPredicat(){
        noeudPredicat++;
    }

    public void computeCC(){
        CC = noeudPredicat+1;
    }

    public void computeBC(){
        if(CC == 0){
            BC = -1;
        } else {
            BC = DC/CC;
        }
    }

    public String getName() {
        return name;
    }

    public int getLOC() {
        return LOC;
    }

    public int getCLOC() {
        return CLOC;
    }

    public double getDC() {
        return DC;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getCC() {
        return CC;
    }

    public double getBC() {
        return BC;
    }

    public void setBC(double BC) {
        this.BC = BC;
    }
}
