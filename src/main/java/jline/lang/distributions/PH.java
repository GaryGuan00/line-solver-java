package jline.lang.distributions;

import jline.util.Matrix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PH extends  MarkovianDistribution  implements Serializable  {
    private final int nPhases;
    List<Double> totalPhaseRate;

    public PH(int nPhases, List<Double> startingPhaseProbability, List<List<Double>> phMatrix) {
        super("PH", 1);
        this.setParam(1, "n", nPhases);
        this.setParam(2, "starting_phase", startingPhaseProbability);
        this.setParam(3, "ph_matrix", phMatrix);

        this.totalPhaseRate = new ArrayList<Double>(nPhases);
        this.nPhases = nPhases;

        for (int i = 0; i < nPhases; i++) {
            double tpr = 0.0;
            tpr -= phMatrix.get(i).get(i);
            this.totalPhaseRate.add(tpr);
        }
    }

    public double getTotalPhaseRate(int phase) {
        return this.totalPhaseRate.get(phase);
    }

    public long getNumberOfPhases() {
        return (long) this.getParam(1).getValue();
    }

    @Override
    public boolean isImmediate() {
        return false;
    }

    public double getMean() {
        throw new RuntimeException("Not Implemented!");
    }

    /**
     * Gets n samples from the distribution
     * @param n - the number of samples
     * @return - n samples from the distribution
     */
    @Override
    public List<Double> sample(long n) {
        return this.sample(n,new Random());
    }

    @Override
    public List<Double> sample(long n, Random random) {
        throw new RuntimeException("Not implemented");
    }


    public double getVar() {
        throw new RuntimeException("Not Implemented!");
    }

    public double getSkew() {
        throw new RuntimeException("Not Implemented!");
    }

    public double getSCV() { throw new RuntimeException("Not Implemented!"); }

    public double getRate() {
        throw new RuntimeException("Not Implemented!");
    }

    public double evalCDF(double t) {
        throw new RuntimeException("Not Implemented!");
    }

    public Map<Integer, Matrix> getPH()  {
        throw new RuntimeException("Not Implemented!");
    }
    public double evalLST(double s) {
        throw new RuntimeException("Not Implemented!");
    }
}
