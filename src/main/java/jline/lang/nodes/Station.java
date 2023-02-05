package jline.lang.nodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.ejml.sparse.csc.CommonOps_DSCC;

import jline.lang.*;
import jline.lang.constant.DropStrategy;
import jline.lang.distributions.DisabledDistribution;
import jline.lang.distributions.Distribution;
import jline.lang.distributions.Immediate;
import jline.lang.distributions.MarkovianDistribution;
import jline.solvers.ssa.events.DepartureEvent;

public class Station extends StatefulNode implements Serializable {
    protected int numberOfServers;
    protected double cap; // double to allow infinite values.
    protected Map<JobClass, Double> classCap;
    protected Map<JobClass, DropStrategy> dropRule;
    protected JLineMatrix lldScaling;
    protected Function<JLineMatrix, Double> lcdScaling;

    protected Map<JobClass, DepartureEvent> departureEvents;

    public Station(String name) {
        super(name);

        this.classCap = new HashMap<JobClass, Double>();
        this.departureEvents = new HashMap<JobClass, DepartureEvent>();

        this.cap = Double.POSITIVE_INFINITY;
        this.dropRule = new HashMap<JobClass, DropStrategy>();
        
        this.lldScaling = new JLineMatrix(0,0);
        this.lcdScaling = null;
    }

    public void setNumberOfServers(int numberOfServers) {
        this.numberOfServers = numberOfServers;
    }

    @Override
    public int getNumberOfServers() {
        return this.numberOfServers;
    }

    public void setCap(double cap) {
        this.cap = cap;
    }

    public void setCap(int cap) {
        this.cap = (double) cap;
    }

    public void setClassCap(JobClass jobClass, double cap) {
        this.classCap.put(jobClass, cap);
    }
    public void setClassCap(JobClass jobClass, int cap) {
        this.classCap.put(jobClass, (double)cap);
    }

    public void setChainCapacity() {

    }

    @Override
    public double getClassCap(JobClass jobClass) {
        if (classCap.containsKey(jobClass)) {
            return Math.min(this.classCap.get(jobClass), cap);
        }

        return cap;
    }

    @Override
    public double getCap() {
        return cap;
    }

    public boolean[] isServiceDefined() {
        throw new RuntimeException("Not Implemented!");
    }

    public boolean isServiceDefined(JobClass j_class)  {
        throw new RuntimeException("Not Implemented!");
    }

    public boolean[] isServiceDisabled()  {
        throw new RuntimeException("Not Implemented!");
    }

    public boolean isServiceDisabled(JobClass j_class)  {
        throw new RuntimeException("Not Implemented!");
    }

    public List<Object> getMarkovianSourceRates()  {
    	int nClasses = this.model.getNumberOfClasses();
    	Map<JobClass, Map<Integer, JLineMatrix>> map = new HashMap<JobClass, Map<Integer, JLineMatrix>>();
    	Map<JobClass, JLineMatrix> mu = new HashMap<JobClass, JLineMatrix>();
    	Map<JobClass, JLineMatrix> phi = new HashMap<JobClass, JLineMatrix>();
    	for(int i = 0; i < nClasses; i++) {
    		Source source = (Source)this;
    		JobClass jobclass = this.model.getClassByIndex(i);
			if (!source.containsJobClass(jobclass)) {
				source.setArrival(jobclass, new DisabledDistribution());
    			JLineMatrix nan_matrix = new JLineMatrix(1,1,1);
    			CommonOps_DSCC.fill(nan_matrix, Double.NaN);
    			Map<Integer, JLineMatrix> tmp = new HashMap<Integer, JLineMatrix>();
    			tmp.put(0, nan_matrix);
    			tmp.put(1, nan_matrix.clone());
    			map.put(jobclass, tmp);
    			mu.put(jobclass, nan_matrix.clone());
    			phi.put(jobclass, nan_matrix.clone());
			} else if (!(source.getArrivalDistribution(jobclass) instanceof DisabledDistribution)) { 
    			//Current JLine only support Exp, Coxian, Erlang, HyperEXP and APH.
    			Distribution distr = source.getArrivalDistribution(jobclass);
				map.put(jobclass, ((MarkovianDistribution)distr).getRepres());
				mu.put(jobclass, ((MarkovianDistribution)distr).getMu());
				phi.put(jobclass, ((MarkovianDistribution)distr).getPhi());
    		} else {
    			JLineMatrix nan_matrix = new JLineMatrix(1,1,1);
    			CommonOps_DSCC.fill(nan_matrix, Double.NaN);
    			Map<Integer, JLineMatrix> tmp = new HashMap<Integer, JLineMatrix>();
    			tmp.put(0, nan_matrix);
    			tmp.put(1, nan_matrix.clone());
    			map.put(jobclass, tmp);
    			mu.put(jobclass, nan_matrix.clone());
    			phi.put(jobclass, nan_matrix.clone());
    		}
    		
    	}
    	return new ArrayList<Object>(Arrays.asList(map, mu, phi));
    }

    public List<Object> getMarkovianServiceRates()  {
    	int nClasses = this.model.getNumberOfClasses();
    	Map<JobClass, Map<Integer, JLineMatrix>> map = new HashMap<JobClass, Map<Integer, JLineMatrix>>();
    	Map<JobClass, JLineMatrix> mu = new HashMap<JobClass, JLineMatrix>();
    	Map<JobClass, JLineMatrix> phi = new HashMap<JobClass, JLineMatrix>();
    	for(int i = 0; i < nClasses; i++) {
    		JobClass jobclass = this.model.getClassByIndex(i);
    		Queue queue = (Queue) this;
    		//Since Delay, Join are all sub-class of Queue, and only queue and source are the sub-class of station, we could cast this to queue to call setService method
    		if(!queue.containsJobClass(jobclass)) {
    			queue.setService(jobclass, new DisabledDistribution());
    			JLineMatrix nan_matrix = new JLineMatrix(1,1,1);
    			CommonOps_DSCC.fill(nan_matrix, Double.NaN);
    			Map<Integer, JLineMatrix> tmp = new HashMap<Integer, JLineMatrix>();
    			tmp.put(0, nan_matrix);
    			tmp.put(1, nan_matrix.clone());
    			map.put(jobclass, tmp);
    			mu.put(jobclass, nan_matrix.clone());
    			phi.put(jobclass, nan_matrix.clone());
    		} else if (queue.getServiceProcess(jobclass) instanceof Immediate) {
    			Distribution distr = this.server.getServiceDistribution(jobclass);
    			JLineMatrix map_matrix_1 = new JLineMatrix(1,1,1);
    			map_matrix_1.set(0, 0, -distr.infRateRep);
    			JLineMatrix map_matrix_2 = new JLineMatrix(1,1,1);
    			map_matrix_2.set(0, 0, distr.infRateRep);
    			JLineMatrix mu_matrix = new JLineMatrix(1,1,1);
    			mu_matrix.set(0, 0, distr.infRateRep);
    			JLineMatrix phi_matrix = new JLineMatrix(1,1,1);
    			phi_matrix.set(0, 0, 1);
    			Map<Integer, JLineMatrix> tmp = new HashMap<Integer, JLineMatrix>();
    			tmp.put(0, map_matrix_1);
    			tmp.put(1, map_matrix_2);
    			map.put(jobclass, tmp);
    			mu.put(jobclass, mu_matrix);
    			phi.put(jobclass, phi_matrix);
    		} else if (!(queue.getServiceProcess(jobclass) instanceof DisabledDistribution)) {
    			//Current JLine only support Exp, Coxian, Erlang, HyperEXP and APH.
    			Distribution distr = this.server.getServiceDistribution(jobclass);
				map.put(jobclass, ((MarkovianDistribution)distr).getRepres());
				mu.put(jobclass, ((MarkovianDistribution)distr).getMu());
				phi.put(jobclass, ((MarkovianDistribution)distr).getPhi());
    		} else {
    			JLineMatrix nan_matrix = new JLineMatrix(1,1,1);
    			CommonOps_DSCC.fill(nan_matrix, Double.NaN);
    			Map<Integer, JLineMatrix> tmp = new HashMap<Integer, JLineMatrix>();
    			tmp.put(0, nan_matrix);
    			tmp.put(1, nan_matrix.clone());
    			map.put(jobclass, tmp);
    			mu.put(jobclass, nan_matrix.clone());
    			phi.put(jobclass, nan_matrix.clone());
    		}
    	}
    	return new ArrayList<Object>(Arrays.asList(map, mu, phi));
    }
    
    public DepartureEvent getDepartureEvent(JobClass jobClass) {
        if (!this.departureEvents.containsKey(jobClass)) {
            this.departureEvents.put(jobClass, new DepartureEvent(this, jobClass));
        }
        return this.departureEvents.get(jobClass);
    }

    @Override
    public boolean isRefstat() {
        for (JobClass jobClass : this.model.getClasses()) {
            if (jobClass instanceof ClosedClass) {
                if (((ClosedClass)jobClass).getRefstat() == this) {
                    return true;
                }
            }
        }

        return false;
    }
       
    public DropStrategy getDropRule(JobClass jobclass) {
    	return this.dropRule.getOrDefault(jobclass, null);
    }
    
    public void setDropRule(JobClass jobclass, DropStrategy drop) {
    	this.dropRule.put(jobclass, drop);
    }

    public void setLimitedLoadDependence(JLineMatrix alpha) {
    	this.lldScaling = alpha;
    }
    
    public JLineMatrix getLimitedLoadDependence() {
    	return this.lldScaling;
    }
    
    public void setLimitedClassDependence(Function<JLineMatrix, Double> gamma) {
    	this.lcdScaling = gamma;
    }
    
    public Function<JLineMatrix, Double> getLimitedClassDependence(){
    	return this.lcdScaling;
    }
}
