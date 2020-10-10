/*
TP1 - Métriques
IFT-3913
Francis Lalonde
Charlotte de Lanauze
 */

package com.francislalonde;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//todo : Gérer les classes imbriquées.
//todo : vérifier que les commentaires sont bien assignés
//todo : fais des tests dammit!
//todo : @Override et autres ( regex : @.*/b) En théorie, devrait être assigné à la méthode qui le suit.
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
