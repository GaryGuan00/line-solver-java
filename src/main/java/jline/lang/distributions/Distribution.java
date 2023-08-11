package jline.lang.distributions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jline.lang.constant.GlobalConstants;
import jline.util.Interval;
import jline.util.Param;
import org.apache.commons.lang3.NotImplementedException;

abstract public class Distribution  implements Serializable  {
    protected double mean;
    protected boolean immediate;

    protected String name;
    protected int numParam;
    protected Interval support;
    protected List<Param> params;

    public abstract List<Double> sample(long n);
    public abstract List<Double> sample(long n, Random random);

    public abstract double getMean();
    public abstract double getRate();
    public abstract double getSCV();
    public abstract double getVar();
    public abstract double getSkew();
    public abstract double evalCDF(double t);
    public abstract double evalLST(double s);

    public List<Double> evalPMF(List<Double> t){
        throw new NotImplementedException("evalPMF not implemented");
    }

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
        return getMean() < GlobalConstants.Zero;
    }

    public boolean isContinuous() {
        return this instanceof ContinuousDistribution;
    }

    public boolean isDiscrete() {
        return this instanceof DiscreteDistribution;
    }

    public boolean isDisabled() {return this instanceof Disabled; }

    public String getName() {
        return name;
    }

    public Interval getSupport() {
        return support;
    }
}
