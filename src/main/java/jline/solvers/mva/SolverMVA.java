package jline.solvers.mva;

import jline.lang.Network;
import jline.lang.NetworkStruct;
import jline.lang.constant.SolverType;
import jline.solvers.NetworkSolver;
import jline.solvers.SolverOptions;

public class SolverMVA extends NetworkSolver{
	
	public SolverMVA(Network model, SolverOptions options) {
		super(model, "MVA", options);
		this.sn = model.getStruct(false);
		this.result = new SolverMVAResult();
	}
	
	public SolverMVA(Network model) {
		super(model, "MVA");
		this.sn = model.getStruct(false);
		this.result = new SolverMVAResult();
	}
	
	public NetworkStruct getStruct() {
		if (this.sn == null) 
			this.sn = this.model.getStruct(false);
		return this.sn;
	}
	
	public void setStruct(NetworkStruct sn) {
		this.sn = sn;
	}

	@Override
	public void runAnalyzer() throws IllegalAccessException {
		if (this.model == null)
			throw new RuntimeException("Model is not provided");
		if (this.sn == null) 
			this.sn = this.model.getStruct(false);
		if (this.options == null)
			this.options = new SolverOptions(SolverType.MVA);
		
		AMVARunner runner = new AMVARunner(this.sn, this.options);
		this.result = runner.run();
	}
}
