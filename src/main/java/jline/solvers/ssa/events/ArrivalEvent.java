package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.lang.nodes.Sink;
import jline.lang.nodes.StatefulNode;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;

import java.io.Serializable;
import java.util.Random;

public class ArrivalEvent extends Event implements NodeEvent, Serializable {
    private int statefulIndex;
    private int classIndex;
    private boolean useBuffer;
    private boolean isStateful;
    protected JobClass jobClass;
    protected Node node;

    public ArrivalEvent(Node node, JobClass jobClass) {
        super();

        this.jobClass = jobClass;
        this.node = node;

        if (node instanceof StatefulNode) {
            this.statefulIndex = ((StatefulNode)node).getStatefulIndex();
            this.isStateful = true;
        } else {
            this.statefulIndex = -1;
            this.isStateful = false;
        }
        this.classIndex = this.node.getModel().getJobClassIndex(this.jobClass);

        this.useBuffer = true;
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        if (this.isStateful) {
            if (!stateMatrix.stateArrival(this.statefulIndex, this.classIndex)) {
                return false;
            }
        } else if (!(this.node instanceof Sink)){
            throw new RuntimeException(String.format("ArrivalEvent at %s not supported!", this.node.getName()));
        }

        timeline.record(this, stateMatrix);

        return true;
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        int res = 0;

        if (this.isStateful) {
            res = stateMatrix.stateArrivalN(n, this.statefulIndex, this.classIndex);
        } else if (!(this.node instanceof Sink)) {
            throw new RuntimeException(String.format("ArrivalEvent at %s not supported!", this.node.getName()));
        }

        timeline.preRecord(this, stateMatrix,n); // NOT n-res to control the buffer.

        return res;
    }

    @Override
    public void printSummary() {
        System.out.format("Arrival event for %s at %s\n", this.jobClass.getName(), this.node.getName());
    }

    public Node getNode() {
        return this.node;
    }

    public JobClass getJobClass() { return this.jobClass; }

    public int getNodeStatefulIdx() {
        return this.statefulIndex;
    }
    public int getClassIdx() {
        return this.classIndex;
    }


    public boolean isStateful() {
        return this.statefulIndex != -1;
    }
}
