package jline.solvers.ssa.events;

import java.util.Random;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.lang.nodes.StatefulNode;
import jline.lang.processes.MAPProcess;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;

public class MAPPhaseEvent extends PhaseEvent {
    MAPProcess mapProcess;

    private int statefulIndex;
    private int classIndex;

    public MAPPhaseEvent(Node node, JobClass jobClass, MAPProcess mapProcess) {
        super();
        this.mapProcess = mapProcess;

        if (node instanceof StatefulNode) {
            this.statefulIndex = ((StatefulNode)node).getStatefulIndex();
        } else {
            this.statefulIndex = -1;
        }
        this.classIndex = node.getModel().getJobClassIndex(jobClass);
    }

    @Override
    public long getNPhases() {
        return mapProcess.getNumberOfPhases();
    }

    @Override
    public double getRate(StateMatrix stateMatrix) {
        return (long)mapProcess.getTotalPhaseRate(stateMatrix.getGlobalPhase(this.statefulIndex, this.classIndex));
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        timeline.record(this, stateMatrix);

        int nextPhase =  this.mapProcess.getNextPhase(stateMatrix.getGlobalPhase(this.statefulIndex, this.classIndex), random);
        return stateMatrix.updateGlobalPhase(this.statefulIndex, this.classIndex,nextPhase);
    }

    @Override
    public int stateUpdateN(int n,StateMatrix stateMatrix, Random random, Timeline timeline) {
        int res = n;
        for (int i = 0; i < n; i++) {

            int nextPhase =  this.mapProcess.getNextPhase(stateMatrix.getGlobalPhase(this.statefulIndex, this.classIndex), random);
            if (stateMatrix.updateGlobalPhase(this.statefulIndex, this.classIndex,nextPhase)) {
                res--;
            }
        }

        timeline.preRecord(this, stateMatrix, n-res);

        return res;
    }

    public boolean updateGlobalPhase(int classIdx, int newPhase) {
        throw new RuntimeException("Not implemented");
    }
}
