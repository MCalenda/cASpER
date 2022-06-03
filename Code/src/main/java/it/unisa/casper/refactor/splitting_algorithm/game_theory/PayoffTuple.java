package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import java.util.ArrayList;

public class PayoffTuple {

    private ArrayList<Double> payoffs;
    private ArrayList<Byte> moves;

    public PayoffTuple() {
        this.moves = new ArrayList<>();
        this.payoffs = new ArrayList<>();
    }

    public void addPayoff(double payoff) {
        this.payoffs.add(payoff);
    }

    public void setMoves(ArrayList<Byte> moves) {
        this.moves = moves;
    }

    public void setPayoffs(ArrayList<Double> payoffs) {
        this.payoffs = payoffs;
    }

    public ArrayList<Double> getPayoffs() {
        return payoffs;
    }

    public ArrayList<Byte> getMoves() {
        return moves;
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();
        for (byte x : moves) {
            toString.append(x).append(" ");
        }
        toString.append("\n").append(payoffs);
        return toString.toString();
    }
}
