package jline.api;

import java.util.List;
import java.util.Set;

import jline.lang.JLineMatrix;
import jline.util.JLineGraph;

public class UTIL {
	
    public static Set<Set<Integer>> weaklyConnect(JLineMatrix param, Set<Integer> colsToIgnore) {
    	JLineGraph graph = new JLineGraph(param, colsToIgnore);
		graph.computeWCC();
		return graph.getWCC();
    }
    
    public static JLineMatrix oner(JLineMatrix N, List<Integer> r) {
    	JLineMatrix res = N.clone();
    	for(Integer s : r) {
    		if (s >= 0)
    			res.set(s, res.get(s) - 1);
    	}
    	return res;
    }
    
    public static double softmin(double x, double y, double alpha) {
    	return -((-x)*Math.exp(-alpha*x) - y*Math.exp(-alpha*y)) / (Math.exp(-alpha*x) + Math.exp(-alpha*y));
    }
}
