package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jline.lang.*;
import jline.util.Cdf;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CdfTest {
    Cdf<Integer> testCdf;

    @BeforeEach
    void setUp() {
        testCdf = new Cdf<Integer>(new Random());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addElement() {
        testCdf.addElement(61, 1);
        assertEquals((int)testCdf.generate(), 61);
    }

    @Test
    void generateEven() {
        for (int i = 0; i < 10; i++) {
            testCdf.addElement(i, 0.1);
        }

        int times_five = 0;
        for (int i = 0; i < 500; i++) {
            if (testCdf.generate() == 5) {
                times_five++;
            }
        }

        assertTrue(times_five > 30);
        assertTrue(times_five < 70);
    }

    @Test
    void generateUnEven() {
        for (int i = 0; i <= 9; i++) {
            testCdf.addElement(i, ((double)i)/(45.0));
        }

        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
        for (int i = 0; i < 500; i++) {
            int n = testCdf.generate();
            if (counts.containsKey(n)) {
                counts.put(n, counts.get(n) + 1);
            } else {
                counts.put(n, 1);
            }
        }

        int testNum = (new Random()).nextInt(9)+1;
        int testCt = counts.get(testNum);
        int estCt = (int)((((double)testNum)/(45.0))*500.0);

        assertTrue(testCt > estCt-20);
        assertTrue(testCt < estCt+20);
    }

    @Test
    void generateRandom() {
        for (int i = 0; i < 100; i++) {
            Random r=  new Random();
            this.testCdf = new Cdf<Integer>(r);

            int nMembers = r.nextInt(29)+1;
            List<Double> pdf = new ArrayList<Double>();
            double totalProb = 0;
            for (int j = 0; j < nMembers; j++) {
                double p = r.nextDouble();
                totalProb += p;
                pdf.add(p);
            }
            for (int j = 0; j < nMembers; j++) {
                pdf.set(j, pdf.get(j)/totalProb);
                this.testCdf.addElement(j, pdf.get(j));
            }

            List<Integer> counts = new ArrayList<Integer>();
        }
    }
}