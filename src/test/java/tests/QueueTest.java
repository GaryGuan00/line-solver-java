package tests;

import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Exp;
import jline.lang.nodes.Queue;
import jline.lang.nodes.Router;
import jline.lang.nodes.Sink;
import jline.lang.nodes.Source;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import jline.solvers.ssa.*;

class QueueTest {
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    //@org.junit.jupiter.api.Test
    void testMM1() {
        int nTests = 10;
        Random random = new Random();
        for (int i = 0; i < nTests; i++) {
            Network model = new Network("M/M/1");

            OpenClass openClass = new OpenClass(model, "MyClass");

            Source source = new Source(model, "mySource");
            double serviceRate = (double)(Math.abs(random.nextInt(100)) + 2);
            double arrivalRate = (double)(Math.abs(random.nextInt((int)Math.floor(serviceRate)-1)) +1);
            source.setArrival(openClass, new Exp(arrivalRate));
            Queue queue = new Queue(model, "MM1Queue", SchedStrategy.FCFS);
            queue.setService(openClass, new Exp(serviceRate));
            Sink sink = new Sink (model, "mySink");

            model.link(model.serialRouting(source,queue,sink));

            SolverSSA solverSSA = new SolverSSA();
            solverSSA.compile(model);
            solverSSA.setOptions().samples(100000);
            Timeline timeline = solverSSA.solve();
            timeline.disableTransientState();
            double expectedUtilization = arrivalRate/serviceRate;
            double utilization = timeline.getMetrics(1,0).getMetricValueByName("Utilization");
            double queueLen = timeline.getMetrics(1,0).getMetricValueByName("Queue Length");
            double expectedQueueLen = expectedUtilization/(1.0-expectedUtilization);
            double throughput = timeline.getMetrics(1,0).getMetricValueByName("Throughput");

            assertTrue(utilization > expectedUtilization-0.1);
            assertTrue(utilization < expectedUtilization+0.1);
            assertTrue(queueLen > expectedQueueLen*.8);
            assertTrue(queueLen < expectedQueueLen*1.2);
        }
    }

    //@org.junit.jupiter.api.Test
    void testMMc() {
        int nTests = 10;
        Random random = new Random();
        for (int i = 0; i < nTests; i++) {
            Network model = new Network("M/M/1");

            OpenClass openClass = new OpenClass(model, "MyClass");

            Source source = new Source(model, "mySource");
            double serviceRate = (double)(Math.abs(random.nextInt(100)) + 2);
            double arrivalRate = (double)(Math.abs(random.nextInt((int)Math.floor(serviceRate)-1)) +1);
            int numberOfServers = Math.abs(random.nextInt(20))+1;
            source.setArrival(openClass, new Exp(arrivalRate));
            Queue queue = new Queue(model, "MMcQueue", SchedStrategy.FCFS);
            queue.setService(openClass, new Exp(serviceRate));
            queue.setNumberOfServers(numberOfServers);
            Sink sink = new Sink (model, "mySink");

            model.link(model.serialRouting(source,queue,sink));

            SolverSSA solverSSA = new SolverSSA();
            solverSSA.compile(model);
            solverSSA.setOptions().samples(100000);
            Timeline timeline = solverSSA.solve();
            timeline.disableTransientState();
            double expectedUtilization = (arrivalRate/serviceRate)/numberOfServers;
            double utilization = timeline.getMetrics(1,0).getMetricValueByName("Utilization");
            double queueLen = timeline.getMetrics(1,0).getMetricValueByName("Queue Length");
            assertTrue(utilization > expectedUtilization-0.1);
            assertTrue(utilization < expectedUtilization+0.1);
        }
    }

    //@org.junit.jupiter.api.Test
    void testMMc_2() {
        int nTests = 10;
        Random random = new Random();
        for (int i = 0; i < nTests; i++) {
            Network model = new Network("M/M/1");

            OpenClass openClass = new OpenClass(model, "MyClass");

            Source source = new Source(model, "mySource");
            double serviceRate2 = (double)(Math.abs(random.nextInt(100)) + 2);
            double serviceRate1 = (double)(Math.abs(random.nextInt((int)Math.floor(serviceRate2)-1)) +2);
            double arrivalRate = (double)(Math.abs(random.nextInt((int)Math.floor(serviceRate1)-1)) +1);

            int numberOfServers1 = Math.abs(random.nextInt(20))+1;
            int numberOfServers2 = Math.abs(random.nextInt(20))+1;
            source.setArrival(openClass, new Exp(arrivalRate));
            Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
            queue1.setService(openClass, new Exp(serviceRate1));
            queue1.setNumberOfServers(numberOfServers1);
            Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
            queue2.setService(openClass, new Exp(serviceRate2));
            queue2.setNumberOfServers(numberOfServers2);
            Sink sink = new Sink (model, "mySink");

            model.link(model.serialRouting(source,queue1,queue2,sink));

            SolverSSA solverSSA = new SolverSSA();
            solverSSA.compile(model);
            solverSSA.setOptions().samples(100000);
            Timeline timeline = solverSSA.solve();
            timeline.disableTransientState();
            double expectedUtilization1 = (arrivalRate/serviceRate1)/numberOfServers1;
            double expectedUtilization2 = (arrivalRate/serviceRate2)/numberOfServers2;
            double utilization1 = timeline.getMetrics(1,0).getMetricValueByName("Utilization");
            double utilization2 = timeline.getMetrics(2,0).getMetricValueByName("Utilization");
            assertTrue(utilization1 > expectedUtilization1-0.1);
            assertTrue(utilization1 < expectedUtilization1+0.1);
            assertTrue(utilization2 > expectedUtilization2-0.1);
            assertTrue(utilization2 < expectedUtilization2+0.1);
        }
    }

    //@org.junit.jupiter.api.Test
    void testMMc_2p() {
        int nTests = 10;
        Random random = new Random();
        for (int i = 0; i < nTests; i++) {
            Network model = new Network("M/M/1");

            OpenClass openClass = new OpenClass(model, "MyClass");

            Source source = new Source(model, "mySource");
            double serviceRate1 = (double)(Math.abs(random.nextInt(100)) + 2);
            double serviceRate2 = (double)(Math.abs(random.nextInt(100)) + 2);
            double arrivalRate = (double)(Math.abs(random.nextInt((int)Math.floor(Math.min(serviceRate1, serviceRate2))-1)) +1)*2;

            int numberOfServers1 = Math.abs(random.nextInt(20))+1;
            int numberOfServers2 = Math.abs(random.nextInt(20))+1;
            source.setArrival(openClass, new Exp(arrivalRate));
            Queue queue1 = new Queue(model, "UpperQueue", SchedStrategy.FCFS);
            queue1.setService(openClass, new Exp(serviceRate1));
            queue1.setNumberOfServers(numberOfServers1);
            Queue queue2 = new Queue(model, "LowerQueue", SchedStrategy.FCFS);
            queue2.setService(openClass, new Exp(serviceRate2));
            queue2.setNumberOfServers(numberOfServers2);
            Sink sink = new Sink (model, "mySink");

            RoutingMatrix routingMatrix = new RoutingMatrix(model,Arrays.asList(openClass), Arrays.asList(source, queue1, queue2, sink));
            routingMatrix.addConnection(source, queue1);
            routingMatrix.addConnection(source, queue2);
            routingMatrix.addConnection(queue1, sink);
            routingMatrix.addConnection(queue2, sink);
            model.link(routingMatrix);

            SolverSSA solverSSA = new SolverSSA();
            solverSSA.compile(model);
            solverSSA.setOptions().samples(100000);
            Timeline timeline = solverSSA.solve();
            timeline.disableTransientState();
            double expectedUtilization1 = 0.5*(arrivalRate/serviceRate1)/numberOfServers1;
            double expectedUtilization2 = 0.5*(arrivalRate/serviceRate2)/numberOfServers2;
            double utilization1 = timeline.getMetrics(1,0).getMetricValueByName("Utilization");
            double utilization2 = timeline.getMetrics(2,0).getMetricValueByName("Utilization");
            assertTrue(utilization1 > expectedUtilization1-0.1);
            assertTrue(utilization1 < expectedUtilization1+0.1);
            assertTrue(utilization2 > expectedUtilization2-0.1);
            assertTrue(utilization2 < expectedUtilization2+0.1);
        }
    }

    //@org.junit.jupiter.api.Test
    void test_router() {
        int nTests = 100;
        Random random = new Random();
        for (int i = 0; i < nTests; i++) {
            Network model = new Network("M/M/1");

            OpenClass openClass = new OpenClass(model, "MyClass");

            Source source = new Source(model, "mySource");
            double routingRatio1 = random.nextDouble();
            double routingRatio2 = random.nextDouble()*(1-routingRatio1);
            double routingRatio3 = 1.0-routingRatio1-routingRatio2;


            double serviceRate1 = (double)(Math.abs(random.nextInt(100)) + 3);
            double serviceRate2 = (double)(Math.abs(random.nextInt(100)) + 3);
            double serviceRate3 = (double)(Math.abs(random.nextInt(100)) + 3);
            double maxServiceRate = (serviceRate1*routingRatio1) + (serviceRate2*(routingRatio2)) + (serviceRate1*routingRatio3);
            double arrivalRate = (double)(Math.abs(random.nextInt((int)maxServiceRate-1)-1) +1);

            int numberOfServers1 = Math.abs(random.nextInt(20))+1;
            int numberOfServers2 = Math.abs(random.nextInt(20))+1;
            int numberOfServers3 = Math.abs(random.nextInt(20))+1;
            source.setArrival(openClass, new Exp(arrivalRate));
            Router router = new Router(model, "router");
            Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
            queue1.setService(openClass, new Exp(serviceRate1));
            queue1.setNumberOfServers(numberOfServers1);
            Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
            queue2.setService(openClass, new Exp(serviceRate2));
            queue2.setNumberOfServers(numberOfServers2);
            Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);
            queue3.setService(openClass, new Exp(serviceRate3));
            queue3.setNumberOfServers(numberOfServers3);
            Sink sink = new Sink (model, "mySink");

            RoutingMatrix routingMatrix = new RoutingMatrix(model, Arrays.asList(openClass), Arrays.asList(source, router, queue1, queue2, queue3, sink));
            routingMatrix.addConnection(source, router);
            routingMatrix.addConnection(router, queue1,openClass,routingRatio1);
            routingMatrix.addConnection(router, queue2,openClass,routingRatio2);
            routingMatrix.addConnection(router, queue3,openClass,routingRatio3);
            routingMatrix.addConnection(queue1, sink);
            routingMatrix.addConnection(queue2, sink);
            routingMatrix.addConnection(queue3, sink);
            model.link(routingMatrix);

            SolverSSA solverSSA = new SolverSSA();
            solverSSA.compile(model);
            solverSSA.setOptions().samples(100000);
            Timeline timeline = solverSSA.solve();
            timeline.disableTransientState();
            double expectedUtilization1 = routingRatio1*(arrivalRate/serviceRate1)/numberOfServers1;
            double expectedUtilization2 = (routingRatio2)*(arrivalRate/serviceRate2)/numberOfServers2;
            double expectedUtilization3 = (routingRatio3)*(arrivalRate/serviceRate3)/numberOfServers3;
            double utilization1 = timeline.getMetrics(2,0).getMetricValueByName("Utilization");
            double utilization2 = timeline.getMetrics(3,0).getMetricValueByName("Utilization");
            double utilization3 = timeline.getMetrics(4,0).getMetricValueByName("Utilization");
            assertTrue(utilization1 > expectedUtilization1-0.2);
            assertTrue(utilization1 < expectedUtilization1+0.2);
            assertTrue(utilization2 > expectedUtilization2-0.2);
            assertTrue(utilization2 < expectedUtilization2+0.2);
            assertTrue(utilization3 > expectedUtilization3-0.2);
            assertTrue(utilization3 < expectedUtilization3+0.2);
        }
    }
}