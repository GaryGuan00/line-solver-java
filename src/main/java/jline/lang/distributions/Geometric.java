package jline.lang.distributions;

import jline.util.Interval;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class Geometric extends DiscreteDistribution implements Serializable {

    public Geometric(double probability) {
        super("Geometric", 1, new Interval(0, Double.POSITIVE_INFINITY));
        this.setParam(1, "p", probability);
    }

    public double getMean()  {
        double p = (double) this.getParam(1).getValue();
        return 1/p;
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
        throw new RuntimeException("Not implemented");
    }

    public double getRate() {
        throw new RuntimeException("Not implemented");
    }
    public double getSCV() {
        throw new RuntimeException("Not implemented");
    }
    public double getVar() {
        throw new RuntimeException("Not implemented");
    }
    public double getSkew() {
        throw new RuntimeException("Not implemented");
    }
    public double evalCDF(double t) {
        throw new RuntimeException("Not implemented");
    }
    public double evalLST(double s) {
        throw new RuntimeException("Not implemented");
    }
}
