package jline.solvers.ssa.events;

import jline.lang.HasSchedStrategy;
import jline.lang.JobClass;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Distribution;
import jline.lang.distributions.PH;
import jline.lang.nodes.Node;
import jline.lang.nodes.Source;
import jline.lang.nodes.StatefulNode;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.SSAStateMatrix;
import jline.util.CumulativeDistribution;

import java.util.List;
import java.util.Random;

public class PHPhaseEvent extends PhaseEvent implements NodeEvent {
    private final int statefulIndex;
    private final int classIndex;
    private final SchedStrategy schedStrategy;
    private final boolean isSource;

    protected Node node;
    private final JobClass jobClass;
    protected boolean isProcessorSharing;
    protected List<List<Double>> phMatrix;

    private final DepartureEvent departureEvent;


    private final PH serviceProcess;
    @SuppressWarnings("unchecked")
    public PHPhaseEvent(Node node, JobClass jobClass, DepartureEvent departureEvent) {
        super();
        this.node = node;
        this.jobClass = jobClass;

        if (node instanceof StatefulNode) {
            this.statefulIndex = ((StatefulNode)this.node).getStatefulIndex();
        } else {
            this.statefulIndex = -1;
        }
        this.classIndex = this.node.getModel().getJobClassIndex(this.jobClass);

        this.isSource = node instanceof Source;
        if (!(node instanceof HasSchedStrategy)) {
            throw new RuntimeException("Scheduling strategy required");
        }

        this.schedStrategy = ((HasSchedStrategy)node).getSchedStrategy();

        Distribution distServiceProcess = ((HasSchedStrategy)node).getServiceProcess(this.jobClass);
        if (!(distServiceProcess instanceof PH)) {
            throw new RuntimeException("PH distribution required");
        }
        this.serviceProcess = (PH)distServiceProcess;


        this.departureEvent = departureEvent;
        this.isProcessorSharing = this.schedStrategy == SchedStrategy.PS;

        this.phMatrix = (List<List<Double>>)this.serviceProcess.getParam(3).getValue();
    }

    @Override
    public long getNPhases() {
        return this.phMatrix.size();
    }

    @Override
    public double getRate(SSAStateMatrix networkState) {
        if (this.isProcessorSharing) {
            double totalRate = 0;
            for (int i = 0; i < this.phMatrix.size(); i++) {
                int inPhase = networkState.getInPhase(this.statefulIndex, this.classIndex, i);
                totalRate += inPhase * this.serviceProcess.getTotalPhaseRate(i);
            }

            double serviceRatio = (double) networkState.getState(this.statefulIndex, this.classIndex)/(double) networkState.totalStateAtNode(this.statefulIndex);
            serviceRatio *= networkState.psTotalCapacity(this.statefulIndex);
            return totalRate*serviceRatio;
        }

        int activeServers = 1;

        if (this.node instanceof StatefulNode) {
            activeServers = networkState.inProcess(this.statefulIndex, this.classIndex);
            if (this.node instanceof Source) {
                // NOTE: Pay active attention to this part
                activeServers = 1;//stateMatrix.getPhaseListSize(this)+1;
            } else if (activeServers == 0) {
                return Double.NaN;
            }
        }

        double totalRate = 0;

        for (int i = 0; i < this.phMatrix.size(); i++) {
            int inPhase = networkState.getInPhase(this.statefulIndex, this.classIndex, i);
            totalRate += inPhase * this.serviceProcess.getTotalPhaseRate(i);
        }


        return totalRate;
    }

    @Override
    public boolean stateUpdate(SSAStateMatrix networkState, Random random, Timeline timeline) {
        int nInPhase = 1;

        if (this.node instanceof StatefulNode) {
            nInPhase = networkState.inProcess(this.statefulIndex, this.classIndex);
            if (this.node instanceof Source) {
                if (networkState.incrementPhase(this.statefulIndex, this.classIndex)) {
                    this.departureEvent.stateUpdate(networkState, random, timeline);
                    timeline.record(this, networkState);
                }

                return true;
            } else if (nInPhase == 0) {
                return true;
            }
        }

        CumulativeDistribution<Integer> startingPhaseCumulativeDistribution = new CumulativeDistribution<Integer>(random);
        int totalInPhase = 0;

        for (int i = 0; i < this.phMatrix.size(); i++) {
            int inPhase = networkState.getInPhase(this.statefulIndex, this.classIndex, i);
            startingPhaseCumulativeDistribution.addElement(i, inPhase);
            totalInPhase += inPhase;
        }
        startingPhaseCumulativeDistribution.normalize(totalInPhase);

        int startingPhase = startingPhaseCumulativeDistribution.generate();

        CumulativeDistribution<Integer> endingPhaseCumulativeDistribution = new CumulativeDistribution<Integer>(random);
        double departureRate = -this.phMatrix.get(startingPhase).get(startingPhase);
        double totalRate = -this.phMatrix.get(startingPhase).get(startingPhase);

        for (int i = 0; i < this.phMatrix.size(); i++) {
            if (i == startingPhase) {
                continue;
            }

            departureRate -= this.phMatrix.get(startingPhase).get(i);
            endingPhaseCumulativeDistribution.addElement(i, this.phMatrix.get(startingPhase).get(i));
        }
        endingPhaseCumulativeDistribution.addElement(-1, departureRate);
        endingPhaseCumulativeDistribution.normalize(totalRate);

        int endingPhase = endingPhaseCumulativeDistribution.generate();

        if (endingPhase == -1) {
            networkState.updatePhase(this.statefulIndex, this.classIndex,startingPhase, -1);
            this.departureEvent.stateUpdate(networkState, random, timeline);
            timeline.record(this, networkState);
            return true;
        }

        networkState.updatePhase(this.statefulIndex, this.classIndex,startingPhase, endingPhase);

        timeline.record(this, networkState);
        return true;
    }

    @Override
    public int stateUpdateN(int n, SSAStateMatrix networkState, Random random, Timeline timeline) {
        int res = n;
        for (int i = 0; i < n; i++) {
            if (this.stateUpdate(networkState, random, timeline)) {
                res -= 1;
            } else {
                return res;
            }
        }
        return res;
    }

    public Node getNode() {
        return this.node;
    }

    public int getNodeStatefulIdx() {
        return this.statefulIndex;
    }
    public int getClassIdx() {
        return this.classIndex;
    }

    public boolean isStateful() {
        return this.statefulIndex != -1;
    }
}
