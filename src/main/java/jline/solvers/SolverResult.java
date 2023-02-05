// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package jline.solvers;

import jline.lang.JLineMatrix;

// Class to store all (interim) results generic to all Solvers from e.g. runAnalyzer method
// For Solver-specific results, create separate class(es) e.g. SolverFluidResult as an example
public class SolverResult {

  public String method;

  public JLineMatrix QN;
  public JLineMatrix UN;
  public JLineMatrix RN;
  public JLineMatrix TN;
  public JLineMatrix CN;
  public JLineMatrix XN;

  // Note: for transient metrics, the time steps are stored separately in 't' - this differs from
  // LINE where they are stored in adjacent columns to the performance metrics QNt, UNt and TNt.
  // This is less about storage efficiency, and more about time efficiency - getting from and
  // setting to JLineMatrix objects is expensive if done many thousands of times, so storing the
  // time step data just once in a separate object is significantly more efficient than storing
  // multiple times. The row indices can be used to reference between the relevant performance
  // metric and 't' i.e. the value in row 150 in QNt was measured at the time step in row 150 in 't'
  public JLineMatrix[][] QNt;
  public JLineMatrix[][] UNt;
  public JLineMatrix[][] TNt;
  public JLineMatrix t;

  public double runtime;

  public SolverResult deepCopy() {

    SolverResult clone = new SolverResult();

    clone.method = this.method;

    clone.QN = this.QN.clone();
    clone.UN = this.UN.clone();
    clone.RN = this.RN.clone();
    clone.TN = this.TN.clone();
    clone.CN = this.CN.clone();
    clone.XN = this.XN.clone();

    clone.QNt = new JLineMatrix[this.QNt.length][this.QNt[0].length];
    clone.UNt = new JLineMatrix[this.UNt.length][this.UNt[0].length];
    clone.TNt = new JLineMatrix[this.TNt.length][this.TNt[0].length];
    for (int i = 0; i < this.QNt.length; i++) {
      for (int j = 0; j < this.QNt[0].length; j++) {
        clone.QNt[i][j] = this.QNt[i][j].clone();
        clone.UNt[i][j] = this.UNt[i][j].clone();
        clone.TNt[i][j] = this.TNt[i][j].clone();
      }
    }

    clone.t = this.t.clone();
    clone.runtime = this.runtime;

    return clone;
  }
}
