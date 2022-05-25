package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import java.util.ArrayList;

public class PayoffMatrix {
    private ArrayList<ArrayList<Integer>> combinations;
    private ArrayList<Integer> remainingMethods;
    private double[][] methodByMethodMatrix;
    private ArrayList<ArrayList<Integer>> playerChoices;
    private double e1, e2;

    private ArrayList<PayoffTuple> payoffs;

    public PayoffMatrix(ArrayList<ArrayList<Integer>> combinations,
                        ArrayList<Integer> remainingMethods,
                        ArrayList<ArrayList<Integer>> playerChoices,
                        double[][] methodByMethodMatrix,
                        double e1, double e2) {
        this.combinations = combinations;
        this.remainingMethods = remainingMethods;
        this.playerChoices = playerChoices;
        this.methodByMethodMatrix = methodByMethodMatrix;
        this.e1 = e1;
        this.e2 = e2;
        this.payoffs = calculatePayoffs();
    }

    private ArrayList<PayoffTuple> calculatePayoffs() {
        ArrayList<PayoffTuple> payoffs = new ArrayList<>();
        for (ArrayList<Integer> combination : combinations) {
            PayoffTuple pt = new PayoffTuple(combination);
            for (int i = 0; i < combination.size() ; i++) {
                ArrayList<Integer> lettingMethods = new ArrayList<>(remainingMethods);
                lettingMethods.remove(combination.get(i));
                double similarityWithRemaining = 0;
                for (int lettingMethod : lettingMethods) {
                    similarityWithRemaining += computeSimilarityUnderConstruction(lettingMethod, playerChoices.get(i));
                }
                similarityWithRemaining /= lettingMethods.size();

                if (combination.get(i) == -1) {
                    pt.addPayoff(this.e1 - similarityWithRemaining);
                } else {
                    double similarityUnderConstruction = computeSimilarityUnderConstruction(combination.get(i), playerChoices.get(i));
                    pt.addPayoff(similarityUnderConstruction - similarityWithRemaining);
                }
            }
            payoffs.add(pt);
        }
        return payoffs;
    }

    private double computeSimilarityUnderConstruction(int method, ArrayList<Integer> classUnderConstruction) {
        double similarity = 0;
        for (int methodInTheClass : classUnderConstruction) {
            similarity += methodByMethodMatrix[method][methodInTheClass];
        }
        return (1.0/classUnderConstruction.size()) * similarity;
    }

    @Override
    public String toString() {
        for (int row = 0; row < methodByMethodMatrix.length; row++) {
            for (int col = 0; col < methodByMethodMatrix[row].length; col++) {
                System.out.printf("%f", methodByMethodMatrix[row][col]);
            }
            System.out.println();
        }
        String toString = "";
        for (int i = 0; i < payoffs.size() ; i++) {
            toString += payoffs.get(i).toString() + "\n";
        }
        return toString;
    }
}
