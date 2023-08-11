package jline.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import jline.lang.JobClass;
import jline.lang.constant.JoinStrategy;
import jline.lang.constant.ReplacementStrategy;

public class NodeParam implements Serializable {

	// Cache
	public int nitems;
	public Matrix[][] accost;
	public Matrix itemcap;
	public Map<Integer, List<Double>> pread;
	public ReplacementStrategy replacement;
	public Matrix hitclass;
	public Matrix missclass;
	
	//Fork
	public double fanout = Double.NaN;
	
	//Join
	public Map<JobClass, JoinStrategy> joinStrategy = null;
	public Map<JobClass, Double> fanIn = null;
	
	//RoutingStrategy WRROBIN:
	public Map<JobClass, Matrix> weights = null;
	
	//RoutingStrategy RROBIN, WRROBIN:
	public Map<JobClass, Matrix> outlinks = null;

	//RoutingStrategy KCHOICES:
	public Map<JobClass, Matrix> withMemory = null;

	public List<String> joinRequired;

	public String fileName = null;

	public String filePath = null;

	public String startTime = null;
	public String loggerName = null;

	public String timestamp = null;

	public String jobID = null;

	public String jobClass = null;

	public String timeSameClass = null;

	public String timeAnyClass = null;

	public boolean isEmpty() {
		return Double.isNaN(fanout) && joinStrategy == null && fanIn == null && weights == null && outlinks == null;
	}
}
