package jline.solvers.ssa.events;

import jline.lang.JLineMatrix;
import jline.lang.distributions.BinomialDistribution;
import jline.lang.distributions.Exp;
import jline.lang.distributions.PoissonDistribution;
import jline.lang.nodes.Node;
import jline.lang.nodes.Source;
import jline.solvers.ssa.TauLeapingType;
import jline.solvers.ssa.TauLeapingVarType;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.metrics.IllegalTauLeapPercentageMetric;
import jline.solvers.ssa.state.StateMatrix;
import jline.solvers.ssa.strategies.TauLeapingOrderStrategy;
import jline.solvers.ssa.strategies.TauLeapingStateStrategy;
import jline.util.Cdf;
import jline.util.Pair;
import jline.lang.OutputStrategy;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.*;

public class EventStack {
    /*
        EventStack -
            Manages the following:
            1. Which event(s) fire(s) next?
            2. Firing the next event(s)
            3. Tracking progress of time
            4. Configuration of tau leaping
     */
    protected List<Event> eventList;

    protected boolean fixedEventOrder;
    protected TauLeapingType tauLeapingType;

    protected double curT;
    protected int tauFailures;

    protected IllegalTauLeapPercentageMetric illegalTauLeapPercentage;

    public EventStack() {
        this.eventList = new ArrayList<Event>();
        //this.eventList = new LinkedList<Event>();
        this.fixedEventOrder = false;
        this.tauLeapingType = null;

        this.tauFailures = 0;
        this.illegalTauLeapPercentage = new IllegalTauLeapPercentageMetric();
    }

    public double getIllegalTauLeapPercentage() {
        return this.illegalTauLeapPercentage.getMetric();
    }

    public void configureTauLeap(TauLeapingType tauLeapingType) {
        this.tauLeapingType = tauLeapingType;
    }

    public void addEvent(Event event) {
        this.eventList.add(event);
    }

    private void handleImmediate(StateMatrix stateMatrix, Timeline timeline, double t, Random random) {
        /*
            Once an immediate event has been found, try to find any others. Build a cdf, and fire.
         */
        Cdf<Event> immediateCdf = new Cdf<Event>(random);
        double totalImmediate = 0;

        for (Event event : this.eventList) {
            if (event.getRate(stateMatrix) == Double.POSITIVE_INFINITY) {
                totalImmediate += 1.0;
                immediateCdf.addElement(event, 1.0);
            }
        }

        immediateCdf.normalize(totalImmediate);

        Event e = immediateCdf.generate();
        e.stateUpdate(stateMatrix, random, timeline);
        if (timeline != null) {
            //timeline.record(t, e, stateMatrix);
        }
    }

    private void orderEventList(Random random) {
        /*
                For DirectedGraph and DirectedCycle ordering methods, re-order the event list according to a topological
                    sort.
         */
        List<Event> outEvents = new ArrayList<Event>();
        Queue<Pair<Node, Event>> candidateEvents = new LinkedList<Pair<Node, Event>>();
        List<Pair<Node, Event>> nodeEvents = new LinkedList<Pair<Node, Event>>();

        // Add any events that don't correspond to a node, first
        for (Event event : this.eventList) {
            if (event instanceof NodeEvent) {
                Node node = ((NodeEvent)event).getNode();
                nodeEvents.add(new Pair(node, event));
            } else {
                outEvents.add(event);
            }
        }

        Collections.shuffle(outEvents, random);

        Iterator<Pair<Node,Event>> eventIterator = nodeEvents.iterator();
        // Iterate through each event, add any that correspond to a Source or a reference station to canidateEvents
        while (eventIterator.hasNext()) {
            Pair<Node,Event> iterPair = eventIterator.next();
            Node eventNode = iterPair.getLeft();

            if ((eventNode instanceof Source) || (eventNode.isRefstat())) {
                candidateEvents.add(iterPair);
                eventIterator.remove();
            }
        }

        // Dequeue any events in candidateEvent, add any new events that are further in the topology, and append it
        //   to outEvents
        while(!candidateEvents.isEmpty()) {
            Pair<Node,Event> iterPair = candidateEvents.remove();
            List<OutputStrategy> outputStrategies = iterPair.getLeft().getOutputStrategies();
            List<Node> outNodes = new ArrayList<Node>();

            for (OutputStrategy outputStrategy : outputStrategies) {
                Node dest = outputStrategy.getDestination();
                if (!outNodes.contains(dest)) {
                    outNodes.add(dest);
                }
            }

            Event event = iterPair.getRight();
            outEvents.add(event);
            eventIterator = nodeEvents.iterator();

            while(eventIterator.hasNext()) {
                Pair<Node, Event> iterPair2 = eventIterator.next();
                Node eventNode = iterPair2.getLeft();
                event = iterPair2.getRight();

                if (outNodes.contains(eventNode)) {
                    candidateEvents.add(iterPair2);
                    eventIterator.remove();
                }
            }
        }

        // Add any remaining events
        for (Pair<Node, Event> iterPair : nodeEvents) {
            outEvents.add(iterPair.getRight());
        }

        this.eventList = outEvents;
    }

    public double tauLeapUpdateMultistep(StateMatrix stateMatrix, Timeline timeline, double t, Random random) {
        /*
                This is part of an optimization, but I've seperated TauLeapUpdate into two functions, one for TwoTimes
                and one for all other ordering strategies. 99% of this is a simple copy+paste from an earlier iteration.

                This is because TwoTimes needs to keep track of unapplied iterations for all events.
         */
        List<Pair<Event, Integer>> eventPairs = new ArrayList<Pair<Event, Integer>>();

        double tau = this.tauLeapingType.getTau();
        boolean foundEvent = false;

        TauLeapingVarType varType = this.tauLeapingType.getVarType();
        TauLeapingOrderStrategy orderStrategy = this.tauLeapingType.getOrderStrategy();
        TauLeapingStateStrategy stateStrategy = this.tauLeapingType.getStateStrategy();

        // Handle any ordering issues
        if (orderStrategy == TauLeapingOrderStrategy.RandomEventFixed) {
            if (!this.fixedEventOrder) {
                Collections.shuffle(this.eventList, random);
                this.fixedEventOrder = true;
            }
        } else if (orderStrategy == TauLeapingOrderStrategy.DirectedGraph) {
            if (!this.fixedEventOrder) {
                this.orderEventList(random);
                this.fixedEventOrder = true;
            }
        } else if (orderStrategy == TauLeapingOrderStrategy.RandomEvent) {
            Collections.shuffle(this.eventList, random);
        } else if (orderStrategy == TauLeapingOrderStrategy.DirectedCycle) {
            if (!this.fixedEventOrder) {
                this.orderEventList(random);
                this.fixedEventOrder = true;
            }

            // cycle through directed graph
            Event first = this.eventList.get(0);
            this.eventList.remove(0);
            this.eventList.add(first);
        }

        // Build a list of all events, as well as firing counts
        for (Event event : this.eventList) {
            double eventRate = event.getRate(stateMatrix);
            if (eventRate == Double.POSITIVE_INFINITY) {
                this.handleImmediate(stateMatrix, timeline, t, random);
                return t;
            } else if (Double.isNaN(eventRate)) {
                continue;
            }

            foundEvent = true;

            int eCount = 0;

            final int MAX_BINOMIAL_REPS = 1000;

            TauLeapingVarType effectiveVarType = varType;

            if (effectiveVarType == TauLeapingVarType.Binomial) {
                int maxReps = event.getMaxRepetitions(stateMatrix);
                if (maxReps > MAX_BINOMIAL_REPS) {
                    effectiveVarType = TauLeapingVarType.Poisson;
                }
            }

            if (effectiveVarType == TauLeapingVarType.Poisson) {
                PoissonDistribution pDist = new PoissonDistribution(eventRate*tau);
                eCount = pDist.getRealization(random);
            } else if (effectiveVarType == TauLeapingVarType.Binomial) {
                int maxJump = event.getMaxRepetitions(stateMatrix);
                double prob = (eventRate*tau)/((double)maxJump);

                prob = Math.min(prob, 1.0);
                if (prob != 0) {
                    BinomialDistribution bDist = new BinomialDistribution(prob, maxJump);
                    eCount = bDist.getRealization(random);
                }
            }

            if (eCount == 0) {
                continue;
            }

            eventPairs.add(new Pair<Event, Integer>(event, eCount));
        }

        if (!foundEvent) {
            System.out.println("No event found!");
            return t;
        }

        t += tau;
        this.curT = t;
        timeline.setNextTime(this.curT);


        // We know that this will only run twice, but it might be
        //      a bit better to make this a variable for future development
        int nTimes = 1;
        if (stateStrategy == TauLeapingStateStrategy.TwoTimes) {
            nTimes = 2;
        }

        for (int timeIter = 0; timeIter < nTimes; timeIter++) {
            Iterator<Pair<Event, Integer>> eventPairIterator = eventPairs.listIterator();
            List<Pair<Event, Integer>> nextList = new ArrayList<Pair<Event, Integer>>();
            while (eventPairIterator.hasNext()) {
                Pair <Event,Integer> eventPair = eventPairIterator.next();
                Event event = eventPair.getLeft();
                int eCount = eventPair.getRight();


                int rem = event.stateUpdateN(eCount, stateMatrix, random, timeline);

                if ((stateStrategy == TauLeapingStateStrategy.TwoTimes) && (timeIter==0)) {
                    // build another list here - it's a bit faster to build two lists rather than keeping track of
                    //   the end-point
                    nextList.add(new Pair<Event, Integer>(event, rem));
                }

                if ((eCount-rem) == 0) {
                    continue;
                }

                if (rem == 0) {
                    this.illegalTauLeapPercentage.addSuccessful();
                } else {
                    this.illegalTauLeapPercentage.addIllegal();
                }
            }

            // reset the list..
            eventPairs = nextList;
        }

        timeline.recordCache();

        return t;
    }

    public double tauLeapUpdateOnestep(StateMatrix stateMatrix, Timeline timeline, double t, Random random) {
        double tau = this.tauLeapingType.getTau();
        boolean foundEvent = false;
        final int MAX_BINOMIAL_REPS = 1000;

        TauLeapingVarType varType = this.tauLeapingType.getVarType();
        TauLeapingOrderStrategy orderStrategy = this.tauLeapingType.getOrderStrategy();
        TauLeapingStateStrategy stateStrategy = this.tauLeapingType.getStateStrategy();

        // apply ordering changes
        if (orderStrategy == TauLeapingOrderStrategy.RandomEventFixed) {
            if (!this.fixedEventOrder) {
                Collections.shuffle(this.eventList, random);
                this.fixedEventOrder = true;
            }
        } else if ((orderStrategy == TauLeapingOrderStrategy.DirectedGraph) ||
                (orderStrategy == TauLeapingOrderStrategy.DirectedCycle)) {
            if (!this.fixedEventOrder) {
                this.orderEventList(random);
                this.fixedEventOrder = true;
            }
        } else if (orderStrategy == TauLeapingOrderStrategy.RandomEvent) {
            Collections.shuffle(this.eventList, random);
        }

        if (orderStrategy == TauLeapingOrderStrategy.DirectedCycle) {
            // cycle through directed graph;
            Event first = this.eventList.get(0);
            this.eventList.remove(0);
            this.eventList.add(first);
        }

        /*if (stateStrategy == TauLeapingStateStrategy.CycleCutoff) {
            stateMatrix.allowIllegalStates();
        } else*/ if ((stateStrategy == TauLeapingStateStrategy.TimeWarp) || (stateStrategy == TauLeapingStateStrategy.TauTimeWarp)) {
            stateMatrix.cacheState();
        }

        t += tau;
        this.curT = t;
        timeline.setNextTime(this.curT);

        // loop through each event, calculate number of reps, and fire accordingly
        for (Event event : this.eventList) {
            double eventRate = event.getRate(stateMatrix);
            if (eventRate == Double.POSITIVE_INFINITY) {
                this.handleImmediate(stateMatrix,timeline, t, random);
                return t;
            } else if (Double.isNaN(eventRate)) {
                continue;
            }

            int eCount = 0;

            if (varType == TauLeapingVarType.Poisson) {
                PoissonDistribution pDist = new PoissonDistribution(eventRate*tau);
                eCount = pDist.getRealization(random);
            } else if (varType == TauLeapingVarType.Binomial) {
                int maxJump = event.getMaxRepetitions(stateMatrix);
                if (maxJump > MAX_BINOMIAL_REPS) {
                    PoissonDistribution pDist = new PoissonDistribution(eventRate*tau);
                    eCount = pDist.getRealization(random);
                } else {
                    double prob = (eventRate * tau) / ((double) maxJump);

                    prob = Math.min(prob, 1.0);
                    if (prob != 0) {
                        BinomialDistribution bDist = new BinomialDistribution(prob, maxJump);
                        eCount = bDist.getRealization(random);
                    }
                }
            }

            if (eCount == 0) {
                continue;
            }

            // Find the number of unapplied reps. Note the max - this is necessary because on some occasions
            //    you'll receive a negative number when CycleCutoff is applied
            int rem = Math.max(event.stateUpdateN(eCount, stateMatrix, random, timeline),0);

            if (stateStrategy == TauLeapingStateStrategy.TimeWarp) {
                if (rem != 0) {
                    if (this.tauFailures == 2) {
                        this.illegalTauLeapPercentage.addIllegal();
                        this.tauFailures = 0;
                    } else {
                        this.tauFailures++;
                        stateMatrix.revertToCache();
                        timeline.clearCache();
                        return t - tau;
                    }
                } else {
                    this.illegalTauLeapPercentage.addSuccessful();
                    this.tauFailures = 0;
                }
            } else if (stateStrategy == TauLeapingStateStrategy.TauTimeWarp) {
                if (rem != 0) {
                    if (this.tauFailures == 2) {
                        this.illegalTauLeapPercentage.addIllegal();
                        this.tauFailures = 0;
                        tauLeapingType.resetTau();
                    } else {
                        stateMatrix.revertToCache();
                        timeline.clearCache();
                        tauLeapingType.setTau(Math.max(tau / 2, 0.0001));
                        this.tauFailures++;
                        return t - tau;
                    }
                } else {
                    this.illegalTauLeapPercentage.addSuccessful();
                    this.tauFailures = 0;
                    tauLeapingType.resetTau();
                }
            } else if ((eCount-rem) == 0) {
                continue;
            } else if (rem != 0) {
                this.illegalTauLeapPercentage.addIllegal();
            } else {
                this.illegalTauLeapPercentage.addSuccessful();
            }

        }

        /*if (stateStrategy == TauLeapingStateStrategy.CycleCutoff) {
            stateMatrix.forbidIllegalStates();
        }*/

        timeline.recordCache();

        return t;
    }


    public double tauLeapUpdate(StateMatrix stateMatrix, Timeline timeline, double t, Random random) {
        // Wrapper function for a cleaner API
        if (tauLeapingType.getStateStrategy() == TauLeapingStateStrategy.TwoTimes) {
            return tauLeapUpdateMultistep(stateMatrix, timeline, t, random);
        } else {
            return tauLeapUpdateOnestep(stateMatrix, timeline, t, random);
        }
    }

    public double updateState(StateMatrix stateMatrix, Timeline timeline, double t, Random random) {
        /*
            This uses the generic Gillespie algorithm to determine and fire the next event
         */
        Cdf<Event> eventCdf = new Cdf<Event>(random);
        double totalRate = 0;

        boolean foundEvent = false;

        for (Event event : this.eventList) {
            double eventRate = event.getRate(stateMatrix);
            if (eventRate == Double.POSITIVE_INFINITY) {
                this.handleImmediate(stateMatrix,timeline, t, random);
                return t;
            } else if (Double.isNaN(eventRate)) {
                continue;
            }

            foundEvent = true;

            totalRate += eventRate;
            eventCdf.addElement(event, eventRate);
        }

        eventCdf.normalize(totalRate);

        if (!foundEvent) {
            System.out.println("No event found!");
            return t;
        }

        double timeDelta = Math.log(1-random.nextDouble())/(-totalRate);
        t += timeDelta;
        this.curT = t;
        timeline.setTime(this.curT);

        Event chosenEvent = eventCdf.generate();
        chosenEvent.stateUpdate(stateMatrix, random, timeline);

        return t;
    }

    public double updateStateSpace(Timeline timeline, double t, Random random, ArrayList<StateMatrix> stateSpace, Queue<StateMatrix> queue) {
        while (!queue.isEmpty()){

            StateMatrix stateMatrix1 = new StateMatrix(queue.remove());

            Cdf<Event> eventCdf = new Cdf<Event>(random);
            double totalRate = 0;

            boolean foundEvent = false;

            for (Event event : this.eventList) {
                double eventRate = event.getRate(stateMatrix1);
                if (eventRate == Double.POSITIVE_INFINITY) {
                    this.handleImmediate(stateMatrix1, timeline, t, random);
                    return t;
                } else if (Double.isNaN(eventRate)) {
                    continue;
                }

                foundEvent = true;

                totalRate += eventRate;

                eventCdf.addElement(event, eventRate);
            }

            eventCdf.normalize(totalRate);

            if (!foundEvent) {
//            System.out.println("No event found!");
                return t;
            }

            double timeDelta = Math.log(1 - random.nextDouble()) / (-totalRate);
            t += timeDelta;
            this.curT = t;
            timeline.setTime(this.curT);


            ArrayList<Event> eventArrayList = eventCdf.getPossibleEvents();
//            System.out.println(eventArrayList.size());
            for (Event event : eventArrayList) {
//                System.out.println(event);
                StateMatrix newMatrix = event.getNextState(stateMatrix1, timeline, stateSpace,queue);
//                JLineMatrix jLineMatrix1 = new JLineMatrix(stateMatrix1.state.length, stateMatrix1.state[0].length);
//                jLineMatrix1.array2DtoJLineMatrix(newMatrix.state).print();
//                if (newMatrix != null && !newMatrix.checkIfVisited(stateSpace)) {
//                    JLineMatrix jLineMatrix = new JLineMatrix(stateMatrix1.state.length, stateMatrix1.state[0].length);
//                    stateSpace.add(newMatrix);
//                    queue.add(newMatrix);
//                    jLineMatrix.array2DtoJLineMatrix(newMatrix.state).print();
//                    stateMatrix1.printStateVector();
//                    newMatrix.printStateVector();
//                }
            }
        }

        return t;

    }

    public double updateEventSpace(Timeline timeline, double t, Random random, ArrayList<Quartet<Event,Pair<OutputEvent,Double>,StateMatrix,StateMatrix>> eventSpace, Queue<StateMatrix> queue) {
        while (!queue.isEmpty()){

            StateMatrix stateMatrix1 = new StateMatrix(queue.remove());

            Cdf<Event> eventCdf = new Cdf<Event>(random);
            double totalRate = 0;

            boolean foundEvent = false;

            for (Event event : this.eventList) {
                double eventRate = event.getRate(stateMatrix1);
                if (eventRate == Double.POSITIVE_INFINITY) {
                    this.handleImmediate(stateMatrix1, timeline, t, random);
                    return t;
                } else if (Double.isNaN(eventRate) || eventRate==0) {
                    continue;
                }

                foundEvent = true;

                totalRate += eventRate;

                eventCdf.addElement(event, eventRate);
            }

            eventCdf.normalize(totalRate);

            if (!foundEvent) {
//            System.out.println("No event found!");
                return t;
            }

            double timeDelta = Math.log(1 - random.nextDouble()) / (-totalRate);
            t += timeDelta;
            this.curT = t;
            timeline.setTime(this.curT);


            ArrayList<Event> eventArrayList = eventCdf.getPossibleEvents();
            for (Event event : eventArrayList) {
                StateMatrix copyOfOld = new StateMatrix(stateMatrix1);
                StateMatrix newMatrix = event.getNextEventState(stateMatrix1, timeline, eventSpace,event,queue,stateMatrix1);
//                if (newMatrix != null && !newMatrix.checkIfVisited(eventSpace,stateMatrix1,event)) {
//                    JLineMatrix jLineMatrix = new JLineMatrix(stateMatrix1.state.length, stateMatrix1.state[0].length);
//                    eventSpace.add(Triplet.with(event,stateMatrix1,newMatrix));
//                    queue.add(newMatrix);
//                    jLineMatrix.array2DtoJLineMatrix(newMatrix.state).print();
//                }
            }
        }

        return t;

    }

}
