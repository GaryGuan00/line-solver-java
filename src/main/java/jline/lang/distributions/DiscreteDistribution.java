package jline.lang.distributions;

import java.io.Serializable;

import jline.util.Interval;

public abstract class DiscreteDistribution extends Distribution implements Serializable {
    public DiscreteDistribution(String name, int numParam, Interval support) {
        super(name, numParam, support);
    }
}
