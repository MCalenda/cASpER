package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import groovy.lang.Tuple;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

public class PayoffMatrix {
    private ArrayList<Byte[]> combinations;
    private ArrayList<Byte> remainingMethods;
    private double[][] methodByMethodMatrix;
    private ArrayList<ArrayList<Byte>> playerChoices;
    private double e1, e2;
    private ArrayList<PayoffTuple> payoffs;
    private double[] maxPayoffs;

    public PayoffMatrix(ArrayList<Byte[]> combinations,
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
        this.maxPayoffs = new double[playerChoices.size()];
        for (int i = 0; i < maxPayoffs.length ; i++) {
            maxPayoffs[i] = -1;
        }
        this.payoffs = new ArrayList<>();
    }

    public void calculatePayoffs() {
        for (Byte[] combination : combinations) {
            PayoffTuple pt = new PayoffTuple(ArrayUtils.toPrimitive(combination));
            for (int i = 0; i < combination.length ; i++) {
                double payoff;
                ArrayList<Byte> lettingMethods = new ArrayList<>(remainingMethods);
                lettingMethods.remove(combination[i]);
                double similarityWithRemaining = 0;
                for (byte lettingMethod : lettingMethods) {
                    similarityWithRemaining += computeSimilarityUnderConstruction(lettingMethod, playerChoices.get(i));
                }
                similarityWithRemaining /= lettingMethods.size();
                if (combination[i] == -1) {
                    payoff = this.e1 - similarityWithRemaining;
                    pt.addPayoff(payoff);
                } else {
                    boolean nullMove = false;
                    for ( byte x : combination) {
                        if (x == -1) nullMove = true;
                    }
                    double similarityUnderConstruction = computeSimilarityUnderConstruction(combination[i], playerChoices.get(i));
                    if (nullMove) {
                        payoff = similarityUnderConstruction - this.e2;
                        pt.addPayoff(payoff);
                    } else {
                        payoff = similarityUnderConstruction - similarityWithRemaining;
                        pt.addPayoff(payoff);
                    }
                }
                if (payoff > maxPayoffs[i]) {
                    maxPayoffs[i] = payoff;
                }
            }
            payoffs.add(pt);
        }
    }

    private double computeSimilarityUnderConstruction(byte method, ArrayList<Byte> classUnderConstruction) {
        double similarity = 0;
        for (byte methodInTheClass : classUnderConstruction) {
            similarity += methodByMethodMatrix[method][methodInTheClass];
        }
        return (1.0/classUnderConstruction.size()) * similarity;
    }

    public PayoffTuple findNashEquilibrium() {
        ArrayList<ArrayList<PayoffTuple>> nashEquilibriums = new ArrayList<>();
        for (int i = 0; i <= maxPayoffs.length ; i++) {
            nashEquilibriums.add(new ArrayList<PayoffTuple>());
        }
        for (PayoffTuple payoff : payoffs) {
            byte maxFound = 0;
            for (int i = 0; i < maxPayoffs.length ; i++) {
                if (payoff.getPayoffs().get(i) >= maxPayoffs[i]) {
                    maxFound++;
                }
            }
            nashEquilibriums.get(maxFound).add(payoff);
        }

        PayoffTuple nashEquilibrium = new PayoffTuple(new byte[]{-1});
        for (int i = maxPayoffs.length; i >= 0; i--) {
            if (nashEquilibriums.get(i).isEmpty()) continue;
            else {
                double maxTotalPayoff = -1;
                for (PayoffTuple pt : nashEquilibriums.get(i)) {
                    double proxyPayoff = -1;
                    for (double x : pt.getPayoffs()){
                        proxyPayoff += x;
                    }
                    if (proxyPayoff >= maxTotalPayoff) {
                        nashEquilibrium = pt;
                    }
                }
            }
        }
        return nashEquilibrium;
    }
}
