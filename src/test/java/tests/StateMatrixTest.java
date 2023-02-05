package tests;

import jline.lang.NetworkStruct;
import jline.lang.constant.SchedStrategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import jline.solvers.ssa.*;
import jline.solvers.ssa.state.StateMatrix;

import java.util.Map;
import java.util.Random;

class StateMatrixTest {
    private StateMatrix stateMatrix;

    @org.junit.jupiter.api.BeforeEach
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
        schedStrategies[1] = SchedStrategy.LCFSPR;
        schedStrategies[2] = SchedStrategy.LCFSPR;
        SSAStruct networkStruct = new SSAStruct();
        networkStruct.nStateful = 3;
        networkStruct.nClasses = 3;
        networkStruct.schedStrategies = schedStrategies;
        networkStruct.capacities = capacityMatrix;
        networkStruct.nodeCapacity = nodeCapacities;
        networkStruct.numberOfServers = servers;
        networkStruct.isDelay = new boolean[3];
        networkStruct.isDelay[0] = false;
        networkStruct.isDelay[1] = false;
        networkStruct.isDelay[2] = false;
        networkStruct.nPhases = new int[3][3];
        networkStruct.startingPhaseProbabilities = new Map[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                networkStruct.nPhases[i][j] = 1;
            }
        }
        this.stateMatrix = new StateMatrix(networkStruct, new Random());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }
    
    @org.junit.jupiter.api.Test
    void totalStateAtNode() {
        stateMatrix.incrementState(0,0);
        stateMatrix.incrementState(0,1);
        stateMatrix.incrementState(0,2);
        assertEquals(stateMatrix.totalStateAtNode(0),3);
    }

    @org.junit.jupiter.api.Test
    void atCapacity() {
        for (int i = 0; i < 9; i++) {
            stateMatrix.incrementState(0,0);
            assertFalse(stateMatrix.atCapacity(0,0));
        }
        stateMatrix.incrementState(0,0);
        assertTrue(stateMatrix.atCapacity(0,0));
        stateMatrix.decrementState(0,0);
        for (int i = 1; i < 3; i++) {
            stateMatrix.incrementState(0,1);
            assertFalse(stateMatrix.atCapacity(0,0));
        }
        stateMatrix.incrementState(0,0);
        assertTrue(stateMatrix.atCapacity(0,0));
    }

    @org.junit.jupiter.api.Test
    void atEmpty() {
        assertTrue(stateMatrix.atEmpty(0,0));
        stateMatrix.incrementState(0,0);
        assertFalse(stateMatrix.atEmpty(0,0));
        assertTrue(stateMatrix.atEmpty(0,1));
        assertTrue(stateMatrix.atEmpty(0,2));
    }

    @org.junit.jupiter.api.Test
    void isBufferEmpty() {
        assertTrue(stateMatrix.isBufferEmpty(0));
        stateMatrix.incrementState(0,0);
        assertTrue(stateMatrix.isBufferEmpty(0));
        stateMatrix.addToBuffer(0,0);
        assertFalse(stateMatrix.isBufferEmpty(0));
    }

    @org.junit.jupiter.api.Test
    void incrementState() {
        assertEquals(stateMatrix.getState(0,0),0);
        stateMatrix.incrementState(0,0);
        assertEquals(stateMatrix.getState(0,0),1);
        assertEquals(stateMatrix.getState(0,1),0);
        assertEquals(stateMatrix.getState(1,0),0);
    }

    @org.junit.jupiter.api.Test
    void decrementState() {
        assertEquals(stateMatrix.getState(0,0),0);
        stateMatrix.incrementState(0,0);
        assertEquals(stateMatrix.getState(0,0),1);
        assertEquals(stateMatrix.getState(0,1),0);
        assertEquals(stateMatrix.getState(1,0),0);

        stateMatrix.decrementState(0,0);
        assertEquals(stateMatrix.getState(0,0),0);
        assertEquals(stateMatrix.getState(0,1),0);
        assertEquals(stateMatrix.getState(1,0),0);
    }

    @org.junit.jupiter.api.Test
    void getState() {
        assertEquals(stateMatrix.getState(0,0),0);
    }

    @org.junit.jupiter.api.Test
    void setState() {
        stateMatrix.setState(0,0,50);
        assertEquals(stateMatrix.getState(0,0),50);
        assertEquals(stateMatrix.getState(0,1),0);
        assertEquals(stateMatrix.getState(1,0),0);
        stateMatrix.setState(0,0,25);
        assertEquals(stateMatrix.getState(0,0),25);
        assertEquals(stateMatrix.getState(0,1),0);
        assertEquals(stateMatrix.getState(1,0),0);
    }

    @org.junit.jupiter.api.Test
    void inProcess() {
        stateMatrix.stateArrival(0,0);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),0);

        stateMatrix.stateArrival(0,0);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),0);

        stateMatrix.stateArrival(0,1);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),0);

        stateMatrix.stateArrival(1,1);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),1);

        stateMatrix.stateArrival(1,1);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),1);

        stateMatrix.stateArrival(1,0);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),1);
        assertEquals(stateMatrix.inProcess(1,1),0);

        stateMatrix.stateDeparture(1,0);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),1);

        stateMatrix.stateDeparture(1,1);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),1);

        stateMatrix.stateDeparture(1,1);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),0);

        stateMatrix.stateDeparture(0,0);
        assertEquals(stateMatrix.inProcess(0,0),1);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),0);

        stateMatrix.stateDeparture(0,0);
        assertEquals(stateMatrix.inProcess(0,0),0);
        assertEquals(stateMatrix.inProcess(0,1),1);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),0);

        stateMatrix.stateDeparture(0,1);
        assertEquals(stateMatrix.inProcess(0,0),0);
        assertEquals(stateMatrix.inProcess(0,1),0);
        assertEquals(stateMatrix.inProcess(1,0),0);
        assertEquals(stateMatrix.inProcess(1,1),0);
    }
}