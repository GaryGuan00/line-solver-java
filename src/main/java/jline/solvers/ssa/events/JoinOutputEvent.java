package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.OutputStrategy;
import jline.lang.nodes.Node;
import jline.lang.sections.OutputSection;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.SSAStateMatrix;


import java.util.*;

public class JoinOutputEvent extends OutputEvent {
    protected int nSources;
    protected Map<OutputSection, Integer> waitingJobs;
    protected Set<OutputSection> seenSet;
    public JoinOutputEvent(OutputSection outputSection, Node targetNode, JobClass jobClass) {
        super(outputSection, targetNode, jobClass);

        Node corresNode = null;

        for (Node node : targetNode.getModel().getNodes()) {
            if (node.getOutput() == outputSection) {
                corresNode = node;
            }
        }

        if (corresNode == null) {
            return;
        }

        this.waitingJobs = new HashMap<OutputSection, Integer>();
        this.seenSet = new HashSet<OutputSection>();

        for (Node node : targetNode.getModel().getNodes()) {
            for (OutputStrategy os : node.getOutputStrategies()) {
                if (os.getDestination() == corresNode) {
                    this.nSources += 1;
                    this.waitingJobs.put(node.getOutput(), 0);
                    break;
                }
            }
        }
    }
    @Override
    public boolean stateUpdate(SSAStateMatrix networkState, Random random, Timeline timeline) {
        OutputSection os = timeline.getLastOutputEvent().getOutputSection();
        this.seenSet.add(os);
        this.waitingJobs.put(os, this.waitingJobs.get(os) + 1);

        if (this.seenSet.size() == this.nSources) {
            for (OutputSection sOs : this.waitingJobs.keySet()) {
                this.waitingJobs.put(sOs, this.waitingJobs.get(sOs) - 1);
            }

            for (OutputStrategy outputStrategy : this.outputSection.getOutputStrategies()) {
                outputStrategy.getDestination().getArrivalEvent(this.jobClass).stateUpdate(networkState, random, timeline);
            }
        }

        return true;
    }

    @Override
    public int stateUpdateN(int n, SSAStateMatrix networkState, Random random, Timeline timeline) {
        int nUnapplied = 0;
        for (int i = 0; i < n; i++) {
            if (this.stateUpdate(networkState, random, timeline)) {
                nUnapplied += (n-i);
            }
        }
        return nUnapplied;
    }
}