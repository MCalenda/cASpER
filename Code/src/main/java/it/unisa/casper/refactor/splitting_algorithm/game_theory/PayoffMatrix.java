package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import com.google.common.primitives.Bytes;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class PayoffMatrix {
    private ArrayList<Byte> remainingMethods;
    private ArrayList<ArrayList<Byte>> playerChoices;
    private double[][] methodByMethodMatrix;
    private double e1, e2;
    public HashMap<ArrayList<Byte>, ArrayList<Double>> totalPayoffs;
    private double[] maxPayoffs;


    public PayoffMatrix(ArrayList<Byte> remainingMethods,
                        ArrayList<ArrayList<Byte>> playerChoices,
                        double[][] methodByMethodMatrix,
                        double e1, double e2) {
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

        System.out.println("Starting computing payoffs, " + remainingMethods.size() + " remaining methods");
        byte[] possibleChoices = new byte[remainingMethods.size()+1];
        for (int i = 0; i < remainingMethods.size() ; i++) {
            possibleChoices[i] = remainingMethods.get(i);
        }
        possibleChoices[remainingMethods.size()] = -1;
        computePayoffs(new ArrayList<>(), playerChoices.size(), 0, possibleChoices);
        System.out.println("Payoffs computed");

    }

    private void computePayoffs(ArrayList<Byte> perm, int size, int pos, byte[] possibleChoices) {
        if (pos == size) {
            ArrayList<Double> payoffs = new ArrayList<>();
            //check how many not null moves are in the variation
            byte notNullMove = 0;
            for (byte x : perm) {
                if (x != -1) {
                    notNullMove++;
                }
            }
            if (notNullMove == 0) return;

            for (int i = 0; i < perm.size() ; i++) {
                double payoff;
                ArrayList<Byte> lettingMethods = new ArrayList<>();
                for (byte x : perm) {
                    if (x == -1 || x == perm.get(i)) continue;
                    lettingMethods.add(x);
                }
                if (perm.get(i) == -1) {
                    payoff = this.e1 - computeSimilarityWithRemaining(playerChoices.get(i), lettingMethods);
                } else {
                    if (notNullMove == 1) {
                        payoff = computeSimilarityUnderConstruction(playerChoices.get(i), perm.get(i)) - this.e2;
                    } else {
                        payoff = computeSimilarityUnderConstruction(playerChoices.get(i), perm.get(i))
                                - computeSimilarityWithRemaining(playerChoices.get(i), lettingMethods);
                    }
                }
                payoffs.add(payoff);
                if (payoff > maxPayoffs[i]) {
                    maxPayoffs[i] = payoff;
                }
            }
            totalPayoffs.put(perm, payoffs);
        } else {
            for (int i = 0 ; i < possibleChoices.length ; i++) {
                if (!perm.contains(possibleChoices[i]) | possibleChoices[i] == -1) {
                    ArrayList<Byte> temp = new ArrayList<>(perm);
                    temp.add(possibleChoices[i]);
                    computePayoffs(temp, size, pos+1, possibleChoices);
                }
            }
        }
    }

    private double computeSimilarityUnderConstruction(ArrayList<Byte> classUnderConstruction, byte method) {
        double similarity = 0;
        for (byte methodInTheClass : classUnderConstruction) {
            similarity += methodByMethodMatrix[method][methodInTheClass];
        }
        return similarity/classUnderConstruction.size();
    }

    private double computeSimilarityWithRemaining(ArrayList<Byte> classUnderConstruction, ArrayList<Byte> lettingMethods) {
        double similarity = 0;
        for (byte method : lettingMethods) {
            similarity += computeSimilarityUnderConstruction(classUnderConstruction, method);
        }
        return similarity/lettingMethods.size();
    }

    public ArrayList<Byte> findNashEquilibrium() {
        HashMap<ArrayList<Byte>, ArrayList<Double>> nashEq = new HashMap<>();
        ArrayList<Byte> possibleMoves = new ArrayList<>(remainingMethods);
        possibleMoves.add((byte) -1);
        int count = 0;
        for (Map.Entry<ArrayList<Byte>, ArrayList<Double>> entry : totalPayoffs.entrySet()) {
            if (++count % 1000 == 0) System.out.println(count + "/" + totalPayoffs.size());
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
        ArrayList<Byte> nashEquilibrium = new ArrayList<>();
        for (Map.Entry<ArrayList<Byte>, ArrayList<Double>> entry : nashEq.entrySet()) {
            double nePayoff = 0;
            for (double x : entry.getValue()) {
                nePayoff += x;
            }
            if (nePayoff >= maxPayoff) {
                maxPayoff = nePayoff;
                nashEquilibrium = entry.getKey();
            }
        }
        return nashEquilibrium;
    }
}
