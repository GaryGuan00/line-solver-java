package tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

import jline.solvers.ssa.state.FCFSClassBuffer;
import jline.solvers.ssa.state.LCFSPreBuffer;
import jline.solvers.ssa.state.PhaseList;
import jline.util.Pair;

class LCFSPreBufferTest {
    private LCFSPreBuffer classBuffer;
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
        this.classBuffer = new LCFSPreBuffer(nClasses,this.nServers, new PhaseList(nPhases,nClasses,new Random()));

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
        for (int i = 0; i < this.nRuns-nServers; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            parallelBuffer.add(nextClass);
            inQueue[nextClass] += 1;
            this.classBuffer.addToBuffer(nextClass);
        }

        for (int i = 0; i < this.nServers; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            parallelBuffer.add(nextClass);
            inService[nextClass] += 1;
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

            inQueue[nextClass] += n;
            this.classBuffer.addNToBuffer(nextClass, n);
        }

        int parallelSize = parallelBuffer.size();

        for (int i = 0; i < this.nServers; i++) {
            int classIdx = parallelBuffer.get(parallelSize-i-1);
            inService[classIdx] += 1;
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

        for (int i = this.nServers; i < this.nRuns; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            parallelBuffer.add(nextClass);
            inQueue[nextClass] += 1;
            this.classBuffer.addToBuffer(nextClass);
        }
        for (int i = 0; i < this.nServers; i++) {
            int nextClass = this.random.nextInt(this.nClasses);
            parallelBuffer.add(nextClass);
            inService[nextClass] += 1;
            inQueue[nextClass] += 1;
            this.classBuffer.addToBuffer(nextClass);
        }

        for (int i = 0; i < this.nRuns-this.nServers; i++) {
            int nToRemove = this.random.nextInt(this.nServers);
            int classToRemove = parallelBuffer.get(parallelBuffer.size()-nToRemove-1);
            int classToAdd = parallelBuffer.get(parallelBuffer.size()-this.nServers-1);
            parallelBuffer.remove(parallelBuffer.size()-nToRemove-1);
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
            int classToRemove = parallelBuffer.get(parallelBuffer.size()-nToRemove-1);
            parallelBuffer.remove(parallelBuffer.size()-nToRemove-1);
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


    @org.junit.jupiter.api.Test
    void testGetInService() {
    }

    @org.junit.jupiter.api.Test
    void testCreateCopy() {
    }


    @org.junit.jupiter.api.Test
    void testGetInQueue() {
        this.classBuffer.addToBuffer(4);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),0);
        assertEquals(this.classBuffer.getInQueue(4),1);
        this.classBuffer.addToBuffer(4);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),0);
        assertEquals(this.classBuffer.getInQueue(4),2);
        this.classBuffer.addToBuffer(4);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),0);
        assertEquals(this.classBuffer.getInQueue(4),3);
        this.classBuffer.addToBuffer(4);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),0);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),1);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),2);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(3);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),0);
        assertEquals(this.classBuffer.getInQueue(3),3);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(2);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),1);
        assertEquals(this.classBuffer.getInQueue(3),3);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(2);
        assertEquals(this.classBuffer.getInQueue(1),0);
        assertEquals(this.classBuffer.getInQueue(2),2);
        assertEquals(this.classBuffer.getInQueue(3),3);
        assertEquals(this.classBuffer.getInQueue(4),4);
        this.classBuffer.addToBuffer(1);
        assertEquals(this.classBuffer.getInQueue(1),1);
        assertEquals(this.classBuffer.getInQueue(2),2);
        assertEquals(this.classBuffer.getInQueue(3),3);
        assertEquals(this.classBuffer.getInQueue(4),4);
    }
}