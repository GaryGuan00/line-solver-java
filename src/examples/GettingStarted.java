package examples;

import jline.lang.nodes.*;
import jline.solvers.ctmc.SolverCTMC;
import jline.lang.nodes.Delay;
import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Erlang;
import jline.lang.distributions.Exp;
import jline.solvers.ssa.events.Event;
import jline.solvers.ssa.events.OutputEvent;
import jline.solvers.ssa.state.StateMatrix;
import jline.util.Pair;
import org.javatuples.Quartet;

import java.util.ArrayList;
import jline.lang.nodes.Queue;
import jline.lang.nodes.Sink;
import jline.lang.nodes.Source;

import java.util.Arrays;

public class GettingStarted {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Network model = GettingStarted.matlabExample3();
        SolverCTMC solverSSA = new SolverCTMC();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(20000).seed(9);
        //solverSSA.setOptions().R5(17);
        // Uncomment below to test Tau Leaping
        /*solverSSA.setOptions().configureTauLeap(new TauLeapingType(
                TauLeapingVarType.Poisson,
                TauLeapingOrderStrategy.DirectedCycle,
                TauLeapingStateStrategy.TimeWarp,
                0.1
        ));*/

//        Timeline solve_soln = solverSSA.solve();
//        System.out.println("Your simulation has finished.");
//        solve_soln.printSummary(model);

//        ArrayList<StateMatrix> stateSpace = solverSSA.getStateSpace();
//        System.out.println(stateSpace.size());

        ArrayList<Quartet<Event, Pair<OutputEvent, Double>, StateMatrix, StateMatrix>> eventSpace = solverSSA.getAllEvents();
        System.out.println(eventSpace.size());

        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
//
//        for(Quartet<Event,Pair<OutputEvent,Double>,StateMatrix,StateMatrix> quartet : eventSpace){
//            System.out.println(quartet.getValue0() +" "+ quartet.getValue1());
//            quartet.getValue2().stateChangePrint(quartet.getValue3());
//        }

        System.out.format("%d samples collected in %d ms\n", 100000, duration/1000000);
        //solve_soln.printTransientState(0, 100);
    }

    public static Network ex1() {
        /*  M/M/1 queue
         */
        Network model = new Network("MM1LowU");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrival(openClass, new Exp(2));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(10));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        return model;
    }

    public static Network ex2() {
        /*  M/M/2 queue
         */
        Network model = new Network("MM2HighU");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrival(openClass, new Exp(8));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(10));
        queue.setNumberOfServers(1);
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        return model;
    }

    public static Network ex3() {
        /*  3 markovian queues in series
         */
        Network model = new Network("3 Series");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrival(openClass, new Exp(8));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Exp(12));
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        queue2.setService(openClass, new Exp(11));
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);
        queue3.setService(openClass, new Exp(10));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue1,queue2,queue3,sink));

        return model;
    }

    public static Network ex4() {
        /*  3 queues in parallel with Erlang-distributed service times
         */
        Network model = new Network("Parallel Erlang");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrival(openClass, new Exp(30));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Erlang(12, 3));
        queue1.setNumberOfServers(3);
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.LCFS);
        queue2.setService(openClass, new Erlang(12,3));
        queue2.setNumberOfServers(3);
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.SIRO);
        queue3.setService(openClass, new Erlang(12,3));
        queue3.setNumberOfServers(3);
        Sink sink = new Sink(model, "Sink");
        
        
        RoutingMatrix routingMatrix = new RoutingMatrix(model, Arrays.asList(openClass),
                Arrays.asList(source, queue1, queue2, queue3, sink));
        routingMatrix.addConnection(source, queue1);
        routingMatrix.addConnection(queue1, sink);
        routingMatrix.addConnection(source, queue2);
        routingMatrix.addConnection(queue2, sink);
        routingMatrix.addConnection(source, queue3);
        routingMatrix.addConnection(queue3, sink);
        model.link(routingMatrix);


        return model;
    }

    public static Network ex5() {
        /* A closed network of 3 queues
         */
        Network model = new Network("3 Closed");
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);

        ClosedClass closedClass = new ClosedClass(model, "Closed Class", 3, queue1);

        queue1.setService(closedClass, new Exp(1));
        queue2.setService(closedClass, new Exp(2));
        queue3.setService(closedClass, new Exp(3));

        model.link(model.serialRouting(queue1, queue2, queue3));
        return model;
    }

    public static Network ex6() {
        /*An M/M/1/10 queue
         */
        Network model = new Network("MM1 10");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrival(openClass, new Exp(8));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(10));
        queue.setCap(10);
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        return model;
    }

    public static Network matlabExample3(){
        Network model = new Network("MRP");
        Delay delay = new Delay(model, "Working State");
        Queue queue = new Queue(model, "RepairQueue", SchedStrategy.PS);
        queue.setNumberOfServers(1);

        ClosedClass closedClass = new ClosedClass(model, "ClosedClass", 3, delay);
        delay.setService(closedClass, new Exp(0.5));
        queue.setService(closedClass, new Exp(4.0));
        model.link(model.serialRouting(delay, queue));
        return model;
    }

    public static Network ex7() {
        /* A queue with two different open classes
         */
        Network model = new Network("2CDSDC");
        OpenClass openClass1 = new OpenClass(model, "Open 1");
        OpenClass openClass2 = new OpenClass(model, "Open 2");
        Source source = new Source(model,"Source");
        source.setArrival(openClass1, new Exp(8));
        source.setArrival(openClass2, new Exp(5));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass1, new Exp(12));
        queue.setService(openClass2, new Exp(16));
        queue.setClassCap(openClass1, 5);
        queue.setClassCap(openClass2, 3);
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        return model;
    }

    public static Network ex8() {
        Network model = new Network("2CDSDC");
        Delay Node1 = new Delay(model, "Delay");
        Queue Node2 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        ClosedClass closedClass1 = new ClosedClass(model, "Closed 1", 10, Node1,0);

        Node1.setService(closedClass1, new Exp(1));
        Node2.setService(closedClass1, new Exp(0.6666667));

        RoutingMatrix routingMatrix = new RoutingMatrix(model, Arrays.asList(closedClass1),
                Arrays.asList(Node1, Node2));
        routingMatrix.addConnection(Node1, Node1, closedClass1,0.7);
        routingMatrix.addConnection(Node1, Node2, closedClass1,0.3);
        routingMatrix.addConnection(Node2, Node1, closedClass1,1.0);
        routingMatrix.addConnection(Node2, Node2, closedClass1,0.0);
        model.link(routingMatrix);
        return model;
    }

    public static Network ex9 () {
        Network model = new Network("ForkJoin");

        Source source = new Source(model,"Src");
        Fork fork = new Fork(model);
        Queue upper = new Queue(model, "upper Q");
        Queue lower = new Queue(model, "Lower Q");
        Join join = new Join(model);
        Queue post = new Queue(model, "Post");
        Sink sink = new Sink(model, "Sink");

        OpenClass openClass = new OpenClass(model, "oclass");

        source.setArrival(openClass, new Exp(10));
        upper.setService(openClass, new Exp(6));
        lower.setService(openClass, new Exp(8));
        post.setService(openClass, new Exp(15));

        RoutingMatrix routingMatrix = new RoutingMatrix(model, Arrays.asList(openClass),
                Arrays.asList(source, fork, upper, lower, join, post, sink));
        routingMatrix.addConnection(source, fork, openClass,1);
        routingMatrix.addConnection(fork, upper, openClass,0.5);
        routingMatrix.addConnection(fork, lower, openClass,0.5);
        routingMatrix.addConnection(upper, join, openClass,1);
        routingMatrix.addConnection(lower, join, openClass,1);
        routingMatrix.addConnection(join, post, openClass,1);
        routingMatrix.addConnection(post, sink, openClass,1);
        model.link(routingMatrix);
        return model;
    }

    public static Network ex10() {
        Network model = new Network("closed net");
        Queue Node1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        Queue Node2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        ClosedClass closedClass1 = new ClosedClass(model, "Closed 1", 6, Node1,0);

        Node1.setService(closedClass1, new Exp(3));
        Node2.setService(closedClass1, new Exp(5));

        model.link(model.serialRouting(Node1, Node2));
        return model;
    }

    public static Network ex11() {
        /*  3 queues in parallel with Erlang-distributed service times
         */
        Network model = new Network("Parallel Erlang");
        OpenClass openClass = new OpenClass(model, "Open Class 1");
        OpenClass oClass2 = new OpenClass(model, "Open Class 2");
        Source source = new Source(model,"Source");
        source.setArrival(openClass, new Exp(30));
        source.setArrival(oClass2, new Exp(10));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Erlang(12, 3));
        queue1.setService(oClass2, new Erlang(15, 2));
        queue1.setNumberOfServers(3);
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.PS);
        queue2.setService(openClass, new Erlang(12,3));
        queue2.setService(oClass2, new Erlang(15, 2));
        queue2.setNumberOfServers(3);
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.SIRO);
        queue3.setService(openClass, new Erlang(12,3));
        queue3.setService(oClass2, new Erlang(15, 2));
        queue3.setNumberOfServers(3);
        Sink sink = new Sink(model, "Sink");


        RoutingMatrix routingMatrix = new RoutingMatrix(model, Arrays.asList(openClass, oClass2),
                Arrays.asList(source, queue1, queue2, queue3, sink));
        routingMatrix.addConnection(source, queue1);
        routingMatrix.addConnection(queue1, sink);
        routingMatrix.addConnection(source, queue2);
        routingMatrix.addConnection(queue2, sink);
        routingMatrix.addConnection(source, queue3);
        routingMatrix.addConnection(queue3, sink);
        model.link(routingMatrix);

        return model;
    }
}
