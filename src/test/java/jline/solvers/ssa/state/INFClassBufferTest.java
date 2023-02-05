package jline.solvers.ssa.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class INFClassBufferTest {
    private INFClassBuffer classBuffer;
    private Random random;
    private int nClasses;
    private int nRuns;
    private int maxBatch;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        this.nClasses = 50;
        this.nRuns = 200;
        this.maxBatch = 15;

        int[] nPhases = new int[this.nClasses];
        for (int i = 0; i < this.nClasses; i++) {
            nPhases[i] += 1;
        }
        this.classBuffer = new INFClassBuffer(this.random, nClasses, new PhaseList(nPhases,nClasses,new Random()));

        this.random = new Random();
    }


    @org.junit.jupiter.api.Test
    void testAddToBuffer() {
        List<Integer> parallelBuffer = new ArrayList<Integer>();
        int[] inQueue = new int[this.nClasses];

        for (int i = 0; i < this.nClasses; i++) {
            inQueue[i] = 0;
        }
        for (int i = 0; i < this.nRuns; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            parallelBuffer.add(nextClass);
            inQueue[nextClass] += 1;
            this.classBuffer.addToBuffer(nextClass);
        }


        for (int i = 0; i < this.nClasses; i++) {
            assertEquals(inQueue[i], this.classBuffer.getInService(i));
            assertEquals(inQueue[i], this.classBuffer.getInQueue(i));
        }
    }


    @org.junit.jupiter.api.Test
    void testAddNToBuffer() {
        List<Integer> parallelBuffer = new ArrayList<Integer>();
        int[] inQueue = new int[this.nClasses];

        for (int i = 0; i < this.nClasses; i++) {
            inQueue[i] = 0;
        }

        for (int i = 0; i < this.nRuns; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            int n = this.random.nextInt(this.maxBatch);
            for (int j = 0; j < n; j++) {
                parallelBuffer.add(nextClass);
            }

            inQueue[nextClass] += n;
            this.classBuffer.addNToBuffer(nextClass, n);
        }


        for (int i = 0; i < this.nClasses; i++) {
            assertEquals(inQueue[i], this.classBuffer.getInService(i));
            assertEquals(inQueue[i], this.classBuffer.getInQueue(i));
        }
    }

}