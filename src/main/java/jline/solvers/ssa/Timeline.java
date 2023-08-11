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
import jline.solvers.ssa.state.SSAStateMatrix;
import jline.solvers.ssa.strategies.CutoffStrategy;
import jline.util.Matrix;
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
    protected int nStations;
    protected int nClasses;
    protected Matrix nServers;
    protected SchedStrategy[] schedStrategies;
    protected double maxTime;
    protected double timeCache;
    protected Metric[][][] metrics;
    protected TotalClassMetric[] totalClassMetrics; // total counts for each class
    protected SSAStateMatrix networkState;
    protected List<Pair<Event,Integer>> eventCache; // list of unapplied events, for tau leaping
    protected CutoffStrategy cutoffStrategy;
    protected Map<Event, Integer> eventClassMap; // cache for the class index of each event
    protected Map<Event, Integer> eventNodeMap;  // ^^ but for nodes
    protected double currentTime;
    protected double nextTime;
    protected boolean useMSER5;
    protected boolean useR5;
    protected int R5value;
    protected boolean metricRecord;
    protected boolean cacheRecordings;
    protected boolean recordTransientState;
    protected boolean inferTimes;

    public Timeline(NetworkStruct sn) {
        this.nStateful = sn.nstateful;
        this.nClasses = sn.nclasses;
        this.nServers = sn.nservers;
        this.schedStrategies = new SchedStrategy[nStateful];
        for (int i=0; i<nStateful; i++){
            this.schedStrategies[i] = sn.sched.get(sn.stations.get(i));
        }
        this.eventTimeline = new ArrayList<Event>();
        this.transientState = new ArrayList<List<Integer>[]>();
        this.timeList = new ArrayList<Double>();

        this.useMSER5 = false;
        this.useR5 = false;
        this.R5value = 19;
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
                this.metrics[i][j][0] = new QueueLengthMetric(i,j, (int) this.nServers.get(i), this.metricRecord);
                this.metrics[i][j][1] = new UtilizationMetric(i,j, (int) this.nServers.get(i), this.metricRecord, sn.sched.get(sn.stations.get(i))==SchedStrategy.INF);
                if (!this.inferTimes) {
                    this.metrics[i][j][2] = new ResponseTimeMetric(i, j, (int) this.nServers.get(i), schedStrategies[i], this.metricRecord);
                    this.metrics[i][j][3] = new ResidenceTimeMetric(i, j, (int) this.nServers.get(i), schedStrategies[i], this.metricRecord, totalClassMetrics[j]);
                }
                this.metrics[i][j][4] = new ThroughputMetric(i,j, (int) this.nServers.get(i), this.metricRecord);

                for (int k = 0; k < 5; k++) {
                    if (this.metrics[i][j][k] == null) {
                        continue;
                    }
                    this.metrics[i][j][k].setRecord(this.metricRecord);
                    if (this.useMSER5){
                        this.metrics[i][j][k].configureMSER5();
                    } else if (this.useR5) {
                        this.metrics[i][j][k].configureR5(this.R5value);
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
        this.R5value = k;
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++)  {
                for (int l = 0; l < 5; l++) {
                    this.metrics[i][j][l].configureR5(this.R5value);
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

    public void record(double t, Event e, SSAStateMatrix networkState) {
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
                this.transientState.add(networkState.getStateVectors());
            }
            return;
        }

        if (this.recordTransientState) {
            this.transientState.add(networkState.getStateVectors());
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
                    this.metrics[i][j][k].fromStateMatrix(t, networkState);
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

    public void record(double t, Event e, SSAStateMatrix networkState, int n) {
        //this.eventTimeline.add(e);
        if (this.recordTransientState) {
            this.transientState.add(networkState.getStateVectors());
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


    public void record(Event e, SSAStateMatrix networkState) {
        this.record(this.currentTime, e, networkState);
    }

    public void preRecord(double t, Event e, SSAStateMatrix networkState, int n) {
        if (!this.cacheRecordings) {
            this.timeCache = t;
            this.networkState = networkState;
            this.record(t, e, networkState, n);
            return;
        }
        if (n == 0) {
            this.timeCache = t;
            this.networkState = networkState;
            return;
        }
        if ((this.timeCache != t) && (this.networkState != null)) {
            this.recordCache();
        }
        this.networkState = networkState;
        this.timeCache = t;
        this.eventCache.add(new Pair<Event, Integer>(e,n));
    }


    public void preRecord(Event e, SSAStateMatrix networkState, int n) {
        this.preRecord(this.nextTime, e, networkState, n);
    }

    public void clearCache() {
        //this.eventCache = new ArrayList<Pair<Event,Integer>>();
        this.eventCache.clear();
    }

    public void recordCache() {
        this.currentTime = this.nextTime;
        this.maxTime = currentTime;

        if (!this.cacheRecordings) {
            if (this.networkState == null) {
                return;
            }
            for (int i = 0; i < this.nStateful; i++) {
                for (int j = 0; j < this.nClasses; j++) {
                    for (int k = 0; k < 2; k++) {
                        this.metrics[i][j][k].fromStateMatrix(this.currentTime, this.networkState);
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
                this.transientState.add(networkState.getStateVectors());
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
                    this.metrics[i][j][k].fromStateMatrix(this.currentTime, this.networkState);
                }
            }
        }

        this.clearCache();
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

    public void resetHistory() {
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                for (int k = 0; k < 5; k++) {
                    this.metrics[i][j][k].resetHistory();
                }
            }
        }
    }

    public OutputEvent getLastOutputEvent() {
        for (int i = this.eventTimeline.size()-1; i >= 0; i--) {
            if (this.eventTimeline.get(i) instanceof OutputEvent) {
                return (OutputEvent) this.eventTimeline.get(i);
            }
        }
        return null;
    }

}
