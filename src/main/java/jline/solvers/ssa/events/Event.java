package jline.solvers.ssa.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;

import jline.lang.nodes.Node;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;
import jline.util.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

public class Event implements Serializable {
    public Node node;
    public Event() {
    }

    public double getRate(StateMatrix stateMatrix) {
        return Double.NaN;
    }

    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        /*
            stateUpdate -
                Attempt to apply an event to the stateMatrix

            Returns: (boolean) - whether the update was successful or not
         */
        return true;
    }

    public boolean updateStateSpace(StateMatrix stateMatrix, Random random, Timeline timeline, ArrayList<StateMatrix> stateSpace, Queue<StateMatrix> queue) {

        return true;
    }

    public boolean updateEventSpace(StateMatrix stateMatrix, Random random, Timeline timeline, ArrayList<Quartet<Event,Pair<OutputEvent,Double>,StateMatrix,StateMatrix>>  eventSpace,Event event, Queue<StateMatrix> queue,StateMatrix copy) {

        return true;
    }


    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        /*
            stateUpdateN -
                Attempt to apply N repetitions of an event to the stateMatrix

            Returns: (int) - number of repetitions left unapplied
         */
        int rem = n;
        for (int i = 0; i < n; i++) {
            if (this.stateUpdate(stateMatrix, random, timeline)) {
                rem--;
            }
        }

        return rem;
    }

    public void printSummary() {
        System.out.format("Generic event\n");
    }

    public int getMaxRepetitions(StateMatrix stateMatrix) {
        return Integer.MAX_VALUE;
    }

    public StateMatrix getNextState(StateMatrix startingState, Timeline timeline, ArrayList<StateMatrix> stateSpace, Queue<StateMatrix> queue) {

        StateMatrix endingState = new StateMatrix(startingState);

        if(updateStateSpace(endingState, new Random(), timeline, stateSpace,queue)){
            return endingState;
        }

        return null;

    }

    public StateMatrix getNextEventState(StateMatrix startingState, Timeline timeline, ArrayList<Quartet<Event,Pair<OutputEvent,Double>,StateMatrix,StateMatrix>> eventSpace,Event event, Queue<StateMatrix> queue, StateMatrix copy) {

        StateMatrix endingState = new StateMatrix(startingState);

        if(updateEventSpace(endingState, new Random(), timeline, eventSpace,event,queue,copy)){
            return endingState;
        }

        return null;
    }

}
