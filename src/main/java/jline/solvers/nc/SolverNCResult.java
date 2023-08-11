package jline.solvers.nc;

import jline.solvers.SolverResult;
import jline.util.Matrix;

public class SolverNCResult extends SolverResult {
  public String solver;
  public Prob prob;

  class Prob {
    public Double logNormConstAggr;
    public Matrix marginal;
  }
}
