package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import com.google.common.primitives.Bytes;
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

        byte[][] playerChoices = new byte[extractionResult.length][remainingMethods.size()];

        for (int i = 0; i < extractionResult.length ; i++) {
            playerChoices[i][0] = extractionResult[i];
            remainingMethods.remove(Byte.valueOf(extractionResult[i]));
        }

        PayoffMatrix pm = computePayoffMatrix(playerChoices, remainingMethods, methodByMethodMatrix, 0.5, 0.2);
        Collection<ClassBean> result = new Vector<>();
        return result;

    }

    private PayoffMatrix computePayoffMatrix(byte[][] playerChoices,
                                             ArrayList<Byte> remainingMethods,
                                             double[][] methodByMethodMatrix,
                                             double e1, double e2) {

        byte[] possibleChoices = new byte[remainingMethods.size() + 1];
        for (int i = 0; i < remainingMethods.size() ; i++) {
            possibleChoices[i] = remainingMethods.get(i);
        }
        possibleChoices[remainingMethods.size()] = -1;

        System.out.print("Possible choices: ");
        for (byte x : possibleChoices) {
            System.out.print(x + " ");
        }
        System.out.println();

        calculateCombination(new ArrayList<>(), playerChoices.length, 0, possibleChoices);
        System.out.println(combinations.size() + " possible combination");


        /*
        combinations.remove(combinations.size()-1);
        PayoffMatrix pm = new PayoffMatrix(combinations, remainingMethods, playerChoices, methodByMethodMatrix, e1, e2);
        System.out.println(pm);
        System.exit(0);
        */
        return null;

    }


    private void calculateCombination(ArrayList<Byte> perm, int size, int pos, byte[] possibleChoices) {
        if (pos == size) {
            combinations.add(ArrayUtils.toObject(Bytes.toArray(perm)));
        } else {
            for (int i = 0 ; i < possibleChoices.length ; i++) {
                ArrayList<Byte> temp = new ArrayList<>(perm);
                if (!temp.contains(possibleChoices[i]) | possibleChoices[i] == -1) {
                    temp.add(possibleChoices[i]);
                    calculateCombination(temp, size, pos+1, possibleChoices);
                }
            }
        }
    }

    private void makeIteration(PayoffMatrix pm, ArrayList<HashMap<Integer, MethodBean>> choices) {
    }

}
