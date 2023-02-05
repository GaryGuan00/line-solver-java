package jline.lang.distributions;

import jline.lang.JLineMatrix;
import jline.lang.distributions.MarkovianDistribution;
import jline.util.Interval;
import jline.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.exp;
import static java.lang.Math.log;

public class Exp extends MarkovianDistribution  implements Serializable {
    public Exp(double lambda) {
        super("Exponential", 1);
        this.setParam(1, "lambda", lambda);
    }

    public List<Double> sample(int n)  {
        double lambda = (double)this.getParam(1).getValue();
        //return exprnd(1/lambda, n, 1);
        throw new RuntimeException("Not Implemented!");
    }

    public long getNumberOfPhases() {
        return 1;
    }

    public double evalCDF(double t) {
        double lambda = (double) this.getParam(1).getValue();
        return 1-exp(-lambda*t);
    }

    public Map<Integer, JLineMatrix> getPH() {
        double lambda = (double) this.getParam(1).getValue();
        JLineMatrix D0 = new JLineMatrix(1,1,1);
        JLineMatrix D1 = new JLineMatrix(1,1,1);
        D0.set(0, 0, -lambda);
        D1.set(0, 0, lambda);
        Map<Integer, JLineMatrix> res = new HashMap<Integer, JLineMatrix>();
        res.put(0, D0);
        res.put(1, D1);
        return res;
    }

    public double evalLST(double s) {
        double lambda = (double) this.getParam(1).getValue();
        return (lambda/(lambda+s));
    }

    public double getSCV() {
        return 1;
    }

    public double getRate() {
        return (double) this.getParam(1).getValue();
    }

    public double getMean() {
        return 1/getRate();
    }

    public double getVar() {
        return 1/(Math.pow(getRate(),2));
    }

    public double getSkew() {
        return 2;
    }

    public String toString() {
        return String.format("jline.Exp(%f)", this.getRate());
    }
}
