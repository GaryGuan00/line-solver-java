package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.lang.sections.StatelessClassSwitcher;
import jline.solvers.ctmc.EventData;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.SSAStateMatrix;
import jline.util.CumulativeDistribution;
import jline.util.Pair;

import java.util.*;

public class ClassSwitchArrivalEvent extends ArrivalEvent {
    private final Map<JobClass, Double> transitions;
    public ClassSwitchArrivalEvent(Node node, JobClass jobClass, StatelessClassSwitcher statelessClassSwitcher) {
        super(node, jobClass);

        this.transitions = new HashMap<>();
        for (JobClass jc : statelessClassSwitcher.getJobClasses()) {
            double transitionProbability = statelessClassSwitcher.applyCsFun(jobClass.getIndex() - 1, jc.getIndex() - 1);
            this.transitions.put(jc, transitionProbability);
        }
    }

    @Override
    public boolean stateUpdate(SSAStateMatrix networkState, Random random, Timeline timeline) {
        CumulativeDistribution<JobClass> transitionCumulativeDistribution = new CumulativeDistribution<JobClass>(random);

        for (JobClass jobClassIter : this.transitions.keySet()) {
            transitionCumulativeDistribution.addElement(jobClassIter, this.transitions.get(jobClassIter));
        }

        timeline.record(this, networkState);

        JobClass outClass = transitionCumulativeDistribution.generate();
        OutputEvent outputEvent = this.node.getOutputEvent(outClass, random);
        timeline.record(outputEvent, networkState);
        return outputEvent.stateUpdate(networkState, random, timeline);
    }

    public boolean updateStateSpace(SSAStateMatrix networkState, Random random, ArrayList<SSAStateMatrix> stateSpace, Queue<SSAStateMatrix> queue, Set<SSAStateMatrix> stateSet) {
        for (JobClass jobClassIter : this.transitions.keySet()) {
            ArrayList<Pair<OutputEvent,Double>> outputEvents = this.node.getOutputEvents(jobClassIter, random);
            for (Pair<OutputEvent,Double> outputEventDoublePair: outputEvents) {
                SSAStateMatrix newNetworkState = new SSAStateMatrix(networkState);
                outputEventDoublePair.getLeft().getNextState(newNetworkState, stateSpace,queue, stateSet);
            }        }
        return true;
    }

    public boolean updateEventSpace(SSAStateMatrix networkState, Random random, ArrayList<EventData> eventSpace, Event event, Queue<SSAStateMatrix> queue, SSAStateMatrix copy, Set<EventData> eventSet, Pair<OutputEvent, Double> outputEventDoublePair) {
        for (JobClass jobClassIter : this.transitions.keySet()) {
            ArrayList<Pair<OutputEvent,Double>> outputEvents = this.node.getOutputEvents(jobClassIter, random);
            for (Pair<OutputEvent,Double> outputEventDoublePair2: outputEvents) {
                SSAStateMatrix newNetworkState = new SSAStateMatrix(networkState);
                Pair<OutputEvent, Double> newOutPutEventPair = new Pair<>(outputEventDoublePair2.getLeft(), outputEventDoublePair.getRight() * outputEventDoublePair2.getRight());
                newOutPutEventPair.getLeft().getNextEventState(newNetworkState, eventSpace, event, queue, copy, eventSet, newOutPutEventPair);
            }
        }
        return true;
    }
    @Override
    public int stateUpdateN(int n, SSAStateMatrix networkState, Random random, Timeline timeline) {
        CumulativeDistribution<JobClass> transitionCumulativeDistribution = new CumulativeDistribution<JobClass>(random);
        Map<JobClass, Integer> transitionCount = new HashMap<JobClass, Integer>();

        for (JobClass jobClassIter : this.transitions.keySet()) {
            transitionCumulativeDistribution.addElement(jobClassIter, this.transitions.get(jobClassIter));
        }

        List<JobClass> jobClasses = this.node.getModel().getClasses();

        for (JobClass jobClass : jobClasses) {
            transitionCount.put(jobClass, 0);
        }

        int res = 0;
        for (int i = 0; i < n; i++) {
            JobClass selectedClass = transitionCumulativeDistribution.generate();
            transitionCount.put(selectedClass, transitionCount.get(selectedClass)+1);
        }

        for (JobClass jobClass : jobClasses) {
            OutputEvent outputEvent = this.node.getOutputEvent(jobClass, random);
            int nSwitched = outputEvent.stateUpdateN(transitionCount.get(jobClass), networkState, random, timeline);
            timeline.preRecord(outputEvent, networkState, nSwitched);
            res += nSwitched;
        }

        timeline.preRecord(this, networkState,n-res);

        return res;
    }
}
