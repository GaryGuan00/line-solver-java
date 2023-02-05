package jline.lang.nodes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ejml.sparse.csc.CommonOps_DSCC;

import jline.lang.*;
import jline.solvers.ssa.events.ArrivalEvent;

public class StatefulNode extends Node implements Serializable {
    private Integer statefulIndex;
    private JLineMatrix state;
    private JLineMatrix statePrior;
    
    public StatefulNode(String name) {
        super(name);
        statefulIndex = null;
        state = new JLineMatrix(0,0,0);
        statePrior = new JLineMatrix(0,0,0);
    }

    protected void clearState()  {
        this.state.reshape(0, 0, 0);
    }

    public int getStatefulIndex() {
        if (this.statefulIndex == null) {
            this.statefulIndex = this.model.getStatefulNodeIndex((Node)this);
        }
        return this.statefulIndex;
    }

    public int getNumberOfServers() {
        return 1;
    }
    
    public JLineMatrix getState(){
    	return this.state;
    }
    
    public void setState(JLineMatrix state) {
    	this.state = state;
    	if (state.getNumRows() != statePrior.getNumRows()) { 		
    		JLineMatrix initPrior = new JLineMatrix(state.getNumRows(), 1, state.getNumRows());
        	initPrior.set(0, 0, 1);
        	this.setStatePrior(initPrior);
    	}
    }
    
    public void setStatePrior(JLineMatrix prior) {
    	this.statePrior = prior;
    	try {
    		if(state.getNumRows() != statePrior.getNumRows())
    			throw new Exception("The prior probability vector must have the same rows of the station state vector");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public JLineMatrix getStatePrior(){
    	return this.statePrior;
    }

    @Override
    public ArrivalEvent getArrivalEvent(JobClass jobClass) {
        if (!this.arrivalEvents.containsKey(jobClass)) {
            this.arrivalEvents.put(jobClass, new ArrivalEvent(this, jobClass));
        }
        return this.arrivalEvents.get(jobClass);
    }
}
