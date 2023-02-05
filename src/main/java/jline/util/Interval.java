package jline.util;

import java.io.Serializable;

public class Interval implements Serializable {
    protected double left, right;

    public Interval(double left, double right) {
        this.left = left;
        this.right = right;
    }

    public double getLeft() {
        return this.left;
    }

    public double getRight() {
        return this.right;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public void setRight(double right) {
        this.right = right;
    }
}