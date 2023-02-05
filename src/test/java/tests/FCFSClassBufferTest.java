package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import jline.solvers.ssa.*;
import jline.solvers.ssa.state.FCFSClassBuffer;
import jline.solvers.ssa.state.PhaseList;
import jline.util.Pair;

class FCFSClassBufferTest {
    private FCFSClassBuffer classBuffer;
    private Random random;
    private int nClasses;
    private int nServers;
    private int nRuns;
    private int maxBatch;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        this.random = new Random();
        this.nClasses = this.random.nextInt(100);
        this.nServers = this.random.nextInt(100);
        this.nRuns = 200;
        this.maxBatch = 15;

        int[] nPhases = new int[this.nClasses];
        for (int i = 0; i < this.nClasses; i++) {
            nPhases[i] += 1;
        }
        this.classBuffer = new FCFSClassBuffer(nClasses,this.nServers, new PhaseList(nPhases,nClasses,new Random()));

    }


    @org.junit.jupiter.api.Test
    void testAddToBuffer() {
        List<Integer> parallelBuffer = new ArrayList<Integer>();
        int[] inService = new int[this.nClasses];
        int[] inQueue = new int[this.nClasses];

        for (int i = 0; i < this.nClasses; i++) {
            inService[i] = 0;
            inQueue[i] = 0;
        }

        for (int i = 0; i < this.nServers; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            parallelBuffer.add(nextClass);
            inService[nextClass] += 1;
            inQueue[nextClass] += 1;
            this.classBuffer.addToBuffer(nextClass);
        }
        for (int i = this.nServers; i < this.nRuns; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            parallelBuffer.add(nextClass);
            inQueue[nextClass] += 1;
            this.classBuffer.addToBuffer(nextClass);
        }


        for (int i = 0; i < this.nClasses; i++) {
            assertEquals(inService[i], this.classBuffer.getInService(i));
            assertEquals(inQueue[i], this.classBuffer.getInQueue(i));
        }
    }


    @org.junit.jupiter.api.Test
    void testAddNToBuffer() {
        List<Integer> parallelBuffer = new ArrayList<Integer>();
        int[] inService = new int[this.nClasses];
        int[] inQueue = new int[this.nClasses];

        for (int i = 0; i < this.nClasses; i++) {
            inService[i] = 0;
            inQueue[i] = 0;
        }

        int totalInService = 0;

        for (int i = 0; i < this.nRuns; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            int n = this.random.nextInt(this.maxBatch);
            for (int j = 0; j < n; j++) {
                parallelBuffer.add(nextClass);
            }

            if (totalInService < this.nServers) {
                int addToService = Math.min(this.nServers-totalInService, n);
                inService[nextClass] += addToService;
                totalInService += addToService;
            }

            inQueue[nextClass] += n;
            this.classBuffer.addNToBuffer(nextClass, n);
        }


        for (int i = 0; i < this.nClasses; i++) {
            assertEquals(inService[i], this.classBuffer.getInService(i));
            assertEquals(inQueue[i], this.classBuffer.getInQueue(i));
        }
    }


    @org.junit.jupiter.api.Test
    public void testRemoveFirstOfClass() {
        List<Integer> parallelBuffer = new ArrayList<Integer>();
        int[] inService = new int[this.nClasses];
        int[] inQueue = new int[this.nClasses];

        for (int i = 0; i < this.nClasses; i++) {
            inService[i] = 0;
            inQueue[i] = 0;
        }

        for (int i = 0; i < this.nServers; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            parallelBuffer.add(nextClass);
            inService[nextClass] += 1;
            inQueue[nextClass] += 1;
            this.classBuffer.addToBuffer(nextClass);
        }
        for (int i = this.nServers; i < this.nRuns; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            parallelBuffer.add(nextClass);
            inQueue[nextClass] += 1;
            this.classBuffer.addToBuffer(nextClass);
        }

        for (int i = 0; i < this.nRuns-this.nServers; i++) {
            int nToRemove = this.random.nextInt(this.nServers);
            int classToRemove = parallelBuffer.get(nToRemove);
            int classToAdd = parallelBuffer.get(this.nServers);
            parallelBuffer.remove(nToRemove);
            this.classBuffer.removeFirstOfClass(classToRemove);
            inService[classToRemove] -= 1;
            inQueue[classToRemove] -= 1;
            inService[classToAdd] += 1;
            assertEquals(inService[classToRemove], this.classBuffer.getInService(classToRemove));
            assertEquals(inQueue[classToRemove], this.classBuffer.getInQueue(classToRemove));
            assertEquals(inService[classToAdd], this.classBuffer.getInService(classToAdd));
            assertEquals(inQueue[classToAdd], this.classBuffer.getInQueue(classToAdd));
        }
        for (int i = 0; i < this.nServers; i++) {
            int nToRemove = this.random.nextInt(Math.min(this.nServers, parallelBuffer.size()));
            int classToRemove = parallelBuffer.get(nToRemove);
            parallelBuffer.remove(nToRemove);
            this.classBuffer.removeFirstOfClass(classToRemove);
            inService[classToRemove] -= 1;
            inQueue[classToRemove] -= 1;
            assertEquals(inService[classToRemove], this.classBuffer.getInService(classToRemove));
            assertEquals(inQueue[classToRemove], this.classBuffer.getInQueue(classToRemove));
        }
        for (int j = 0; j < this.nClasses; j++) {
            assertEquals(inService[j], this.classBuffer.getInService(j));
            assertEquals(inQueue[j], this.classBuffer.getInQueue(j));
        }
    }
}