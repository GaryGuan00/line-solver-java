package tests;

import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Immediate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import jline.solvers.ssa.*;
import jline.solvers.ssa.events.EventStack;
import jline.solvers.ssa.state.StateMatrix;

class EventStackTest {
    private StateMatrix stateMatrix;
    private EventStack eventStack;
    private Timeline timeline;

    @BeforeEach
    void setUp() {
        int[][] capacityMatrix = new int[3][3];
        int[] nodeCapacities = new int[3];
        int[] servers = new int[3];
        capacityMatrix[0][0] = 10;
        capacityMatrix[0][1] = 9;
        capacityMatrix[0][2] = 8;
        capacityMatrix[1][0] = 9;
        capacityMatrix[1][1] = 8;
        capacityMatrix[1][2] = 7;
        capacityMatrix[2][0] = 8;
        capacityMatrix[2][1] = 7;
        capacityMatrix[2][2] = 6;
        nodeCapacities[0] = 13;
        nodeCapacities[1] = 12;
        nodeCapacities[2] = 1;
        servers[0] = 1;
        servers[1] = 1;
        servers[2] = 2;
        SchedStrategy[] schedStrategies = new SchedStrategy[3];
        schedStrategies[0] = SchedStrategy.FCFS;
        schedStrategies[1] = SchedStrategy.FCFS;
        schedStrategies[2] = SchedStrategy.FCFS;
        SSAStruct networkStruct = new SSAStruct();
        networkStruct.nStateful = 3;
        networkStruct.nClasses = 3;
        networkStruct.schedStrategies = schedStrategies;
        networkStruct.capacities = capacityMatrix;
        networkStruct.nodeCapacity = nodeCapacities;
        networkStruct.numberOfServers = servers;
        networkStruct.isDelay = new boolean[3];
        networkStruct.nPhases = new int[3][3];
        networkStruct.startingPhaseProbabilities = new Map[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                networkStruct.nPhases[i][j] = 1;
            }
        }
        this.stateMatrix = new StateMatrix(networkStruct, new Random());
        this.timeline = new Timeline(networkStruct);

        this.eventStack = new EventStack();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testImmediate() {
        int timeA = 0;
        DummyEvent eventA = new DummyEvent(new Immediate(),0,0,1);
        DummyEvent eventB = new DummyEvent(new Immediate(),0,0,-1);
        DummyEvent eventC = new DummyEvent(new Immediate(),0,0,2);
        DummyEvent eventD = new DummyEvent(new Immediate(),0,0,-2);


        this.eventStack.addEvent(eventA);
        this.eventStack.addEvent(eventB);
        this.eventStack.addEvent(eventC);
        this.eventStack.addEvent(eventD);

        Random random = new Random();

        for (int i = 0; i < 500; i++) {
            this.eventStack.updateState(this.stateMatrix,this.timeline,0, random);
        }
        assertTrue(this.stateMatrix.getState(0,0)>-50);
        assertTrue(this.stateMatrix.getState(0,0)<50);
    }

    @Test
    void addEvent() {
    }

    @Test
    void updateState() {
    }
}