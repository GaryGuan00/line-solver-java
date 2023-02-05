package kpcToolbox;

import jline.api.MAM;
import jline.lang.JLineMatrix;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.equation.Equation;
import org.qore.KPC.MAP;

public class Map {

  public static JLineMatrix mapCDF(MAP map, JLineMatrix points) {

    JLineMatrix CDFVals = points.transpose();
    CDFVals.zero();
    JLineMatrix pie = MAM.map_pie(map);
    JLineMatrix e1 = new JLineMatrix(Math.max(map.D1.numRows, map.D1.numCols), 1);
    e1.ones();

    JLineMatrix mapD0Copy = new JLineMatrix(map.D0.numRows, map.D0.numCols);
    for (int i = 0; i < map.D0.numRows; i++) {
      for (int j = 0; j < map.D0.numCols; j++) {
        mapD0Copy.set(i, j, map.D0.get(i, j));
      }
    }

    JLineMatrix inputToEXPM = new JLineMatrix(mapD0Copy.numRows, mapD0Copy.numCols);
    for (int t = 0; t < points.length(); t++) {
      mapD0Copy.scale(points.get(t), inputToEXPM);
      JLineMatrix outputOfEXPM = inputToEXPM.expm();

      DMatrixSparseCSC pieDMS = new DMatrixSparseCSC(pie);
      DMatrixSparseCSC expmDMS = new DMatrixSparseCSC(outputOfEXPM);
      DMatrixSparseCSC e1DMS = new DMatrixSparseCSC(e1);
      Equation eq = new Equation();
      eq.alias(pieDMS, "pie", expmDMS, "expm", e1DMS, "e1");
      eq.process("output = pie * expm * e1");
      JLineMatrix output = new JLineMatrix(eq.lookupSimple("output"));

      CDFVals.set(0, t, 1 - output.get(0, 0));
    }

    return CDFVals;
  }
}
