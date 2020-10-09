package com.francislalonde;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MethodeTest {



    @Test
    void assertCLOCJavaDocCount(){
        Methode methode = new Methode("MethodTest", 0, 10, 5);
        assertEquals(5, methode.getCLOC());
    }

    @Test
    void incrementLOC() {
        Methode methode = new Methode("MethodTest", 0, 10, 0);
        methode.incrementLOC();
        assertEquals(1, methode.getLOC());
    }

    @Test
    void incrementCLOC() {
        Methode methode = new Methode("MethodTest", 0, 10, 5);
        methode.incrementCLOC();
        assertEquals(6, methode.getCLOC());
    }

    @Test
    void computeDC_CC_BC() {
        Methode methode = new Methode("MethodTest", 0, 10, 6);

        // DC
        methode.incrementLOC();
        methode.incrementLOC(); // LOC devrait être 2 ici
        methode.computeDC();
        assertEquals(3, methode.getDC());

        // CC
        methode.computeCC();
        assertEquals(1,methode.getCC());

        methode.incrementNoeudPredicat();
        methode.computeCC();
        assertEquals(2,methode.getCC());

        // BC

        methode.computeBC();
        assertEquals((double)3/2, methode.getBC());
    }

}