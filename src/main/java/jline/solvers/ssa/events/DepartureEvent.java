package jline.solvers.ssa.events;

import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.*;
import jline.lang.nodes.ClassSwitch;
import jline.lang.nodes.Node;
import jline.lang.nodes.Source;
import jline.lang.nodes.StatefulNode;
import jline.lang.processes.MAPProcess;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;
import jline.util.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;

public class DepartureEvent extends Event implements NodeEvent {
    protected int statefulIndex;
    protected int classIndex;
    protected boolean useBuffer;
    protected SchedStrategy schedStrategy;
    protected boolean isSource;
    public final Distribution serviceProcess;
    public Node node;
    protected JobClass jobClass;
    protected PhaseEvent phaseEvent;
    protected boolean isMAP;
    protected boolean isReference;
    protected boolean isProcessorSharing;

    public static Event fromNodeAndClass(Node node, JobClass jobClass) {
        if (node instanceof HasSchedStrategy) {
            Distribution serviceDist = ((HasSchedStrategy)node).getServiceProcess(jobClass);
            if (serviceDist instanceof Erlang) {
                DepartureEvent depEvent = new DepartureEvent(node, jobClass);
                ErlangPhaseEvent ePhase = new ErlangPhaseEvent(node, jobClass, depEvent);
                depEvent.setPhaseEvent(ePhase);
                return ePhase;
            } else if (serviceDist instanceof MAPProcess) {
                MAPPhaseEvent mapPhaseEvent = new MAPPhaseEvent(node, jobClass, (MAPProcess) serviceDist);
                return new DepartureEvent(node, jobClass, mapPhaseEvent);
            } else if (serviceDist instanceof  PH) {
                DepartureEvent depEvent = new DepartureEvent(node, jobClass);
                PHPhaseEvent phPhaseEvent = new PHPhaseEvent(node, jobClass, depEvent);
                depEvent.setPhaseEvent(phPhaseEvent);
                return phPhaseEvent;
            } else if (serviceDist instanceof Exp) {
                DepartureEvent depEvent = new DepartureEvent(node, jobClass);
                ExpActiveEvent activeEvent = new ExpActiveEvent(node, jobClass, depEvent);
                depEvent.setPhaseEvent(activeEvent);
                return activeEvent;
            }

        }

        return new DepartureEvent(node, jobClass);
    }

    public DepartureEvent(Node node, JobClass jobClass) {
        super();
        this.node = node;
        this.jobClass = jobClass;


        if (node instanceof StatefulNode) {
            this.statefulIndex = ((StatefulNode)this.node).getStatefulIndex();
        } else {
            this.statefulIndex = -1;
        }

        if (node instanceof Source) {
            this.isReference = true;
        } else if (node instanceof ClassSwitch) {
            this.isReference = true;
        } else if (jobClass instanceof ClosedClass) {
            if (node == ((ClosedClass) jobClass).getRefstat()) {
                this.isReference = true;
            }
        } else {
            this.isReference = false;
        }

        this.classIndex = this.node.getModel().getJobClassIndex(this.jobClass);

        this.isSource = node instanceof Source;
        this.useBuffer = !this.isSource;
        this.schedStrategy = SchedStrategy.FCFS;
        if (node instanceof HasSchedStrategy) {
            this.schedStrategy = ((HasSchedStrategy)node).getSchedStrategy();
            this.serviceProcess = ((HasSchedStrategy)node).getServiceProcess(this.jobClass);
        } else {
            this.serviceProcess = new Immediate();
        }

        this.phaseEvent = null;
        this.isMAP = (this.serviceProcess) instanceof MAPProcess;

        this.isProcessorSharing = this.schedStrategy == SchedStrategy.PS;
    }

    public DepartureEvent(Node node, JobClass jobClass, PhaseEvent phaseEvent) {
        this(node, jobClass);
        this.phaseEvent = phaseEvent;
    }

    public void setPhaseEvent(PhaseEvent phaseEvent) {
        this.phaseEvent = phaseEvent;
    }

    public PhaseEvent getPhaseEvent() {
        return this.phaseEvent;
    }

    @Override
    public double getRate(StateMatrix stateMatrix) {
        if (this.isProcessorSharing) {
            double serviceRatio = (double)stateMatrix.getState(this.statefulIndex, this.classIndex)/(double)stateMatrix.totalStateAtNode(this.statefulIndex);
            serviceRatio *= stateMatrix.psTotalCapacity(this.statefulIndex);
            return this.serviceProcess.getRate()*serviceRatio;
        }


        int activeServers = 1;

        if (this.node instanceof StatefulNode) {
            activeServers = stateMatrix.inProcess(this.statefulIndex, this.classIndex);
            if (this.node instanceof Source) {
                activeServers = 1;
            } else if (activeServers == 0) {
                return Double.NaN;
            }
        }

        if (this.serviceProcess instanceof Immediate) {
            return Double.POSITIVE_INFINITY;
        } else if (this.serviceProcess instanceof Exp) {
            return this.serviceProcess.getRate()*activeServers;
        } else if (this.serviceProcess instanceof DisabledDistribution) {
            return Double.NaN;
        } else if (this.serviceProcess instanceof Erlang) {
            // Rate logic should be handled by PhaseEvent
            return Double.NaN;
        } else if (this.serviceProcess instanceof MAPProcess) {
            //System.out.format("Map phase: %f\n", ((MAPProcess)this.serviceProcess).getDepartureRate(stateMatrix.getGlobalPhase(this.statefulIndex, this.classIndex))*activeServers);
            return ((MAPProcess)this.serviceProcess).getDepartureRate(stateMatrix.getGlobalPhase(this.statefulIndex, this.classIndex))*activeServers;
        } else if (this.serviceProcess instanceof PH) {
            return Double.NaN;
        }

        return Double.NaN;
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        if (this.isMAP) {
            MAPProcess mapProcess = (MAPProcess)(this.serviceProcess);
            int nextPhase = mapProcess.getNextPhaseAfterDeparture(stateMatrix.getGlobalPhase(this.statefulIndex, this.classIndex), random);
            stateMatrix.updateGlobalPhase(this.statefulIndex, this.classIndex, nextPhase);
        }

        if (this.node instanceof Source) {
            if (this.node.getOutputEvent(this.jobClass, random).stateUpdate(stateMatrix, random, timeline)) {
                timeline.record(this, stateMatrix);
                return true;
            }
            return false;
        }

        boolean res = stateMatrix.stateDeparture(this.statefulIndex, classIndex);
        if (!res) {
            return false;
        }

        this.node.getOutputEvent(this.jobClass, random).stateUpdate(stateMatrix, random, timeline);


        timeline.record(this, stateMatrix);

        return true;
    }


    @Override
    public boolean updateStateSpace(StateMatrix stateMatrix, Random random, Timeline timeline, ArrayList<StateMatrix> stateSpace, Queue<StateMatrix> queue) {
        if (this.isMAP) {

            MAPProcess mapProcess = (MAPProcess)(this.serviceProcess);
            int nextPhase = mapProcess.getNextPhaseAfterDeparture(stateMatrix.getGlobalPhase(this.statefulIndex, this.classIndex), random);
            stateMatrix.updateGlobalPhase(this.statefulIndex, this.classIndex, nextPhase);
        }

        if (this.node instanceof Source) {
            ArrayList<Pair<OutputEvent,Double>> eventArrayList = this.node.getOutputEvents(this.jobClass, random);

            for (Pair<OutputEvent,Double> outputEventDoublePair : eventArrayList) {
                StateMatrix newMatrix = outputEventDoublePair.getLeft().getNextState(stateMatrix, timeline, stateSpace,queue);
                if (newMatrix!=null && !newMatrix.checkIfVisited(stateSpace)) {
                    JLineMatrix jLineMatrix = new JLineMatrix(stateMatrix.state.length, stateMatrix.state[0].length);
                    stateSpace.add(newMatrix);
                    queue.add(newMatrix);
                    jLineMatrix.array2DtoJLineMatrix(newMatrix.state).print();
                }
            }

            return true;
        }
        StateMatrix newStateMatrix = new StateMatrix(stateMatrix);
        boolean res = newStateMatrix.stateDeparture(this.statefulIndex, classIndex);
        stateMatrix = newStateMatrix;
        if (!res) {
            return false;
        }

        ArrayList<Pair<OutputEvent,Double>>  eventArrayList = this.node.getOutputEvents(this.jobClass, random);
//        System.out.println(eventArrayList.size());
        for (Pair<OutputEvent,Double> outputEventDoublePair: eventArrayList) {
            StateMatrix newMatrix = outputEventDoublePair.getLeft().getNextState(newStateMatrix, timeline, stateSpace,queue);
            if (!newMatrix.checkIfVisited(stateSpace)) {
                JLineMatrix jLineMatrix = new JLineMatrix(newStateMatrix.state.length, newStateMatrix.state[0].length);
                stateSpace.add(newMatrix);
                queue.add(newMatrix);
                jLineMatrix.array2DtoJLineMatrix(newMatrix.state).print();
                newMatrix.printStateVector();
            }
            stateMatrix = newMatrix;
        }

        timeline.record(this, stateMatrix);

        return true;
    }

    @Override
    public boolean updateEventSpace(StateMatrix stateMatrix, Random random, Timeline timeline, ArrayList<Quartet<Event,Pair<OutputEvent,Double>,StateMatrix,StateMatrix>>  eventSpace,Event event, Queue<StateMatrix> queue,StateMatrix copy) {
        if (this.isMAP) {

            MAPProcess mapProcess = (MAPProcess)(this.serviceProcess);
            int nextPhase = mapProcess.getNextPhaseAfterDeparture(stateMatrix.getGlobalPhase(this.statefulIndex, this.classIndex), random);
            stateMatrix.updateGlobalPhase(this.statefulIndex, this.classIndex, nextPhase);
        }

        if (this.node instanceof Source) {
            ArrayList<Pair<OutputEvent,Double>> eventArrayList = this.node.getOutputEvents(this.jobClass, random);

            for (Pair<OutputEvent,Double> outputEventDoublePair : eventArrayList) {
                StateMatrix newMatrix = outputEventDoublePair.getLeft().getNextEventState(stateMatrix, timeline, eventSpace,event,queue,copy);
                if (newMatrix!=null && !newMatrix.checkIfVisited(eventSpace,copy,event,outputEventDoublePair.getLeft())) {
                    JLineMatrix jLineMatrix = new JLineMatrix(stateMatrix.state.length, stateMatrix.state[0].length);
                    eventSpace.add(Quartet.with(event, outputEventDoublePair,copy,newMatrix));
                    queue.add(newMatrix);
                    jLineMatrix.array2DtoJLineMatrix(newMatrix.state).print();
                }
            }

            return true;
        }
        StateMatrix newStateMatrix = new StateMatrix(stateMatrix);
        boolean res = newStateMatrix.stateDeparture(this.statefulIndex, classIndex);
        StateMatrix copyOfOldStateMatrix = stateMatrix;
        stateMatrix = newStateMatrix;
        if (!res) {
            return false;
        }
        ArrayList<Pair<OutputEvent, Double>> eventArrayList = this.node.getOutputEvents(this.jobClass, random);
        for (Pair<OutputEvent, Double> outputEventDoublePair : eventArrayList) {

            StateMatrix newMatrix = outputEventDoublePair.getLeft().getNextEventState(stateMatrix, timeline, eventSpace,event,queue,copy);
            if (newMatrix!=null && !newMatrix.checkIfVisited(eventSpace,copy,event,outputEventDoublePair.getLeft())) {
                JLineMatrix jLineMatrix = new JLineMatrix(stateMatrix.state.length, stateMatrix.state[0].length);
                eventSpace.add(Quartet.with(event,outputEventDoublePair,copy,newMatrix));
                queue.add(newMatrix);
                jLineMatrix.array2DtoJLineMatrix(newMatrix.state).print();
            }
            stateMatrix = newMatrix;
        }

        timeline.record(this, stateMatrix);

        return true;
    }


    @Override
    public void printSummary() {
        System.out.format("Departure event for %s at %s\n", this.jobClass.getName(), this.node.getName());
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        int res = 0;

        if (this.isMAP) {
            MAPProcess mapProcess = (MAPProcess)(this.serviceProcess);
            int nextPhase = mapProcess.getNextPhaseAfterDeparture(stateMatrix.getGlobalPhase(this.statefulIndex, this.classIndex), random);
            stateMatrix.updateGlobalPhase(this.statefulIndex, this.classIndex, nextPhase);
        }

        if (this.node instanceof Source) {
            res = this.node.getOutputEvent(this.jobClass, random).stateUpdateN(n, stateMatrix, random, timeline);
        } else {
            res = stateMatrix.stateDepartureN(n, this.statefulIndex, classIndex);
            this.node.getOutputEvent(this.jobClass, random).stateUpdateN(n-res, stateMatrix, random, timeline);
        }

        timeline.preRecord(this, stateMatrix, n-res);

        return res;
    }

    @Override
    public int getMaxRepetitions(StateMatrix stateMatrix) {
        if (this.node instanceof Source) {
            return Integer.MAX_VALUE;
        }

        return stateMatrix.getState(this.statefulIndex, this.classIndex);
    }

    public Node getNode() {
        return this.node;
    }

    public JobClass getJobClass() { return this.jobClass; }

    public int getNodeStatefulIdx() {
        return this.statefulIndex;
    }
    public int getClassIdx() {
        return this.classIndex;
    }

    public boolean isStateful() {
        return this.statefulIndex != -1;
    }

    public boolean isReference() {
        return this.isReference;
    }

    @Override
    public StateMatrix getNextState(StateMatrix startingState, Timeline timeline, ArrayList<StateMatrix> stateSpace,Queue<StateMatrix> queue) {

        StateMatrix endingState = new StateMatrix(startingState);

        if(updateStateSpace(endingState, new Random(), timeline, stateSpace,queue)){
            return endingState;
        }

        return null;

    }


    public StateMatrix getNextEventState(StateMatrix startingState, Timeline timeline, ArrayList<Quartet<Event,Pair<OutputEvent,Double>,StateMatrix,StateMatrix>>  eventSpace, Event event, Queue<StateMatrix> queue,StateMatrix copy) {

        StateMatrix endingState = new StateMatrix(startingState);

        if(updateEventSpace(endingState, new Random(), timeline, eventSpace,event,queue,copy)){
            return endingState;
        }

        return null;
    }

}
