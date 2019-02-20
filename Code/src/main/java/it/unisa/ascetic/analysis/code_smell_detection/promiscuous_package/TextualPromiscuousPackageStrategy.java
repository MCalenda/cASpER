package it.unisa.ascetic.analysis.code_smell_detection.promiscuous_package;

import it.unisa.ascetic.analysis.code_smell_detection.ComponentMutation;
import it.unisa.ascetic.analysis.code_smell_detection.smellynessMetricProcessing.SmellynessMetric;
import it.unisa.ascetic.analysis.code_smell_detection.strategy.PackageSmellDetectionStrategy;
import it.unisa.ascetic.storage.beans.PackageBean;
import it.unisa.ascetic.structuralMetrics.CKMetrics;

import java.io.IOException;
import java.util.List;

/*

Promiscuous Package: Un pacchetto viene considerato promiscuo se contiene classi che implementano troppe funzioni,
rendendo difficile la comprensione e il mantenimento.
Per il refactoring si usa Extract Package, necessaria per dividere i pacchetti in sotto pacchetti e riorganizzando le responsabilità.
Questo smell è caratterizzato da insiemi di classi semanticamente diverse dalle altre classi del pacchetto.

 */

public class TextualPromiscuousPackageStrategy implements PackageSmellDetectionStrategy {

    public boolean isSmelly(PackageBean pPackage) {
        SmellynessMetric smellyness = new SmellynessMetric();
        ComponentMutation componentMutation = new ComponentMutation();

        String mutatedPackage = componentMutation.alterPackage(pPackage);
        double smellynessIndex = 0;
        try {
            smellynessIndex = smellyness.computeSmellyness(mutatedPackage);

            if (smellynessIndex > 0.6)
                return true;

        } catch (IOException e) {
            return false;
        }

        return false;
    }

    public double getPromiscuousPackageProbability(PackageBean pPackage) {
        SmellynessMetric smellyness = new SmellynessMetric();
        ComponentMutation componentMutation = new ComponentMutation();

        String mutatedPackage = componentMutation.alterPackage(pPackage);

        try {
            double similarity = smellyness.computeSmellyness(mutatedPackage);

            if (CKMetrics.getNumberOfClasses(pPackage) < 20) {
                similarity -= ((similarity * 75) / 100);
            }

            return smellyness.computeSmellyness(mutatedPackage);

        } catch (IOException e) {
            return 0.0;
        }
    }

}