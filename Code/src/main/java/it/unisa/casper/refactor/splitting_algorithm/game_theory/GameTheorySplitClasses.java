package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import com.google.common.primitives.Bytes;
import com.intellij.codeInsight.completion.CompletionPhase;
import com.intellij.util.ArrayUtil;
import it.unisa.casper.refactor.splitting_algorithm.MethodByMethodMatrixConstruction;
import it.unisa.casper.storage.beans.*;

import java.lang.reflect.Array;
import java.util.*;

import it.unisa.casper.refactor.exceptions.SplittingException;
import it.unisa.casper.refactor.strategy.SplittingStrategy;
import org.apache.commons.lang3.ArrayUtils;

public class GameTheorySplitClasses implements SplittingStrategy {

    private ArrayList<Byte[]> combinations;

    public GameTheorySplitClasses() {
        combinations = new ArrayList<>();
    }

    /**
     * Splits the input class in n classes using Game Theory.
     *
     * @param toSplit   the class to be splitted
     * @param threshold the threshold to filter the method-by-method matrix
     * @return a Collection of ClassBean containing the new classes
     * @throws SplittingException, Exception
     * @throws Exception
     */
    @Override
    public Collection<ClassBean> split(ClassBean toSplit, double threshold) throws SplittingException, Exception {
        ArrayList<Byte> remainingMethods = new ArrayList<>();
        for (byte i = 0; i < toSplit.getMethodList().size() ; i++) {
            remainingMethods.add(i);
        }
        System.out.println(remainingMethods.size() + " initial methods");

        TopicExtractor te = new TopicExtractor();
        byte[] extractionResult = te.extractTopic(toSplit.getMethodList(), 10, 0.25);
        System.out.println(extractionResult.length + " topics extracted");

        MethodByMethodMatrixConstruction matrixConstruction = new MethodByMethodMatrixConstruction();
        double[][] methodByMethodMatrix = matrixConstruction.buildMethodByMethodMatrix(0.4, 0.1, 0.5, toSplit);
        double[][] methodByMethodMatrixFiltered = matrixConstruction.filterMatrix(methodByMethodMatrix, threshold);

        ArrayList<ArrayList<Byte>> playerChoices = new ArrayList<>();

        for (int i = 0; i < extractionResult.length ; i++) {
            playerChoices.add(new ArrayList<>());
            playerChoices.get(i).add(extractionResult[i]);
            remainingMethods.remove(Byte.valueOf(extractionResult[i]));
        }

        System.out.println(playerChoices);
        System.out.println(remainingMethods);
        while (remainingMethods.size() != 0) {
            PayoffMatrix pm = computePayoffMatrix(playerChoices, remainingMethods, methodByMethodMatrixFiltered, 0.5, 0.2);
            pm.calculatePayoffs();
            PayoffTuple nashEquilibrium = pm.findNashEquilibrium();
            for (int i = 0; i < nashEquilibrium.getMoves().length ; i++) {
                if (nashEquilibrium.getMoves()[i] != -1) {
                    playerChoices.get(i).add(nashEquilibrium.getMoves()[i]);
                    remainingMethods.remove(Byte.valueOf(nashEquilibrium.getMoves()[i]));
                }
            }

            System.out.println(playerChoices);
            System.out.println(remainingMethods);
        }

        System.exit(0);

        Collection<ClassBean> result = new Vector<>();
        return result;
    }

    private PayoffMatrix computePayoffMatrix(ArrayList<ArrayList<Byte>> playerChoices,
                                             ArrayList<Byte> remainingMethods,
                                             double[][] methodByMethodMatrix,
                                             double e1, double e2) {

        byte[] possibleChoices = new byte[remainingMethods.size()+1];
        for (int i = 0; i < remainingMethods.size() ; i++) {
            possibleChoices[i] = remainingMethods.get(i);
        }
        possibleChoices[remainingMethods.size()] = -1;

        calculateCombination(new ArrayList<>(), playerChoices.size(), 0, possibleChoices);
        combinations.remove(combinations.size()-1);
        System.out.println(combinations.size() + " possible combination");

        PayoffMatrix pm = new PayoffMatrix(combinations, remainingMethods, playerChoices, methodByMethodMatrix, e1, e2);
        return pm;

    }


    private void calculateCombination(ArrayList<Byte> perm, int size, int pos, byte[] possibleChoices) {
        if (pos == size) {
            combinations.add(ArrayUtils.toObject(Bytes.toArray(perm)));
        } else {
            for (int i = 0 ; i < possibleChoices.length ; i++) {
                if (!perm.contains(possibleChoices[i]) | possibleChoices[i] == -1) {
                    ArrayList<Byte> temp = new ArrayList<>(perm);
                    temp.add(possibleChoices[i]);
                    calculateCombination(temp, size, pos+1, possibleChoices);
                }
            }
        }
    }

}
