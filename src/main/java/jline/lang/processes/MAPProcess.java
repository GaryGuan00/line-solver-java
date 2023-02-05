package jline.lang.processes;

import jline.lang.JLineMatrix;
import jline.lang.distributions.MarkovianDistribution;
import jline.util.Cdf;
import jline.util.Interval;
import jline.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MAPProcess extends MarkovianDistribution implements Serializable {
    /*
        Note: "Distribution" is a very inappropriate classification for a MAP process

        However, this is necessary to admit it as an element of a service process. This should be fixed in a future refactor.
     */
    List<Double> totalDepartureRate;
    List<Double> totalPhaseRate;
    private int nPhases;
    public MAPProcess(int nPhases, List<List<Double>> hiddenTransitions, List<List<Double>> visibleTransitions) {
        super("jline.MAPProcess", 1);
        this.setParam(1, "n", (long)nPhases);
        this.setParam(2, "hidden_transitions", hiddenTransitions);
        this.setParam(3, "visible_transitions", visibleTransitions);

        this.totalDepartureRate = new ArrayList<Double>(nPhases);
        this.totalPhaseRate = new ArrayList<Double>(nPhases);

        for (int i = 0; i < nPhases; i++) {
            double tpr = 0.0;
            double tdr = 0.0;
            for (int j = 0; j < nPhases; j++) {
                tdr += visibleTransitions.get(i).get(j);
                if (i == j) {
                    continue;
                }
                tpr += hiddenTransitions.get(i).get(j);
            }
            this.totalPhaseRate.add(tpr);
            this.totalDepartureRate.add(tdr);
        }

        this.nPhases = nPhases;
        throw new RuntimeException("MAP Processes are not yet supported");
    }

    public long getNumberOfPhases() {
        return (long) this.getParam(1).getValue();
    }

    public double getMean() {
        throw new RuntimeException("Not Implemented!");
    }

    public List<Double> sample(int n)  {
        //return exprnd(1/lambda, n, 1);
        throw new RuntimeException("Not Implemented!");
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

    public double getDepartureRate(int phase) {
        return this.totalDepartureRate.get(phase);
    }

    public double getTotalPhaseRate(int phase) {
        return this.totalPhaseRate.get(phase);
    }

    public int getNextPhaseAfterDeparture(int curPhase, Random random) {
        List<List<Double>> phaseRates = (List<List<Double>>)this.getParam(3).getValue();
        List<Double> phaseTransitions = phaseRates.get(curPhase);
        double tdr = this.totalDepartureRate.get(curPhase);

        Cdf<Integer> phaseCdf = new Cdf<Integer>(random);

        for (int i = 0; i < this.nPhases; i++) {
            phaseCdf.addElement(i, phaseTransitions.get(i)/tdr);
        }

        return phaseCdf.generate();
    }

    public int getNextPhase(int curPhase, Random random) {
        List<List<Double>> phaseRates = (List<List<Double>>)this.getParam(2).getValue();
        List<Double> phaseTransitions = phaseRates.get(curPhase);

        Cdf<Integer> phaseCdf = new Cdf<Integer>(random);

        double tpr = this.getTotalPhaseRate(curPhase);

        for (int i = 0; i < this.nPhases; i++) {
            if (i == curPhase) {
                continue;
            }
            phaseCdf.addElement(i, phaseTransitions.get(i)/tpr);
        }

        return phaseCdf.generate();
    }

    public double evalCDF(double t) {
        throw new RuntimeException("Not Implemented!");
    }

    public Map<Integer, JLineMatrix> getPH()  {
        throw new RuntimeException("Not Implemented!");
    }
    public double evalLST(double s) {
        throw new RuntimeException("Not Implemented!");
    }
}
