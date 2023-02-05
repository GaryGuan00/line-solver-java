package jline.solvers.ssa.state;

import jline.lang.NetworkStruct;
import jline.lang.constant.SchedStrategy;
import jline.lang.constant.SchedStrategyType;


import jline.solvers.ssa.SSAStruct;
import jline.solvers.ssa.events.Event;

import jline.solvers.ssa.events.OutputEvent;
import jline.util.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;


import java.lang.reflect.Array;
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
    protected int[][] nPhases;

    // information on the state (jobs at each station, value at other StatefulNode objects, and phases)
    public int[][] state; // [node][class]
    protected StateCell[] buffers;

    // caching, for TimeWarp
    protected int[][] stateCache;
    protected StateCell[] bufferCache;

    // used to temporarily allow illegal states, e.g. negative jobs at a station or more jobs than capacity. - REMOVED (MS)
    //protected boolean allowIllegalStates;
    protected Random random;

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

    public StateMatrix(SSAStruct networkStruct, Random random) {
        this.nStateful = networkStruct.nStateful;
        this.nClasses = networkStruct.nClasses;
        this.capacities = networkStruct.capacities;
        this.nodeCapacity = networkStruct.nodeCapacity;
        this.nPhases = networkStruct.nPhases;
        this.random = random;

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
            PhaseList phaseList = new PhaseList(this.nPhases[i], this.nClasses, this.random);
            if (networkStruct.startingPhaseProbabilities[i] != null) {
                for (int j = 0; j < nClasses; j++) {
                    if (networkStruct.startingPhaseProbabilities[i].containsKey(j)) {
                        phaseList.setPhaseStart(j, networkStruct.startingPhaseProbabilities[i].get(j));
                    }
                }
            }
            if (networkStruct.schedStrategies[i] == SchedStrategy.FCFS) {
                this.buffers[i] = new FCFSClassBuffer(nClasses, networkStruct.numberOfServers[i], phaseList);
            } else if(networkStruct.schedStrategies[i] == SchedStrategy.INF) {
                this.buffers[i] = new INFClassBuffer(this.random, nClasses, phaseList);
            } else if (networkStruct.schedStrategies[i] == SchedStrategy.EXT) {
                this.buffers[i] = new SourceBuffer(nClasses, phaseList);
            } else if (networkStruct.schedStrategies[i] == SchedStrategy.LCFS) {
                    this.buffers[i] = new LCFSNonPreBuffer(nClasses, networkStruct.numberOfServers[i], phaseList);
            } else if (networkStruct.schedStrategies[i] == SchedStrategy.LCFSPR) {
                this.buffers[i] = new LCFSPreBuffer(nClasses, networkStruct.numberOfServers[i], phaseList);
            } else if (networkStruct.schedStrategies[i] == SchedStrategy.PS) {
                this.buffers[i] = new ProcessorSharingBuffer(this.random, nClasses, networkStruct.numberOfServers[i], phaseList);
            } else if (networkStruct.schedStrategies[i] == SchedStrategy.SIRO) {
                this.buffers[i] = new SIROClassBuffer(this.random, nClasses, networkStruct.numberOfServers[i], phaseList, false);
            /*} else if (networkStruct.schedStrategies[i] == SchedStrategy.SIROPR) {
                this.buffers[i] = new SIROClassBuffer(this.random, nClasses, networkStruct.numberOfServers[i], phaseList, true);*/
            } else {
                System.out.println(networkStruct.schedStrategies[i]);
                throw new RuntimeException("Unsupported Scheduling Strategy");
            }
        }
    }

    public StateMatrix(StateMatrix that) {
        this.nStateful = that.nStateful;
        this.nClasses = that.nClasses;
        this.capacities = that.capacities.clone();
        this.state = new int[that.state.length][that.state[0].length];
        for(int i=0;i<state.length;i++){
            for (int j=0;j<state[0].length;j++){
                this.state[i][j] = that.state[i][j];
            }
        }

        this.nPhases = that.nPhases;
        this.random = that.random;
        this.buffers = new StateCell[that.nStateful];
        for (int i = 0 ; i < that.nStateful; i++) {
            this.buffers[i] = that.buffers[i].createCopy();
        }
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
        if (state[nodeIdx][classIdx] >= capacities[nodeIdx][classIdx]) {
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

        this.buffers[nodeIdx].removeNClass(nToApply, classIdx);
        this.state[nodeIdx][classIdx] -= nToApply;

        return n-nToApply;
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

    public int psTotalCapacity(int nodeIdx) {
        return ((ProcessorSharingBuffer)this.buffers[nodeIdx]).getTotalCapacity();
    }

    public boolean incrementPhase(int nodeIdx, int classIdx) {
        /*
            Signal a class-specific phase update
         */
        return this.buffers[nodeIdx].incrementPhase(classIdx);
    }

    public int incrementPhaseN(int n, int nodeIdx, int classIdx) {
        return this.buffers[nodeIdx].incrementPhaseN(n, classIdx);
    }

    public boolean updatePhase (int nodeIdx, int classIdx, int startingPhase, int endingPhase) {
        return this.buffers[nodeIdx].updatePhase(classIdx, startingPhase, endingPhase);
    }

    public boolean updateGlobalPhase(int nodeIdx, int classIdx, int newPhase) {
        /*
            Signal a global phase update
         */
        return this.buffers[nodeIdx].updateGlobalPhase(classIdx, newPhase);
    }

    public int getGlobalPhase(int nodeIdx, int classIdx) {
        return this.buffers[nodeIdx].getGlobalPhase(classIdx);
    }


    public int getInPhase(int nodeIdx, int classIdx, int phase) {
        return this.buffers[nodeIdx].getPhaseList().getNInPhase(classIdx, phase);
    }


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

    public List<Integer>[] getStateVectors() {
        /*
            Return state vectors for transient analysis:

            Ext: [Inf, s11, ... S1K,...sR1, ...SRk]
            FCFS, HOL, LCFS: [cb,...c1, s11, ... S1K,...sR1, ...SRk]
         */
        List<Integer>[] outList = new List[this.nStateful];

        for (int i = 0; i < this.nStateful; i++) {
            outList[i] = this.buffers[i].stateVector();
        }

        return outList;
    }

    public int[][] copy(){
        return this.state.clone();
    }

    public boolean checkIfVisited(ArrayList<StateMatrix> stateSpace){
        List<Integer>[] newState = getStateVectors();

        boolean checkIfPresent = false;
        for (StateMatrix stateMatrix : stateSpace) {
            boolean ifMatches = true;
            List<Integer>[] visitedState = stateMatrix.getStateVectors();
            for (int j = 0; j < visitedState.length; j++) {
                if(newState[j].size()!=visitedState[j].size()){
                    ifMatches=false;
                    break;
                }
                for (int k = 0; k < visitedState[j].size(); k++) {
                    if (!Objects.equals(newState[j].get(k), visitedState[j].get(k))) {
                        ifMatches = false;
                    }
                }
            }
            if (ifMatches) {
                checkIfPresent = true;
                break;
            }
        }
        return  checkIfPresent;
    }

    public boolean checkIfVisited(ArrayList<Quartet<Event, Pair<OutputEvent,Double>,StateMatrix,StateMatrix>>  eventSpace, StateMatrix oldState, Event event, OutputEvent outputEvent){
        List<Integer>[] newState = getStateVectors();
        List<Integer>[] oldStateVector = oldState.getStateVectors();

        boolean checkIfPresent = false;

        for (Quartet<Event, Pair<OutputEvent,Double>,StateMatrix,StateMatrix> eventMatrix : eventSpace) {
            boolean ifMatches = true;
            if(eventMatrix.getValue3()!=null) {
                for (int j = 0; j < eventMatrix.getValue3().getStateVectors().length; j++) {
                    if (newState[j].size() != eventMatrix.getValue3().getStateVectors()[j].size()) {
                        ifMatches = false;
                        break;
                    }
                    for (int k = 0; k < eventMatrix.getValue3().getStateVectors()[j].size(); k++) {
                        if (!Objects.equals(newState[j].get(k), eventMatrix.getValue3().getStateVectors()[j].get(k))) {
                            ifMatches = false;
                        }
                    }
                }
            }else{
                ifMatches=false;
            }
            if(eventMatrix.getValue2()!=null) {
                for (int j = 0; j < eventMatrix.getValue2().getStateVectors().length; j++) {
                    if(oldStateVector[j].size()!=eventMatrix.getValue2().getStateVectors()[j].size()){
                        ifMatches=false;
                        break;
                    }
                    for (int k = 0; k < eventMatrix.getValue2().getStateVectors()[j].size(); k++) {
                        if (!Objects.equals(oldStateVector[j].get(k), eventMatrix.getValue2().getStateVectors()[j].get(k))) {
                            ifMatches = false;
                        }
                    }
                }
            }else{
                ifMatches=false;
            }
            if (ifMatches && event==eventMatrix.getValue0() && outputEvent==eventMatrix.getValue1().getLeft()) {
                checkIfPresent = true;
            }
        }
        return  checkIfPresent;
    }

    public void stateChangePrint(StateMatrix that){
        List<Integer>[] newState = that.getStateVectors();
        List<Integer>[] oldStateVector = this.getStateVectors();
        assert newState.length==oldStateVector.length;
        for(int i = 0; i < oldStateVector.length;i++){
            for(int j=0; j < oldStateVector[i].size();j++){
                System.out.print(oldStateVector[i].get(j)+"  ");
            }
            System.out.print("\t\t");
            for(int j=0; j < newState[i].size();j++){
                System.out.print(newState[i].get(j)+"  ");
            }
            System.out.println();
        }
    }

    public static boolean sameState(List<Integer>[] state1, List<Integer>[] state2){
        assert state1.length==state2.length;
        for(int i=0;i<state1.length;i++){
            if(state1[i].size()!=state2[i].size()){
                return false;
            }
            for(int j = 0;j<state1[i].size();j++){
                if(state1[i].get(j)!=state2[i].get(j)){
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean multipleEventSameState(Quartet<Event, OutputEvent,StateMatrix,StateMatrix> quartet1, Quartet<Event, OutputEvent,StateMatrix,StateMatrix> quartet2){
        List<Integer>[] q1s1 = quartet1.getValue2().getStateVectors();
        List<Integer>[] q1s2 = quartet1.getValue3().getStateVectors();
        List<Integer>[] q2s1 = quartet2.getValue2().getStateVectors();
        List<Integer>[] q2s2 = quartet2.getValue3().getStateVectors();
        if(sameState(q1s1,q2s1)&&sameState(q1s2,q2s2)){
            return true;
        }else{
            return false;
        }
    }

//    public StateMatrix stateToStateMatrix(int[][] state){
//        StateMatrix stateMatrix = new StateMatrix(this);
//
//        for(int i = 0; i< this.state.length; i++){
//            for(int j=0;j<this.state[0].length;j++){
//                stateMatrix.state[i][j] = state[i][j];
//            }
//        }
//
//        return stateMatrix;
//    }

    public void printStateVector(){
        List<Integer>[] state = this.getStateVectors();
        for (List<Integer> list : state){
            for (int i : list){
                System.out.print(i + " ");
            }
            System.out.println();
        }
    }

    @Override
    public boolean equals(Object o){
        StateMatrix that = (StateMatrix) o;
        List<Integer>[] state1 = this.getStateVectors();
        List<Integer>[] state2 = that.getStateVectors();
        assert state1.length==state2.length;
        for(int i=0;i<state1.length;i++){
            if(state1[i].size()!=state2[i].size()){
                return false;
            }
            for(int j = 0;j<state1[i].size();j++){
                if(!Objects.equals(state1[i].get(j), state2[i].get(j))){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.getStateVectors());
    }

}
