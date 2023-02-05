package tests;

import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Exp;
import jline.lang.nodes.Queue;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import jline.solvers.ssa.*;
import jline.solvers.ssa.events.DepartureEvent;
import jline.solvers.ssa.events.EventStack;
import jline.solvers.ssa.state.StateMatrix;
import jline.solvers.ssa.strategies.CutoffStrategy;

class DepartureEventTest {
    private DepartureEvent departureEvent1;
    private StateMatrix stateMatrix;
    private Queue queue1;
    private Queue queue2;
    private Queue queue3;
    private Network network;
    private JobClass jobClass1;
    private JobClass jobClass2;
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
        servers[0] = 3;
        servers[1] = 5;
        servers[2] = 1;
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
        this.timeline = new Timeline(networkStruct);

//        this.eventStack = new EventStack();
        this.network = new Network("Test Network");
        this.jobClass1 = new OpenClass(this.network, "Job Class", 1);
        this.jobClass2 = new OpenClass(this.network, "Job Class 2", 2);
        this.queue1 = new Queue(this.network, "Queue 1", SchedStrategy.FCFS);
        this.queue1.setService(this.jobClass1,new Exp(3));
        this.queue1.setService(this.jobClass2,new Exp(4));
        this.queue1.setNumberOfServers(3);
        this.queue2 = new Queue(this.network, "Queue 2", SchedStrategy.LCFS);
        this.queue2.setService(this.jobClass1,new Exp(5));
        this.queue2.setService(this.jobClass2,new Exp(4));
        this.queue2.setNumberOfServers(5);
        this.queue3 = new Queue(this.network, "Queue 3", SchedStrategy.LCFS);
        this.queue3.setService(this.jobClass1,new Exp(6));
        this.queue3.setService(this.jobClass2,new Exp(7));
        this.network.link(this.network.serialRouting(this.queue1, this.queue2, this.queue3));

        this.departureEvent1 = new DepartureEvent(this.queue1, this.jobClass1);
    }

    @org.junit.jupiter.api.Test
    void testGetRate() {
        /*assertEquals(this.departureEvent1.getRate(this.stateMatrix), Double.NaN);
        this.stateMatrix.setState(0,0,1);
        this.stateMatrix.addToBuffer(0,0,1);
        assertEquals(this.departureEvent1.getRate(this.stateMatrix), 3);
        assertEquals(this.departureEvent4.getRate(this.stateMatrix), Double.NaN);
        this.stateMatrix.setState(0,0,2);
        this.stateMatrix.addToBuffer(0,0,1);
        assertEquals(this.departureEvent1.getRate(this.stateMatrix), 6);
        assertEquals(this.departureEvent4.getRate(this.stateMatrix), Double.NaN);
        this.stateMatrix.setState(0,0,3);
        this.stateMatrix.addToBuffer(0,0,1);
        assertEquals(this.departureEvent1.getRate(this.stateMatrix), 9);
        assertEquals(this.departureEvent4.getRate(this.stateMatrix), Double.NaN);
        this.stateMatrix.setState(0,0,4);
        this.stateMatrix.addToBuffer(0,0,1);
        assertEquals(this.departureEvent1.getRate(this.stateMatrix), 9);
        assertEquals(this.departureEvent4.getRate(this.stateMatrix), Double.NaN);
        this.stateMatrix.setState(0,1,1);
        this.stateMatrix.addToBuffer(0,1,1);
        assertEquals(this.departureEvent1.getRate(this.stateMatrix), 9);
        assertEquals(this.departureEvent4.getRate(this.stateMatrix), Double.NaN);


        assertEquals(this.departureEvent2.getRate(this.stateMatrix), Double.NaN);
        this.stateMatrix.incrementState(1,0);
        this.stateMatrix.addToBuffer(1,0,1);
        assertEquals(this.departureEvent3.getRate(this.stateMatrix), Double.NaN);
        assertEquals(this.departureEvent2.getRate(this.stateMatrix), 5);
        assertEquals(this.departureEvent5.getRate(this.stateMatrix), Double.NaN);

        this.stateMatrix.incrementState(1,1);
        this.stateMatrix.addToBuffer(1,1,1);
        assertEquals(this.departureEvent3.getRate(this.stateMatrix), Double.NaN);
        assertEquals(this.departureEvent2.getRate(this.stateMatrix), 5);
        assertEquals(this.departureEvent5.getRate(this.stateMatrix), 4);

        this.stateMatrix.incrementState(1,1);
        this.stateMatrix.addToBuffer(1,1,1);
        assertEquals(this.departureEvent3.getRate(this.stateMatrix), Double.NaN);
        assertEquals(this.departureEvent2.getRate(this.stateMatrix), 5);
        assertEquals(this.departureEvent5.getRate(this.stateMatrix), 8);

        this.stateMatrix.incrementState(1,1);
        this.stateMatrix.addToBuffer(1,1,1);
        assertEquals(this.departureEvent3.getRate(this.stateMatrix), Double.NaN);
        assertEquals(this.departureEvent2.getRate(this.stateMatrix), 5);
        assertEquals(this.departureEvent5.getRate(this.stateMatrix), 12);

        this.stateMatrix.incrementState(1,1);
        this.stateMatrix.addToBuffer(1,1,1);
        assertEquals(this.departureEvent3.getRate(this.stateMatrix), Double.NaN);
        assertEquals(this.departureEvent2.getRate(this.stateMatrix), 5);
        assertEquals(this.departureEvent5.getRate(this.stateMatrix), 16);*/
    }

    @org.junit.jupiter.api.Test
    void testStateUpdate() {
        Random random = new Random();
        this.stateMatrix.setState(0,0,5);
        this.stateMatrix.addToBuffer(0,0,5);
        this.departureEvent1.stateUpdate(this.stateMatrix, random, this.timeline);
        assertEquals(this.stateMatrix.getState(0,0),4);
        assertEquals(this.stateMatrix.getState(1,0),1);
        this.departureEvent1.stateUpdate(this.stateMatrix, random, this.timeline);
        assertEquals(this.stateMatrix.getState(0,0),3);
        assertEquals(this.stateMatrix.getState(1,0),2);
        this.departureEvent1.stateUpdate(this.stateMatrix, random, this.timeline);
        assertEquals(this.stateMatrix.getState(0,0),2);
        assertEquals(this.stateMatrix.getState(1,0),3);
        this.departureEvent1.stateUpdate(this.stateMatrix, random, this.timeline);
        assertEquals(this.stateMatrix.getState(0,0),1);
        assertEquals(this.stateMatrix.getState(1,0),4);
    }
    @org.junit.jupiter.api.Test
    void testStateUpdateN() {
        Random random = new Random();
        this.stateMatrix.setState(0,0,5);
        this.stateMatrix.addToBuffer(0,0,5);
        this.departureEvent1.stateUpdateN(2,this.stateMatrix, random, this.timeline);
        assertEquals(this.stateMatrix.getState(0,0),3);
        assertEquals(this.stateMatrix.getState(1,0),2);
    }
}
