package jline.solvers.ssa.state;

import jline.lang.NetworkStruct;
import jline.lang.constant.SchedStrategy;
import jline.solvers.ssa.events.PhaseEvent;
import jline.util.Pair;

import java.util.*;

public class StateMatrix {
    /*
        In theory, this should be a one-stop point to handle all stateful information about the system.

        The system isn't quite there yet, e.g. some events track stateful info (e.g. JoinOutputEvent).
     */

    // configuration parameters
    protected int[][] capacities; // [node][class]
    protected int[] nodeCapacity; // [node]
    protected int nStateful;
    protected int nClasses;

    // information on the state (jobs at each station, value at other StatefulNode objects, and phases)
    protected int[][] state; // [node][class]
    protected StateCell[] buffers;
    protected Map<PhaseEvent, PhaseList> phaseTracker;

    // caching, for TimeWarp
    protected int[][] stateCache;
    protected StateCell[] bufferCache;

    // used to temporarily allow illegal states, e.g. negative jobs at a station or more jobs than capacity. - REMOVED (MS)
    //protected boolean allowIllegalStates;

    private static int[] defaultNodeCapacity(int nStateful, int nClasses, int[][] capacities) {
        // initialize node capacities
        int[] outArr = new int[nStateful];

        for (int i = 0; i < nStateful; i++) {
            int totalCap = 0;
            for (int j = 0; j < nClasses; j++) {
                totalCap += capacities[i][j];
            }
            outArr[i] = totalCap;
        }

        return outArr;
    }

    public StateMatrix(NetworkStruct networkStruct) {
        this.nStateful = networkStruct.nStateful;
        this.nClasses = networkStruct.nClasses;
        this.capacities = networkStruct.capacities;
        this.nodeCapacity = networkStruct.nodeCapacity;
        //this.allowIllegalStates = false;

        this.state = new int[this.nStateful][this.nClasses];
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                this.state[i][j] = 0;
            }
        }

        this.stateCache = new int[nStateful][nClasses];

        // build StateCell instances according to the scheduling strategy at each node.
        this.buffers = new StateCell[nStateful];
        this.bufferCache = new StateCell[nStateful];
        for (int i = 0; i < nStateful; i++) {
            if ((networkStruct.schedStrategies[i] == SchedStrategy.FCFS) || (networkStruct.schedStrategies[i] == SchedStrategy.EXT)
                || (networkStruct.schedStrategies[i] == SchedStrategy.INF)) {
                this.buffers[i] = new FCFSClassBuffer(nClasses, networkStruct.numberOfServers[i]);
            } else if (networkStruct.schedStrategies[i] == SchedStrategy.LCFS) {
                this.buffers[i] = new LCFSClassBuffer(nClasses, networkStruct.numberOfServers[i]);
            } else if (networkStruct.schedStrategies[i] == SchedStrategy.PS) {
                this.buffers[i] = new ProcessorSharingBuffer(new Random(), networkStruct.numberOfServers[i]);
            } else if (networkStruct.schedStrategies[i] == SchedStrategy.SIRO) {
                this.buffers[i] = new SIROClassBuffer(new Random(), networkStruct.numberOfServers[i]);
            } else {
                System.out.println(networkStruct.schedStrategies[i]);
                throw new RuntimeException("Unsupported Scheduling Strategy");
            }
        }
        this.phaseTracker = new HashMap<PhaseEvent, PhaseList>();
    }

    public StateMatrix(StateMatrix that) {
        this.nStateful = that.nStateful;
        this.nClasses = that.nClasses;
        this.capacities = that.capacities.clone();
        this.state = that.state.clone();
    }

    public void addToBuffer(int nodeIdx, int classIdx) {
        this.buffers[nodeIdx].addToBuffer(classIdx);
    }

    public void addToBuffer(int nodeIdx, int classIdx, int count) {
        this.buffers[nodeIdx].addNToBuffer(classIdx, count);
    }

    public boolean stateArrival(int nodeIdx, int classIdx) {
        // arrive 1 instance of [class] at [node]
        // returns: true if successful, false otherwise
        if (state[nodeIdx][classIdx] == capacities[nodeIdx][classIdx]) {
            return false;
        }
        this.addToBuffer(nodeIdx, classIdx);
        this.state[nodeIdx][classIdx]++;

        return true;
    }

    public int stateArrivalN(int n, int nodeIdx, int classIdx) {
        /*
            Try to arrive n instances of [class] at [node]
            returns: number of UNapplied arrivals (e.g. expect a 0 from this in normal cases).
         */
        int curState = this.state[nodeIdx][classIdx];
        int maxState = this.capacities[nodeIdx][classIdx];

        /*if (this.allowIllegalStates) {
            maxState = Integer.MAX_VALUE;
        }*/

        int nToApply = Math.min(n, maxState - curState);
        int rem = Math.min(n - nToApply, n);

        this.addToBuffer(nodeIdx, classIdx, nToApply);
        this.setState(nodeIdx, classIdx, this.getState(nodeIdx, classIdx) + nToApply);
        return rem;
    }

    public boolean stateDeparture(int nodeIdx, int classIdx) {
        // depart 1 instance of [class] from [node]
        // returns: true if successful departure, false otherwise
        if ((state[nodeIdx][classIdx] == 0)/* && (!this.allowIllegalStates)*/) {
            return false;
        }

        this.buffers[nodeIdx].removeFirstOfClass(classIdx);
        this.state[nodeIdx][classIdx]--;
        return true;
    }

    public int stateDepartureN(int n, int nodeIdx, int classIdx) {
        // depart n instances of [class] from [node]
        // returns: number of UNapplied departures
        int curState = this.state[nodeIdx][classIdx];
        int nToApply = Math.min(curState, n);

        /*if (this.allowIllegalStates) {
            nToApply = n;
        }*/

        this.buffers[nodeIdx].removeNClass(nToApply, classIdx);
        this.state[nodeIdx][classIdx] -= nToApply;


        return n-nToApply;
    }

    public Pair<Map<Integer, Integer>, Integer> stateDepartureN(int n, int nodeIdx) {
        // depart n instances of [class] from [node]
        // returns: <<index of class departed, number departed>, number of unapplied departures>
        int res = n;

        Map<Integer, Integer> classOutputs = new HashMap<Integer, Integer>();
        for (int i = 0; i < this.nClasses; i++) {
            classOutputs.put(i, 0);
        }

        for (int i = 0; i < n; i++) {
            if (this.buffers[nodeIdx].isEmpty()) {
                break;
            }

            int classRemoved = this.buffers[nodeIdx].popFromBuffer();
            classOutputs.put(classRemoved, classOutputs.get(classRemoved) + 1);
            res--;
        }

        return new Pair<Map<Integer, Integer>, Integer>(classOutputs, res);
    }

    public int popFromBuffer(int nodeIdx) {
        // remove one job from the buffer at node, this doesn't necessarily update the state
        return this.buffers[nodeIdx].popFromBuffer();
    }

    public int peakBuffer(int nodeIdk) {
        // find the job class of the "next" job at a node, usually the one in service
        return this.buffers[nodeIdk].peakBuffer();
    }

    public int totalStateAtNode(int nodeIdx) {
        // total jobs at a certain node
        int totalState = 0;
        for (int i = 0; i < nClasses; i++) {
            totalState += this.state[nodeIdx][i];
        }

        return totalState;
    }

    public boolean atCapacity(int nodeIdx, int classIdx) {
        // is this node at capacity? both in terms of class-specific capacities and overall capacity
        return (this.state[nodeIdx][classIdx] >= this.capacities[nodeIdx][classIdx]) ||
                (this.totalStateAtNode(nodeIdx) >= this.nodeCapacity[nodeIdx]);
    }

    public int getCapacity(int nodeIdx, int classIdx) {
        return this.capacities[nodeIdx][classIdx];
    }

    public boolean atEmpty(int nodeIdx, int classIdx) {
        return this.state[nodeIdx][classIdx] == 0;
    }

    public boolean isBufferEmpty(int nodeIdx) {
        return this.buffers[nodeIdx].isEmpty();
    }

    public void incrementState(int nodeIdx, int classIdx) {
        this.state[nodeIdx][classIdx]++;
    }

    public void decrementState(int nodeIdx, int classIdx) {
        this.state[nodeIdx][classIdx]--;
    }

    public int getState(int nodeIdx, int classIdx) {
        return this.state[nodeIdx][classIdx];
    }

    public void setState(int nodeIdx, int classIdx, int state) {
        // mostly used for debugging
        this.state[nodeIdx][classIdx] = state;
    }

    public int inProcess(int nodeIdx, int classIdx) {
        if (atEmpty(nodeIdx, classIdx)) {
            return 0;
        }

        return this.buffers[nodeIdx].getInService(classIdx);
    }

    public int getNInPhase(PhaseEvent phaseEvent) {
        // Total jobs that have a phase value.
        // This might not accurately reflect the true amount,
        // since not all jobs that are in service (which theoretically have a phase) are tracked.
        // Refer to PhaseList for more info
        if (this.phaseTracker.containsKey(phaseEvent)) {
            return this.phaseTracker.get(phaseEvent).getListSize();
        }

        return 0;
    }

    public int getPhase(PhaseEvent phaseEvent) {
        // Find the *scalar* phase of a PhaseEvent
        // ErlangPhase event has multiple phases for each job in service, while MAPPhaseEvent has one phase
        if (!this.phaseTracker.containsKey(phaseEvent)) {
            this.phaseTracker.put(phaseEvent, new PhaseList((int)phaseEvent.getNPhases()));
        }

        return this.phaseTracker.get(phaseEvent).getPhase();
    }

    public boolean phaseUpdate(PhaseEvent phaseEvent, int newPhase) {
        /*
            Signal a phase update
         */
        if (!this.phaseTracker.containsKey(phaseEvent)) {
            this.phaseTracker.put(phaseEvent, new PhaseList((int)phaseEvent.getNPhases()));
        }

        PhaseList phaseList = this.phaseTracker.get(phaseEvent);
        phaseList.setPhase(newPhase);
        return true;
    }

    public boolean phaseUpdate(PhaseEvent phaseEvent, int activeServers, Random random) {
        /*
            Signal a phase update, for ErlangPhaseEvent
         */
        if (!this.phaseTracker.containsKey(phaseEvent)) {
            this.phaseTracker.put(phaseEvent, new PhaseList((int)phaseEvent.getNPhases(), random));
        }
        PhaseList phaseList = this.phaseTracker.get(phaseEvent);
        return phaseList.updatePhase(activeServers);
    }

    public boolean phaseUpdate(PhaseEvent phaseEvent, Random random) {
        /*
            Signal a *scalar* phase update, for MAPPhaseEvent
         */
        if (!this.phaseTracker.containsKey(phaseEvent)) {
            this.phaseTracker.put(phaseEvent, new PhaseList((int)phaseEvent.getNPhases(), random));
        }
        PhaseList phaseList = this.phaseTracker.get(phaseEvent);
        return phaseList.updatePhase();
    }

    public int phaseUpdateN(int n, PhaseEvent phaseEvent, int activeServers, Random random) {
        int nDepartures = 0;
        for (int i = 0; i < n; i++) {
            if (this.phaseUpdate(phaseEvent, activeServers, random)) {
                nDepartures++;
            }
        }

        return nDepartures;
    }
    public int phaseUpdateN(int n, PhaseEvent phaseEvent, Random random) {
        int nDepartures = 0;
        for (int i = 0; i < n; i++) {
            if (this.phaseUpdate(phaseEvent, random)) {
                nDepartures++;
            }
        }

        return nDepartures;
    }

    public int getPhaseListSize(PhaseEvent phaseEvent) {
        if (!this.phaseTracker.containsKey(phaseEvent)) {
            return 0;
        }

        return this.phaseTracker.get(phaseEvent).getListSize();
    }

    /*public void allowIllegalStates() {
        this.allowIllegalStates = true;
    }

    public void forbidIllegalStates() {
        // set allowIllegalStates to false and then clip the state at proper values.
        this.allowIllegalStates = false;

        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                int oldState = this.state[i][j];
                int newState = Math.min(oldState, this.capacities[i][j]);
                int bufferSize = this.buffers[i].getInQueue(j);
                if (bufferSize > newState) {
                    this.buffers[i].removeNClass(bufferSize-newState, j);
                }
                this.state[i][j] = Math.max(newState, 0);
            }
        }
    }*/

    public void cacheState() {
        /*
            Create caches for each StateCell and state
         */
        this.stateCache = new int[this.nStateful][this.nClasses];
        this.bufferCache = new StateCell[this.nStateful];
        for (int i = 0; i < this.nStateful; i++) {
            for (int j = 0; j < this.nClasses; j++) {
                this.stateCache[i][j] = this.state[i][j];
            }
            this.bufferCache[i] = this.buffers[i].createCopy();
        }
    }

    public void revertToCache() {
        this.state = this.stateCache;
        this.buffers = this.bufferCache;
    }
}
