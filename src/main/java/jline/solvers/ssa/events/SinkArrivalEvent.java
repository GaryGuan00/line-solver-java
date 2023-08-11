package jline.solvers.ssa.events;

import java.util.Random;

import jline.lang.nodes.Node;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.SSAStateMatrix;

public class SinkArrivalEvent extends ArrivalEvent{
    public SinkArrivalEvent(Node node) {
        super(node, null);
    }

    @Override
    public boolean stateUpdate(SSAStateMatrix networkState, Random random, Timeline timeline) {
        timeline.record(this, networkState);
        return true;
    }
    @Override
    public int stateUpdateN(int n, SSAStateMatrix networkState, Random random, Timeline timeline) {
        timeline.preRecord(this, networkState, n);
        return 0;
    }
}
