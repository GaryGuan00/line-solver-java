package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.lang.sections.OutputSection;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;
import jline.util.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;

public class OutputEvent extends Event {
    protected OutputSection outputSection;
    public Node node;
    protected JobClass jobClass;
    protected boolean isClassSwitched;
    protected int jobClassIdx;

    public OutputEvent(OutputSection outputSection, Node node, JobClass jobClass) {
        super();
        this.jobClass = jobClass;
        this.node = node;
        this.isClassSwitched = false;
        this.jobClassIdx = jobClass.getJobClassIdx();
    }

    public OutputEvent(OutputSection outputSection, Node node, JobClass jobClass, boolean isClassSwitched) {
        this(outputSection, node, jobClass);
        this.isClassSwitched = isClassSwitched;
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        timeline.record(this, stateMatrix);
        return this.node.getArrivalEvent(this.jobClass).stateUpdate(stateMatrix, random, timeline);
    }

    @Override
    public boolean updateStateSpace(StateMatrix stateMatrix, Random random, Timeline timeline, ArrayList<StateMatrix> stateSpace, Queue<StateMatrix> queue) {
        timeline.record(this, stateMatrix);
        return this.node.getArrivalEvent(this.jobClass).stateUpdate(stateMatrix, random, timeline);
    }

    @Override
    public boolean updateEventSpace(StateMatrix stateMatrix, Random random, Timeline timeline, ArrayList<Quartet<Event, Pair<OutputEvent,Double>,StateMatrix,StateMatrix>>  eventSpace, Event event, Queue<StateMatrix> queue, StateMatrix copy) {
        timeline.record(this, stateMatrix);
        return this.node.getArrivalEvent(this.jobClass).stateUpdate(stateMatrix, random, timeline);
    }


    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        timeline.record(n, this, stateMatrix);
        return this.node.getArrivalEvent(this.jobClass).stateUpdateN(n, stateMatrix, random, timeline);
    }

    public boolean isClassSwitched() {
        return this.isClassSwitched;
    }

    public int getClassIdx() {
        return this.jobClassIdx;
    }

    public OutputSection getOutputSection() {
        return this.outputSection;
    }

    public StateMatrix getNextState(StateMatrix startingState, Timeline timeline, ArrayList<StateMatrix> stateSpace,Queue<StateMatrix> queue) {

        StateMatrix endingState = new StateMatrix(startingState);

        if(updateStateSpace(endingState, new Random(), timeline, stateSpace,queue)){
            return endingState;
        }

        return null;

    }

    public StateMatrix getNextEventState(StateMatrix startingState, Timeline timeline, ArrayList<Quartet<Event,Pair<OutputEvent,Double>,StateMatrix,StateMatrix>>  eventSpace,Event event, Queue<StateMatrix> queue,StateMatrix copy) {

        StateMatrix endingState = new StateMatrix(startingState);

        if(updateEventSpace(endingState, new Random(), timeline, eventSpace,event,queue,copy)){
            return endingState;
        }

        return null;
    }

}
