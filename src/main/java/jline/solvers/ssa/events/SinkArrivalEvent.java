package jline.solvers.ssa.events;

import java.util.Random;

import jline.lang.nodes.Node;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;

public class SinkArrivalEvent extends ArrivalEvent{
    public SinkArrivalEvent(Node node) {
        super(node, null);
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        timeline.record(this, stateMatrix);
        return true;
    }
    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        timeline.preRecord(this, stateMatrix, n);
        return 0;
    }
}
