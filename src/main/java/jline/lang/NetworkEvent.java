package jline.lang;

import java.util.Map;
import java.util.function.Function;

import jline.lang.constant.EventType;
import jline.lang.nodes.Node;
import jline.util.Pair;

public class NetworkEvent {
	
	protected int nodeIdx;
	protected EventType event;
	protected int jobclassIdx;
	protected double prob;
	protected Function<Pair<Map<Node, JLineMatrix>, Map<Node, JLineMatrix>>, Double> probFun;
	protected JLineMatrix state;
	protected double t;
	protected double job;
	
	/*
	 * prob = NaN if not set
	 * t = NaN if not set
	 * job = NaN if not set
	 * state = new JLineMatrix(0,0) if not set
	 */
	public NetworkEvent(EventType event, int nodeIdx, int jobclassIdx, double prob, JLineMatrix state, double t, double job) {
		this.event = event;
		this.nodeIdx = nodeIdx;
		this.jobclassIdx = jobclassIdx;
		this.prob = prob;
		this.state = state;
		this.t = t;
		this.job = job;
		this.probFun = null;
	}
	
	/*
	 * The input probability might be a function
	 */
	public NetworkEvent(EventType event, int nodeIdx, int jobclassIdx, Function<Pair<Map<Node, JLineMatrix>, Map<Node, JLineMatrix>>, Double> probFun, JLineMatrix state, double t, double job) {
		this.event = event;
		this.nodeIdx = nodeIdx;
		this.jobclassIdx = jobclassIdx;
		this.prob = Double.NaN;
		this.state = state;
		this.t = t;
		this.job = job;
		this.probFun = probFun;
	}
	
	public int getNodeIdx() {
		return nodeIdx;
	}
	
	public void setNodeIdx(int nodeIdx) {
		this.nodeIdx = nodeIdx;
	}

	public EventType getEvent() {
		return event;
	}

	public void setEvent(EventType event) {
		this.event = event;
	}

	public int getJobclassIdx() {
		return jobclassIdx;
	}

	public void setJobclassIdx(int jobclassIdx) {
		this.jobclassIdx = jobclassIdx;
	}

	public double getProb() {
		return prob;
	}
	
	public double getProb(Pair<Map<Node, JLineMatrix>, Map<Node, JLineMatrix>> state) {
		return this.probFun.apply(state);
	}

	public void setProb(double prob) {
		this.prob = prob;
	}

	public Function<Pair<Map<Node, JLineMatrix>, Map<Node, JLineMatrix>>, Double> getProbFun() {
		return probFun;
	}

	public void setProbFun(Function<Pair<Map<Node, JLineMatrix>, Map<Node, JLineMatrix>>, Double> probFun) {
		this.probFun = probFun;
	}

	public JLineMatrix getState() {
		return state;
	}

	public void setState(JLineMatrix state) {
		this.state = state;
	}

	public double getT() {
		return t;
	}

	public void setT(double t) {
		this.t = t;
	}

	public double getJob() {
		return job;
	}

	public void setJob(double job) {
		this.job = job;
	}	
}
