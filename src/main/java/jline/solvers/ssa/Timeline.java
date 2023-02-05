package jline.solvers.ssa;

import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.solvers.ssa.events.*;
import jline.solvers.ssa.metrics.Metric;
import jline.solvers.ssa.metrics.Metrics;
import jline.solvers.ssa.metrics.QueueLengthMetric;
import jline.solvers.ssa.metrics.ResidenceTimeMetric;
import jline.solvers.ssa.metrics.ResponseTimeMetric;
import jline.solvers.ssa.metrics.ThroughputMetric;
import jline.solvers.ssa.metrics.TotalClassMetric;
import jline.solvers.ssa.metrics.UtilizationMetric;
import jline.solvers.ssa.state.PhaseList;
import jline.solvers.ssa.state.StateMatrix;
import jline.solvers.ssa.strategies.CutoffStrategy;
import jline.util.Pair;

import java.util.*;

public class Timeline {
    /*
        Maintains a list of all events in the simulation, acts as an interface point for Metric objects,
            handles steady-state
     */
    protected List<List<Integer>[]> transientState;
    protected List<Event> eventTimeline;
    protected List<Double> timeList;
    protected int nStateful;
    protected int nClasses;
    protected int[] nServers;
    protected SchedStrategy[] schedStrategies;
    protected double maxTime;
    protected double timeCache;
    protected Metric[][][] metrics;
    protected TotalClassMetric[] totalClassMetrics; // total counts for each class
    protected StateMatrix stateMatrix;
    protected List<Pair<Event,Integer>> eventCache; // list of unapplied events, for tau leaping
    protected CutoffStrategy cutoffStrategy;
    protected Map<Event, Integer> eventClassMap; // cache for the class index of each event
    protected Map<Event, Integer> eventNodeMap;  // ^^ but for nodes
    protected double currentTime;
    protected double nextTime;
    protected boolean useMSER5;
    protected boolean useR5;
    protected int r5value;
    protected boolean metricRecord;
    protected boolean cacheRecordings;
    protected boolean recordTransientState;
    protected boolean inferTimes;

    public Timeline(SSAStruct networkStruct) {
        this.nStateful = networkStruct.nStateful;
        this.nClasses = networkStruct.nClasses;
        this.nServers = networkStruct.numberOfServers;
        this.schedStrategies = networkStruct.schedStrategies;
        this.eventTimeline = new ArrayList<Event>();
        this.transientState = new ArrayList<List<Integer>[]>();
        this.timeList = new ArrayList<Double>();

        this.useMSER5 = false;
        this.useR5 = false;
        this.r5value = 19;
        this.metricRecord = true;
        this.recordTransientState = true;

        // this.metrics = new Metric[this.nStateful][this.nClasses][5];
        // this.totalClassMetrics = new TotalClassMetric[this.nClasses];

        this.metrics = new Metric[this.nStateful][this.nClasses][5];
        this.totalClassMetrics = new TotalClassMetric[this.nClasses];
        this.cutoffStrategy = cutoffStrategy;
        this.timeCache = 0;
        this.cacheRecordings = false;

        this.currentTime = 0;
        this.nextTime = 0;


        this.inferTimes = false;
        boolean recordMetrics = true;
        if (cutoffStrategy == CutoffStrategy.None) {
            recordMetrics = true;
        }

        for (int i = 0; i < this.nClasses; i++) {
            this.totalClassMetrics[i] = new TotalClassMetric(i);
        }

        // Build all 5 metrics. In the future, it might be desirable to allow configuration of this
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++)  {
                this.metrics[i][j][0] = new QueueLengthMetric(i,j, this.nServers[i], this.metricRecord);
                this.metrics[i][j][1] = new UtilizationMetric(i,j, this.nServers[i], this.metricRecord, networkStruct.isDelay[i]);
                if (!this.inferTimes) {
                    this.metrics[i][j][2] = new ResponseTimeMetric(i, j, this.nServers[i], schedStrategies[i], this.metricRecord);
                    this.metrics[i][j][3] = new ResidenceTimeMetric(i, j, this.nServers[i], schedStrategies[i], this.metricRecord, totalClassMetrics[j]);
                }
                this.metrics[i][j][4] = new ThroughputMetric(i,j, this.nServers[i], this.metricRecord);

                for (int k = 0; k < 5; k++) {
                    if (this.metrics[i][j][k] == null) {
                        continue;
                    }
                    this.metrics[i][j][k].setRecord(this.metricRecord);
                    if (this.useMSER5){
                        this.metrics[i][j][k].configureMSER5();
                    } else if (this.useR5) {
                        this.metrics[i][j][k].configureR5(this.r5value);
                    }
                }
            }
        }
        this.timeList.add(0.0);
        this.maxTime = 0;

        this.eventCache = new ArrayList<Pair<Event,Integer>>(this.nStateful*this.nClasses);
        this.eventClassMap = new HashMap<Event, Integer>();
        this.eventNodeMap = new HashMap<Event, Integer>();
    }

    public void disableResidenceTime() {
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                ((ResidenceTimeMetric)this.metrics[i][j][3]).disable();
            }
        }
    }

    public void cacheRecordings() {
        this.cacheRecordings = true;
    }

    public void useMSER5 () {
        this.useMSER5 = true;
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++)  {
                for (int l = 0; l < 5; l++) {
                    this.metrics[i][j][l].configureMSER5();
                }
            }
        }
    }
    public void useR5(int k) {
        this.useR5 = true;
        this.r5value = k;
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++)  {
                for (int l = 0; l < 5; l++) {
                    this.metrics[i][j][l].configureR5(this.r5value);
                }
            }
        }
    }

    public void setMetricRecord(boolean record) {
        this.metricRecord = record;
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++)  {
                for (int k = 0; k < 5; k++) {
                    this.metrics[i][j][k].setRecord(this.metricRecord);
                }
            }
        }
    }

    public void disableTransientState() {
        this.recordTransientState = false;
    }

    public void setTime(double t) {
        this.currentTime = t;
    }

    public void setNextTime(double t) {
        this.nextTime = t;
    }

    public void record(double t, Event e, StateMatrix stateMatrix) {
        //this.eventTimeline.add(e);
        this.timeList.add(t);
        this.maxTime = t;

        if (e instanceof DepartureEvent) {
            if (((DepartureEvent) e).isReference()) {
                this.totalClassMetrics[((DepartureEvent) e).getClassIdx()].increment();
            }
            NodeEvent ne = (NodeEvent) e;

            if (!this.inferTimes) {
                for (int k = 2; k < 5; k++) {
                    this.metrics[ne.getNodeStatefulIdx()][ne.getClassIdx()][k].fromEvent(t, e);
                }
            }

            return;
        } else if (e instanceof OutputEvent) {
            if (((OutputEvent) e).isClassSwitched()) {
                this.totalClassMetrics[((OutputEvent) e).getClassIdx()].increment();
            }
            return;
        } else if (e instanceof PhaseEvent) {
            if (this.recordTransientState) {
                this.transientState.add(stateMatrix.getStateVectors());
            }
            return;
        }

        if (this.recordTransientState) {
            this.transientState.add(stateMatrix.getStateVectors());
        }
        boolean foundNode = (e instanceof NodeEvent) && ((NodeEvent) e).isStateful();

        if (foundNode) {
            if (this.inferTimes) {
                this.metrics[((NodeEvent) e).getNodeStatefulIdx()][((NodeEvent) e).getClassIdx()][4].fromEvent(t,e);
            } else {
                for (int k = 2; k < 5; k++) {
                    this.metrics[((NodeEvent) e).getNodeStatefulIdx()][((NodeEvent) e).getClassIdx()][k].fromEvent(t,e);
                }
            }
        }

        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                for (int k = 0; k < 2; k++) {
                    this.metrics[i][j][k].fromStateMatrix(t, stateMatrix);
                }
                if (!foundNode) {
                    if (this.inferTimes) {
                        this.metrics[i][j][4].fromEvent(t,e);
                    } else {
                        for (int k = 2; k < 5; k++) {
                            this.metrics[i][j][k].fromEvent(t, e);
                        }
                    }
                }
            }
        }
    }

    public void record(double t, Event e, StateMatrix stateMatrix, int n) {
        //this.eventTimeline.add(e);
        if (this.recordTransientState) {
            this.transientState.add(stateMatrix.getStateVectors());
        }
        this.timeList.add(t);

        if (e instanceof DepartureEvent) {
            if (((DepartureEvent) e).isReference()) {
                this.totalClassMetrics[((DepartureEvent) e).getClassIdx()].increment(n);
            }
            NodeEvent ne = (NodeEvent) e;

            if (!this.inferTimes) {
                for (int k = 2; k < 5; k++) {
                    this.metrics[ne.getNodeStatefulIdx()][ne.getClassIdx()][k].fromEvent(t, e, n);
                }
            }

            return;
        } else if (e instanceof OutputEvent) {
            if (((OutputEvent) e).isClassSwitched()) {
                this.totalClassMetrics[((OutputEvent) e).getClassIdx()].increment(n);
            }
            return;
        } else if (!(e instanceof ArrivalEvent)){
            return;
        } else if (e instanceof PhaseEvent) {
            return;
        }

        NodeEvent ne = (NodeEvent) e;

        if (!this.inferTimes) {
            for (int k = 2; k < 5; k++) {
                this.metrics[ne.getNodeStatefulIdx()][ne.getClassIdx()][k].fromEvent(t, e, n);
            }
        } else {
            this.metrics[ne.getNodeStatefulIdx()][ne.getClassIdx()][4].fromEvent(t,e);
        }
    }


    public void record(Event e, StateMatrix stateMatrix) {
        this.record(this.currentTime, e, stateMatrix);
    }

    public void preRecord(double t, Event e, StateMatrix stateMatrix, int n) {
        if (!this.cacheRecordings) {
            this.timeCache = t;
            this.stateMatrix = stateMatrix;
            this.record(t, e, stateMatrix, n);
            return;
        }
        if (n == 0) {
            this.timeCache = t;
            this.stateMatrix = stateMatrix;
            return;
        }
        if ((this.timeCache != t) && (this.stateMatrix != null)) {
            this.recordCache();
        }
        this.stateMatrix = stateMatrix;
        this.timeCache = t;
        this.eventCache.add(new Pair<Event, Integer>(e,n));
    }


    public void preRecord(Event e, StateMatrix stateMatrix, int n) {
        this.preRecord(this.nextTime, e, stateMatrix, n);
    }

    public void clearCache() {
        //this.eventCache = new ArrayList<Pair<Event,Integer>>();
        this.eventCache.clear();
    }

    public void recordCache() {
        this.currentTime = this.nextTime;
        this.maxTime = currentTime;

        if (!this.cacheRecordings) {
            if (this.stateMatrix == null) {
                return;
            }
            for (int i = 0; i < this.nStateful; i++) {
                for (int j = 0; j < this.nClasses; j++) {
                    for (int k = 0; k < 2; k++) {
                        this.metrics[i][j][k].fromStateMatrix(this.currentTime, this.stateMatrix);
                    }
                }
            }
            return;
        } else if (this.eventCache.isEmpty()) {
            return;
        }

        for (Pair<Event,Integer> ePair : this.eventCache) {
            Event e = ePair.getLeft();
            int n = ePair.getRight();
            double t = this.currentTime;
            if (this.recordTransientState) {
                this.transientState.add(stateMatrix.getStateVectors());
            }

            //this.eventTimeline.add(e);
            this.timeList.add(t);

            if (e instanceof DepartureEvent) {
                if (((DepartureEvent) e).isReference()) {
                    this.totalClassMetrics[((DepartureEvent) e).getClassIdx()].increment(n);
                }
            } else if (e instanceof OutputEvent) {
                if (((OutputEvent) e).isClassSwitched()) {
                    this.totalClassMetrics[((OutputEvent) e).getClassIdx()].increment(n);
                }
                continue;
            } else if (!(e instanceof ArrivalEvent)){
                continue;
            }

            NodeEvent ne = (NodeEvent) e;

            if (!this.inferTimes) {
                for (int k = 2; k < 5; k++) {
                    this.metrics[ne.getNodeStatefulIdx()][ne.getClassIdx()][k].fromEvent(t, e, n);
                }
            } else {
                this.metrics[ne.getNodeStatefulIdx()][ne.getClassIdx()][4].fromEvent(t, e, n);
            }
        }

        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                for (int k = 0; k < 2; k++) {
                    this.metrics[i][j][k].fromStateMatrix(this.currentTime, this.stateMatrix);
                }
            }
        }

        this.clearCache();
    }

    public void printSummary(Network network) {
        Metrics[][] mMetrics = new Metrics[this.nStateful][this.nClasses];
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                mMetrics[i][j] = new Metrics();
                if (this.useMSER5) {
                    mMetrics[i][j].MSER5();
                } else if (this.useR5) {
                    mMetrics[i][j].R5(this.r5value);
                }

                if (!this.metricRecord) {
                    mMetrics[i][j].setRecord(false);
                }
                for (int k = 0; k < 5; k++) {
                    if (this.metrics[i][j][k] == null) {continue;}
                    mMetrics[i][j].addMetric(this.metrics[i][j][k]);
                }
            }
        }
        Metrics.outputSummary(network, mMetrics);
    }

    public void saveSummary(String filename, Network network, List<String> additionallines) {
        Metrics[][] mMetrics = new Metrics[this.nStateful][this.nClasses];
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                mMetrics[i][j] = new Metrics();
                if (this.useMSER5) {
                    mMetrics[i][j].MSER5();
                } else if (this.useR5) {
                    mMetrics[i][j].R5(this.r5value);
                }

                if (!this.metricRecord) {
                    mMetrics[i][j].setRecord(false);
                }

                for (int k = 0; k < 5; k++) {
                    if (mMetrics[i][j] == null) {continue;}
                    mMetrics[i][j].addMetric(this.metrics[i][j][k]);
                }
            }
        }
        Metrics.saveSummary(filename, network, mMetrics, additionallines);
        //Metrics.saveSummary(filename, network, this.metrics, additionallines);
    }

    public String getlineHeader(Network network) {
        Metrics[][] mMetrics = new Metrics[this.nStateful][this.nClasses];
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                mMetrics[i][j] = new Metrics();
                if (this.useMSER5) {
                    mMetrics[i][j].MSER5();
                } else if (this.useR5) {
                    mMetrics[i][j].R5(this.r5value);
                }

                if (!this.metricRecord) {
                    mMetrics[i][j].setRecord(false);
                }

                for (int k = 0; k < 5; k++) {
                    if (mMetrics[i][j] == null) {continue;}
                    mMetrics[i][j].addMetric(this.metrics[i][j][k]);
                }
            }
        }

        return Metrics.getlineHeader(network, mMetrics);
    }

    public String getlineSummary(Network network) {
        Metrics[][] mMetrics = new Metrics[this.nStateful][this.nClasses];
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                mMetrics[i][j] = new Metrics();
                if (this.useMSER5) {
                    mMetrics[i][j].MSER5();
                } else if (this.useR5) {
                    mMetrics[i][j].R5(this.r5value);
                }
                if (!this.metricRecord) {
                    mMetrics[i][j].setRecord(false);
                }

                for (int k = 0; k < 5; k++) {
                    if (mMetrics[i][j] == null) {continue;}
                    mMetrics[i][j].addMetric(this.metrics[i][j][k]);
                }
            }
        }

        return Metrics.getlineSummary(network, mMetrics);
    }


    public List<Double> getlineValues(Network network) {
        Metrics[][] mMetrics = new Metrics[this.nStateful][this.nClasses];
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                mMetrics[i][j] = new Metrics();
                if (this.useMSER5) {
                    mMetrics[i][j].MSER5();
                } else if (this.useR5) {
                    mMetrics[i][j].R5(this.r5value);
                }
                if (!this.metricRecord) {
                    mMetrics[i][j].setRecord(false);
                }
                for (int k = 0; k < 5; k++) {
                    if (mMetrics[i][j] == null) {continue;}
                    mMetrics[i][j].addMetric(this.metrics[i][j][k]);
                }
            }
        }

        return Metrics.getlineValues(network, mMetrics);
    }

    public Metrics getMetrics(int nodeIdx, int classIdx) {
        Metrics mMetrics = new Metrics();
        for (int k = 0; k < 5; k++) {

            if (this.metrics[nodeIdx][classIdx] == null) {continue;}
            mMetrics.addMetric(this.metrics[nodeIdx][classIdx][k]);
        }
        return mMetrics;
        //return this.metrics[nodeIdx][classIdx];
    }

    public void taper(double t) {
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                for (int k = 0; k < 5; k++) {
                    if (this.metrics[i][j][k] == null) {
                        continue;
                    }
                    this.metrics[i][j][k].taper(t);
                }
                //this.metrics[i][j].taper(t);
            }
        }
    }

    public double totalQueueLength() {
        double acc = 0;
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                for (int k = 0; k < 5; k++) {
                    if (this.metrics[i][j][k] instanceof QueueLengthMetric) {
                        acc += ((QueueLengthMetric) this.metrics[i][j][k]).getMetric();
                    }
                }
            }
        }
        return acc;
    }
    public List<Double> allQueueLengths() {
        List<Double> outList = new ArrayList<Double>(this.nStateful*this.nClasses);
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                for (int k = 0; k < 5; k++) {
                    if (this.metrics[i][j][k] instanceof QueueLengthMetric) {
                        double mVal = ((QueueLengthMetric) this.metrics[i][j][k]).getMetric();
                        outList.add(mVal);
                    }
                }
            }
        }
        return outList;
    }
    public boolean isLikelyUnstable() {
        final double unstableThreshold = 0.98;

        for (int i = 0; i < this.nStateful; i++) {
            double totalU = 0;
            for (int j = 0; j < this.nClasses; j++) {
                for (int k = 0; k < 5; k++) {
                    if (this.metrics[i][j][k] instanceof UtilizationMetric) {
                        totalU += ((UtilizationMetric) this.metrics[i][j][k]).getMetric();
                        break;
                    }
                }
            }
            if (totalU > unstableThreshold) {
                return true;
            }
        }
        return false;
    }

    public void resetHistory() {
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                for (int k = 0; k < 5; k++) {
                    this.metrics[i][j][k].resetHistory();
                }
            }
        }
    }

    public void printTransientState(int start, int end) {
        if (!this.recordTransientState) {
            return;
        }
        for (int i = start; i < end; i++) {
            System.out.format("Iteration %d\n", i);
            for (int j = 0; j < this.transientState.get(i).length; j++) {
                System.out.format("Node %d", j);
                System.out.println(Arrays.toString(this.transientState.get(i)[j].toArray()));
            }
        }
    }

    public DepartureEvent getLastDepartureEvent() {
        for (int i = this.eventTimeline.size()-1; i <= 0; i--) {
            if (this.eventTimeline.get(i) instanceof DepartureEvent) {
                return (DepartureEvent) this.eventTimeline.get(i);
            }
        }
        return null;
    }

    public OutputEvent getLastOutputEvent() {
        for (int i = this.eventTimeline.size()-1; i >= 0; i--) {
            if (this.eventTimeline.get(i) instanceof OutputEvent) {
                return (OutputEvent) this.eventTimeline.get(i);
            }
        }
        return null;
    }

    public List<List<Integer>[]> getTransientState() {
        return this.transientState;
    }

    public List<Double> getT() {
        return this.timeList;
    }
}
