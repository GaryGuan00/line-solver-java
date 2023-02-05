package jline.solvers.ssa;

import jline.lang.HasSchedStrategy;
import jline.lang.JobClass;
import jline.lang.Network;
import jline.lang.constant.DropStrategy;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.*;
import jline.lang.nodes.Delay;
import jline.lang.nodes.Node;
import jline.lang.nodes.StatefulNode;
import jline.lang.nodes.Station;
import jline.lang.processes.MAPProcess;
import jline.solvers.ssa.events.DepartureEvent;
import jline.solvers.ssa.events.Event;
import jline.solvers.ssa.events.EventStack;

import java.util.*;

public class SSAData {
    /*
        Yet another class for handling network information.

        In this case, it's a wrapper for NetworkStruct and EventStack for SolverSSA.
     */
    public EventStack eventStack;
    public SSAStruct simStruct;

    public SSAData(Network network) {
        if (this.simStruct == null) {
            this.simStruct = new SSAStruct();
        }
        this.simStruct.nStateful = network.getNumberOfStatefulNodes();
        List<JobClass> classes = network.getClasses();
        this.simStruct.nClasses = classes.size();

        this.eventStack = new EventStack();

        // find capacities
        this.simStruct.capacities = new int[this.simStruct.nStateful][this.simStruct.nClasses];
        this.simStruct.nodeCapacity = new int[this.simStruct.nStateful];

        this.simStruct.numberOfServers = new int[this.simStruct.nStateful];
        this.simStruct.schedStrategies = new SchedStrategy[this.simStruct.nStateful];
        this.simStruct.isDelay = new boolean[this.simStruct.nStateful];

        this.simStruct.nPhases = new int[this.simStruct.nStateful][this.simStruct.nClasses];
        this.simStruct.startingPhaseProbabilities = new Map[this.simStruct.nStateful];

        // loop through each node and add active events to the eventStack
        ListIterator<Node> nodeIter = network.getNodes().listIterator();
        int nodeIdx = -1;
        while (nodeIter.hasNext()) {
            Node node = nodeIter.next();
            if (!(node instanceof StatefulNode)) {
                continue;
            }

            nodeIdx++;
            Iterator<JobClass> jobClassIter = network.getClasses().listIterator();

            if (node instanceof Delay) {
                this.simStruct.isDelay[nodeIdx] = true;
            } else {
                this.simStruct.isDelay[nodeIdx] = false;
            }

            this.simStruct.startingPhaseProbabilities[nodeIdx] = null;

            while (jobClassIter.hasNext()) {
                JobClass jobClass = jobClassIter.next();
                int jobClassIdx = jobClass.getJobClassIdx();
                if (network.getClassLinks(node, jobClass) == 0) {
                    this.simStruct.capacities[nodeIdx][jobClassIdx] = 0;
                } else {
                    double jobCap = jobClass.getNumberOfJobs();
                    jobCap = Math.min(jobCap, node.getClassCap(jobClass));
                    if ((jobCap == Double.POSITIVE_INFINITY) || (node.getDropStrategy() == DropStrategy.WaitingQueue)) {
                        this.simStruct.capacities[nodeIdx][jobClassIdx] = Integer.MAX_VALUE;
                    } else {
                        this.simStruct.capacities[nodeIdx][jobClassIdx] = (int) jobCap;
                    }
                }
                Event dEvent = DepartureEvent.fromNodeAndClass((StatefulNode) node, jobClass);
                this.eventStack.addEvent(dEvent);
                if (dEvent instanceof DepartureEvent) {
                    if (((DepartureEvent) dEvent).getPhaseEvent() != null) {
                        this.eventStack.addEvent(((DepartureEvent) dEvent).getPhaseEvent());
                    }
                }
                if (node instanceof HasSchedStrategy) {
                    Distribution dist = ((HasSchedStrategy) node).getServiceProcess(jobClass);
                    if (dist instanceof Erlang) {
                        long nPhases = (long) dist.getParam(2).getValue();
                        this.simStruct.nPhases[nodeIdx][jobClassIdx] = (int) nPhases;
                    /*} else if (dist instanceof HyperExp) {
                        this.networkStruct.nPhases[nodeIdx][jobClassIdx] = ((List<Double>) dist.getParam(1).getValue()).size();*/
                    } else if (dist instanceof MAPProcess) {
                        this.simStruct.nPhases[nodeIdx][jobClassIdx] = (int) dist.getParam(1).getValue();
                    } else if (dist instanceof PH) {
                        this.simStruct.nPhases[nodeIdx][jobClassIdx] = (int) dist.getParam(1).getValue();
                        if (this.simStruct.startingPhaseProbabilities[nodeIdx] == null) {
                            this.simStruct.startingPhaseProbabilities[nodeIdx] = new HashMap<Integer, List<Double>>();
                        }
                        this.simStruct.startingPhaseProbabilities[nodeIdx].put(jobClassIdx, (List<Double>)dist.getParam(2).getValue());
                    } else if (dist instanceof Exp) {
                        this.simStruct.nPhases[nodeIdx][jobClassIdx] = 1;
                    }
                }
            }

            double nodeCap = node.getCap();
            if (nodeCap == Double.POSITIVE_INFINITY) {
                this.simStruct.nodeCapacity[nodeIdx] = Integer.MAX_VALUE;
            } else {
                this.simStruct.nodeCapacity[nodeIdx] = (int) nodeCap;
            }
        }


        // update server counts
        for (int i = 0; i < simStruct.nStateful; i++) {
            Node nodeIter2 = network.getNodeByStatefulIndex(i);
            if (nodeIter2 instanceof Station) {
                Station stationIter = (Station) nodeIter2;
                simStruct.numberOfServers[i] = stationIter.getNumberOfServers();
                if (nodeIter2 instanceof HasSchedStrategy) {
                    simStruct.schedStrategies[i] = ((HasSchedStrategy) nodeIter2).getSchedStrategy();
                } else {
                    simStruct.schedStrategies[i] = SchedStrategy.FCFS;
                }
            } else {
                simStruct.numberOfServers[i] = 1;
                simStruct.schedStrategies[i] = SchedStrategy.FCFS;
            }
        }
    }

    public void applyCutoff(SSAOptions ssaOptions, Network network) {
        for (int i = 0; i < this.simStruct.nStateful; i++) {
            Node nodeIter = network.getStatefulNodeFromIndex(i);
            for (int j = 0; j < this.simStruct.nClasses; j++) {
                JobClass jobClassIter = network.getJobClassFromIndex(j);
                double cutoff = Math.min(ssaOptions.cutoffMatrix.get(nodeIter).get(jobClassIter), ssaOptions.cutoff);
                if (cutoff != Double.POSITIVE_INFINITY) {
                    this.simStruct.capacities[i][j] = Math.min(this.simStruct.capacities[i][j], (int) cutoff);
                }
            }
            double nodeCutoff = Math.min(ssaOptions.nodeCutoffMatrix.get(nodeIter), ssaOptions.cutoff);

            if (nodeCutoff != Double.POSITIVE_INFINITY) {
                this.simStruct.nodeCapacity[i] = Math.min(this.simStruct.nodeCapacity[i], (int) nodeCutoff);
            }
        }
    }

    public SSAData(SSAStruct simStruct) {
        this.simStruct = simStruct;
    }
}
