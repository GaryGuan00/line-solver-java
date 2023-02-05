package tests;

import jline.solvers.ssa.*;
import jline.solvers.ssa.events.ArrivalEvent;
import jline.solvers.ssa.events.EventStack;
import jline.solvers.ssa.state.StateMatrix;
import jline.solvers.ssa.strategies.CutoffStrategy;
import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.nodes.Queue;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ArrivalEventTest {
    private ArrivalEvent arrivalEvent1;
    private ArrivalEvent arrivalEvent2;
    private ArrivalEvent arrivalEvent3;
    private StateMatrix stateMatrix;
    private EventStack eventStack;
    private Queue queue1;
    private Queue queue2;
    private Queue queue3;
    private Network network;
    private JobClass jobClass;
    private Timeline timeline;

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
        schedStrategies[1] = SchedStrategy.LCFS;
        schedStrategies[2] = SchedStrategy.LCFS;
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
        this.timeline = new Timeline(networkStruct);

        this.eventStack = new EventStack();
        this.network = new Network("Test Network");
        this.queue1 = new Queue(this.network, "Queue 1", SchedStrategy.FCFS);
        this.queue2 = new Queue(this.network, "Queue 2", SchedStrategy.LCFS);
        this.queue3 = new Queue(this.network, "Queue 3", SchedStrategy.LCFS);
        this.network.link(this.network.serialRouting(this.queue1, this.queue2, this.queue3));
        this.jobClass = new OpenClass(this.network, "Job Class", 1);

        this.arrivalEvent1 = new ArrivalEvent(this.queue1, this.jobClass);
        this.arrivalEvent2 = new ArrivalEvent(this.queue2, this.jobClass);
        this.arrivalEvent3 = new ArrivalEvent(this.queue3, this.jobClass);
    }

    @org.junit.jupiter.api.Test
    void testStateUpdate() {
        Random random = new Random();
        assertTrue(this.stateMatrix.getState(0,0) == 0);
        assertTrue(this.stateMatrix.isBufferEmpty(0));
        assertTrue(this.stateMatrix.isBufferEmpty(1));
        this.arrivalEvent1.stateUpdate(this.stateMatrix, random, this.timeline);
        assertTrue(this.stateMatrix.getState(0,0) == 1);
        assertFalse(this.stateMatrix.isBufferEmpty(0));
        this.arrivalEvent1.stateUpdate(this.stateMatrix, random, this.timeline);
        assertTrue(this.stateMatrix.getState(0,0) == 2);
        assertFalse(this.stateMatrix.isBufferEmpty(0));
        assertTrue(this.stateMatrix.isBufferEmpty(1));
    }


    @org.junit.jupiter.api.Test
    void testStateUpdateN() {
        Random random = new Random();
        assertTrue(this.stateMatrix.getState(0,0) == 0);
        assertTrue(this.stateMatrix.isBufferEmpty(0));
        assertTrue(this.stateMatrix.isBufferEmpty(1));
        this.arrivalEvent1.stateUpdateN(5,this.stateMatrix, random, this.timeline);
        assertEquals(this.stateMatrix.getState(0,0), 5);
        assertFalse(this.stateMatrix.isBufferEmpty(0));
        this.arrivalEvent1.stateUpdateN(2,this.stateMatrix, random, this.timeline);
        assertEquals(this.stateMatrix.getState(0,0), 7);
        this.arrivalEvent1.stateUpdateN(15,this.stateMatrix, random, this.timeline);
        assertEquals(this.stateMatrix.getState(0,0), 10);
        assertFalse(this.stateMatrix.isBufferEmpty(0));
        assertTrue(this.stateMatrix.isBufferEmpty(1));
    }
}