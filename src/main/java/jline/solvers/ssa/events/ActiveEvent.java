package jline.solvers.ssa.events;

import jline.lang.HasSchedStrategy;
import jline.lang.JobClass;
import jline.lang.OutputStrategy;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Distribution;
import jline.lang.distributions.MarkovianDistribution;
import jline.lang.nodes.Node;
import jline.lang.nodes.Source;
import jline.lang.nodes.StatefulNode;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.SSAStateMatrix;

import java.util.List;
import java.util.Random;

public class ActiveEvent extends PhaseEvent {
    /*
        Combined Phase/Departure Event,

        Eventually to replace both of them.
     */
    private final int sourceIdx;
    private final int destIdx;
    private final int classIndex;
    private final SchedStrategy schedStrategy;
    private final boolean isSource;
    private final List<List<Double>> phMatrix;
    protected Node sourceNode;
    protected Node destNode;
    protected double routingProb;

    private final JobClass jobClass;
    protected boolean isProcessorSharing;
    private final int nPhases;


    public ActiveEvent(Node sourceNode, Node destNode, JobClass jobClass, List<List<Double>> phMatrix) {
        super();

        if (!(sourceNode instanceof HasSchedStrategy)) {
            throw new RuntimeException("Scheduling strategy required");
        }

        this.sourceNode = sourceNode;
        this.destNode = destNode;
        this.jobClass = jobClass;
        this.routingProb = 0;

        for (OutputStrategy outputStrategy : sourceNode.getOutputStrategies()) {
            if (outputStrategy.getDestination() == destNode) {
                this.routingProb = outputStrategy.getProbability();
                break;
            }
        }

        if (this.sourceNode instanceof StatefulNode) {
            this.sourceIdx = ((StatefulNode)this.sourceNode).getStatefulIndex();
        } else {
            this.sourceIdx = -1;
        }

        if (this.destNode instanceof StatefulNode) {
            this.destIdx = ((StatefulNode)this.destNode).getStatefulIndex();
        } else {
            this.destIdx = -1;
        }

        this.classIndex = this.sourceNode.getModel().getJobClassIndex(this.jobClass);

        this.isSource = sourceNode instanceof Source;

        this.schedStrategy = ((HasSchedStrategy)this.sourceNode).getSchedStrategy();
        Distribution distServiceProcess = ((HasSchedStrategy)this.sourceNode).getServiceProcess(this.jobClass);
        if (!(distServiceProcess instanceof MarkovianDistribution)) {
            throw new RuntimeException("MarkovianDistribution distribution required");
        }

        this.phMatrix = phMatrix;
        this.nPhases = phMatrix.size();

        this.isProcessorSharing = this.schedStrategy == SchedStrategy.PS;
    }

    @Override
    public long getNPhases() {
        return this.nPhases;
    }

    @Override
    public double getRate(SSAStateMatrix networkState) {
        double totalRate = 0;
        for (int i = 0; i < this.nPhases; i++) {
            totalRate += -(this.phMatrix.get(i).get(i));
        }

        return totalRate*this.routingProb;
    }

    @Override
    public boolean stateUpdate(SSAStateMatrix networkState, Random random, Timeline timeline) {
        int nInPhase = 1;

        if (this.isProcessorSharing) {
            nInPhase = networkState.getState(this.sourceIdx, this.classIndex);
        } else if (this.isSource) {
            nInPhase = 1;
        } else if (this.sourceNode instanceof StatefulNode) {
            nInPhase = networkState.inProcess(this.sourceIdx, this.classIndex);
            if (nInPhase == 0) {
                return true;
            }
        }

        int referenceJob = random.nextInt(nInPhase);
        int acc = 0;
        int startPhase = -1;

        while (acc < referenceJob) {
            startPhase += 1;
            acc += networkState.getInPhase(this.sourceIdx, this.classIndex, startPhase);
        }

        // get next phase
        double inverseProb = random.nextDouble();
        double totalRate = -this.phMatrix.get(startPhase).get(startPhase);
        double cumProb = 0;
        int nextPhase = -1;

        while (cumProb < inverseProb) {
            nextPhase += 1;
            if (nextPhase == startPhase) {
                nextPhase += 1;
            }
            cumProb += this.phMatrix.get(startPhase).get(nextPhase)/totalRate;
        }

        // update matrix, and check if there's a departure
        if (networkState.updatePhase(this.sourceIdx, this.classIndex, startPhase, nextPhase)) {
            if (!networkState.stateDeparture(this.sourceIdx, this.classIndex)) {
                return false;
            }

            this.destNode.getArrivalEvent(jobClass).stateUpdate(networkState, random, timeline);
            timeline.record(this, networkState);
            return true;
        }

        timeline.record(this, networkState);
        return true;
    }

    @Override
    public int stateUpdateN(int n, SSAStateMatrix networkState, Random random, Timeline timeline) {
        throw new RuntimeException("Not Implemented");
    }

    public Node getNode() {
        return this.sourceNode;
    }

    public int getClassIdx() {
        return this.classIndex;
    }
}
