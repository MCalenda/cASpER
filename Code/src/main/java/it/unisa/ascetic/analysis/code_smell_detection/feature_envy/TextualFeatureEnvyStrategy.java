package it.unisa.ascetic.analysis.code_smell_detection.feature_envy;

import it.unisa.ascetic.analysis.code_smell_detection.BeanDetection;
import it.unisa.ascetic.analysis.code_smell_detection.MisplacedComponentsUtilities;
import it.unisa.ascetic.analysis.code_smell_detection.similarityComputation.CosineSimilarity;
import it.unisa.ascetic.analysis.code_smell_detection.strategy.MethodSmellDetectionStrategy;
import it.unisa.ascetic.storage.beans.ClassBean;
import it.unisa.ascetic.storage.beans.MethodBean;
import it.unisa.ascetic.storage.beans.PackageBean;

import java.io.IOException;
import java.util.*;

/*

Feature Envy: Si ha quando un metodo è più usato in un'altra classe rispetto a quella in cui si trova.
Quindi questo metodo non è correttamente posizionato ed aumenta l'accoppiamento con classi differenti da quella in cui si trova.
Il refactoring per rimuovere questo smell è il Move Method, che sposta il metodo nella classe adatta.
Un metodo è affetto da questo smell quando è molto più simile ai concetti implementati nella classe esterna rispetto a quella in cui si trova.

 */

public class TextualFeatureEnvyStrategy implements MethodSmellDetectionStrategy {

    private List<PackageBean> systemPackages;
    private double soglia;

    public TextualFeatureEnvyStrategy(List<PackageBean> systemPackages, double soglia) {
        this.systemPackages = systemPackages;
        this.soglia = soglia;
    }

    public boolean isSmelly(MethodBean pMethod) {

        if (!BeanDetection.detection(pMethod)) {

            SortedMap<ClassBean, Double> similaritiesWithMethod = new TreeMap<ClassBean, Double>();
            CosineSimilarity cosineSimilarity = new CosineSimilarity();
            ArrayList<ClassBean> candidateEnviedClasses = MisplacedComponentsUtilities.getCandidates(pMethod, systemPackages, soglia);

            String[] document1 = new String[2];
            document1[0] = "method";
            document1[1] = pMethod.getTextContent();

            for (ClassBean classBean : candidateEnviedClasses) {

                String[] document2 = new String[2];
                document2[0] = "class";
                document2[1] = classBean.getTextContent();

                try {
                    similaritiesWithMethod.put(classBean, cosineSimilarity.computeSimilarity(document1, document2));

                } catch (IOException e) {
                    e.getMessage();
                    return false;
                }
            }

            if (!similaritiesWithMethod.entrySet().iterator().hasNext()) {
                return false;
            }

            Set<ClassBean> list = similaritiesWithMethod.keySet();
            ClassBean firstRankedClass = list.iterator().next();

            for (ClassBean classBean : list) {
                if (classBean.getSimilarity() > firstRankedClass.getSimilarity()) {
                    firstRankedClass = classBean;
                }
            }

            if (firstRankedClass.getFullQualifiedName().equals(pMethod.getBelongingClass().getFullQualifiedName())) {
                return false;
            } else {
                pMethod.setEnviedClass(firstRankedClass);
                return true;
            }
        }
        return false;
    }

    public HashMap<String, Double> getThresold(MethodBean pMethod) {
        HashMap<String, Double> list = new HashMap<String, Double>();

        String[] document1 = new String[2];
        document1[0] = "method";
        document1[1] = pMethod.getTextContent();

        String[] document2 = new String[2];
        document2[0] = "class";
        document2[1] = pMethod.getEnviedClass().getTextContent();
        try {
            CosineSimilarity cosineSimilarity = new CosineSimilarity();
            list.put("coseno", cosineSimilarity.computeSimilarity(document1, document2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

}