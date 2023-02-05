package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import jline.solvers.ssa.*;
import jline.solvers.ssa.state.PhaseList;

class PhaseListTest {
    private PhaseList phaseList;
    private int nReps;

    @BeforeEach
    void setUp() {
        int[] nPhases = new int[3];
        nPhases[0] = 3;
        nPhases[1] = 3;
        nPhases[2] = 3;
        this.phaseList = new PhaseList(nPhases, 3, new Random());

        this.nReps = 500;
    }

    @Test
    void testIncrementPhase() {
        // For class 1, we start off with 3 jobs in state 0, and we increment twice.
        // 2/3 of the time, the phases should look like: [1, 2, 0]
        // 1/3 of the time, the phases should look like: [2, 0, 1]
        // Therefore, the average counts are: [4/3, 4/3, 1/3]
        double[] cmPhaseCount = new double[3];
        cmPhaseCount[0] = 0;
        cmPhaseCount[1] = 0;
        cmPhaseCount[2] = 0;

        for (int i = 0; i < this.nReps; i++) {
            int[] nPhases = new int[3];
            nPhases[0] = 3;
            nPhases[1] = 3;
            nPhases[2] = 3;
            this.phaseList = new PhaseList(nPhases, 3, new Random());

            this.phaseList.incrementPhase(0, 3);
            this.phaseList.incrementPhase(0, 3);
            this.phaseList.incrementPhase(2, 3);

            assertEquals(2, this.phaseList.getNInPhase(2, 0));
            assertEquals(1, this.phaseList.getNInPhase(2, 1));

            for (int j = 0; j < 3; j++) {
                cmPhaseCount[j] += this.phaseList.getNInPhase(0, j);
            }
        }
        assertEquals(4.0/3.0, cmPhaseCount[0]/this.nReps, 0.1);
        assertEquals(4.0/3.0, cmPhaseCount[1]/this.nReps, 0.1);
        assertEquals(1.0/3.0, cmPhaseCount[2]/this.nReps, 0.1);

        // incrementing [n phases] times creates an arrival
        int[] nPhases = new int[3];
        nPhases[0] = 3;
        nPhases[1] = 3;
        nPhases[2] = 3;
        this.phaseList = new PhaseList(nPhases, 3, new Random());

        assertFalse(this.phaseList.incrementPhase(0,1));
        assertFalse(this.phaseList.incrementPhase(0,1));
        assertTrue(this.phaseList.incrementPhase(0,1));
    }

    @Test
    void testGetNInPhase() {

    }

    @Test
    void testUpdatePhase() {

    }

    @Test
    void testUpdateGlobalPhase() {

    }

    @Test
    void testGetGlobalPhase() {

    }

    @Test
    void testCreateCopy() {

    }
}