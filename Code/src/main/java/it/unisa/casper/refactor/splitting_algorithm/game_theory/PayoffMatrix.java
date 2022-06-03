package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public class PayoffMatrix {
    private ListIterator<Byte[]> combinations;
    private ArrayList<Byte> remainingMethods;
    private double[][] methodByMethodMatrix;
    private ArrayList<ArrayList<Byte>> playerChoices;
    private HashMap<ArrayList<Byte>, ArrayList<Double>> totalPayoffs;
    private double e1, e2;
    private double[] maxPayoffs;


    public PayoffMatrix(ListIterator<Byte[]> combinations,
                        ArrayList<Byte> remainingMethods,
                        ArrayList<ArrayList<Byte>> playerChoices,
                        double[][] methodByMethodMatrix,
                        double e1, double e2) {
        this.combinations = combinations;
        this.remainingMethods = remainingMethods;
        this.playerChoices = playerChoices;
        this.methodByMethodMatrix = methodByMethodMatrix;
        this.e1 = e1;
        this.e2 = e2;
        this.totalPayoffs = new HashMap<>();
        this.maxPayoffs = new double[playerChoices.size()];
        for (int i = 0; i < maxPayoffs.length ; i++) {
            maxPayoffs[i] = -1;
        }
    }

    public void calculatePayoffs() {
        while (combinations.hasNext()) {
            ArrayList<Byte> combination = new ArrayList<>(Arrays.asList(combinations.next()));
            ArrayList<Double> payoffs = new ArrayList<>();
            for (int i = 0; i < combination.size() ; i++) {
                double payoff;
                ArrayList<Byte> lettingMethods = new ArrayList<>();
                for (byte x : combination) {
                    if (x == -1 || x == combination.get(i)) continue;
                    else lettingMethods.add(x);
                }

                //calculate similarity with methods except the chosen one
                double similarityWithRemaining = 0;
                for (byte lettingMethod : lettingMethods) {
                    similarityWithRemaining += computeSimilarityUnderConstruction(lettingMethod, playerChoices.get(i));
                }
                similarityWithRemaining /= lettingMethods.size();
                if (combination.get(i) == -1) {
                    payoff = this.e1 - similarityWithRemaining;
                    payoffs.add(payoff);
                } else {
                    byte notNullMove = 0;
                    for (byte x : combination) {
                        if (x != -1) notNullMove += 1;
                    }

                    double similarityUnderConstruction = computeSimilarityUnderConstruction(combination.get(i), playerChoices.get(i));
                    if (notNullMove == 1) {
                        payoff = similarityUnderConstruction - this.e2;
                        payoffs.add(payoff);
                    } else {
                        payoff = similarityUnderConstruction - similarityWithRemaining;
                        payoffs.add(payoff);
                    }
                }
                if (payoff > maxPayoffs[i]) {
                    maxPayoffs[i] = payoff;
                }
            }
            totalPayoffs.put(combination, payoffs);
            combinations.remove();
        }
    }

    private double computeSimilarityUnderConstruction(byte method, ArrayList<Byte> classUnderConstruction) {
        double similarity = 0;
        for (byte methodInTheClass : classUnderConstruction) {
            similarity += methodByMethodMatrix[method][methodInTheClass];
        }
        return similarity/classUnderConstruction.size();
    }

    public PayoffTuple findNashEquilibrium() {
        HashMap<ArrayList<Byte>, ArrayList<Double>> nashEq = new HashMap<>();
        ArrayList<Byte> possibleMoves = new ArrayList<>(remainingMethods);
        possibleMoves.add((byte) -1);
        for (Map.Entry<ArrayList<Byte>, ArrayList<Double>> entry : totalPayoffs.entrySet()) {
            ArrayList<Byte> moves = entry.getKey();
            ArrayList<Double> payoffs = entry.getValue();
            boolean isNE = true;
            for (int i = 0; i < payoffs.size() ; i++) {
                if (payoffs.get(i) >= maxPayoffs[i]) continue;
                for (Byte possibleMove : possibleMoves) {
                    if (possibleMove == -1 || !moves.contains(possibleMove)) {
                        ArrayList<Byte> alternativeMoves = new ArrayList<>(moves);
                        alternativeMoves.set(i, possibleMove);
                        ArrayList<Double> alternativePayoffs = totalPayoffs.get(alternativeMoves);
                        if (alternativePayoffs == null) continue;
                        double newPayoff = alternativePayoffs.get(i);
                        if (newPayoff > payoffs.get(i)) {
                            isNE = false;
                            break;
                        }
                    }
                }
                if (!isNE) break;
            }
            if (isNE) nashEq.put(moves, payoffs);
        }

        double maxPayoff = -1;
        PayoffTuple nashEquilibrium = new PayoffTuple();
        for (Map.Entry<ArrayList<Byte>, ArrayList<Double>> entry : nashEq.entrySet()) {
            double nePayoff = 0;
            for (double x : entry.getValue()) {
                nePayoff += x;
            }
            if (nePayoff >= maxPayoff) {
                maxPayoff = nePayoff;
                nashEquilibrium.setMoves(entry.getKey());
                nashEquilibrium.setPayoffs(entry.getValue());
            }
        }
        return nashEquilibrium;
    }
}
