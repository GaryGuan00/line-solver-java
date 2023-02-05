package jline.lang.distributions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jline.util.Interval;

public class Immediate extends Distribution implements Serializable {
	
    public Immediate() {
        super("jline.lang.Immediate", 0, new Interval(0,0));
    }

    public boolean isDisabled() {
        return false;
    }

    public List<Double> sample(int n) {
        List<Double> ret_list = new ArrayList<Double>();
        for (int i = 0; i < n; i++) {
            ret_list.add(0.0);
        }

        return ret_list;
    }

    public double getRate() {
        return infRateRep;
    }

    public double getMean() {
        return 0;
    }

    public double getSCV() {
        return 0;
    }

    public double getMu() {
        return infRateRep;
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
        return new Interval(-infRateRep, infRateRep);
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
