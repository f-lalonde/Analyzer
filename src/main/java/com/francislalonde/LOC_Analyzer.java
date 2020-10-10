package com.francislalonde;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* PAR RAPPORT À LA COMPLEXITÉ CYCLOMATIQUE :
 *
 * Raisonnement : Tout d'abord, un programme qui n'aurait pas de noeud prédicat aura une CC de 1.
 * Ensuite, pour chaque présence des expressions suivantes, on va augmenter le nombre de 1 :
 * if et else if (également sous la forme X? Y:Z) - for - forEach - while - case (mais pas default) - catch - (assert?)
 *
 *   HYPOTHÈSE 1 :  Le code est bien formatté et fonctionnel.
 *
 *   HYPOTHÈSE 2 :  Étant donné qu'une exception dans un bloc try { ... } catch { ... } ne peut être levé qu'une seule
 *                  fois par bloc, on va monter le nombre de nœuds prédicats de 1 pour chaque bloc catch.
 *
 *  Addendum Michalis Famelis sur hypothèse 2
 *  (7 septembre, 13h56, channel Question sur Slack) :
 *              "Il serait bien sur interessant de voir si une telle hypothèse revele des cas plus interessants dans
 *               JFreechart. Peut-être vous auriez une option de configuration qui l'active et le désactive, et puis
 *               vous comparez?"  todo : <-- faire ça.
 *
 *   HYPOTHÈSE 3 :  Les lignes qui se trouvent à l'extérieur des classes (commentaires, imports, etc) appartiennent à
 *                  toutes les classes se trouvant dans le fichier, à l'exception de la javaDoc.
 *
 *
 * */

public class LOC_Analyzer {

    private final String classErrorName = "ERROR : Class could not be identified";
    private final Classe classError = new Classe(classErrorName, 0,0,0);
    private final ArrayList<Classe> listClasses = new ArrayList<>();

    private final ArrayList<String> fileLines;
    private int javadocLines = 0;
    private int commentOutsideOfAClass = 0;
    private int LOCOutsideOfAClass = 0;
    private String currentMethod = " ";

    // Patrons regex

    private static final Pattern methodPattern = Pattern.compile(
            "\\b(?!(catch\\s*\\(|while\\s*\\(|if\\s*\\(|for\\s*\\(|switch\\s*\\())\\b\\s*" +
            "(?<methodName>[a-zA-Z_$][a-zA-Z0-9_$]*)\\s*" +
            "\\((?<signature>(\\s*(?<type1>[a-zA-Z_$][a-zA-Z0-9_$]*" +
            "(<.*>\\s+)?" + // on accepte tout dans <> pour simplifier le regex
            "(\\[])*)\\s+" +
            "\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\s*,)*\\s*" +
            "((?<type2>\\b[a-zA-Z_$][a-zA-Z0-9_$]*" +
            "(<.*>\\s+)?" + "((\\[])*\\s+)?)" +
            "\\b[a-zA-Z_$][a-zA-Z0-9_$]*))?\\s*\\)" +
            "(\\s+throws\\s+[a-zA-Z_$][a-zA-Z0-9_$]*)?\\s*\\{");

    private static final Pattern classPattern = Pattern.compile(
            "\\b(class|interface|enum)\\b\\s+" +
            "(?<className>[a-zA-Z_$][a-zA-Z0-9_$]*)(\\s*<.*>)?" +
            "(\\s+(extends|implements)\\s+[a-zA-Z_$][a-zA-Z0-9_$.]*(\\s*<.*>)?\\s*" +
            "(,\\s*[a-zA-Z_$][a-zA-Z0-9_$]*(\\s*<.*>)?)*?)*?\\s*\\{");

    private static final Pattern multiLineClassDeclarationCheck = Pattern.compile("\\b(class|interface|enum)\\b");

    private final Pattern methodPartialMatch = Pattern.compile(
            "\\b(?!(catch\\s*\\(|while\\s*\\(|if\\s*\\(|for\\s*\\(|switch\\s*\\(|new\\s+.*))\\b\\s*" +
            "(?<methodName>\\s[a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\("); // présume la présence du type de retour.

    private final Pattern patternMultiLineCommentonOneLine = Pattern.compile("/\\*.*\\*/");

    private final Pattern patternNoeudPredicat = Pattern.compile("(" +
            "(?<if>\\s*if(\\s|\\Q(\\E))|" + "(?<ifShortHand>.*\\s*?\\s*.*\\s*:\\s*.*)|" + "(?<for>\\s*for(\\s|\\Q(\\E))|" +
            "(?<forEach>\\.forEach(\\s|\\Q(\\E))|" + "(?<while>\\s*while(\\s|\\Q(\\E))|" +
            "(?<case>\\s*case\\s)" + ")", Pattern.CASE_INSENSITIVE);

    private final Pattern patternNoeudPredicatWithCatch = Pattern.compile("(" +
            "(?<if>\\s*if(\\s|\\Q(\\E))|" + "(?<ifShortHand>.*\\s*?\\s*.*\\s*:\\s*.*)|" + "(?<for>\\s*for(\\s|\\Q(\\E))|" +
            "(?<forEach>\\.forEach(\\s|\\Q(\\E))|" + "(?<while>\\s*while(\\s|\\Q(\\E))|" +
            "(?<case>\\s*case\\s)|" + "(?<catch>(\\s*|})catch(\\s|\\Q(\\E))" +
            ")", Pattern.CASE_INSENSITIVE);

    public LOC_Analyzer(ArrayList<String> fileLines) {
        this.fileLines = fileLines;
        analyzer();
    }

    /**
     * Méthode principale de la classe. Effectue ou appelle toutes les opération d'analyse des lignes.
     */
    private void analyzer(){

        boolean outsideOfAClass = true;
        boolean outsideOfAMethod = true;
        int currentClassIndex = 0;
        Methode thisMethode = null;

        boolean mlCommentFound = false;
        boolean javaDocfound = false;
        boolean singleCommentFound;

        for(int i = 0; i<fileLines.size(); ++i) {

            singleCommentFound = false;

            /* Stratégie générale : on vérifie s'il y a un type de commentaire dans la ligne, on enlève tout
               ce qu'il contient, puis on vérifie ce qu'il reste. */

            String line = fileLines.get(i);

            // on ignore les lignes vides
            if(line.isBlank()){
                continue;
            }

            // On empêche la détection dans les strings, en s'assurant de ne pas capter une mauvaise paire de " ".
            // Ex : fin de commentaire " multi-ligne */ String oops = "on perd tout ceci";
            line = line.replaceAll("\".*\\*/", "SafeGuarded! */");
            line = line.replaceAll("\".*\"", "\"replacedString\" ");

            /* On vérifie d'abord si on est à l'intérieur d'un commentaire multi-ligne.
               Si oui, on doit ajouter une ligne de commentaire, et vérifier si il y a la fin du commentaire sur cette
               ligne-ci. Si oui, on doit vérifier pour la présence de code. Sinon, on passe à la ligne suivante.
             */
            if(mlCommentFound || javaDocfound){

                if(line.contains("*/")){
                    // on gère d'abord la possibilité d'avoir ouvert et fermé un nouveau commentaire multi-ligne pour
                    // éviter une détection trop gourmande.
                    line = line.replaceAll("/\\*.*\\*/", "");

                    // puis on enlève la partie commentée et on continue l'analyse sur ce qu'il reste.
                    line = line.replaceAll(".*\\*/", "");
                    if(javaDocfound){
                        javadocLines++;
                    } else {
                        singleCommentFound = true;
                    }

                } else {
                    if (outsideOfAClass || outsideOfAMethod) {
                        if (javaDocfound) {
                            javadocLines++;
                        } else if(outsideOfAClass){
                            commentOutsideOfAClass++;
                        }
                    } else {
                        listClasses.get(currentClassIndex).incrementCLOC();
                        thisMethode.incrementCLOC();

                    }
                    continue;
                }
            }

            // Détection des commentaires
            if (findSingleComment(line)) {
                singleCommentFound = true;

                // on enlève la partie commentée et on continue l'analyse sur ce qu'il reste.
                line = line.replaceAll("//.*", "");
            }

            // tableau de 3 booléens, représentant : 0 - singleCommentFound, 1 - mlCommentFound, 2 - javaDocfound
            boolean[] values = findMultiLineComment(line);

            // On enlève les commentaires.
            if(values[0]){
                line = line.replaceAll("/\\*.*\\*/", "");
            } else if(values[1] || values[2]){
                line = line.replaceAll("/\\*.*", "");
            }

            singleCommentFound = singleCommentFound || values[0];
            mlCommentFound = values[1];
            javaDocfound = values[2];

            // Détection des classes et des méthodes

            // Gère les cas où la déclaration est séparée sur deux lignes. Pour l'instant, on juge que sur trois lignes
            // serait exagéré... mais qui sait, peut-être?
            String checkIfOnMultipleLines = line;
            if(fileLines.size() > i +3){
                checkIfOnMultipleLines = checkIfOnMultipleLines.concat(fileLines.get(i+1)).concat(fileLines.get(i+2)).concat(fileLines.get(i+3)).replaceAll("\\R\\t", "");
            }

            Matcher multiLineClassCheck = multiLineClassDeclarationCheck.matcher(line);

            if (classMatcher(line, i) || (multiLineClassCheck.find() && classMatcher(checkIfOnMultipleLines, i))) {    //  <--- il y a des effets de bords ici.
                outsideOfAClass = false;
                currentClassIndex = listClasses.size() - 1;

            } else if (!listClasses.isEmpty() && i > listClasses.get(currentClassIndex).getEnd()) {
                outsideOfAClass = true;
            }

            Matcher partialMethodMatcher = methodPartialMatch.matcher(line);

            if (methodMatcher(line, i) || (partialMethodMatcher.find() && methodMatcher(checkIfOnMultipleLines, i))) {   //  <--- il y a des effets de bords ici.
                outsideOfAMethod = false;
                thisMethode = listClasses.get(currentClassIndex).getMethod(currentMethod);
            } else if (thisMethode != null && i > thisMethode.getEnd()) {
                outsideOfAMethod = true;
            }

            // si on a trouvé des commentaires, CLOC++
            if(singleCommentFound || mlCommentFound || javaDocfound) {
                if (outsideOfAClass) {
                    if (javaDocfound) {
                        javadocLines++;
                    } else {
                        commentOutsideOfAClass++;
                    }
                } else {
                    listClasses.get(currentClassIndex).incrementCLOC();
                    if (!outsideOfAMethod) {
                        assert thisMethode != null : "On a détecté que l'on est dans une méthode, mais aucune méthode n'a été chargée.";
                        thisMethode.incrementCLOC();
                    }
                }
            }
            // si après tous les retraits il reste encore des charactères non vide, LOC++
            if(!line.isBlank()){
                if(outsideOfAClass){
                    LOCOutsideOfAClass++;
                } else {
                    listClasses.get(currentClassIndex).incrementLOC();
                    if (!outsideOfAMethod) {
                        assert thisMethode != null : "On a détecté que l'on est dans une méthode, mais aucune méthode n'a été chargée.";
                        thisMethode.incrementLOC();

                        // On en profite au passage pour vérifier la présence d'un noeud prédicat :
                        //todo : mettre switch (catch / pas catch) ici. Note à moi même : trouver comment accepter des arguments en entrée :|
                        Matcher matcherNoeudPredicat = patternNoeudPredicat.matcher(line);
                        while(matcherNoeudPredicat.find()) {
                            thisMethode.incrementNoeudPredicat();

                        }
                    }
                }
            }
        }

        // on ajoute les lignes de code à l'extérieur des classes à toutes les classes, puisqu'elles en dépendent toutes.
        listClasses.forEach(classe -> {
            while(LOCOutsideOfAClass > 0){
                classe.incrementLOC();
                LOCOutsideOfAClass--;
            }
            while (commentOutsideOfAClass > 0){
                classe.incrementCLOC();
                commentOutsideOfAClass--;
            }

            classe.getClass_methods().forEach((name,method) -> {
                method.computeDC();
                method.computeCC();
                method.computeBC();
            });

            classe.computeDC();
            classe.computeWMC();
            classe.computeBC();
        });
    }

    /**
     * Vérifie si la ligne est une déclaration de classe. Si oui, on crée un instance de com.francislalonde.Classe et on l'ajoute au
     * hashmap listClasses. Sinon, il ne se passe rien.
     * Agit par effet de bord.
     * @param line contenu de la ligne actuelle
     * @param currentLine numéro de la ligne actuelle
     * @return le nom de la classe si on a trouvé une classe. String vide sinon.
     */
    private boolean classMatcher(String line, int currentLine){
        Matcher classMatcher = classPattern.matcher(line);

        if (classMatcher.find()) {
            String className = classMatcher.group("className");
            int classEnd = FindBalancedSymbol.findInnerBalance(currentLine, fileLines, '{', '}');

            if(javadocLines > 0){
                listClasses.add(new Classe(className, currentLine, classEnd, javadocLines));
                javadocLines = 0; // on remet le compteur à 0 puisque javadocs assignés.

            } else {
                listClasses.add(new Classe(className, currentLine, classEnd, 0));
            }
            return true;
        }
        return false;
    }

    /**
     * Vérifie si la ligne est une déclaration de méthode. Si oui, on crée une instance de com.francislalonde.Methode et on l'associe à la
     * classe dans laquelle elle se trouve. On met également à jour la variable String "currentMethod", qui aura le nom
     * et la signature de la méthode que nous venons de détecter.
     * Sinon, il ne se passe rien.
     * Agit par effet de bord.
     * @param line contenu de la ligne actuelle
     * @param currentLine numéro de la ligne actuelle
     * @return vrai si on a trouvé une méthode
     */
    private boolean methodMatcher(String line, int currentLine){

        Matcher methodMatcher = methodPattern.matcher(line);
        if (methodMatcher.find()) {


            // Extrait les types de la signature et les lie par "_"
            String tempSignature;
            if(methodMatcher.group("signature") != null) {
                tempSignature = methodMatcher.group("signature").replaceAll("\\s*[a-zA-Z_$][a-zA-Z0-9_$]*\\s*,\\s*", "_");
                String[] temp = tempSignature.split("\\s+");
                if (temp[0].isBlank()) {
                    tempSignature = "_"+temp[1];
                } else {
                    tempSignature = "_"+temp[0];
                }
            }else {
                tempSignature = "";
            }
            currentMethod = methodMatcher.group("methodName")+tempSignature;
            int methodEnd = FindBalancedSymbol.findInnerBalance(currentLine, fileLines, '{', '}');

            // on assume que la méthode trouvée se trouve dans la dernière classe trouvée.
            Classe currentClass;
            try{
                currentClass = listClasses.get(listClasses.size() - 1);
            } catch (IndexOutOfBoundsException e) {
                currentClass = classError;
                listClasses.add(classError);
            }
            if(javadocLines > 0){
                currentClass.addMethod(currentMethod, new Methode(currentMethod, currentLine, methodEnd, javadocLines));
                // on remet le compteur à 0 en assignant aussi les javadocs à la classe de la méthode.
                while(javadocLines > 0){
                    currentClass.incrementCLOC();
                    javadocLines--;
                }
            } else {
                try{
                    listClasses.get(listClasses.size() - 1).addMethod(currentMethod, new Methode(currentMethod, currentLine, methodEnd, 0));
                } catch (IndexOutOfBoundsException e) {
                    classError.addMethod(currentMethod, new Methode(currentMethod, currentLine, methodEnd, 0));
                }
            }
            return true;
        }
        return false;
    }

    private boolean findSingleComment(String line) {
        // on vérifie que "//" n'est pas imbriqué dans un commentaire /* ... */ ou /* ... (sinon il sera compté deux fois)
        return line.contains("//") &&
                line.replaceAll(patternMultiLineCommentonOneLine.pattern(), "").
                        replaceAll("/\\*", "").contains("//");
    }

    private boolean[] findMultiLineComment(String line){
        Matcher mlCommentOneLine = patternMultiLineCommentonOneLine.matcher(line);
        boolean[] returnedValues = {false, false, false};
        if(line.contains("/*")){
            if(mlCommentOneLine.find()){
                returnedValues[0] = true;       // singleCommentFound
            } else {
                if(line.contains("/**")){
                    returnedValues[2] = true;   // javaDocfound
                } else {
                    returnedValues[1] = true;   // mlCommentFound
                }
            }
        }
        return returnedValues;
    }

    public ArrayList<Classe> getListClasses() {
        return listClasses;
    }

}