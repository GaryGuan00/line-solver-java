// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package jline.util;

import jline.lang.JLineMatrix;

public class MAPE {

  // TODO: polymorphic version that also returns nanMean
  // Return mean absolute percentage error of approx with respect to exact
  public static double mape(JLineMatrix approx, JLineMatrix exact) {

    int numRows = approx.getNumRows();
    double totalAbsolutePercentageError = 0;
    int numExactGreaterThanZero = 0;
    for (int row = 0; row < numRows; row++) {
      if (exact.get(row, 0) > 0) {
        totalAbsolutePercentageError += Math.abs(1 - (approx.get(row, 0) / exact.get(row, 0)));
        numExactGreaterThanZero++;
      }
    }
    return totalAbsolutePercentageError / numExactGreaterThanZero;
  }
}
