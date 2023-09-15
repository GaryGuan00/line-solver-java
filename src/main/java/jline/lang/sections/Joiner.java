package jline.lang.sections;

import jline.lang.JobClass;
import jline.lang.constant.JoinStrategy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Joiner extends InputSection implements Serializable {
    protected List<JobClass> jobClasses;
    
    public Map<JobClass, JoinStrategy> joinStrategy;
    public Map<JobClass, Double> joinRequired;
    public Map<JobClass, JobClass> joinJobClasses;
    
    public Joiner(List<JobClass> customerClasses) {
        super("Joiner");
        this.joinJobClasses = new HashMap<>();
        this.jobClasses = customerClasses;
        this.joinStrategy = new HashMap<>();
        this.joinRequired = new HashMap<>();
        
        for(JobClass jobclass : this.jobClasses) {
        	this.joinStrategy.put(jobclass, JoinStrategy.STD);
        	this.joinRequired.put(jobclass, -1.0);
            this.joinJobClasses.put(jobclass, jobclass);
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
