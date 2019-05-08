package com.easyfitness;

public class DateGraphData {
    private double x, y;

    public DateGraphData(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public DateGraphData(double x, double y, int y_unit) {
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
