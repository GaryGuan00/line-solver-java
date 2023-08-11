package jline.solvers.ssa.events;

import jline.lang.HasSchedStrategy;
import jline.lang.JobClass;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.APH;
import jline.lang.distributions.Distribution;
import jline.lang.nodes.Node;
import jline.lang.nodes.Source;
import jline.lang.nodes.StatefulNode;
import jline.solvers.ctmc.EventData;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.SSAStateMatrix;
import jline.util.CumulativeDistribution;
import jline.util.Matrix;
import jline.util.Pair;

import java.util.*;

public class APHPhaseEvent extends PhaseEvent implements NodeEvent {

    private final int statefulIndex;
    private final int classIndex;
    private final SchedStrategy schedStrategy;
    private int sourceCurrentState = -1;

    protected Node node;
    private final JobClass jobClass;
    protected boolean isProcessorSharing;
    private final DepartureEvent departureEvent;

    private final APH serviceProcess;

    public APHPhaseEvent(Node node, JobClass jobClass, DepartureEvent departureEvent) {
        super();
        this.node = node;
        this.jobClass = jobClass;

        if (node instanceof StatefulNode) {
            this.statefulIndex = ((StatefulNode)this.node).getStatefulIndex();
        } else {
            this.statefulIndex = -1;
        }
        this.classIndex = this.node.getModel().getJobClassIndex(this.jobClass);

        if (!(node instanceof HasSchedStrategy)) {
            throw new RuntimeException("Scheduling strategy required");
        }

        this.schedStrategy = ((HasSchedStrategy)node).getSchedStrategy();

        Distribution distServiceProcess = ((HasSchedStrategy)node).getServiceProcess(this.jobClass);
        if (!(distServiceProcess instanceof APH)) {
            throw new RuntimeException("APH distribution required");
        }
        this.serviceProcess = (APH)distServiceProcess;


        this.departureEvent = departureEvent;
        this.isProcessorSharing = this.schedStrategy == SchedStrategy.PS;
    }


    @Override
    public long getNPhases() {
        return this.serviceProcess.getNumberOfPhases();
    }


    @Override
    public double getRate(SSAStateMatrix networkState) {
        Matrix T = this.serviceProcess.getSubgenerator();
        if (this.isProcessorSharing) {
            double totalRate = 0;
            for (int i = 0; i < T.getNumRows(); i++) {
                int inPhase = networkState.getInPhase(this.statefulIndex, this.classIndex, i);
                totalRate += inPhase * this.serviceProcess.getTotalPhaseRate(i);
            }

            double serviceRatio = (double) networkState.getState(this.statefulIndex, this.classIndex) / (double) networkState.totalStateAtNode(this.statefulIndex);
            serviceRatio *= networkState.psTotalCapacity(this.statefulIndex);
            if(this.node instanceof Source) {
                if(sourceCurrentState == -1) {
                    totalRate = 0;
                    Matrix alpha = this.serviceProcess.getInitProb();
                    CumulativeDistribution<Integer> startingPhaseCumulativeDistribution = new CumulativeDistribution<Integer>(networkState.getRandom());
                    for (int i = 0; i < alpha.getNumCols(); i++) {
                        startingPhaseCumulativeDistribution.addElement(i, alpha.get(0, i));
                        totalRate += alpha.get(0, i);
                    }
                    startingPhaseCumulativeDistribution.normalize(totalRate);

                    sourceCurrentState = startingPhaseCumulativeDistribution.generate();
                }
                return serviceProcess.getTotalPhaseRate(sourceCurrentState);
            }
            return totalRate * serviceRatio;
        }

        int activeServers = 1;

        if (this.node instanceof StatefulNode) {
            activeServers = networkState.inProcess(this.statefulIndex, this.classIndex);
            if(this.node instanceof Source) {
                if(sourceCurrentState == -1) {
                    double totalRate = 0;
                    Matrix alpha = this.serviceProcess.getInitProb();
                    CumulativeDistribution<Integer> startingPhaseCumulativeDistribution = new CumulativeDistribution<Integer>(networkState.getRandom());
                    for (int i = 0; i < alpha.getNumCols(); i++) {
                        startingPhaseCumulativeDistribution.addElement(i, alpha.get(0, i));
                        totalRate += alpha.get(0, i);
                    }
                    startingPhaseCumulativeDistribution.normalize(totalRate);

                    sourceCurrentState = startingPhaseCumulativeDistribution.generate();
                }
                return serviceProcess.getTotalPhaseRate(sourceCurrentState);
            } else if (activeServers == 0) {
                return Double.NaN;
            }
        }

        double totalRate = 0;

        for (int i = 0; i < T.getNumRows(); i++) {
            int inPhase = networkState.getInPhase(this.statefulIndex, this.classIndex, i);
            totalRate += inPhase * this.serviceProcess.getTotalPhaseRate(i);
        }


        return totalRate;
    }

    @Override
    public boolean stateUpdate(SSAStateMatrix networkState, Random random, Timeline timeline) {
        int nInPhase = 1;
        Matrix T = this.serviceProcess.getSubgenerator();

        if (this.node instanceof StatefulNode) {
            nInPhase = networkState.inProcess(this.statefulIndex, this.classIndex);
            if (this.node instanceof Source) {
                int startingPhase = sourceCurrentState;
                double totalRate;

                CumulativeDistribution<Integer> endingPhaseCumulativeDistribution = new CumulativeDistribution<Integer>(random);
                double departureRate = -T.get(startingPhase, startingPhase);
                totalRate = -T.get(startingPhase, startingPhase);

                for (int i = startingPhase + 1; i < T.getNumRows(); i++) {
                    departureRate -= T.get(startingPhase, i);
                    endingPhaseCumulativeDistribution.addElement(i, T.get(startingPhase, i));
                }
                endingPhaseCumulativeDistribution.addElement(-1, departureRate);
                endingPhaseCumulativeDistribution.normalize(totalRate);

                int endingPhase = endingPhaseCumulativeDistribution.generate();
                sourceCurrentState = endingPhase;
                if(endingPhase == -1) {
                    networkState.updatePhase(this.statefulIndex, this.classIndex,startingPhase, endingPhase);
                    this.departureEvent.stateUpdate(networkState, random, timeline);
                    timeline.record(this, networkState);
                    return true;
                }
                networkState.updatePhase(this.statefulIndex, this.classIndex,startingPhase, endingPhase);
                timeline.record(this, networkState);
                return true;
            } else if (nInPhase == 0) {
                return true;
            }
        }

        CumulativeDistribution<Integer> startingPhaseCumulativeDistribution = new CumulativeDistribution<Integer>(random);
        double totalRate = 0;

        for (int i = 0; i < T.getNumRows(); i++) {
            int inPhase = networkState.getInPhase(this.statefulIndex, this.classIndex, i);
            startingPhaseCumulativeDistribution.addElement(i, inPhase * this.serviceProcess.getTotalPhaseRate(i));
            totalRate += inPhase * this.serviceProcess.getTotalPhaseRate(i);
        }
        startingPhaseCumulativeDistribution.normalize(totalRate);

        int startingPhase = startingPhaseCumulativeDistribution.generate();

        CumulativeDistribution<Integer> endingPhaseCumulativeDistribution = new CumulativeDistribution<Integer>(random);
        double departureRate = -T.get(startingPhase, startingPhase);
        totalRate = -T.get(startingPhase, startingPhase);

        for (int i = startingPhase + 1; i < T.getNumRows(); i++) {
            departureRate -= T.get(startingPhase, i);
            endingPhaseCumulativeDistribution.addElement(i, T.get(startingPhase, i));
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


    public double getDepartureRate(SSAStateMatrix networkState) {
        Matrix T = this.serviceProcess.getSubgenerator();
        double rate = 0.0;
        for(int i = 0; i < T.getNumRows(); i++) {
            int nInPhase = networkState.inProcess(this.statefulIndex, this.classIndex);
            if(nInPhase > 0) {
                double currRate = -T.get(i, i);
                for(int j = i + 1; j < T.getNumCols(); j++) {
                    currRate -= T.get(i, j);
                }
                rate += currRate * networkState.getInPhase(this.statefulIndex, this.classIndex, (int) this.serviceProcess.getParam(1).getValue() - 1);
            }
        }
        return rate;
    }
    @Override
    public boolean updateStateSpace(SSAStateMatrix networkState, Random random, ArrayList<SSAStateMatrix> stateSpace, Queue<SSAStateMatrix> queue, Set<SSAStateMatrix> stateSet) {
        int nInPhase = 1;
        Matrix T = this.serviceProcess.getSubgenerator();
        if (this.node instanceof StatefulNode) {
            nInPhase = networkState.inProcess(this.statefulIndex, this.classIndex);
            if (this.node instanceof Source) {
                int startingPhase = sourceCurrentState;
                Matrix alpha = this.serviceProcess.getInitProb();
                if(networkState.getInPhase(this.statefulIndex, this.classIndex, startingPhase) > 0) {
                    networkState.updatePhase(this.statefulIndex, this.classIndex, startingPhase, -1);
                }
                for (int i = 0; i < T.getNumRows(); i++) {
                    SSAStateMatrix copy = new SSAStateMatrix(networkState);
                    if(alpha.get(0, i) > 0) {
                        copy.updatePhase(this.statefulIndex, this.classIndex, -1, i);
                        if (!stateSet.contains(copy)) {
                            stateSet.add(copy);
                            stateSpace.add(copy);
                            queue.add(copy);
                        }
                    }
                }
                SSAStateMatrix copy = new SSAStateMatrix(networkState);
                this.departureEvent.getNextState(copy, stateSpace, queue, stateSet);
                return true;
            } else if (nInPhase == 0) {
                return true;
            }
        }

        for (int i = 0; i < T.getNumRows(); i++) {
            int inPhase = networkState.getInPhase(this.statefulIndex, this.classIndex, i);
            if(inPhase == 0) {
                continue;
            }
            for (int j = i + 1; j < T.getNumRows(); j++) {
                SSAStateMatrix copy = new SSAStateMatrix(networkState);
                if(T.get(i, j) > 0) {
                    copy.updatePhase(this.statefulIndex, this.classIndex, i, j);
                    if (!copy.exceedsCutoff() && !stateSet.contains(copy)) {
                        stateSet.add(copy);
                        stateSpace.add(copy);
                        queue.add(copy);
                    }
                }
            }
            SSAStateMatrix copy = new SSAStateMatrix(networkState);
            copy.updatePhase(this.statefulIndex, this.classIndex, i, -1);
            this.departureEvent.getNextState(copy, stateSpace, queue, stateSet);
        }
        return true;
    }

    @Override
    public boolean updateEventSpace(SSAStateMatrix networkState, Random random, ArrayList<EventData> eventSpace, Event event, Queue<SSAStateMatrix> queue, SSAStateMatrix copy, Set<EventData> eventSet) {
        int nInPhase = 1;
        Matrix T = this.serviceProcess.getSubgenerator();

        for (int i = 0; i < T.getNumRows(); i++) {
            int inPhase = networkState.getInPhase(this.statefulIndex, this.classIndex, i);
            if(inPhase == 0) {
                continue;
            }
            double totalRate = -T.get(i, i);
            for (int j = i + 1; j < T.getNumRows(); j++) {
                SSAStateMatrix copy2 = new SSAStateMatrix(networkState);
                if(T.get(i, j) > 0) {
                    double rate = T.get(i, j);
                    totalRate -= rate;
                    copy2.updatePhase(this.statefulIndex, this.classIndex, i, j);
                    rate *= inPhase;
                    OutputEvent dummyEvent = new OutputEvent(classIndex);
                    Pair<OutputEvent, Double> pair = new Pair<>(dummyEvent, rate);
                    EventData eventData = new EventData(event, pair, copy, copy2);
                    if (!copy2.exceedsCutoff() && !eventSet.contains(eventData)) {
                        eventSet.add(eventData);
                        eventSpace.add(eventData);
                        queue.add(copy2);
                    }
                }
            }
            if(totalRate > 0){
                SSAStateMatrix copy2 = new SSAStateMatrix(networkState);
                copy2.updatePhase(this.statefulIndex, this.classIndex, i, -1);
                this.departureEvent.getNextEventState(copy2, eventSpace,event, queue,copy, eventSet);
            }
        }
        return true;
    }

    @Override
    public SSAStateMatrix getNextState(SSAStateMatrix startingState, ArrayList<SSAStateMatrix> stateSpace, Queue<SSAStateMatrix> queue, Set<SSAStateMatrix> stateSet) {

        SSAStateMatrix endingState = new SSAStateMatrix(startingState);

        if(updateStateSpace(endingState, new Random(), stateSpace,queue, stateSet)){
            return endingState;
        }

        return null;

    }

    @Override
    public SSAStateMatrix getNextEventState(SSAStateMatrix startingState, ArrayList<EventData> eventSpace, Event event, Queue<SSAStateMatrix> queue, SSAStateMatrix copy, Set<EventData> eventSet) {

        SSAStateMatrix endingState = new SSAStateMatrix(startingState);

        if(updateEventSpace(endingState, new Random(), eventSpace,event,queue,copy, eventSet)){
            return endingState;
        }

        return null;
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

