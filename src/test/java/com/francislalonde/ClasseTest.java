package com.francislalonde;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClasseTest {


    @Test
    void Add_GetMethod(){
        Classe classe = new Classe("ClasseTest", 0, 10, 0);

        assertEquals(0, classe.getClass_methods().size());
        classe.addMethod("MethodTest", new Methode("MethodTest", 0, 10, 5));

        assertEquals(1, classe.getClass_methods().size());
        assertEquals(5, classe.getMethod("MethodTest").getCLOC());
    }

    @Test
    void computeWMC_and_BC(){
        Classe classe = new Classe("ClasseTest", 0, 10, 0);
        Methode methodeTest = new Methode("MethodTest", 0, 10, 5);
        for(int i = 0; i<5; ++i){
            methodeTest.incrementNoeudPredicat();
        }
        methodeTest.computeCC(); // CC == 6
        classe.addMethod("MethodTest", methodeTest);
        classe.computeWMC();
        assertEquals(6, classe.getWMC());

        classe.incrementLOC();
        classe.incrementLOC();
        for(int i = 0; i<6; ++i){
            classe.incrementCLOC();
        }
        classe.computeDC(); // DC = CLOC / LOC = 6 / 2 = 3
        assertEquals(3, classe.getDC());

        classe.computeBC(); // BC = DC / WMC = 0.5
        assertEquals((double)1/2, classe.getBC());

    }

}