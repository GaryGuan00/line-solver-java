package jline.solvers.ssa.events;

import java.util.Random;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.lang.nodes.StatefulNode;
import jline.lang.processes.MAP;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.SSAStateMatrix;

public class MAPPhaseEvent extends PhaseEvent {
    MAP MAP;

    private final int statefulIndex;
    private final int classIndex;

    public MAPPhaseEvent(Node node, JobClass jobClass, MAP MAP) {
        super();
        this.MAP = MAP;

        if (node instanceof StatefulNode) {
            this.statefulIndex = ((StatefulNode)node).getStatefulIndex();
        } else {
            this.statefulIndex = -1;
        }
        this.classIndex = node.getModel().getJobClassIndex(jobClass);
    }

    @Override
    public long getNPhases() {
        return MAP.getNumberOfPhases();
    }

    @Override
    public double getRate(SSAStateMatrix networkState) {
        return (long)MAP.getTotalPhaseRate(networkState.getGlobalPhase(this.statefulIndex, this.classIndex));
    }

    @Override
    public boolean stateUpdate(SSAStateMatrix networkState, Random random, Timeline timeline) {
        timeline.record(this, networkState);

        int nextPhase =  this.MAP.getNextPhase(networkState.getGlobalPhase(this.statefulIndex, this.classIndex), random);
        return networkState.updateGlobalPhase(this.statefulIndex, this.classIndex,nextPhase);
    }

    @Override
    public int stateUpdateN(int n, SSAStateMatrix networkState, Random random, Timeline timeline) {
        int res = n;
        for (int i = 0; i < n; i++) {

            int nextPhase =  this.MAP.getNextPhase(networkState.getGlobalPhase(this.statefulIndex, this.classIndex), random);
            if (networkState.updateGlobalPhase(this.statefulIndex, this.classIndex,nextPhase)) {
                res--;
            }
        }

        timeline.preRecord(this, networkState, n-res);

        return res;
    }

    public boolean updateGlobalPhase(int classIdx, int newPhase) {
        throw new RuntimeException("Not implemented");
    }
}
