package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import com.google.common.primitives.Bytes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.util.*;

public class PayoffMatrix {
    private ArrayList<Integer> remainingMethods;
    private ArrayList<ArrayList<Integer>> playerChoices;
    private double[][] methodByMethodMatrix;
    private BigDecimal e1, e2;
    public HashMap<ArrayList<Integer>, ArrayList<Double>> totalPayoffs;

    public PayoffMatrix(ArrayList<Integer> remainingMethods,
                        ArrayList<ArrayList<Integer>> playerChoices,
                        double[][] methodByMethodMatrix,
                        double e1, double e2) {
        this.remainingMethods = remainingMethods;
        this.playerChoices = playerChoices;
        this.methodByMethodMatrix = methodByMethodMatrix;
        this.e1 = BigDecimal.valueOf(e1);
        this.e2 = BigDecimal.valueOf(e2);
        this.totalPayoffs = new HashMap<>();
    }

    public void computePayoffs(ArrayList<Integer> perm, int size, int pos, ArrayList<Integer> possibleChoices) {
        if (pos == size) {
            ArrayList<Double> payoffs = new ArrayList<>();
            byte notNullMove = 0;
            for (int x : perm) {
                if (x != -1) {
                    notNullMove++;
                }
            }
            if (notNullMove == 0) return;
            for (int i = 0; i < perm.size() ; i++) {
                BigDecimal payoff;
                ArrayList<Integer> lettingMethods = new ArrayList<>();
                for (int x : perm) {
                    if (x == -1 || x == perm.get(i)) continue;
                    lettingMethods.add(x);
                }
                if (perm.get(i) == -1) {
                    payoff = this.e1.subtract(computeSimilarityWithRemaining(playerChoices.get(i), lettingMethods));
                } else {
                    if (notNullMove == 1) {
                        payoff = computeSimilarityUnderConstruction(playerChoices.get(i), perm.get(i)).subtract(this.e2);
                    } else {
                        payoff = computeSimilarityUnderConstruction(playerChoices.get(i), perm.get(i))
                                .subtract(computeSimilarityWithRemaining(playerChoices.get(i), lettingMethods));
                    }
                }
                payoffs.add(payoff.doubleValue());
            }
            totalPayoffs.put(perm, payoffs);
        } else {
            for (int possibleChoice : possibleChoices) {
                if (!perm.contains(possibleChoice) || possibleChoice == -1) {
                    ArrayList<Integer> temp = new ArrayList<>(perm);
                    temp.add(possibleChoice);
                    computePayoffs(temp, size, pos + 1, possibleChoices);
                }
            }
        }
    }

    private BigDecimal computeSimilarityUnderConstruction(ArrayList<Integer> classUnderConstruction, int method) {
        BigDecimal similarity = new BigDecimal(0.0);
        for (int methodInTheClass : classUnderConstruction) {
            similarity = similarity.add(BigDecimal.valueOf(methodByMethodMatrix[method][methodInTheClass]));
        }
        return similarity.divide(new BigDecimal(classUnderConstruction.size()));
    }

    private BigDecimal computeSimilarityWithRemaining(ArrayList<Integer> classUnderConstruction, ArrayList<Integer> lettingMethods) {
        BigDecimal similarity = new BigDecimal(0);
        for (int method : lettingMethods) {
            similarity = similarity.add(computeSimilarityUnderConstruction(classUnderConstruction, method));
        }
        return similarity.divide(new BigDecimal(lettingMethods.size()));
    }

    public HashMap<ArrayList<Integer>, ArrayList<Double>> findNashEquilibriums() {
        ArrayList<Integer> possibleMoves = new ArrayList<>(remainingMethods);
        HashMap<ArrayList<Integer>, ArrayList<Double>> nashEquilibriums = new HashMap<>();
        possibleMoves.add(-1);
        for (Map.Entry<ArrayList<Integer>, ArrayList<Double>> entry : totalPayoffs.entrySet()) {
            ArrayList<Integer> combination = entry.getKey();
            ArrayList<Double> payoffs = entry.getValue();
            boolean isNE = true;
            for (int i = 0; i < combination.size() ; i++) {
                ArrayList<Integer> alternativeCombination = new ArrayList<>(combination);
                for (int possibleMove : possibleMoves) {
                    alternativeCombination.set(i, possibleMove);
                    ArrayList<Double> alternativeEntry = totalPayoffs.get(alternativeCombination);
                    if (alternativeEntry == null) continue;
                    if (alternativeEntry.get(i) > payoffs.get(i)) {
                        isNE = false;
                        break;
                    }
                }
                if (!isNE) break;
            }
            if (isNE) nashEquilibriums.put(combination, payoffs);
        }
        return nashEquilibriums;
    }

    public HashMap<ArrayList<Integer>, ArrayList<Double>> getTotalPayoffs() {
        return totalPayoffs;
    }

    public ArrayList<Integer> getRemainingMethods() {
        return remainingMethods;
    }

    public void setTotalPayoffs(HashMap<ArrayList<Integer>, ArrayList<Double>> totalPayoffs) {
        this.totalPayoffs = totalPayoffs;
    }
}
