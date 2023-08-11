package jline.lang.distributions;

import jline.util.Interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Det extends Distribution implements Serializable {

    public Det(double value) {
        super("jline.Det", 1, new Interval(value, value));
        this.setParam(1, "value", value);
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
        if (t < (double) this.getParam(1).getValue()) {
            return 0;
        } else {
            return 1;
        }
    }

    public double evalLST(double s) {
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
}
