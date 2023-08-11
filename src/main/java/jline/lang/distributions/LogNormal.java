package jline.lang.distributions;

import jline.util.Interval;
import jline.util.Numerics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LogNormal extends ContinuousDistribution implements Serializable {
    public LogNormal(double mu, double sigma) {
        super("LogNormal", 2, new Interval(0, Double.POSITIVE_INFINITY));
        this.setParam(1, "mu", mu);
        this.setParam(2, "sigma", sigma);
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
        double mu = (double) this.getParam(1).getValue();
        double sigma = (double) this.getParam(2).getValue();
        for (int i = 0; i < n; i++) {
            double z = random.nextGaussian(); // Standard Normal Distribution
            double value = Math.exp(mu + sigma * z); // Log-normal Distribution
            samples.add(value);
        }
        return samples;
    }

    @Override
    public double getMean() {
        double mu = (double) this.getParam(1).getValue();
        double sigma = (double) this.getParam(2).getValue();
        return Math.exp(mu + sigma * sigma / 2.0);
    }

    @Override
    public double getRate() {
        return 1 / getMean();
    }

    @Override
    public double getSCV() {
        double mu = (double) this.getParam(1).getValue();
        double sigma = (double) this.getParam(2).getValue();
        return (Math.exp(sigma * sigma) - 1) * Math.exp(2 * mu + sigma * sigma);
    }

    @Override
    public double getVar() {
        double mu = (double) this.getParam(1).getValue();
        double sigma = (double) this.getParam(2).getValue();
        return (Math.exp(sigma * sigma) - 1) * Math.exp(2 * mu + sigma * sigma);
    }

    @Override
    public double getSkew() {
        double sigma = (double) this.getParam(2).getValue();
        return (Math.exp(sigma * sigma) + 2) * Math.sqrt(Math.exp(sigma * sigma) - 1);
    }

    @Override
    public double evalCDF(double t) {
        double mu = (double) this.getParam(1).getValue();
        double sigma = (double) this.getParam(2).getValue();
        return 0.5 + 0.5 * Numerics.erf((Math.log(t) - mu) / (Math.sqrt(2) * sigma));
    }

    @Override
    public double evalLST(double s) {
        throw new RuntimeException("Not implemented");
    }

}

