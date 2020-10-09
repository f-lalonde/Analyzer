package com.francislalonde;//com.francislalonde.Classe pour créer un fichier CSV

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FichierCSV {

    public static void exportToCSV (LOC_Analyzer analyzer, File file) throws IOException {

        File classCSV = new File(System.getProperty("user.dir") + File.separator + "classes.csv");
        File methodCSV = new File(System.getProperty("user.dir") + File.separator + "methodes.csv");

        FileWriter classWriter;
        FileWriter methodWriter;

        if(classCSV.createNewFile()) {
            classWriter = new FileWriter(classCSV);
            classWriter.write("chemin, class, classe_LOC, classe_CLOC, classe_DC, WMC, classe_BC");
        } else {
            classWriter = new FileWriter(classCSV, true);
        }

        if(methodCSV.createNewFile()){
            methodWriter = new FileWriter(methodCSV);
            methodWriter.write("chemin, class, methode, methode_LOC, methode_CLOC, methode_DC, CC, methode_BC");
        } else {
            methodWriter = new FileWriter(methodCSV, true);
        }

        String currentPath = file.getAbsolutePath();

        analyzer.getListClasses().forEach(classe -> {
            try {
                classWriter.write("\n"+ currentPath+", "+ classe.getName() + ", " + classe.getLOC() +", " +
                        classe.getCLOC() + ", " + classe.getDC()+ ", " + classe.getWMC() + ", " + classe.getBC());
            } catch (IOException e) {
                e.printStackTrace();
            }
            classe.getClass_methods().forEach((methodName, method) -> {
                try {
                    methodWriter.write("\n"+ currentPath + ", " + classe.getName() + ", " + methodName + ", " +
                            method.getLOC() +", " + method.getCLOC() + ", " + method.getDC() + ", " + method.getCC() +
                            ", " + method.getBC());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        classWriter.close();
        methodWriter.close();
    }

}