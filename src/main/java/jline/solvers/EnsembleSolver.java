// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package jline.solvers;

import jline.lang.Ensemble;
import jline.lang.Network;
import jline.lang.constant.SolverType;

import java.util.*;

// Abstract class for solvers applicable to Ensemble models
public abstract class EnsembleSolver extends Solver {

  protected Network[] ensemble;

  protected NetworkSolver[] solvers;

  protected Map<Integer, Map<Integer, SolverResult>> results;

  protected EnsembleSolver(Ensemble ensModel, String name, SolverOptions options) {
    super(name);
    this.options = options; // TODO: self.setOptions(options);
    this.ensemble = ensModel.getEnsemble().toArray(new Network[0]);
    this.solvers = new NetworkSolver[ensemble.length];
    this.results = new HashMap<>();
  }

  protected EnsembleSolver(Ensemble ensModel, String name) {
    this(ensModel, name, EnsembleSolver.defaultOptions());
  }

  // True if model is supported by the solver
  protected abstract boolean supports(Ensemble model);

  protected abstract void getEnsembleAvg();

  // Operations before starting to iterate
  protected abstract void init();

  // Operations before an iteration
  protected abstract void pre(int it);

  // Operations within an iteration
  protected abstract void analyze(int e);

  // Operations after an iteration
  protected abstract void post(int it);

  // Operations after iterations are completed
  protected abstract void finish();

  // Convergence test at iteration it
  protected abstract boolean converged(int it);

  private int getIteration() {
    return results.size();
  }

  public int getNumberOfModels() {
    return ensemble.length;
  }

  protected void iterate() {

    long outerStartTime = System.nanoTime();
    int it = 0;
    int E = getNumberOfModels();
    List<Double> solveRuntimes = new ArrayList<>();
    List<Double> synchRuntimes = new ArrayList<>();
    double totalRuntime = 0.0;

    init();

    while (!converged(it) && it < options.iter_max) {
      it++; // Starts at Iteration 1
      pre(it);
      long solveStartTime = System.nanoTime();
      if (Objects.equals(options.method, "para")) {
        // TODO: add in parallel solving
      } else {
        for (int e = 0; e < E; e++) {
          analyze(e);
          if (results.containsKey(it)) {
            results.get(it).put(e, solvers[e].result.deepCopy());
          } else {
            HashMap<Integer, SolverResult> modelIdxToResultMap = new HashMap<>();
            modelIdxToResultMap.put(e, solvers[e].result.deepCopy());
            results.put(it, modelIdxToResultMap);
          }
        }
      }

      if (options.verbose != SolverOptions.VerboseType.SILENT) {
        solveRuntimes.add(((System.nanoTime() - solveStartTime) / 1000000000.0));
        totalRuntime = (System.nanoTime() - outerStartTime) / 1000000000.0;
        System.out.format("Iter %d. ", it);
      }

      long synchStartTime = System.nanoTime();
      post(it);
      synchRuntimes.add((System.nanoTime() - synchStartTime) / 1000000000.0);
      if (options.verbose != SolverOptions.VerboseType.SILENT) {
        System.out.format(
            "Analyze time: %fs. Update time: %fs. Runtime: %fs. \n",
            solveRuntimes.get(it - 1), synchRuntimes.get(it - 1), totalRuntime);
      }
    }

    finish();

    double finalRuntime = (System.nanoTime() - outerStartTime) / 1000000000.0;
    if (options.verbose != SolverOptions.VerboseType.SILENT) {
      double totalSolveRuntime = 0;
      double totalSynchRuntime = 0;
      for (Double runtime : solveRuntimes) {
        totalSolveRuntime += runtime;
      }
      for (Double runtime : synchRuntimes) {
        totalSynchRuntime += runtime;
      }
      System.out.format(
          "Summary: Analyze avg time: %fs. Update avg time: %fs. Total runtime: %fs. \n",
          totalSolveRuntime / solveRuntimes.size(),
          totalSynchRuntime / synchRuntimes.size(),
          finalRuntime);
    }
  }

  public void printEnsembleAvgTables() {
    int E = getNumberOfModels();
    for (int e = 0; e < E; e++) {
      solvers[e].getAvgTable();
    }
  }

  // Ensemble Solver options
  public static SolverOptions defaultOptions() {
    return new SolverOptions(SolverType.ENV);
  }

  // NOTE: the following LINE methods have not been migrated to JLINE
  // a) list() - duplicative purpose with getNumberOfModels()
  // b) getSolver() - solvers is public instead of using getter
  // c) setSolver() x 3 - solvers is public so no need for setters
}
