package jline.solvers.ssa;

import jline.lang.Network;
import jline.solvers.ssa.strategies.TauLeapingOrderStrategy;
import jline.solvers.ssa.strategies.TauLeapingStateStrategy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Experiment implements Serializable {
    protected List<ExperimentConfiguration> configs;
    private class ExperimentConfiguration implements Serializable {
        public TauLeapingOrderStrategy orderStrategy;
        public TauLeapingStateStrategy stateStrategy;
        public TauLeapingVarType varType;
        public double MAPE;
        public double totalExecutionTime;
        public double totalExpectedExecutionTime;
        public int totalSamples;
        public List<Double> MAPEvalues;

        public ExperimentConfiguration(TauLeapingVarType varType, TauLeapingOrderStrategy orderStrategy, TauLeapingStateStrategy stateStrategy) {
            this.orderStrategy = orderStrategy;
            this.stateStrategy = stateStrategy;
            this.varType = varType;
            this.totalSamples = 0;
            this.MAPEvalues = new ArrayList<Double>();
        }

        public void addSample(double expectedQ, double actQ) {
            if (actQ == 0) {
                return;
            }
            this.MAPE *= this.totalSamples;
            this.MAPE += Math.abs(actQ-expectedQ)/actQ;
            this.totalSamples++;
            this.MAPE /= this.totalSamples;
            this.MAPEvalues.add(Math.abs(actQ-expectedQ)/actQ);
        }

        public double getMAPEStandardDev() {
            double acc = 0;
            for (double mValue : this.MAPEvalues) {
                acc += Math.pow(mValue-this.MAPE, 2);
            }

            return Math.sqrt(acc/this.MAPEvalues.size());
        }

        public void runExperiment(Network network, Timeline expectedTimeline, double expTime) {
            List<Double> expQueues;
            List<Double> actQueues;
            double duration;

            for (int j = 0; j <= 0; j++) {
                SolverSSA solverSSA = new SolverSSA();
                solverSSA.compile(network);
                solverSSA.setOptions().steadyStateTime(1000.0 / network.minRate());
                solverSSA.setOptions().samples(10000000);
                solverSSA.setOptions().setEndTime(10000);
                solverSSA.setOptions().setTimeout(1);
                solverSSA.setOptions().recordMetricTimeline(false);

                TauLeapingType tauLeapingType = new TauLeapingType(this.varType,
                        this.orderStrategy,
                        this.stateStrategy,
                        1.5/network.avgRate());
                System.out.println(tauLeapingType.stringRep());
                solverSSA.setOptions().configureTauLeap(tauLeapingType);

                long startTime = System.nanoTime();
                Timeline timeline = solverSSA.solve();
                long endTime = System.nanoTime();
                duration = (endTime - startTime);

                this.totalExecutionTime += duration;
                this.totalExpectedExecutionTime += expTime;

                if (j==0) {
                    expQueues = expectedTimeline.allQueueLengths();
                    actQueues = timeline.allQueueLengths();
                    for (int i = 0; i < expQueues.size(); i++) {
                        if ((expQueues.get(i) == 0) || Double.isNaN(expQueues.get(i)) || Double.isNaN(actQueues.get(i))) {
                            continue;
                        }
                        this.addSample(expQueues.get(i), actQueues.get(i));
                    }
                }
            }
        }

        public void printSummary() {
            System.out.format("%s | %s | %f | %f | %f | %f\n", this.orderStrategy.toString(),
                    this.stateStrategy.toString(),
                    this.MAPE,
                    this.getMAPEStandardDev(),
                    this.totalExecutionTime,
                    this.totalExecutionTime/this.totalExpectedExecutionTime);
        }

        public String summaryString(String header) {
            return String.format("%s | %s | %s | %f | %f | %f | %f",
                    header,
                    this.orderStrategy.toString(),
                    this.stateStrategy.toString(),
                    this.MAPE,
                    this.getMAPEStandardDev(),
                    this.totalExecutionTime,
                    this.totalExecutionTime/this.totalExpectedExecutionTime);
        }
    }

    public Experiment() {
        List<TauLeapingVarType> varTypeList = new ArrayList<TauLeapingVarType>();
        varTypeList.add(TauLeapingVarType.Poisson);
        //varTypeList.add(TauLeapingVarType.Binomial);
        List<TauLeapingOrderStrategy> orderStrategyList = new ArrayList<TauLeapingOrderStrategy>();
        orderStrategyList.add(TauLeapingOrderStrategy.RandomEventFixed);
        orderStrategyList.add(TauLeapingOrderStrategy.RandomEvent);
        orderStrategyList.add(TauLeapingOrderStrategy.DirectedGraph);
        orderStrategyList.add(TauLeapingOrderStrategy.DirectedCycle);
        List<TauLeapingStateStrategy> stateStrategyList = new ArrayList<TauLeapingStateStrategy>();
        stateStrategyList.add(TauLeapingStateStrategy.Cutoff);
        stateStrategyList.add(TauLeapingStateStrategy.TwoTimes);
        //stateStrategyList.add(TauLeapingStateStrategy.TimeWarp);
        //stateStrategyList.add(TauLeapingStateStrategy.TauTimeWarp);

        configs = new ArrayList<ExperimentConfiguration>();
        for (TauLeapingVarType varType : varTypeList) {
            for (TauLeapingOrderStrategy orderStrategy : orderStrategyList) {
                for (TauLeapingStateStrategy stateStrategy : stateStrategyList) {
                    configs.add(new ExperimentConfiguration(varType, orderStrategy, stateStrategy));
                }
            }
        }
    }

    public boolean runExperiment(Network network) {
        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(network);
        solverSSA.setOptions().steadyStateTime(1000.0/network.minRate());
        solverSSA.setOptions().samples(10000000);
        solverSSA.setOptions().setEndTime(10000);
        solverSSA.setOptions().setTimeout(1);
        solverSSA.setOptions().recordMetricTimeline(false);

        long startTime = System.nanoTime();
        Timeline eTimeline = solverSSA.solve();
        long endTime = System.nanoTime();
        double duration = (endTime - startTime);

        if (eTimeline.isLikelyUnstable()) {
            return false;
        }

        for (ExperimentConfiguration config : this.configs) {
            config.runExperiment(network, eTimeline, duration);
        }

        return true;
    }

    public void printSummary() {
        System.out.println("Order Strat     | State Strat     | Total MAPE    | Total MAPE std   | Total Execution   | Execution Difference");

        for (ExperimentConfiguration config : this.configs) {
            config.printSummary();
        }
    }

    public void serializeModel(Network model) {

    }

    public List<String> summaryString(String header) {
        List<String> outList = new ArrayList<String>();
        for (ExperimentConfiguration config : this.configs) {
            outList.add(config.summaryString(header));
        }

        return outList;
    }

}
