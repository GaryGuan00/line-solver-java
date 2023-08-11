package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.SSAStateMatrix;

import java.util.Random;

public class NodeArrivalEvent extends ArrivalEvent {
    public NodeArrivalEvent(Node node, JobClass jobClass) {
        super(node, jobClass);
    }

    @Override
    public boolean stateUpdate(SSAStateMatrix networkState, Random random, Timeline timeline) {
        OutputEvent nodeOutputEvent = node.getOutputEvent(jobClass, random);
        return nodeOutputEvent.stateUpdate(networkState, random, timeline);
    }
    @Override
    public int stateUpdateN(int n, SSAStateMatrix networkState, Random random, Timeline timeline) {
        OutputEvent nodeOutputEvent = node.getOutputEvent(jobClass, random);
        return nodeOutputEvent.stateUpdateN(n, networkState, random, timeline);
    }
}
