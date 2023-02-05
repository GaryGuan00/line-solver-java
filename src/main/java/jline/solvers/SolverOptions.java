// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package jline.solvers;

import jline.lang.JLineMatrix;
import jline.lang.constant.SolverType;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.POSITIVE_INFINITY;

public class SolverOptions {

  public enum VerboseLevel {
    SILENT,
    STD,
    DEBUG
  }

  public static class Config {

    public String highVar; // TODO: enum?
    public String multiServer; // TODO: enum?
    public String np_priority; // TODO: enum?
    public List<Double> pStar; // For p-norm smoothing in SolverFluid
  }

  public static class ODESolvers {

    public FirstOrderIntegrator fastODESolver;
    public FirstOrderIntegrator accurateODESolver;
    public FirstOrderIntegrator fastStiffODESolver;
    public FirstOrderIntegrator accurateStiffODESolver;
  }

  public boolean cache;
  public double cutoff;
  public Config config;
  public boolean force;
  public boolean hide_immediate;
  public JLineMatrix init_sol;
  public int iter_max;
  public double iter_tol;
  public double tol;
  public boolean keep;
  public String method;
  public boolean remote;
  // TODO: remote_endpoint
  public double odeMinStep;
  public double odeMaxStep;
  public ODESolvers odeSolvers;
  public int samples;
  public int seed;
  public boolean stiff;
  public double[] timespan;
  public VerboseLevel verbose;

  public SolverOptions() {

    // Solver Default Options
    this.cache = true;
    this.cutoff = POSITIVE_INFINITY;
    this.config = new Config();
    this.config.pStar = new ArrayList<>();
    this.force = false;
    this.hide_immediate = true; // Hide immediate transitions if possible
    this.init_sol = new JLineMatrix(0, 0);
    this.iter_max = 10;
    this.iter_tol = 0.0001; // Convergence tolerance to stop iterations
    this.tol = 0.0001; // Tolerance for all other uses
    this.keep = false;
    this.method = "default";
    this.remote = false;
    // TODO: this.remote_endpoint = '127.0.0.1';

    this.odeMinStep = 0.001;
    //this.odeMinStep = 0.00000001;
    this.odeMaxStep = POSITIVE_INFINITY;
    this.odeSolvers = new ODESolvers();
    this.odeSolvers.fastODESolver = null; // TODO
    this.odeSolvers.accurateODESolver =
	new DormandPrince54Integrator(odeMinStep, odeMaxStep, tol, tol);
    this.odeSolvers.fastStiffODESolver = null; // TODO
    this.odeSolvers.accurateStiffODESolver = null; // TODO

    this.samples = 10000;
    // TODO: this.seed = randi([1,1e6]);
    this.stiff = true;
    this.timespan = new double[2];
    this.timespan[0] = POSITIVE_INFINITY;
    this.timespan[1] = POSITIVE_INFINITY;
    this.verbose = VerboseLevel.STD;
  }

  public SolverOptions(SolverType solverType) {

    this();

    // Solver-specific Defaults
    switch (solverType) {
      case ENV:
        this.iter_max = 100;
        this.verbose = VerboseLevel.SILENT;
        break;
      case FLUID:
        this.config.highVar = "none";
        this.iter_max = 5;
        this.timespan[0] = 0;
        break;
      case LN:
        this.iter_tol = 0.05;
        this.iter_max = 100;
        break;
      case LQNS:
        this.keep = true;
        break;
      case MAM:
        this.iter_max = 100;
        break;
      case MVA:
        this.iter_max = 1000;
        this.config.highVar = "none";
        this.config.multiServer = "default";
        this.config.np_priority = "default";
        break;
      case NC:
        this.samples = 100000;
        this.config.highVar = "interp";
        break;
      case SSA:
        this.timespan[0] = 0;
        break;
      default: // Global options unless overridden by a solver
    }
  }

  public void setODEMinStep(double odeMinStep) {
    this.odeMinStep = odeMinStep;
    this.odeSolvers.fastODESolver = null; // TODO
    this.odeSolvers.accurateODESolver =
            new DormandPrince54Integrator(this.odeMinStep, odeMaxStep, tol, tol);
    this.odeSolvers.fastStiffODESolver = null; // TODO
    this.odeSolvers.accurateStiffODESolver = null; // TODO
  }

  public void setODEMaxStep(double odeMaxStep) {
    this.odeMaxStep = odeMaxStep;
    this.odeSolvers.fastODESolver = null; // TODO
    this.odeSolvers.accurateODESolver =
            new DormandPrince54Integrator(odeMinStep, this.odeMaxStep, tol, tol);
    this.odeSolvers.fastStiffODESolver = null; // TODO
    this.odeSolvers.accurateStiffODESolver = null; // TODO
  }
}
