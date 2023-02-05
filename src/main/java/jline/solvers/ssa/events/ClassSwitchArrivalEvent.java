package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;
import jline.util.Cdf;

import java.util.*;

public class ClassSwitchArrivalEvent extends ArrivalEvent {
    private Map<JobClass, Double> transitions;
    public ClassSwitchArrivalEvent(Node node, JobClass jobClass, Map<JobClass, Map<JobClass, Double>> csMatrix) {
        super(node, jobClass);

        this.transitions = csMatrix.get(jobClass);
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        Cdf<JobClass> transitionCdf = new Cdf<JobClass>(random);

        for (JobClass jobClassIter : this.transitions.keySet()) {
            transitionCdf.addElement(jobClassIter, this.transitions.get(jobClassIter));
        }

        timeline.record(this, stateMatrix);

        JobClass outClass = transitionCdf.generate();
        OutputEvent outputEvent = this.node.getOutputEvent(outClass, random);
        timeline.record(outputEvent, stateMatrix);
        return outputEvent.stateUpdate(stateMatrix, random, timeline);
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        Cdf<JobClass> transitionCdf = new Cdf<JobClass>(random);
        Map<JobClass, Integer> transitionCount = new HashMap<JobClass, Integer>();

        for (JobClass jobClassIter : this.transitions.keySet()) {
            transitionCdf.addElement(jobClassIter, this.transitions.get(jobClassIter));
        }

        List<JobClass> jobClasses = this.node.getModel().getClasses();

        for (JobClass jobClass : jobClasses) {
            transitionCount.put(jobClass, 0);
        }

        int res = 0;
        for (int i = 0; i < n; i++) {
            JobClass selectedClass = transitionCdf.generate();
            transitionCount.put(selectedClass, transitionCount.get(selectedClass)+1);
        }

        for (JobClass jobClass : jobClasses) {
            OutputEvent outputEvent = this.node.getOutputEvent(jobClass, random);
            int nSwitched = outputEvent.stateUpdateN(transitionCount.get(jobClass), stateMatrix, random, timeline);
            timeline.preRecord(outputEvent, stateMatrix, nSwitched);
            res += nSwitched;
        }

        timeline.preRecord(this, stateMatrix,n-res);

        return res;
    }
}
