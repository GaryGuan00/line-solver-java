package jline.lang.distributions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jline.lang.*;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;
import jline.util.Interval;
import jline.util.Param;

abstract public class Distribution  implements Serializable  {
    protected double mean;
    protected boolean immediate;

    protected String name;
    protected int numParam;
    protected Interval support;
    protected List<Param> params;

    public final static double zeroRn = 1e-8; // right neighborhood of zero
    public final static double tolerance = 1e-3; // tolerance for distribution fitting
    public final static double infRep = 1e8; // generic representation of infinity
    public final static double infTimeRep = 1e8; // generic representation of infinite time
    public final static double infRateRep = 1e8; // generic representation of an infinite rate

    public abstract List<Double> sample(int n);
    public abstract double getMean();
    public abstract double getRate();
    public abstract double getSCV();
    public abstract double getVar();
    public abstract double getSkew();
    public abstract double evalCDF(double t);
    public abstract double evalLST(double s);

    public Distribution(String name, int numParam, Interval support) {
        this.params = new ArrayList<Param>();

        this.name = name;
        this.numParam = numParam;
        this.support = support;
        for (int i = 0; i < this.numParam; i++) {
            this.params.add(new Param("NULL_PARAM", null));
        }
    }

    public void setParam(int id, String name, Object value) {
        if (id >= this.params.size()) {
            int shortfall = (id - this.params.size());
            for (int i = 0; i < shortfall; i++) {
                this.params.add(new Param("NULL_PARAM", null));
            }
        }
        this.params.set(id-1, new Param(name, value));
    }

    public Param getParam(int id) {
        return this.params.get(id-1);
    }

    public boolean isImmediate() {
        return getMean() < zeroRn;
    }

    public boolean isContinuous() {
        return this instanceof ContinuousDistribution;
    }

    public boolean isDiscrete() {
        return this instanceof DiscreteDistribution;
    }
}
