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
import jline.solvers.ssa.state.StateMatrix;
import jline.util.Cdf;

import java.util.List;
import java.util.Random;

public class PHPhaseEvent extends PhaseEvent implements NodeEvent {
    private int statefulIndex;
    private int classIndex;
    private SchedStrategy schedStrategy;
    private boolean isSource;

    protected Node node;
    private JobClass jobClass;
    protected boolean isProcessorSharing;
    protected List<List<Double>> phMatrix;

    private DepartureEvent departureEvent;


    private final PH serviceProcess;

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
    public double getRate(StateMatrix stateMatrix) {
        if (this.isProcessorSharing) {
            double totalRate = 0;
            for (int i = 0; i < this.phMatrix.size(); i++) {
                int inPhase = stateMatrix.getInPhase(this.statefulIndex, this.classIndex, i);
                totalRate += inPhase * this.serviceProcess.getTotalPhaseRate(i);
            }

            double serviceRatio = (double)stateMatrix.getState(this.statefulIndex, this.classIndex)/(double)stateMatrix.totalStateAtNode(this.statefulIndex);
            serviceRatio *= stateMatrix.psTotalCapacity(this.statefulIndex);
            return totalRate*serviceRatio;
        }

        int activeServers = 1;

        if (this.node instanceof StatefulNode) {
            activeServers = stateMatrix.inProcess(this.statefulIndex, this.classIndex);
            if (this.node instanceof Source) {
                // NOTE: Pay active attention to this part
                activeServers = 1;//stateMatrix.getPhaseListSize(this)+1;
            } else if (activeServers == 0) {
                return Double.NaN;
            }
        }

        double totalRate = 0;

        for (int i = 0; i < this.phMatrix.size(); i++) {
            int inPhase = stateMatrix.getInPhase(this.statefulIndex, this.classIndex, i);
            totalRate += inPhase * this.serviceProcess.getTotalPhaseRate(i);
        }


        return totalRate;
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        int nInPhase = 1;

        if (this.node instanceof StatefulNode) {
            nInPhase = stateMatrix.inProcess(this.statefulIndex, this.classIndex);
            if (this.node instanceof Source) {
                if (stateMatrix.incrementPhase(this.statefulIndex, this.classIndex)) {
                    this.departureEvent.stateUpdate(stateMatrix, random, timeline);
                    timeline.record(this, stateMatrix);
                }

                return true;
            } else if (nInPhase == 0) {
                return true;
            }
        }

        Cdf<Integer> startingPhaseCdf = new Cdf<Integer>(random);
        int totalInPhase = 0;

        for (int i = 0; i < this.phMatrix.size(); i++) {
            int inPhase = stateMatrix.getInPhase(this.statefulIndex, this.classIndex, i);
            startingPhaseCdf.addElement(i, inPhase);
            totalInPhase += inPhase;
        }
        startingPhaseCdf.normalize(totalInPhase);

        int startingPhase = startingPhaseCdf.generate();

        Cdf<Integer> endingPhaseCdf = new Cdf<Integer>(random);
        double departureRate = -this.phMatrix.get(startingPhase).get(startingPhase);
        double totalRate = -this.phMatrix.get(startingPhase).get(startingPhase);

        for (int i = 0; i < this.phMatrix.size(); i++) {
            if (i == startingPhase) {
                continue;
            }

            departureRate -= this.phMatrix.get(startingPhase).get(i);
            endingPhaseCdf.addElement(i, this.phMatrix.get(startingPhase).get(i));
        }
        endingPhaseCdf.addElement(-1, departureRate);
        endingPhaseCdf.normalize(totalRate);

        int endingPhase = endingPhaseCdf.generate();

        if (endingPhase == -1) {
            stateMatrix.updatePhase(this.statefulIndex, this.classIndex,startingPhase, -1);
            this.departureEvent.stateUpdate(stateMatrix, random, timeline);
            timeline.record(this, stateMatrix);
            return true;
        }

        stateMatrix.updatePhase(this.statefulIndex, this.classIndex,startingPhase, endingPhase);

        timeline.record(this, stateMatrix);
        return true;
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        int res = n;
        for (int i = 0; i < n; i++) {
            if (this.stateUpdate(stateMatrix, random, timeline)) {
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
