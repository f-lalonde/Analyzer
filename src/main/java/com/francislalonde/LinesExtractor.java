package com.francislalonde;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class LinesExtractor {

    /**
     * Prend un fichier et retourne un ArrayList contenant chaque ligne du fichier sous forme de String.
     * @param file fichier duquel on veut extraire les lignes une à une
     * @return ArrayList contenant toutes les lignes du fichier
     * @throws FileNotFoundException si le fichier n'existe pas au moment de la lecture
     */
    public static ArrayList<String> fileContentLineByLine(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        ArrayList<String> javaFileContent = new ArrayList<>();
        while(scanner.hasNext()){
            javaFileContent.add(scanner.nextLine());
        }
        return javaFileContent;
    }
}
