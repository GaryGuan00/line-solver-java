package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.lang.sections.OutputSection;
import jline.solvers.ctmc.EventData;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.SSAStateMatrix;
import jline.util.Pair;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class OutputEvent extends Event {
    protected OutputSection outputSection;
    public Node node;
    protected JobClass jobClass;
    protected boolean isClassSwitched;
    protected int jobClassIdx;
    private boolean isDummy = false;

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

    //WARNING: THIS IS ONLY USED AS A DUMMY EVENT FOR SELF-LOOPING EVENTS IN CTMC
    public OutputEvent(int jobClassIdx) {
        this.jobClassIdx = jobClassIdx;
        this.isDummy = true;
    }
    @Override
    public boolean stateUpdate(SSAStateMatrix networkState, Random random, Timeline timeline) {
        timeline.record(this, networkState);
        return this.node.getArrivalEvent(this.jobClass).stateUpdate(networkState, random, timeline);
    }

    @Override
    public boolean updateStateSpace(SSAStateMatrix networkState, Random random, ArrayList<SSAStateMatrix> stateSpace, Queue<SSAStateMatrix> queue, Set<SSAStateMatrix> stateSet) {
        return this.node.getArrivalEvent(this.jobClass).updateStateSpace(networkState, random, stateSpace, queue, stateSet);
    }

    public boolean updateEventSpace(SSAStateMatrix networkState, Random random, ArrayList<EventData> eventSpace, Event event, Queue<SSAStateMatrix> queue, SSAStateMatrix copy, Set<EventData> eventSet, Pair<OutputEvent, Double> outputEventDoublePair) {
        return this.node.getArrivalEvent(this.jobClass).updateEventSpace(networkState, random, eventSpace, event, queue, copy, eventSet, outputEventDoublePair) ;
    }


    @Override
    public int stateUpdateN(int n, SSAStateMatrix networkState, Random random, Timeline timeline) {
        timeline.record(n, this, networkState);
        return this.node.getArrivalEvent(this.jobClass).stateUpdateN(n, networkState, random, timeline);
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

    public SSAStateMatrix getNextState(SSAStateMatrix startingState, ArrayList<SSAStateMatrix> stateSpace, Queue<SSAStateMatrix> queue, Set<SSAStateMatrix> stateSet) {

        SSAStateMatrix endingState = new SSAStateMatrix(startingState);

        if(updateStateSpace(endingState, new Random(), stateSpace,queue, stateSet)){
            return endingState;
        }

        return null;

    }

    public SSAStateMatrix getNextEventState(SSAStateMatrix startingState, ArrayList<EventData> eventSpace, Event event, Queue<SSAStateMatrix> queue, SSAStateMatrix copy, Set<EventData> eventSet, Pair<OutputEvent, Double> outputEventDoublePair) {

        SSAStateMatrix endingState = new SSAStateMatrix(startingState);

        if(updateEventSpace(endingState, new Random(), eventSpace,event,queue,copy, eventSet, outputEventDoublePair)){
            return endingState;
        }

        return null;
    }

    @Override
    public Node getNode() {
        return node;
    }

    public boolean isDummy() {
        return this.isDummy;
    }
}
