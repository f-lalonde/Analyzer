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

    // Variables que l'on veut accéder dans toutes les méthodes de la classe (pour effets de bords)

    private final ArrayList<Classe> listClasses = new ArrayList<>();

    private final ArrayList<String> fileLines;
    private int javadocLines = 0;
    private int commentOutsideOfAClass = 0;
    private int LOCOutsideOfAClass = 0;
    private String currentMethod = " ";

    private boolean outsideOfAClass;
    private boolean outsideOfAMethod;

    private boolean mlCommentFound;
    private boolean javaDocfound;
    private boolean singleCommentFound;

    private int currentClassIndex;

    private Methode thisMethode;

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

        outsideOfAClass = true;
        outsideOfAMethod = true;
        currentClassIndex = 0;
        mlCommentFound = false;
        javaDocfound = false;

        for(int i = 0; i<fileLines.size(); ++i) {

            String line = fileLines.get(i);

            // On empêche la détection dans les strings, en s'assurant de ne pas capter une mauvaise paire de " ".
            // Ex : fin de commentaire " multi-ligne */ String oops = "on perd tout ceci";
            line = line.replaceAll("\\R", "");
            line = line.replaceAll("\".*\\*/", "SafeGuarded! */");
            line = line.replaceAll("\".*\"", "\"replacedString\" ");

            line = commentHandling(line);

            // on ignore les lignes vides
            if(line.isBlank()){
                continue;
            }

            classAndMethodHandling(line, i);

            // si après tous les retraits il reste encore des charactères non vide, LOC++
            if(!line.isBlank()){
                if(outsideOfAClass){
                    LOCOutsideOfAClass++;
                } else {
                    listClasses.get(currentClassIndex).incrementLOC();
                    if (!outsideOfAMethod) {

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
     *
     * @return true if still inside a multi-line comment / javadoc, false in any other case.
     */
    private String commentHandling(String line){
        /*  On vérifie d'abord si on est à l'intérieur d'un commentaire multi-ligne.
            Si oui, on doit ajouter une ligne de commentaire, et vérifier si il y a la fin du commentaire sur cette
            ligne-ci. Si oui, on doit vérifier pour la présence de code. Sinon, on passe à la ligne suivante.
        */

        singleCommentFound = false;
        if(mlCommentFound || javaDocfound){

            if(line.contains("*/")){
                // on gère d'abord la possibilité d'avoir ouvert et fermé un nouveau commentaire multi-ligne pour
                // éviter une détection trop gourmande.
                line = line.replaceAll("/\\*.*\\*/", "");

                // puis on enlève la partie commentée et on continue l'analyse sur ce qu'il reste.
                line = line.replaceAll(".*\\*/", "");
                if(javaDocfound){
                    javaDocfound = false;
                    javadocLines++;
                } else {
                    mlCommentFound = false;
                    singleCommentFound = true;
                }

            } else {

                if (javaDocfound) {
                    javadocLines++;

                } else if(outsideOfAClass){
                    commentOutsideOfAClass++;

                } else {
                    listClasses.get(listClasses.size() - 1).incrementCLOC();
                    thisMethode.incrementCLOC();

                }
                return line;
            }
        }

        // Détection et élimination des commentaires

        if (findSingleComment(line)) {
            singleCommentFound = true;
            line = line.replaceAll("//.*", "");
        }

        findMultiLineComment(line);

        if(singleCommentFound){
            line = line.replaceAll("/\\*.*\\*/", "");
        } else if(mlCommentFound || javaDocfound){
            line = line.replaceAll("/\\*.*", "");
        }

        return line;
    }

    private boolean findSingleComment(String line) {
        // on vérifie que "//" n'est pas imbriqué dans un commentaire /* ... */ ou /* ... (sinon il sera compté deux fois)
        return line.contains("//") &&
                line.replaceAll(patternMultiLineCommentonOneLine.pattern(), "").
                        replaceAll("/\\*", "").contains("//");
    }

    private void findMultiLineComment(String line){
        Matcher mlCommentOneLine = patternMultiLineCommentonOneLine.matcher(line);

        if(line.contains("/*")){
            if(mlCommentOneLine.find()){
                singleCommentFound = true;
            } else {
                if(line.contains("/**")){
                    javaDocfound = true;
                } else {
                    mlCommentFound = true;
                }
            }
        }
    }

    private void classAndMethodHandling(String line, int currentIndex){
        // Gère les cas où la déclaration est séparée sur deux lignes. Pour l'instant, on juge que sur plus de
        // quatre lignes, ce serait exagéré... mais qui sait?
        String checkIfOnMultipleLines = line;
        if(fileLines.size() > currentIndex +3){
            checkIfOnMultipleLines = checkIfOnMultipleLines.concat(fileLines.get(currentIndex+1)).concat(fileLines.get(currentIndex+2)).concat(fileLines.get(currentIndex+3)).replaceAll("\\R\\t", "");
        }

        Matcher multiLineClassCheck = multiLineClassDeclarationCheck.matcher(line);

        if (classMatcher(line, currentIndex) || (multiLineClassCheck.find() && classMatcher(checkIfOnMultipleLines, currentIndex))) {    //  <--- il y a des effets de bords ici.
            outsideOfAClass = false;


        } else if (!listClasses.isEmpty() && currentIndex > listClasses.get(currentClassIndex).getEnd()) {
            outsideOfAClass = true;
        }

        Matcher partialMethodMatcher = methodPartialMatch.matcher(line);

        if (methodMatcher(line, currentIndex) || (partialMethodMatcher.find() && methodMatcher(checkIfOnMultipleLines, currentIndex))) {   //  <--- il y a des effets de bords ici.
            outsideOfAMethod = false;
            thisMethode = listClasses.get(currentClassIndex).getMethod(currentMethod);
        } else if (thisMethode != null && currentIndex > thisMethode.getEnd()) {
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
                    thisMethode.incrementCLOC();
                }
            }
        }
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
                currentClassIndex = listClasses.size() - 1;
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
                javadocLines = 0;
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

    public ArrayList<Classe> getListClasses() {
        return listClasses;
    }

}