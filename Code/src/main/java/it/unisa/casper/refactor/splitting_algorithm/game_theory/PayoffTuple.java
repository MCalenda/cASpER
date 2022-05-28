package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import java.util.ArrayList;

public class PayoffTuple {

    private ArrayList<Double> payoffs;
    private byte[] moves;

    public PayoffTuple(byte[] moves) {
        this.moves = moves;
        this.payoffs = new ArrayList<>();
    }

    public void addPayoff(double payoff) {
        this.payoffs.add(payoff);
    }

    public ArrayList<Double> getPayoffs() {
        return payoffs;
    }

    public byte[] getMoves() {
        return moves;
    }
}
