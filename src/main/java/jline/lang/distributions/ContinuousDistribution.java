package jline.lang.distributions;

import java.io.Serializable;

import jline.util.Interval;

abstract public class ContinuousDistribution extends Distribution implements Serializable {
    public ContinuousDistribution(String name, int numParam, Interval support) {
        super(name, numParam, support);
    }
}
