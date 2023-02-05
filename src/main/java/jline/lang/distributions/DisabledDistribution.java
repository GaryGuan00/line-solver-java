package jline.lang.distributions;

import java.io.Serializable;
import java.util.List;

import jline.util.Interval;

public class DisabledDistribution extends Distribution implements Serializable {
    public DisabledDistribution() {
        super("Disabled", 0, new Interval(0, 0));
    }

    public List<Double> sample(int n)  {
        throw new RuntimeException("Not Implemented!");
    }

    public double getRate() {
        return Double.NaN;
    }

    public double getMean() {
        return Double.NaN;
    }

    public double getSCV() {
        return Double.NaN;
    }

    public double getVar() {
        return Double.NaN;
    }

    public double getSkew() {
        return Double.NaN;
    }

    public double evalCDF(double t) {
        return Double.NaN;
    }

    public double evalLST(double s) {
        return Double.NaN;
    }
}
