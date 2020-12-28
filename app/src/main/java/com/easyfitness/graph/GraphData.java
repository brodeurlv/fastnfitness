package com.easyfitness.graph;

public class GraphData {
    private final double x;
    private final double y;

    public GraphData(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public GraphData(double x, double y, int y_unit) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    } // in days

    public double getY() {
        return this.y;
    }
}
