package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.OutputStrategy;
import jline.lang.nodes.Node;
import jline.lang.sections.OutputSection;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;

import java.util.List;
import java.util.Random;

public class ForkOutputEvent extends OutputEvent {
    protected List<OutputStrategy> outputStrategies;
    public ForkOutputEvent(OutputSection outputSection, Node targetNode, JobClass jobClass) {
        super(outputSection, targetNode, jobClass);
        this.outputStrategies = outputSection.getOutputStrategies();
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        for (OutputStrategy outputStrategy : this.outputStrategies) {
            outputStrategy.getDestination().getArrivalEvent(this.jobClass).stateUpdate(stateMatrix, random, timeline);
        }
        timeline.record(this, stateMatrix);
        return true;
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        int res = 0;
        for (OutputStrategy outputStrategy : this.outputStrategies) {
            res += outputStrategy.getDestination().getArrivalEvent(this.jobClass).stateUpdateN(n, stateMatrix, random, timeline);
        }
        timeline.record(this, stateMatrix);
        return res;
    }
}
