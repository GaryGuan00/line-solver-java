package jline.lang.distributions;

import jline.util.Interval;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class Gamma extends ContinuousDistribution implements Serializable {

    public Gamma(double shape, double scale) {
        super("Gamma", 2, new Interval(0, Double.POSITIVE_INFINITY));
        this.setParam(1, "shape", shape);
        this.setParam(2, "scale", scale);
    }

    @Override
    public List<Double> sample(long n) {
        throw new RuntimeException("Not implemented");
    }

    public List<Double> sample(long n, Random random) {
        return this.sample(n,random);
    }

    @Override
    public double getMean() {
        double shape = (double) this.getParam(1).getValue();
        double scale = (double) this.getParam(2).getValue();
        return shape * scale;
    }

    @Override
    public double getRate() {
        return 1.0 / getMean();
    }

    @Override
    public double getSCV() {
        double shape = (double) this.getParam(1).getValue();
        return 1.0 / shape;
    }

    @Override
    public double getVar() {
        double shape = (double) this.getParam(1).getValue();
        double scale = (double) this.getParam(2).getValue();
        return shape * scale * scale;
    }

    @Override
    public double getSkew() {
        double shape = (double) this.getParam(1).getValue();
        return 2.0 / Math.sqrt(shape);
    }

    @Override
    public double evalCDF(double t) {
        // TODO: Implement Gamma distribution CDF
        throw new RuntimeException("Not implemented");
    }

    @Override
    public double evalLST(double s) {
        // TODO: Implement Gamma distribution LST
        throw new RuntimeException("Not implemented");
    }
}

