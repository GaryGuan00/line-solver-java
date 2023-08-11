package jline.lang.distributions;

import jline.util.Interval;
import jline.util.Numerics;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class Weibull extends ContinuousDistribution implements Serializable {

    public Weibull(double shape, double scale) {
        super("Weibull", 2, new Interval(0, Double.POSITIVE_INFINITY));
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
        throw new RuntimeException("Not implemented");
    }

    @Override
    public double getMean() {
        double shape = (double) this.getParam(1).getValue();
        double scale = (double) this.getParam(2).getValue();

        // Mean of Weibull: scale * Gamma(1 + 1/shape)
        return scale * Numerics.gammaFunction(1 + 1/shape);
    }

    @Override
    public double getRate() {
        return 1 / this.getMean();
    }

    @Override
    public double getSCV() {
        double shape = (double) this.getParam(1).getValue();

        // SCV of Weibull: Gamma(1 + 2/shape) - [Gamma(1 + 1/shape)]^2
        double gamma1 = Numerics.gammaFunction(1 + 1/shape);
        double gamma2 = Numerics.gammaFunction(1 + 2/shape);

        return gamma2 - gamma1 * gamma1;
    }

    @Override
    public double getVar() {
        double mean = this.getMean();
        return this.getSCV() * mean * mean;
    }

    @Override
    public double getSkew() {
        // TODO: implement skewness calculation for Weibull
        throw new RuntimeException("Not implemented");
    }

    @Override
    public double evalCDF(double t) {
        double shape = (double) this.getParam(1).getValue();
        double scale = (double) this.getParam(2).getValue();

        // CDF of Weibull: 1 - exp(-(t / scale)^shape)
        return 1 - Math.exp(-Math.pow(t / scale, shape));
    }

    @Override
    public double evalLST(double s) {
        // TODO: implement if necessary
        throw new RuntimeException("Not implemented");
    }


}

