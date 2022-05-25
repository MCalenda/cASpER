package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import java.util.ArrayList;

public class PayoffTuple {

    private ArrayList<Double> payoffs;
    private ArrayList<Integer> moves;

    public PayoffTuple(ArrayList<Integer> moves) {
        this.moves = moves;
        this.payoffs = new ArrayList<>();
    }

    public void addPayoff(double payoff) {
        this.payoffs.add(payoff);
    }

    @Override
    public String toString() {
        return moves + " " + payoffs;
    }
}
