package jline.lang.distributions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jline.util.Interval;

public class Disabled extends Distribution implements Serializable {
	
    public Disabled() {
        super("jline.lang.Disabled", 0, new Interval(Double.NaN,Double.NaN));
    }

    public boolean isDisabled() {
        return true;
    }

    public List<Double> sample(int n) {
        List<Double> ret_list = new ArrayList<Double>();
        for (int i = 0; i < n; i++) {
            ret_list.add(Double.NaN);
        }

        return ret_list;
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

    public double getMu() {
        return Double.NaN;
    }

    public double getPhi() {
        return Double.NaN;
    }

    public double evalCDF(double t) {
        return Double.NaN;
    }

    public double evalLST(double s) {
        return Double.NaN;
    }

    public Interval getPH() {
        return new Interval(Double.NaN, Double.NaN);
    }

    public boolean isImmediate() {
        return false;
    }

    public double getSkew() {
        return Double.NaN;
    }

    public double getVar() {
        return Double.NaN;
    }
}
