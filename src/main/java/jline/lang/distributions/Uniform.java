package jline.lang.distributions;

import jline.util.Interval;

import java.util.*;
import java.io.Serializable;

public class Uniform extends Distribution implements Serializable {
    public Uniform(double a, double b) {
        super("Uniform", 2, new Interval(a, b));
        this.setParam(1, "a", a);
        this.setParam(2, "b", b);
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
        double a = (double) this.getParam(1).getValue();
        double b = (double) this.getParam(2).getValue();
        List<Double> samples = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double randomValue = a + (b - a) * random.nextDouble();
            samples.add(randomValue);
        }
        return samples;
    }

    public double getMean() {
        double a = (double) this.getParam(1).getValue();
        double b = (double) this.getParam(2).getValue();
        return (a + b) / 2.0;
    }

    public double getRate() {
        return 1 / getMean();
    }

    public double getSCV() {
        double a = (double) this.getParam(1).getValue();
        double b = (double) this.getParam(2).getValue();
        return ((b - a) * (b - a)) / 12.0;
    }

    public double getVar() {
        double a = (double) this.getParam(1).getValue();
        double b = (double) this.getParam(2).getValue();
        return ((b - a) * (b - a)) / 12.0;
    }

    public double getSkew() {
        return 0;
    }

    public double evalCDF(double t) {
        double a = (double) this.getParam(1).getValue();
        double b = (double) this.getParam(2).getValue();
        if (t < a) {
            return 0;
        } else if (t > b) {
            return 1;
        } else {
            return (t - a) / (b - a);
        }
    }

    public double evalLST(double s) {
        double a = (double) this.getParam(1).getValue();
        double b = (double) this.getParam(2).getValue();
        if (s == 0) {
            return (b - a);
        } else {
            return (Math.exp(s * b) - Math.exp(s * a)) / (s * (b - a));
        }
    }
}

