package es.urjc.etsii.grafo.flayouts.constructives.tetris;

import java.util.Arrays;

/**
 * Solution fragment, used in the tetris constructive
 */
public record Piece(int[][] data, double cost, Piece a, Piece b) {
    public double increment(){
        return this.cost - a.cost - b.cost;
    }

    @Override
    public String toString() {
        return "Piece{" +
                String.format("d0=%s;d1=%s", Arrays.toString(data[0]), Arrays.toString(data[1])) +
                ", c=" + cost +
                ", a=" + a +
                ", b=" + b +
                '}';
    }
}
