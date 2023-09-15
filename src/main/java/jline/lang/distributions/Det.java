package jline.lang.distributions;

import jline.util.Interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Det extends Distribution implements Serializable {

    public Det(double t) {
        super("Det", 1, new Interval(t, t));
        this.setParam(1, "t", t);
    }

    public boolean isDisabled() {
        return false;
    }
    @Override
    public List<Double> sample(long n) {
        return this.sample(n,null);
    }
    @Override
    public List<Double> sample(long n, Random random) {
        List<Double> ret_list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ret_list.add((double)this.getParam(1).getValue());
        }
        return ret_list;
//        throw new RuntimeException("Not implemented");
    }

    public double getRate() {
        return 1 / (double) this.getParam(1).getValue();
    }

    public double getMean() {
        return (double) this.getParam(1).getValue();
    }

    public double getSCV() {
        return 0;
    }

    public double getMu() {
        return 1 / (double) this.getParam(1).getValue();
    }

    public double getPhi() {
        return 1;
    }

    public double evalCDF(double t) {
        // Evaluate the cumulative distribution function at t
        if (t < (double) this.getParam(1).getValue()) {
            return 0;
        } else {
            return 1;
        }
    }

    public double evalLST(double s) {
        // Evaluate the Laplace-Stieltjes transform of the distribution function at t
        return Math.exp(-s * (double) this.getParam(1).getValue());
    }

    public Interval getPH() {
        return new Interval(-1 / (double) this.getParam(1).getValue(), 1 / (double) this.getParam(1).getValue());
    }

    public boolean isImmediate() {
        return false;
    }

    public double getSkew() {
        return 0;
    }

    public double getVar() {
        return 0;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean isDiscrete() {
        return true;
    }


}
