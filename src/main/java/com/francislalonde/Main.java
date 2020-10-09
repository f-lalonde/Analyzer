package com.francislalonde;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//todo : Gérer les classes imbriquées.
//todo : vérifier que les commentaires sont bien assignés
//todo : fais des tests dammit!

public class Main {
    public static void main(String[] args) {

        ArrayList<File> javaFileList = FindFiles.getFilesList();
        javaFileList.forEach(file -> {
            try {
                ArrayList<String> linesCoded = LinesExtractor.fileContentLineByLine(file);
                LOC_Analyzer analyzer = new LOC_Analyzer(linesCoded);
                FichierCSV.exportToCSV(analyzer, file);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

    }
}
