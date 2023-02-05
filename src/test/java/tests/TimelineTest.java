package tests;

import jline.lang.NetworkStruct;
import jline.lang.constant.SchedStrategy;

import jline.solvers.ssa.*;
import jline.solvers.ssa.state.StateMatrix;

import java.util.Map;
import java.util.Random;

class TimelineTest {
    Timeline timeline;
    SSAStruct networkStruct;
    StateMatrix stateMatrix;
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
        this.networkStruct = new SSAStruct();
        this.networkStruct.nStateful = 3;
        this.networkStruct.nClasses = 3;
        this.networkStruct.schedStrategies = schedStrategies;
        this.networkStruct.capacities = capacityMatrix;
        this.networkStruct.nodeCapacity = nodeCapacities;
        this.networkStruct.numberOfServers = servers;
        networkStruct.isDelay = new boolean[3];
        networkStruct.isDelay[0] = false;
        networkStruct.isDelay[1] = false;
        networkStruct.isDelay[2] = false;
        networkStruct.nPhases = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                networkStruct.nPhases[i][j] = 1;
            }
        }

        networkStruct.startingPhaseProbabilities = new Map[3];
        this.stateMatrix = new StateMatrix(networkStruct, new Random());
        this.timeline = new Timeline(networkStruct);
    }

    @org.junit.jupiter.api.Test
    void testRecord() {
    }

    @org.junit.jupiter.api.Test
    void testPreRecord() {
    }

    @org.junit.jupiter.api.Test
    void testClearCache() {
    }

    @org.junit.jupiter.api.Test
    void testRecordCache() {
    }
}