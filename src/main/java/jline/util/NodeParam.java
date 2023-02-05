package jline.util;

import java.util.HashMap;
import java.util.Map;

import jline.lang.JLineMatrix;
import jline.lang.JobClass;
import jline.lang.constant.JoinStrategy;
import jline.lang.nodes.Node;

public class NodeParam {
	
	//Fork
	public double fanout = Double.NaN;
	
	//Join
	public Map<JobClass, JoinStrategy> joinStrategy = null;
	public Map<JobClass, Double> fanIn = null;
	
	//RoutingStrategy WRROBIN:
	public Map<JobClass, JLineMatrix> weights = null;
	
	//RoutingStrategy RROBIN, WRROBIN:
	public Map<JobClass, JLineMatrix> outlinks = null;
	
	public boolean isEmpty() {
		if (Double.isNaN(fanout) && joinStrategy == null && fanIn == null && weights == null && outlinks == null)
			return true;
		
		return false;
	}
}
