package com.francislalonde;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindFiles {
    // Patron pour extension de fichier
    private static final Pattern patternJavaFile = Pattern.compile(".java$", Pattern.CASE_INSENSITIVE);

    public static ArrayList<File> getFilesList(){
        ArrayList<File> fileList = new ArrayList<>();
        File currentDirectory = new File(System.getProperty("user.dir"));
        return ListJavaFilesInDirectory(currentDirectory, fileList);
    }

    /**
     * Cherche tous les fichiers qui se terminent par ".java" dans le dossier spécifié, ainsi
     * que dans ses sous-dossiers, et les ajoute dans le ArrayList fourni en argument.
     * @param dir dossier dans lequel faire la recherche.
     * @param fileList liste des fichiers déjà trouvés.
     * @return le ArrayList mis à jour avec les nouvelles entrées.
     */
    private static ArrayList<File> ListJavaFilesInDirectory(File dir, ArrayList<File> fileList) {

        for(File file : Objects.requireNonNull(dir.listFiles())){
            if(file.isDirectory()){
                fileList = ListJavaFilesInDirectory(file, fileList);
            } else {
                Matcher matcher = patternJavaFile.matcher(file.getName());
                if (matcher.find()) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

}
