package jline.solvers.ssa.events;

import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Distribution;
import jline.lang.distributions.Erlang;
import jline.lang.nodes.Node;
import jline.lang.nodes.Source;
import jline.lang.nodes.StatefulNode;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;

import java.util.Random;

public class ErlangPhaseEvent extends PhaseEvent implements NodeEvent {
    private int statefulIndex;
    private int classIndex;
    private SchedStrategy schedStrategy;
    private boolean isSource;
    private final Erlang serviceProcess;
    protected Node node;
    private JobClass jobClass;
    protected boolean isProcessorSharing;

    private DepartureEvent departureEvent;

    public ErlangPhaseEvent(Node node, JobClass jobClass, DepartureEvent departureEvent) {
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
        if (!(distServiceProcess instanceof Erlang)) {
            throw new RuntimeException("Erlang distribution required");
        }

        this.serviceProcess = (Erlang)distServiceProcess;
        this.departureEvent = departureEvent;

        this.isProcessorSharing = this.schedStrategy == SchedStrategy.PS;
    }

    @Override
    public long getNPhases() {
        return this.serviceProcess.getNumberOfPhases();
    }

    @Override
    public double getRate(StateMatrix stateMatrix) {
        int activeServers = 1;

        if (this.isProcessorSharing) {
            double serviceRatio = (double)stateMatrix.getState(this.statefulIndex, this.classIndex)/(double)stateMatrix.totalStateAtNode(this.statefulIndex);
            serviceRatio *= stateMatrix.psTotalCapacity(this.statefulIndex);
            return this.serviceProcess.getRate()*serviceRatio;
        }

        if (this.node instanceof StatefulNode) {
            activeServers = stateMatrix.inProcess(this.statefulIndex, this.classIndex);
            if (this.node instanceof Source) {
                // NOTE: Pay active attention to this part
                activeServers = 1;//stateMatrix.getPhaseListSize(this)+1;
            } else if (activeServers == 0) {
                return Double.NaN;
            }
        }

        return this.serviceProcess.getRate()*activeServers;
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        if (this.node instanceof StatefulNode) {
            if (this.node instanceof Source) {
                if (stateMatrix.incrementPhase(this.statefulIndex, this.classIndex)) {
                    this.departureEvent.stateUpdate(stateMatrix, random, timeline);
                    timeline.record(this, stateMatrix);
                }

                return true;
            } else if (stateMatrix.getState(this.statefulIndex, this.classIndex) == 0) {
                return true;
            }
        }

        if (stateMatrix.incrementPhase(this.statefulIndex, this.classIndex)) {
            this.departureEvent.stateUpdate(stateMatrix, random, timeline);
            timeline.record(this, stateMatrix);
            return true;
        }

        timeline.record(this, stateMatrix);
        return true;
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        if (this.node instanceof StatefulNode) {
            if (this.node instanceof Source) {
                int nDepartures = stateMatrix.incrementPhaseN(n,this.statefulIndex, this.classIndex);
                int nRemDepartures = this.departureEvent.stateUpdateN(nDepartures,stateMatrix, random, timeline);
                timeline.preRecord(this, stateMatrix, nDepartures-nRemDepartures);
                return 0;
            } else if (stateMatrix.getState(this.statefulIndex, this.classIndex) == 0) {
                return 0;
            }
        }

        int nDepartures = stateMatrix.incrementPhaseN(n, this.statefulIndex, this.classIndex);
        int nRemDepartures = this.departureEvent.stateUpdateN(nDepartures, stateMatrix, random, timeline);
        timeline.preRecord(this, stateMatrix, nDepartures-nRemDepartures);
        return nRemDepartures;
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
