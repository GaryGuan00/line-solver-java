package jline.lang.distributions;

import jline.util.Interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Pareto extends ContinuousDistribution implements Serializable {

    public Pareto(double shape, double scale) {
        super("Pareto", 2, new Interval(0, Double.POSITIVE_INFINITY));
        this.setParam(1, "shape", shape);
        this.setParam(2, "scale", scale);
    }

    /**
     * Gets n samples from the distribution
     * @param n - the number of samples
     * @return - n samples from the distribution
     */
    @Override
    public List<Double> sample(long n) {
        return this.sample(n,new Random());
    }

    @Override
    public List<Double> sample(long n, Random random) {
        List<Double> samples = new ArrayList<>();
        double shape = (double)this.getParam(1).getValue();
        double scale = (double)this.getParam(2).getValue();

        for (long i = 0; i < n; i++) {
            double u = random.nextDouble();
            double sample = scale / Math.pow(u, 1 / shape);
            samples.add(sample);
        }

        return samples;
    }

    @Override
    public double getMean() {
        double shape = (double)this.getParam(1).getValue();
        double scale = (double)this.getParam(2).getValue();

        if (shape <= 1) {
            return Double.POSITIVE_INFINITY;
        } else {
            return shape * scale / (shape - 1);
        }
    }

    @Override
    public double getRate() {
        return 1 / getMean();
    }

    @Override
    public double getSCV() {
        double shape = (double)this.getParam(1).getValue();
        if (shape <= 2) {
            return Double.POSITIVE_INFINITY;
        } else {
            return shape / (shape - 2);
        }
    }

    @Override
    public double getVar() {
        double shape = (double)this.getParam(1).getValue();
        double scale = (double)this.getParam(2).getValue();

        if (shape <= 2) {
            return Double.POSITIVE_INFINITY;
        } else {
            return (scale * scale * shape) / ((shape - 1) * (shape - 1) * (shape - 2));
        }
    }

    @Override
    public double getSkew() {
        double shape = (double)this.getParam(1).getValue();
        if (shape <= 3) {
            return Double.POSITIVE_INFINITY;
        } else {
            return 2*(1 + shape) / (shape - 3) * Math.sqrt((shape - 2) / shape);
        }
    }

    @Override
    public double evalCDF(double t) {
        double shape = (double)this.getParam(1).getValue();
        double scale = (double)this.getParam(2).getValue();

        if (t < scale) {
            return 0;
        } else {
            return 1 - Math.pow(scale / t, shape);
        }
    }

    @Override
    public double evalLST(double s) {
        double shape = (double)this.getParam(1).getValue();
        double scale = (double)this.getParam(2).getValue();

        if (s < 0) {
            return Double.POSITIVE_INFINITY;
        } else {
            return Math.pow(1 - s * scale, -shape);
        }
    }
}

