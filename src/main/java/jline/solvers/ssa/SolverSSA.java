package jline.solvers.ssa;

import jline.lang.*;
import jline.lang.constant.SolverType;
import jline.lang.nodes.Node;
import jline.lang.nodes.StatefulNode;
import jline.solvers.NetworkSolver;
import jline.solvers.SolverOptions;
import jline.solvers.SolverResult;
import jline.solvers.ssa.events.DepartureEvent;
import jline.solvers.ssa.events.Event;
import jline.solvers.ssa.events.EventStack;
import jline.solvers.ssa.metrics.Metrics;
import jline.solvers.ssa.state.SSAStateMatrix;
import jline.solvers.ssa.strategies.TauLeapingStateStrategy;
import jline.solvers.ssa.strategies.TauLeapingType;
import jline.util.Matrix;
//import jline.util.JLineAPI;

import java.util.*;

public class SolverSSA extends NetworkSolver {

    protected NetworkStruct sn;
    public Map<Node, Map<JobClass, Double>> cutoffMatrix;
    public Map<Node, Double> nodeCutoffMatrix;
    public double timeout;

    // tau leaping configuration
    public TauLeapingType tauLeapingType;
    public boolean useTauLeap;

    // metrics configurations
    public boolean useMSER5;
    public boolean useR5;
    public int R5value;
    public boolean recordMetricTimeline;
    public double steadyStateTime;
    public boolean disableResTime;
    public boolean disableTransientState;
    public EventStack eventStack;

    public SolverSSA(Network model) {
        this(model, new SolverOptions(SolverType.SSA));
    }

    public SolverSSA(Network model, SolverOptions options) {
            super(model, "SSA", options);

        this.model = model;
        this.sn = null;

        this.disableResTime = false;
        this.timeout = Double.POSITIVE_INFINITY;
        this.cutoffMatrix = new HashMap<Node, Map<JobClass, Double>>();
        this.nodeCutoffMatrix = new HashMap<Node, Double>();
        this.tauLeapingType = null;
        this.useTauLeap = false;
        this.useMSER5 = false;
        this.useR5 = false;
        this.recordMetricTimeline = true;
        this.R5value = 19;
        this.steadyStateTime = -1;
        this.disableTransientState = false;

        this.eventStack = new EventStack();

        // loop through each node and add active events to the eventStack
        ListIterator<Node> nodeIter = model.getNodes().listIterator();
        int nodeIdx = -1;
        while (nodeIter.hasNext()) {
            Node node = nodeIter.next();
            if (!(node instanceof StatefulNode)) {
                continue;
            }

            nodeIdx++;
            Iterator<JobClass> jobClassIter = model.getClasses().listIterator();

            while (jobClassIter.hasNext()) {
                JobClass jobClass = jobClassIter.next();
                int jobClassIdx = jobClass.getJobClassIdx();
//                if (network.getClassLinks(node, jobClass) == 0) {
//                    this.simStruct.classcap[nodeIdx][jobClassIdx] = 0;
//                } else {
//                    double jobCap = jobClass.getNumberOfJobs();
//                    jobCap = Math.min(jobCap, node.getClassCap(jobClass));
//                    if ((jobCap == Double.POSITIVE_INFINITY) || (node.getDropStrategy() == DropStrategy.WaitingQueue)) {
//                        this.simStruct.classcap[nodeIdx][jobClassIdx] = Integer.MAX_VALUE;
//                    } else {
//                        this.simStruct.classcap[nodeIdx][jobClassIdx] = (int) jobCap;
//                    }
//                }
                Event dEvent = DepartureEvent.fromNodeAndClass(node, jobClass);
                this.eventStack.addEvent(dEvent);
                if (dEvent instanceof DepartureEvent) {
                    if (((DepartureEvent) dEvent).getPhaseEvent() != null) {
                        this.eventStack.addEvent(((DepartureEvent) dEvent).getPhaseEvent());
                    }
                }
            }

//            double nodeCap = node.getCap();
//            if (nodeCap == Double.POSITIVE_INFINITY) {
//                this.simStruct.cap[nodeIdx] = Integer.MAX_VALUE;
//            } else {
//                this.simStruct.cap[nodeIdx] = (int) nodeCap;
//            }
        }
    }

    public void runAnalyzer() throws IllegalAccessException {
        Timeline timeline = this.solve();

        this.result = new SolverResult();

        int M = this.sn.nstateful;
        int K = this.sn.nclasses;

        this.result.UN = new Matrix(M,K);
        this.result.QN = new Matrix(M,K);
        this.result.RN = new Matrix(M,K);
        this.result.TN = new Matrix(M,K);
        this.result.XN = new Matrix(1,K);
        this.result.CN = new Matrix(1,K);

        for (int i = 0; i <M; i++) {
            for (int r = 0; r <K; r++) {
                Metrics metrics = timeline.getMetrics(i, r);
                this.result.QN.set(i,r,metrics.getMetricValueByName("Queue Length"));
                this.result.UN.set(i,r,metrics.getMetricValueByName("Utilization"));
                this.result.TN.set(i,r,metrics.getMetricValueByName("Throughput"));
                this.result.RN.set(i,r,metrics.getMetricValueByName("Response Time"));
            }
        }
    }

    public Timeline solve() {
            if (this.sn == null) {
                this.sn = this.model.getStruct(true);
            }

        this.random = new Random(this.options.seed);
        int samplesCollected = 1;
        int maxSamples = options.samples;
        double curTime = options.timespan[0];
        double maxTime = options.timespan[1];

        // Add ClosedClass instances to the reference station
        SSAStateMatrix networkState = new SSAStateMatrix(this.sn, this.random);
        for (JobClass jobClass : this.model.getClasses()) {
            if (jobClass instanceof ClosedClass) {
                int classIdx = this.model.getJobClassIndex(jobClass);
                ClosedClass cClass = (ClosedClass) jobClass;
                int stationIdx = this.model.getStatefulNodeIndex(cClass.getRefstat());
                networkState.setState(stationIdx, classIdx, (int)cClass.getPopulation());
                for (int i = 0; i < cClass.getPopulation(); i++) {
                    networkState.addToBuffer(stationIdx, classIdx);
                }
            }
        }

        Timeline timeline = new Timeline(this.sn);

        if (this.disableResTime) {
            timeline.disableResidenceTime();
        }

        if (this.disableTransientState) {
            timeline.disableTransientState();
        }

        if (this.useMSER5) {
            timeline.useMSER5();
        } else if (this.useR5) {
            timeline.useR5(this.R5value);
        }

        if (!this.recordMetricTimeline) {
            timeline.setMetricRecord(false);
        }

        if (this.useTauLeap) {
            this.eventStack.configureTauLeap(this.tauLeapingType);
            if ((this.tauLeapingType.getStateStrategy() == TauLeapingStateStrategy.TimeWarp) ||
                    (this.tauLeapingType.getStateStrategy() == TauLeapingStateStrategy.TauTimeWarp)) {
                timeline.cacheRecordings();
            }
        }

        double sysTime = 0;
        double startTime = System.currentTimeMillis();

        boolean beforeSState = false;

        // collect samples and update states
        while ((samplesCollected < maxSamples) && (curTime < maxTime) && (sysTime < this.timeout)) {
            beforeSState = curTime < this.steadyStateTime;

            if (this.useTauLeap) {
                curTime = this.eventStack.tauLeapUpdate(networkState, timeline, curTime, random);
            } else {
                curTime = this.eventStack.updateState(networkState, timeline, curTime, random);
            }

            if (beforeSState && (curTime > this.steadyStateTime)) {
                timeline.resetHistory();
            }
            samplesCollected++;
            sysTime = (System.currentTimeMillis() - startTime)/1000.0;

        }

        timeline.taper(curTime);

        return timeline;
    }

    public void enableMSER5() {
        this.useMSER5 = true;
        this.useR5 = false;
    }

    public void enableR5(int k) {
        this.useR5 = true;
        this.useMSER5 = false;
        this.R5value = k;
    }
}
