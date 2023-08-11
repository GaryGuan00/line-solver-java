package jline.solvers.fluid;

import jline.lang.Network;
import jline.lang.constant.SolverType;
import jline.solvers.SolverOptions;
import jline.solvers.SolverResult;
import org.junit.jupiter.api.Test;

import static jline.examples.FluidModels.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolverFluidOpenExamplesTest {

  static double tol = 0.005; // Ideally should be 0.0001 but some results don't quite match MatLab

  @Test
  public void openEx1ReturnsCorrectResultFromRunAnalyzer() {

    Network model = open_ex1();

    SolverOptions options = new SolverOptions(SolverType.FLUID);
    options.iter_max = 200;
    SolverFluid solver = new SolverFluid(model, options);

    solver.options.stiff = true;
    solver.runAnalyzer();
    SolverFluidResult fluidResult = solver.fluidResult;
    SolverResult result = solver.result;

    // method
    assertEquals("closing", result.method);

    // QN
    assertEquals(3, result.QN.getNumRows());
    assertEquals(1, result.QN.getNumCols());
    assertEquals(3, result.QN.getNumElements());
    assertEquals(0.625, result.QN.get(0, 0), tol);
    assertEquals(0.25, result.QN.get(1, 0), tol);
    assertEquals(0.125, result.QN.get(2, 0), tol);

    // RN
    assertEquals(3, result.RN.getNumRows());
    assertEquals(1, result.RN.getNumCols());
    assertEquals(3, result.RN.getNumElements());
    assertEquals(0.3125, result.RN.get(0, 0), tol);
    assertEquals(0.125, result.RN.get(1, 0), tol);
    assertEquals(0.0625, result.RN.get(2, 0), tol);

    // XN
    assertEquals(1, result.XN.getNumRows());
    assertEquals(1, result.XN.getNumCols());
    assertEquals(1, result.XN.getNumElements());
    assertEquals(2, result.XN.get(0, 0), tol);

    // UN
    assertEquals(3, result.UN.getNumRows());
    assertEquals(1, result.UN.getNumCols());
    assertEquals(3, result.UN.getNumElements());
    assertEquals(0.625, result.UN.get(0, 0), tol);
    assertEquals(0.25, result.UN.get(1, 0), tol);
    assertEquals(0.125, result.UN.get(2, 0), tol);

    // TN
    assertEquals(3, result.TN.getNumRows());
    assertEquals(1, result.TN.getNumCols());
    assertEquals(3, result.TN.getNumElements());
    assertEquals(2, result.TN.get(0, 0), tol);
    assertEquals(2, result.TN.get(1, 0), tol);
    assertEquals(2.0003, result.TN.get(2, 0), tol);

    // CN
    assertEquals(1, result.CN.getNumRows());
    assertEquals(1, result.CN.getNumCols());
    assertEquals(1, result.CN.getNumElements());
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 0), tol);

    // QNt
    assertEquals(3, result.QNt.length);
    assertEquals(1, result.QNt[0].length);
    assertEquals(1, result.QNt[0][0].getNumCols());
    assertEquals(1, result.QNt[0][0].get(0, 0), tol);
    assertEquals(0, result.QNt[1][0].get(0, 0), tol);
    assertEquals(0, result.QNt[2][0].get(0, 0), tol);
    int Tmax = result.QNt[0][0].getNumRows();
    assertEquals(0.625, result.QNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.QNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.QNt[2][0].get(Tmax - 1, 0), tol);

    // UNt
    assertEquals(3, result.UNt.length);
    assertEquals(1, result.UNt[0].length);
    assertEquals(1, result.UNt[0][0].getNumCols());
    assertEquals(1, result.UNt[0][0].get(0, 0), tol);
    assertEquals(0, result.UNt[1][0].get(0, 0), tol);
    assertEquals(0, result.UNt[2][0].get(0, 0), tol);
    assertEquals(0.625, result.UNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.UNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.UNt[2][0].get(Tmax - 1, 0), tol);

    // TNt
    assertEquals(3, result.TNt.length);
    assertEquals(1, result.TNt[0].length);
    assertEquals(1, result.TNt[0][0].getNumCols());
    assertEquals(2, result.TNt[0][0].get(0, 0), tol);
    assertEquals(0, result.TNt[1][0].get(0, 0), tol);
    assertEquals(0, result.TNt[2][0].get(0, 0), tol);
    assertEquals(1.25, result.TNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(2.0003, result.TNt[2][0].get(Tmax - 1, 0), tol);

    // t
    int sizeT = 0;
    int numElements = 0;
    sizeT += result.t.getNumRows();
    numElements += result.t.getNumElements();

    assertEquals(Tmax, sizeT);
    assertEquals(1, result.t.getNumCols());
    assertEquals(Tmax, numElements);
    assertEquals(0.00000001, result.t.get(0, 0));
    assertEquals(1000, result.t.get(result.t.getNumRows() - 1, 0));

    // odeStateVec
    assertEquals(0.625, fluidResult.odeStateVec.get(0, 0), tol);
    assertEquals(0.25, fluidResult.odeStateVec.get(0, 1), tol);
    assertEquals(0.125, fluidResult.odeStateVec.get(0, 2), tol);
  }

  @Test
  public void openEx2ReturnsCorrectResultFromRunAnalyzer() {

    Network model = open_ex2();

    SolverOptions options = new SolverOptions(SolverType.FLUID);
    options.iter_max = 200;
    SolverFluid solver = new SolverFluid(model, options);

    solver.options.stiff = true;
    solver.runAnalyzer();
    SolverFluidResult fluidResult = solver.fluidResult;
    SolverResult result = solver.result;

    // method
    assertEquals("closing", result.method);

    // QN
    assertEquals(5, result.QN.getNumRows());
    assertEquals(1, result.QN.getNumCols());
    assertEquals(5, result.QN.getNumElements());
    assertEquals(0.25, result.QN.get(0, 0), tol);
    assertEquals(0.25, result.QN.get(1, 0), tol);
    assertEquals(0.125, result.QN.get(2, 0), tol);
    assertEquals(0.25, result.QN.get(3, 0), tol);
    assertEquals(0.125, result.QN.get(4, 0), tol);

    // RN
    assertEquals(5, result.RN.getNumRows());
    assertEquals(1, result.RN.getNumCols());
    assertEquals(5, result.RN.getNumElements());
    assertEquals(0.125, result.RN.get(0, 0), tol);
    assertEquals(0.125, result.RN.get(1, 0), tol);
    assertEquals(0.0625, result.RN.get(2, 0), tol);
    assertEquals(0.125, result.RN.get(3, 0), tol);
    assertEquals(0.0625, result.RN.get(4, 0), tol);

    // XN
    assertEquals(1, result.XN.getNumRows());
    assertEquals(1, result.XN.getNumCols());
    assertEquals(1, result.XN.getNumElements());
    assertEquals(2, result.XN.get(0, 0), tol);

    // UN
    assertEquals(5, result.UN.getNumRows());
    assertEquals(1, result.UN.getNumCols());
    assertEquals(5, result.UN.getNumElements());
    assertEquals(0.25, result.UN.get(0, 0), tol);
    assertEquals(0.25, result.UN.get(1, 0), tol);
    assertEquals(0.125, result.UN.get(2, 0), tol);
    assertEquals(0.25, result.UN.get(3, 0), tol);
    assertEquals(0.125, result.UN.get(4, 0), tol);

    // TN
    assertEquals(5, result.TN.getNumRows());
    assertEquals(1, result.TN.getNumCols());
    assertEquals(5, result.TN.getNumElements());
    assertEquals(2, result.TN.get(0, 0), tol);
    assertEquals(2, result.TN.get(1, 0), tol);
    assertEquals(2, result.TN.get(2, 0), tol);
    assertEquals(2, result.TN.get(3, 0), tol);
    assertEquals(2.0002, result.TN.get(4, 0), tol);

    // CN
    assertEquals(1, result.CN.getNumRows());
    assertEquals(1, result.CN.getNumCols());
    assertEquals(1, result.CN.getNumElements());
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 0), tol);

    // QNt
    assertEquals(5, result.QNt.length);
    assertEquals(1, result.QNt[0].length);
    assertEquals(1, result.QNt[0][0].getNumCols());
    assertEquals(1, result.QNt[0][0].get(0, 0), tol);
    assertEquals(0, result.QNt[1][0].get(0, 0), tol);
    assertEquals(0, result.QNt[2][0].get(0, 0), tol);
    assertEquals(0, result.QNt[3][0].get(0, 0), tol);
    assertEquals(0, result.QNt[4][0].get(0, 0), tol);
    int Tmax = result.QNt[0][0].getNumRows();
    assertEquals(0.25, result.QNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.QNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.QNt[2][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.QNt[3][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.QNt[4][0].get(Tmax - 1, 0), tol);

    // UNt
    assertEquals(5, result.UNt.length);
    assertEquals(1, result.UNt[0].length);
    assertEquals(1, result.UNt[0][0].getNumCols());
    assertEquals(1, result.UNt[0][0].get(0, 0), tol);
    assertEquals(0, result.UNt[1][0].get(0, 0), tol);
    assertEquals(0, result.UNt[2][0].get(0, 0), tol);
    assertEquals(0, result.UNt[3][0].get(0, 0), tol);
    assertEquals(0, result.UNt[4][0].get(0, 0), tol);
    assertEquals(0.25, result.UNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.UNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.UNt[2][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.UNt[3][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.UNt[4][0].get(Tmax - 1, 0), tol);

    // TNt
    assertEquals(5, result.TNt.length);
    assertEquals(1, result.TNt[0].length);
    assertEquals(1, result.TNt[0][0].getNumCols());
    assertEquals(2, result.TNt[0][0].get(0, 0), tol);
    assertEquals(0, result.TNt[1][0].get(0, 0), tol);
    assertEquals(0, result.TNt[2][0].get(0, 0), tol);
    assertEquals(0, result.TNt[3][0].get(0, 0), tol);
    assertEquals(0, result.TNt[4][0].get(0, 0), tol);
    assertEquals(0.5, result.TNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[2][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[3][0].get(Tmax - 1, 0), tol);
    assertEquals(2.0002, result.TNt[4][0].get(Tmax - 1, 0), tol);

    // t
    int sizeT = 0;
    int numElements = 0;
    sizeT += result.t.getNumRows();
    numElements += result.t.getNumElements();

    assertEquals(Tmax, sizeT);
    assertEquals(1, result.t.getNumCols());
    assertEquals(Tmax, numElements);
    assertEquals(0.00000001, result.t.get(0, 0));
    assertEquals(1000, result.t.get(result.t.getNumRows() - 1, 0));

    // odeStateVec
    assertEquals(0.25, fluidResult.odeStateVec.get(0, 0), tol);
    assertEquals(0.25, fluidResult.odeStateVec.get(0, 1), tol);
    assertEquals(0.125, fluidResult.odeStateVec.get(0, 2), tol);
    assertEquals(0.25, fluidResult.odeStateVec.get(0, 3), tol);
    assertEquals(0.125, fluidResult.odeStateVec.get(0, 4), tol);
  }

  @Test
  public void openEx3ReturnsCorrectResultFromRunAnalyzer() {

    Network model = open_ex3();

    SolverOptions options = new SolverOptions(SolverType.FLUID);
    options.iter_max = 200;
    SolverFluid solver = new SolverFluid(model, options);

    solver.options.stiff = true;
    solver.runAnalyzer();
    SolverFluidResult fluidResult = solver.fluidResult;
    SolverResult result = solver.result;

    // method
    assertEquals("closing", result.method);

    // QN
    assertEquals(9, result.QN.getNumRows());
    assertEquals(1, result.QN.getNumCols());
    assertEquals(9, result.QN.getNumElements());
    assertEquals(0.0001, result.QN.get(0, 0), tol);
    assertEquals(0.25, result.QN.get(1, 0), tol);
    assertEquals(0.125, result.QN.get(2, 0), tol);
    assertEquals(0.25, result.QN.get(3, 0), tol);
    assertEquals(0.125, result.QN.get(4, 0), tol);
    assertEquals(0.25, result.QN.get(5, 0), tol);
    assertEquals(0.125, result.QN.get(6, 0), tol);
    assertEquals(0.25, result.QN.get(7, 0), tol);
    assertEquals(0.125, result.QN.get(8, 0), tol);

    // RN
    assertEquals(9, result.RN.getNumRows());
    assertEquals(1, result.RN.getNumCols());
    assertEquals(9, result.RN.getNumElements());
    assertEquals(0, result.RN.get(0, 0), tol);
    assertEquals(0.125, result.RN.get(1, 0), tol);
    assertEquals(0.0625, result.RN.get(2, 0), tol);
    assertEquals(0.125, result.RN.get(3, 0), tol);
    assertEquals(0.0625, result.RN.get(4, 0), tol);
    assertEquals(0.125, result.RN.get(5, 0), tol);
    assertEquals(0.0625, result.RN.get(6, 0), tol);
    assertEquals(0.125, result.RN.get(7, 0), tol);
    assertEquals(0.0625, result.RN.get(8, 0), tol);

    // XN
    assertEquals(1, result.XN.getNumRows());
    assertEquals(1, result.XN.getNumCols());
    assertEquals(1, result.XN.getNumElements());
    assertEquals(2, result.XN.get(0, 0), tol);

    // UN
    assertEquals(9, result.UN.getNumRows());
    assertEquals(1, result.UN.getNumCols());
    assertEquals(9, result.UN.getNumElements());
    assertEquals(0, result.UN.get(0, 0), tol);
    assertEquals(0.25, result.UN.get(1, 0), tol);
    assertEquals(0.125, result.UN.get(2, 0), tol);
    assertEquals(0.25, result.UN.get(3, 0), tol);
    assertEquals(0.125, result.UN.get(4, 0), tol);
    assertEquals(0.25, result.UN.get(5, 0), tol);
    assertEquals(0.125, result.UN.get(6, 0), tol);
    assertEquals(0.25, result.UN.get(7, 0), tol);
    assertEquals(0.125, result.UN.get(8, 0), tol);

    // TN
    assertEquals(9, result.TN.getNumRows());
    assertEquals(1, result.TN.getNumCols());
    assertEquals(9, result.TN.getNumElements());
    assertEquals(2, result.TN.get(0, 0), tol);
    assertEquals(2, result.TN.get(1, 0), tol);
    assertEquals(2, result.TN.get(2, 0), tol);
    assertEquals(2, result.TN.get(3, 0), tol);
    assertEquals(2, result.TN.get(4, 0), tol);
    assertEquals(2, result.TN.get(5, 0), tol);
    assertEquals(2, result.TN.get(6, 0), tol);
    assertEquals(2, result.TN.get(7, 0), tol);
    assertEquals(2.0002, result.TN.get(8, 0), tol);

    // CN
    assertEquals(1, result.CN.getNumRows());
    assertEquals(1, result.CN.getNumCols());
    assertEquals(1, result.CN.getNumElements());
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 0), tol);

    // QNt
    assertEquals(9, result.QNt.length);
    assertEquals(1, result.QNt[0].length);
    assertEquals(1, result.QNt[0][0].getNumCols());
    assertEquals(1, result.QNt[0][0].get(0, 0), tol);
    assertEquals(0, result.QNt[1][0].get(0, 0), tol);
    assertEquals(0, result.QNt[2][0].get(0, 0), tol);
    assertEquals(0, result.QNt[3][0].get(0, 0), tol);
    assertEquals(0, result.QNt[4][0].get(0, 0), tol);
    assertEquals(0, result.QNt[5][0].get(0, 0), tol);
    assertEquals(0, result.QNt[6][0].get(0, 0), tol);
    assertEquals(0, result.QNt[7][0].get(0, 0), tol);
    assertEquals(0, result.QNt[8][0].get(0, 0), tol);
    int Tmax = result.QNt[0][0].getNumRows();
    assertEquals(0, result.QNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.QNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.QNt[2][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.QNt[3][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.QNt[4][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.QNt[5][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.QNt[6][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.QNt[7][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.QNt[8][0].get(Tmax - 1, 0), tol);

    // UNt
    assertEquals(9, result.UNt.length);
    assertEquals(1, result.UNt[0].length);
    assertEquals(1, result.UNt[0][0].getNumCols());
    assertEquals(1, result.UNt[0][0].get(0, 0), tol);
    assertEquals(0, result.UNt[1][0].get(0, 0), tol);
    assertEquals(0, result.UNt[2][0].get(0, 0), tol);
    assertEquals(0, result.UNt[3][0].get(0, 0), tol);
    assertEquals(0, result.UNt[4][0].get(0, 0), tol);
    assertEquals(0, result.UNt[5][0].get(0, 0), tol);
    assertEquals(0, result.UNt[6][0].get(0, 0), tol);
    assertEquals(0, result.UNt[7][0].get(0, 0), tol);
    assertEquals(0, result.UNt[8][0].get(0, 0), tol);
    assertEquals(0, result.UNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.UNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.UNt[2][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.UNt[3][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.UNt[4][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.UNt[5][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.UNt[6][0].get(Tmax - 1, 0), tol);
    assertEquals(0.25, result.UNt[7][0].get(Tmax - 1, 0), tol);
    assertEquals(0.125, result.UNt[8][0].get(Tmax - 1, 0), tol);

    // TNt
    assertEquals(9, result.TNt.length);
    assertEquals(1, result.TNt[0].length);
    assertEquals(1, result.TNt[0][0].getNumCols());
    assertEquals(2, result.TNt[0][0].get(0, 0), tol);
    assertEquals(0, result.TNt[1][0].get(0, 0), tol);
    assertEquals(0, result.TNt[2][0].get(0, 0), tol);
    assertEquals(0, result.TNt[3][0].get(0, 0), tol);
    assertEquals(0, result.TNt[4][0].get(0, 0), tol);
    assertEquals(0, result.TNt[5][0].get(0, 0), tol);
    assertEquals(0, result.TNt[6][0].get(0, 0), tol);
    assertEquals(0, result.TNt[7][0].get(0, 0), tol);
    assertEquals(0, result.TNt[8][0].get(0, 0), tol);
    assertEquals(0, result.TNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[2][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[3][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[4][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[5][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[6][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[7][0].get(Tmax - 1, 0), tol);
    assertEquals(2.0002, result.TNt[8][0].get(Tmax - 1, 0), tol);

    // t
    int sizeT = 0;
    int numElements = 0;
    sizeT += result.t.getNumRows();
    numElements += result.t.getNumElements();

    assertEquals(Tmax, sizeT);
    assertEquals(1, result.t.getNumCols());
    assertEquals(Tmax, numElements);
    assertEquals(0.00000001, result.t.get(0, 0));
    assertEquals(1000, result.t.get(result.t.getNumRows() - 1, 0));

    // odeStateVec
    assertEquals(0, fluidResult.odeStateVec.get(0, 0), tol);
    assertEquals(0.25, fluidResult.odeStateVec.get(0, 1), tol);
    assertEquals(0.125, fluidResult.odeStateVec.get(0, 2), tol);
    assertEquals(0.25, fluidResult.odeStateVec.get(0, 3), tol);
    assertEquals(0.125, fluidResult.odeStateVec.get(0, 4), tol);
    assertEquals(0.25, fluidResult.odeStateVec.get(0, 5), tol);
    assertEquals(0.125, fluidResult.odeStateVec.get(0, 6), tol);
    assertEquals(0.25, fluidResult.odeStateVec.get(0, 7), tol);
    assertEquals(0.125, fluidResult.odeStateVec.get(0, 8), tol);
  }

  @Test
  public void openEx4ReturnsCorrectResultFromRunAnalyzer() {

    Network model = open_ex4();

    SolverOptions options = new SolverOptions(SolverType.FLUID);
    options.iter_max = 200;
    SolverFluid solver = new SolverFluid(model, options);

    solver.options.stiff = true;
    solver.runAnalyzer();
    SolverFluidResult fluidResult = solver.fluidResult;
    SolverResult result = solver.result;
    // method
    assertEquals("closing", result.method);

    // QN
    assertEquals(2, result.QN.getNumRows());
    assertEquals(2, result.QN.getNumCols());
    assertEquals(4, result.QN.getNumElements());
    assertEquals(0.9167, result.QN.get(0, 0), tol);
    assertEquals(0.9, result.QN.get(0, 1), tol);
    assertEquals(0.0833, result.QN.get(1, 0), tol);
    assertEquals(0.1, result.QN.get(1, 1), tol);

    // RN
    assertEquals(2, result.RN.getNumRows());
    assertEquals(2, result.RN.getNumCols());
    assertEquals(4, result.RN.getNumElements());
    assertEquals(0.4584, result.RN.get(0, 0), tol);
    assertEquals(0.45, result.RN.get(0, 1), tol);
    assertEquals(0.0417, result.RN.get(1, 0), tol);
    assertEquals(0.05, result.RN.get(1, 1), tol);

    // XN
    assertEquals(1, result.XN.getNumRows());
    assertEquals(2, result.XN.getNumCols());
    assertEquals(2, result.XN.getNumElements());
    assertEquals(2, result.XN.get(0, 0), tol);
    assertEquals(2, result.XN.get(0, 1), tol);

    // UN
    assertEquals(2, result.UN.getNumRows());
    assertEquals(2, result.UN.getNumCols());
    assertEquals(4, result.UN.getNumElements());
    assertEquals(0.9167, result.UN.get(0, 0), tol);
    assertEquals(0.9, result.UN.get(0, 1), tol);
    assertEquals(0.0833, result.UN.get(1, 0), tol);
    assertEquals(0.1, result.UN.get(1, 1), tol);

    // TN
    assertEquals(2, result.TN.getNumRows());
    assertEquals(2, result.TN.getNumCols());
    assertEquals(4, result.TN.getNumElements());
    assertEquals(2, result.TN.get(0, 0), tol);
    assertEquals(2, result.TN.get(0, 1), tol);
    assertEquals(1.999, result.TN.get(1, 0), tol);
    assertEquals(2, result.TN.get(1, 1), tol);

    // CN
    assertEquals(1, result.CN.getNumRows());
    assertEquals(2, result.CN.getNumCols());
    assertEquals(2, result.CN.getNumElements());
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 0), tol);
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 1), tol);

    // QNt
    assertEquals(2, result.QNt.length);
    assertEquals(2, result.QNt[0].length);
    assertEquals(1, result.QNt[0][0].getNumCols());
    assertEquals(1, result.QNt[0][0].get(0, 0), tol);
    assertEquals(1, result.QNt[0][1].get(0, 0), tol);
    assertEquals(0, result.QNt[1][0].get(0, 0), tol);
    assertEquals(0, result.QNt[1][1].get(0, 0), tol);
    int Tmax = result.QNt[0][0].getNumRows();
    assertEquals(0.9167, result.QNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.9, result.QNt[0][1].get(Tmax - 1, 0), tol);
    assertEquals(0.0833, result.QNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.1, result.QNt[1][1].get(Tmax - 1, 0), tol);

    // UNt
    assertEquals(2, result.UNt.length);
    assertEquals(2, result.UNt[0].length);
    assertEquals(1, result.UNt[0][0].getNumCols());
    assertEquals(0.5, result.UNt[0][0].get(0, 0), tol);
    assertEquals(0.5, result.UNt[0][1].get(0, 0), tol);
    assertEquals(0, result.UNt[1][0].get(0, 0), tol);
    assertEquals(0, result.UNt[1][1].get(0, 0), tol);
    assertEquals(0.5046, result.UNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.4954, result.UNt[0][1].get(Tmax - 1, 0), tol);
    assertEquals(0.0833, result.UNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.1, result.UNt[1][1].get(Tmax - 1, 0), tol);

    // TNt
    assertEquals(2, result.TNt.length);
    assertEquals(2, result.TNt[0].length);
    assertEquals(1, result.TNt[0][0].getNumCols());
    assertEquals(1, result.TNt[0][0].get(0, 0), tol);
    assertEquals(1, result.TNt[0][1].get(0, 0), tol);
    assertEquals(0, result.TNt[1][0].get(0, 0), tol);
    assertEquals(0, result.TNt[1][1].get(0, 0), tol);
    assertEquals(1.0092, result.TNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.9908, result.TNt[0][1].get(Tmax - 1, 0), tol);
    assertEquals(1.9990, result.TNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[1][1].get(Tmax - 1, 0), tol);

    // t
    int sizeT = 0;
    int numElements = 0;
    sizeT += result.t.getNumRows();
    numElements += result.t.getNumElements();

    assertEquals(Tmax, sizeT);
    assertEquals(1, result.t.getNumCols());
    assertEquals(Tmax, numElements);
    assertEquals(0.00000001, result.t.get(0, 0));
    assertEquals(1000, result.t.get(result.t.getNumRows() - 1, 0));

    // odeStateVec
    assertEquals(0.9167, fluidResult.odeStateVec.get(0, 0), tol);
    assertEquals(0.9, fluidResult.odeStateVec.get(0, 1), tol);
    assertEquals(0.0833, fluidResult.odeStateVec.get(0, 2), tol);
    assertEquals(0.1, fluidResult.odeStateVec.get(0, 3), tol);
  }

  // TODO: Fails, Out Of Memory Error (too many transient steps)

  /*  @Test
  public void openEx5ReturnsCorrectResultFromRunAnalyzer() throws IllegalAccessException {

    Network model = open_ex5();

        SolverOptions options = new SolverOptions(SolverType.FLUID);
    options.iter_max = 200;
    SolverFluid solver = new SolverFluid(model, options);

    solver.options.stiff = true;
    solver.runAnalyzer();
    SolverFluidResult result = solver.result;

    // method
    assertEquals("closing", result.method);

    // QN
    assertEquals(2, result.QN.getNumRows());
    assertEquals(4, result.QN.getNumCols());
    assertEquals(8, result.QN.getNumElements());
    assertEquals(0.9167, result.QN.get(0, 0), tol);
    assertEquals(0.9, result.QN.get(0, 1), tol);
    assertEquals(0.9167, result.QN.get(0, 2), tol);
    assertEquals(0.9, result.QN.get(0, 3), tol);
    assertEquals(0.0833, result.QN.get(1, 0), tol);
    assertEquals(0.1, result.QN.get(1, 1), tol);
    assertEquals(0.0833, result.QN.get(1, 2), tol);
    assertEquals(0.1, result.QN.get(1, 3), tol);

    // RN
    assertEquals(2, result.RN.getNumRows());
    assertEquals(4, result.RN.getNumCols());
    assertEquals(8, result.RN.getNumElements());
    assertEquals(0.4584, result.RN.get(0, 0), tol);
    assertEquals(0.45, result.RN.get(0, 1), tol);
    assertEquals(0.4584, result.RN.get(0, 2), tol);
    assertEquals(0.45, result.RN.get(0, 3), tol);
    assertEquals(0.0417, result.RN.get(1, 0), tol);
    assertEquals(0.05, result.RN.get(1, 1), tol);
    assertEquals(0.0417, result.RN.get(1, 2), tol);
    assertEquals(0.05, result.RN.get(1, 3), tol);

    // XN
    assertEquals(1, result.XN.getNumRows());
    assertEquals(4, result.XN.getNumCols());
    assertEquals(4, result.XN.getNumElements());
    assertEquals(2, result.XN.get(0, 0), tol);
    assertEquals(2, result.XN.get(0, 1), tol);
    assertEquals(2, result.XN.get(0, 2), tol);
    assertEquals(2, result.XN.get(0, 3), tol);

    // UN
    assertEquals(2, result.UN.getNumRows());
    assertEquals(4, result.UN.getNumCols());
    assertEquals(8, result.UN.getNumElements());
    assertEquals(0.9167, result.UN.get(0, 0), tol);
    assertEquals(0.9, result.UN.get(0, 1), tol);
    assertEquals(0.9167, result.UN.get(0, 2), tol);
    assertEquals(0.9, result.UN.get(0, 3), tol);
    assertEquals(0.0833, result.UN.get(1, 0), tol);
    assertEquals(0.1, result.UN.get(1, 1), tol);
    assertEquals(0.0833, result.UN.get(1, 2), tol);
    assertEquals(0.1, result.UN.get(1, 3), tol);

    // TN
    assertEquals(2, result.TN.getNumRows());
    assertEquals(4, result.TN.getNumCols());
    assertEquals(8, result.TN.getNumElements());
    assertEquals(2, result.TN.get(0, 0), tol);
    assertEquals(2, result.TN.get(0, 1), tol);
    assertEquals(2, result.TN.get(0, 2), tol);
    assertEquals(2, result.TN.get(0, 3), tol);
    assertEquals(1.999, result.TN.get(1, 0), tol);
    assertEquals(2, result.TN.get(1, 1), tol);
    assertEquals(1.999, result.TN.get(1, 2), tol);
    assertEquals(2, result.TN.get(1, 3), tol);

    // CN
    assertEquals(1, result.CN.getNumRows());
    assertEquals(4, result.CN.getNumCols());
    assertEquals(4, result.CN.getNumElements());
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 0), tol);
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 1), tol);
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 2), tol);
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 3), tol);

    // QNt
    assertEquals(2, result.QNt.length);
    assertEquals(4, result.QNt[0].length);
    assertEquals(1, result.QNt[0][0].getNumCols());
    assertEquals(1, result.QNt[0][0].get(0, 0), tol);
    assertEquals(1, result.QNt[0][1].get(0, 0), tol);
    assertEquals(1, result.QNt[0][2].get(0, 0), tol);
    assertEquals(1, result.QNt[0][3].get(0, 0), tol);
    assertEquals(0, result.QNt[1][0].get(0, 0), tol);
    assertEquals(0, result.QNt[1][1].get(0, 0), tol);
    assertEquals(0, result.QNt[1][2].get(0, 0), tol);
    assertEquals(0, result.QNt[1][3].get(0, 0), tol);
    int Tmax = result.QNt[0][0].getNumRows();
    assertEquals(0.9167, result.QNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.9, result.QNt[0][1].get(Tmax - 1, 0), tol);
    assertEquals(0.9167, result.QNt[0][2].get(Tmax - 1, 0), tol);
    assertEquals(0.9, result.QNt[0][3].get(Tmax - 1, 0), tol);
    assertEquals(0.0833, result.QNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.1, result.QNt[1][1].get(Tmax - 1, 0), tol);
    assertEquals(0.0833, result.QNt[1][2].get(Tmax - 1, 0), tol);
    assertEquals(0.1, result.QNt[1][3].get(Tmax - 1, 0), tol);

    // UNt
    assertEquals(2, result.UNt.length);
    assertEquals(4, result.UNt[0].length);
    assertEquals(1, result.UNt[0][0].getNumCols());
    assertEquals(0.25, result.UNt[0][0].get(0, 0), tol);
    assertEquals(0.25, result.UNt[0][1].get(0, 0), tol);
    assertEquals(0.25, result.UNt[0][2].get(0, 0), tol);
    assertEquals(0.25, result.UNt[0][3].get(0, 0), tol);
    assertEquals(0, result.UNt[1][0].get(0, 0), tol);
    assertEquals(0, result.UNt[1][1].get(0, 0), tol);
    assertEquals(0, result.UNt[1][2].get(0, 0), tol);
    assertEquals(0, result.UNt[1][3].get(0, 0), tol);
    assertEquals(0.2523, result.UNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.2477, result.UNt[0][1].get(Tmax - 1, 0), tol);
    assertEquals(0.2523, result.UNt[0][2].get(Tmax - 1, 0), tol);
    assertEquals(0.2477, result.UNt[0][3].get(Tmax - 1, 0), tol);
    assertEquals(0.0833, result.UNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(0.1, result.UNt[1][1].get(Tmax - 1, 0), tol);
    assertEquals(0.0833, result.UNt[1][2].get(Tmax - 1, 0), tol);
    assertEquals(0.1, result.UNt[1][3].get(Tmax - 1, 0), tol);

    // TNt
    assertEquals(2, result.TNt.length);
    assertEquals(4, result.TNt[0].length);
    assertEquals(1, result.TNt[0][0].getNumCols());
    assertEquals(0.5, result.TNt[0][0].get(0, 0), tol);
    assertEquals(0.5, result.TNt[0][1].get(0, 0), tol);
    assertEquals(0.5, result.TNt[0][2].get(0, 0), tol);
    assertEquals(0.5, result.TNt[0][3].get(0, 0), tol);
    assertEquals(0, result.TNt[1][0].get(0, 0), tol);
    assertEquals(0, result.TNt[1][1].get(0, 0), tol);
    assertEquals(0, result.TNt[1][2].get(0, 0), tol);
    assertEquals(0, result.TNt[1][3].get(0, 0), tol);
    assertEquals(0.5046, result.TNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.4954, result.TNt[0][1].get(Tmax - 1, 0), tol);
    assertEquals(0.5046, result.TNt[0][2].get(Tmax - 1, 0), tol);
    assertEquals(0.4954, result.TNt[0][3].get(Tmax - 1, 0), tol);
    assertEquals(1.9990, result.TNt[1][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[1][1].get(Tmax - 1, 0), tol);
    assertEquals(1.9990, result.TNt[1][2].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[1][3].get(Tmax - 1, 0), tol);

    // t
    int sizeT = 0;
    int numElements = 0;
    for (int i = 0; i < result.t.size(); i++) {
      sizeT += result.t.get(i).getNumRows();
      numElements += result.t.get(i).getNumElements();
    }
    assertEquals(Tmax, sizeT);
    assertEquals(1, result.t.getLast().getNumCols());
    assertEquals(Tmax, numElements);
    assertEquals(0.00000001, result.t.getLast().get(0, 0));
    assertEquals(1000, result.t.getLast().get(result.t.getLast().getNumRows() - 1, 0));

    // odeStateVec
    assertEquals(0.9167, result.odeStateVec.get(0, 0), tol);
    assertEquals(0.9, result.odeStateVec.get(0, 1), tol);
    assertEquals(0.9167, result.odeStateVec.get(0, 2), tol);
    assertEquals(0.9, result.odeStateVec.get(0, 3), tol);
    assertEquals(0.0833, result.odeStateVec.get(0, 4), tol);
    assertEquals(0.1, result.odeStateVec.get(0, 5), tol);
    assertEquals(0.0833, result.odeStateVec.get(0, 6), tol);
    assertEquals(0.1, result.odeStateVec.get(0, 7), tol);
  }*/
  /*


  // TODO: OpenEx6 Fails, Out Of Memory Error (too many transient steps)
  // Not implemented at this stage as it is larger than openEx5

  // TODO: Fails, Out Of Memory Error (too many transient steps)
  */
  /*  @Test
  public void openEx7ReturnsCorrectResultFromRunAnalyzer() throws IllegalAccessException {

    Network model = open_ex7();

        SolverOptions options = new SolverOptions(SolverType.FLUID);
    options.iter_max = 200;
    SolverFluid solver = new SolverFluid(model, options);

    solver.options.stiff = true;
    solver.runAnalyzer();
    SolverFluidResult result = solver.result;

    // method
    assertEquals("closing", result.method);

    // QN
    assertEquals(2, result.QN.getNumRows());
    assertEquals(1, result.QN.getNumCols());
    assertEquals(2, result.QN.getNumElements());
    assertEquals(0.9688, result.QN.get(0, 0), tol);
    assertEquals(0.0312, result.QN.get(1, 0), tol);

    // RN
    assertEquals(2, result.RN.getNumRows());
    assertEquals(1, result.RN.getNumCols());
    assertEquals(2, result.RN.getNumElements());
    assertEquals(0.4844, result.RN.get(0, 0), tol);
    assertEquals(0.0156, result.RN.get(1, 0), tol);

    // XN
    assertEquals(1, result.XN.getNumRows());
    assertEquals(1, result.XN.getNumCols());
    assertEquals(1, result.XN.getNumElements());
    assertEquals(2, result.XN.get(0, 0), tol);

    // UN
    assertEquals(2, result.UN.getNumRows());
    assertEquals(1, result.UN.getNumCols());
    assertEquals(2, result.UN.getNumElements());
    assertEquals(0.9688, result.UN.get(0, 0), tol);
    assertEquals(0.0312, result.UN.get(1, 0), tol);

    // TN
    assertEquals(2, result.TN.getNumRows());
    assertEquals(1, result.TN.getNumCols());
    assertEquals(2, result.TN.getNumElements());
    assertEquals(2, result.TN.get(0, 0), tol);
    assertEquals(1.996, result.TN.get(1, 0), tol);

    // CN
    assertEquals(1, result.CN.getNumRows());
    assertEquals(1, result.CN.getNumCols());
    assertEquals(1, result.CN.getNumElements());
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 0), tol);

    // QNt
    assertEquals(2, result.QNt.length);
    assertEquals(1, result.QNt[0].length);
    assertEquals(1, result.QNt[0][0].getNumCols());
    assertEquals(1, result.QNt[0][0].get(0, 0), tol);
    assertEquals(0, result.QNt[1][0].get(0, 0), tol);
    int Tmax = result.QNt[0][0].getNumRows();
    assertEquals(0.9688, result.QNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.0312, result.QNt[1][0].get(Tmax - 1, 0), tol);

    // UNt
    assertEquals(2, result.UNt.length);
    assertEquals(1, result.UNt[0].length);
    assertEquals(1, result.UNt[0][0].getNumCols());
    assertEquals(1, result.UNt[0][0].get(0, 0), tol);
    assertEquals(0, result.UNt[1][0].get(0, 0), tol);
    assertEquals(0.9688, result.UNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.0312, result.UNt[1][0].get(Tmax - 1, 0), tol);

    // TNt
    assertEquals(2, result.TNt.length);
    assertEquals(1, result.TNt[0].length);
    assertEquals(1, result.TNt[0][0].getNumCols());
    assertEquals(2, result.TNt[0][0].get(0, 0), tol);
    assertEquals(0, result.TNt[1][0].get(0, 0), tol);
    assertEquals(1.9376, result.TNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(1.9960, result.TNt[1][0].get(Tmax - 1, 0), tol);

    // t
    int sizeT = 0;
    int numElements = 0;
    for (int i = 0; i < result.t.size(); i++) {
      sizeT += result.t.get(i).getNumRows();
      numElements += result.t.get(i).getNumElements();
    }
    assertEquals(Tmax, sizeT);
    assertEquals(1, result.t.getLast().getNumCols());
    assertEquals(Tmax, numElements);
    assertEquals(0.00000001, result.t.getLast().get(0, 0));
    assertEquals(1000, result.t.getLast().get(result.t.getLast().getNumRows() - 1, 0));

    // odeStateVec
    assertEquals(0.9688, result.odeStateVec.get(0, 0), tol);
    assertEquals(0.0312, result.odeStateVec.get(0, 1), tol);
  }*/

  @Test
  public void openEx8ReturnsCorrectResultFromRunAnalyzer() {

    Network model = open_ex8();

    SolverOptions options = new SolverOptions(SolverType.FLUID);
    options.iter_max = 200;
    SolverFluid solver = new SolverFluid(model, options);

    solver.options.stiff = true;
    solver.runAnalyzer();
    SolverFluidResult fluidResult = solver.fluidResult;
    SolverResult result = solver.result;

    // method
    assertEquals("closing", result.method);

    // QN
    assertEquals(2, result.QN.getNumRows());
    assertEquals(1, result.QN.getNumCols());
    assertEquals(2, result.QN.getNumElements());
    assertEquals(0.0244, result.QN.get(0, 0), tol);
    assertEquals(0.9756, result.QN.get(1, 0), tol);

    // RN
    assertEquals(2, result.RN.getNumRows());
    assertEquals(1, result.RN.getNumCols());
    assertEquals(2, result.RN.getNumElements());
    assertEquals(0.0122, result.RN.get(0, 0), tol);
    assertEquals(0.4878, result.RN.get(1, 0), tol);

    // XN
    assertEquals(1, result.XN.getNumRows());
    assertEquals(1, result.XN.getNumCols());
    assertEquals(1, result.XN.getNumElements());
    assertEquals(2, result.XN.get(0, 0), tol);

    // UN
    assertEquals(2, result.UN.getNumRows());
    assertEquals(1, result.UN.getNumCols());
    assertEquals(2, result.UN.getNumElements());
    assertEquals(0.0244, result.UN.get(0, 0), tol);
    assertEquals(0.9756, result.UN.get(1, 0), tol);

    // TN
    assertEquals(2, result.TN.getNumRows());
    assertEquals(1, result.TN.getNumCols());
    assertEquals(2, result.TN.getNumElements());
    assertEquals(2, result.TN.get(0, 0), tol);
    assertEquals(2, result.TN.get(1, 0), tol);

    // CN
    assertEquals(1, result.CN.getNumRows());
    assertEquals(1, result.CN.getNumCols());
    assertEquals(1, result.CN.getNumElements());
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 0), tol);

    // QNt
    assertEquals(2, result.QNt.length);
    assertEquals(1, result.QNt[0].length);
    assertEquals(1, result.QNt[0][0].getNumCols());
    assertEquals(1, result.QNt[0][0].get(0, 0), tol);
    assertEquals(0, result.QNt[1][0].get(0, 0), tol);
    int Tmax = result.QNt[0][0].getNumRows();
    assertEquals(0.0244, result.QNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.9756, result.QNt[1][0].get(Tmax - 1, 0), tol);

    // UNt
    assertEquals(2, result.UNt.length);
    assertEquals(1, result.UNt[0].length);
    assertEquals(1, result.UNt[0][0].getNumCols());
    assertEquals(1, result.UNt[0][0].get(0, 0), tol);
    assertEquals(0, result.UNt[1][0].get(0, 0), tol);
    assertEquals(0.0244, result.UNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.9756, result.UNt[1][0].get(Tmax - 1, 0), tol);

    // TNt
    assertEquals(2, result.TNt.length);
    assertEquals(1, result.TNt[0].length);
    assertEquals(1, result.TNt[0][0].getNumCols());
    assertEquals(2, result.TNt[0][0].get(0, 0), tol);
    assertEquals(0, result.TNt[1][0].get(0, 0), tol);
    assertEquals(0.0488, result.TNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[1][0].get(Tmax - 1, 0), tol);

    // t
    int sizeT = 0;
    int numElements = 0;
    sizeT += result.t.getNumRows();
    numElements += result.t.getNumElements();

    assertEquals(Tmax, sizeT);
    assertEquals(1, result.t.getNumCols());
    assertEquals(Tmax, numElements);
    assertEquals(0.00000001, result.t.get(0, 0));
    assertEquals(1000, result.t.get(result.t.getNumRows() - 1, 0));

    // odeStateVec
    assertEquals(0.0244, fluidResult.odeStateVec.get(0, 0), tol);
    assertEquals(0.9756, fluidResult.odeStateVec.get(0, 1), tol);
  }

  @Test
  public void openEx9ReturnsCorrectResultFromRunAnalyzer() {

    Network model = open_ex9();

    SolverOptions options = new SolverOptions(SolverType.FLUID);
    options.iter_max = 200;
    SolverFluid solver = new SolverFluid(model, options);

    solver.options.stiff = true;
    solver.runAnalyzer();
    SolverFluidResult fluidResult = solver.fluidResult;
    SolverResult result = solver.result;

    // method
    assertEquals("closing", result.method);

    // QN
    assertEquals(2, result.QN.getNumRows());
    assertEquals(1, result.QN.getNumCols());
    assertEquals(2, result.QN.getNumElements());
    assertEquals(0.5, result.QN.get(0, 0), tol);
    assertEquals(0.5, result.QN.get(1, 0), tol);

    // RN
    assertEquals(2, result.RN.getNumRows());
    assertEquals(1, result.RN.getNumCols());
    assertEquals(2, result.RN.getNumElements());
    assertEquals(0.25, result.RN.get(0, 0), tol);
    assertEquals(0.25, result.RN.get(1, 0), tol);

    // XN
    assertEquals(1, result.XN.getNumRows());
    assertEquals(1, result.XN.getNumCols());
    assertEquals(1, result.XN.getNumElements());
    assertEquals(2, result.XN.get(0, 0), tol);

    // UN
    assertEquals(2, result.UN.getNumRows());
    assertEquals(1, result.UN.getNumCols());
    assertEquals(2, result.UN.getNumElements());
    assertEquals(0.5, result.UN.get(0, 0), tol);
    assertEquals(0.5, result.UN.get(1, 0), tol);

    // TN
    assertEquals(2, result.TN.getNumRows());
    assertEquals(1, result.TN.getNumCols());
    assertEquals(2, result.TN.getNumElements());
    assertEquals(2, result.TN.get(0, 0), tol);
    assertEquals(2, result.TN.get(1, 0), tol);

    // CN
    assertEquals(1, result.CN.getNumRows());
    assertEquals(1, result.CN.getNumCols());
    assertEquals(1, result.CN.getNumElements());
    assertEquals(Double.POSITIVE_INFINITY, result.CN.get(0, 0), tol);

    // QNt
    assertEquals(2, result.QNt.length);
    assertEquals(1, result.QNt[0].length);
    assertEquals(1, result.QNt[0][0].getNumCols());
    assertEquals(1, result.QNt[0][0].get(0, 0), tol);
    assertEquals(0, result.QNt[1][0].get(0, 0), tol);
    int Tmax = result.QNt[0][0].getNumRows();
    assertEquals(0.5, result.QNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.5, result.QNt[1][0].get(Tmax - 1, 0), tol);

    // UNt
    assertEquals(2, result.UNt.length);
    assertEquals(1, result.UNt[0].length);
    assertEquals(1, result.UNt[0][0].getNumCols());
    assertEquals(1, result.UNt[0][0].get(0, 0), tol);
    assertEquals(0, result.UNt[1][0].get(0, 0), tol);
    assertEquals(0.5, result.UNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(0.5, result.UNt[1][0].get(Tmax - 1, 0), tol);

    // TNt
    assertEquals(2, result.TNt.length);
    assertEquals(1, result.TNt[0].length);
    assertEquals(1, result.TNt[0][0].getNumCols());
    assertEquals(2, result.TNt[0][0].get(0, 0), tol);
    assertEquals(0, result.TNt[1][0].get(0, 0), tol);
    assertEquals(1, result.TNt[0][0].get(Tmax - 1, 0), tol);
    assertEquals(2, result.TNt[1][0].get(Tmax - 1, 0), tol);

    // t
    int sizeT = 0;
    int numElements = 0;
    sizeT += result.t.getNumRows();
    numElements += result.t.getNumElements();

    assertEquals(Tmax, sizeT);
    assertEquals(1, result.t.getNumCols());
    assertEquals(Tmax, numElements);
    assertEquals(0.00000001, result.t.get(0, 0));
    assertEquals(1000, result.t.get(result.t.getNumRows() - 1, 0));

    // odeStateVec
    assertEquals(0.5, fluidResult.odeStateVec.get(0, 0), tol);
    assertEquals(0.5, fluidResult.odeStateVec.get(0, 1), tol);
  }

  // TODO: OpenEx6 Fails, Out Of Memory Error (too many transient steps)
  // Not implemented at this stage as it is larger than openEx5
}
