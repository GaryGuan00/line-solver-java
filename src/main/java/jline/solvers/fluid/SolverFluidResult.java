// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package jline.solvers.fluid;

import jline.lang.JLineMatrix;

// Class to store results specific to SolverFluid
public class SolverFluidResult {

  // Results from getAvg
  public JLineMatrix odeStateVec;

  // Results from getCdfRespT and getTranCdfPassT
  public JLineMatrix[][] distribC;
  public double distribRuntime;

  // Results from getProbAggr
  public double Pnir;
  public double logPnir;
}
