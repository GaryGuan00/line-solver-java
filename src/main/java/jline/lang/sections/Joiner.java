package jline.lang.sections;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jline.lang.JobClass;
import jline.lang.Network;
import jline.lang.OutputStrategy;
import jline.lang.constant.JoinStrategy;
import jline.lang.constant.RoutingStrategy;
import jline.lang.nodes.Node;
import jline.solvers.ssa.events.JoinOutputEvent;

public class Joiner extends InputSection implements Serializable {
    protected List<JobClass> jobClasses;
    
    public Map<JobClass, JoinStrategy> joinStrategy;
    public Map<JobClass, Double> joinRequired;
    
    public Joiner(Network model) {
        super("Joiner");
        this.jobClasses = model.getClasses();
        joinStrategy = new HashMap<JobClass, JoinStrategy>();
        joinRequired = new HashMap<JobClass, Double>();
        
        for(JobClass jobclass : this.jobClasses) {
        	joinStrategy.put(jobclass, JoinStrategy.STD);
        	joinRequired.put(jobclass, -1.0);
        }
    }

//    @Override
//    public void setOutputStrategy(JobClass jobClass, RoutingStrategy routingStrategy, Node destination, double probability) {
//        for (OutputStrategy outputStrategy : this.outputStrategies) {
//            if ((outputStrategy.getJobClass() == jobClass) && (outputStrategy.getDestination() == destination)) {
//                outputStrategy.setRoutingStrategy(routingStrategy);
//                outputStrategy.setProbability(probability);
//                this.probabilityUpdate();
//                return;
//            }
//        }
//
//        OutputStrategy outputStrategy = new OutputStrategy(jobClass, routingStrategy, destination, probability);
//        outputStrategies.add(outputStrategy);
//        outputEvents.put(outputStrategy, new JoinOutputEvent(this, destination, jobClass));
//        this.probabilityUpdate();
//    }
}
