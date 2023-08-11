package jline.lang.distributions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jline.lang.constant.GlobalConstants;
import jline.util.Interval;

public class Immediate extends Distribution implements Serializable {
	
    public Immediate() {
        super("jline.lang.Immediate", 0, new Interval(0,0));
    }

    public boolean isDisabled() {
        return false;
    }

    /**
     * Gets n samples from the distribution
     * @param n - the number of samples
     * @return - n samples from the distribution
     */
    @Override
    public List<Double> sample(long n) {
        return this.sample(n,null);
    }

    @Override
    public List<Double> sample(long n, Random random) {
        List<Double> ret_list = new ArrayList<Double>();
        for (int i = 0; i < n; i++) {
            ret_list.add(0.0);
        }

        return ret_list;
    }

    public double getRate() {
        return GlobalConstants.Immediate;
    }

    public double getMean() {
        return 1 / GlobalConstants.Immediate;
    }

    public double getSCV() {
        return 0;
    }

    public double getMu() {
        return GlobalConstants.Immediate;
    }

    public double getPhi() {
        return 1;
    }

    public double evalCDF(double t) {
        return 1;
    }

    public double evalLST(double s) {
        return 1;
    }

    public Interval getPH() {
        return new Interval(-GlobalConstants.Immediate, GlobalConstants.Immediate);
    }

    public boolean isImmediate() {
        return true;
    }

    public double getSkew() {
        return 0;
    }

    public double getVar() {
        return 0;
    }
}
