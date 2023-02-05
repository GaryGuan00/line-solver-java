// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package m3a;

import jline.lang.JLineMatrix;
import org.ejml.data.DMatrixRMaj;
import org.qore.KPC.MAP;

import java.util.Map;

public class Mmap {

  // Fixes MMAP feasibility by setting negative values to zero and forcing the other conditions.
  public static Map<Integer, JLineMatrix> mmapNormalize(Map<Integer, JLineMatrix> mmap) {

    if (mmap.isEmpty()) {
      return mmap;
    }

    int K = mmap.get(0).getNumRows();
    int C = mmap.size() - 2;

    for (int i = 0; i < K; i++) {
      for (int j = 0; j < K; j++) {
        if (i != j) {
          mmap.get(0).set(i, j, Math.max(mmap.get(0).get(i, j), 0));
        }
      }
    }

    mmap.put(1, mmap.get(0).clone());
    mmap.get(1).zero();
    for (int c = 0; c < C; c++) {
      for (int row = 0; row < mmap.get(c + 2).getNumRows(); row++) {
        for (int col = 0; col < mmap.get(c + 2).getNumCols(); col++) {
          if (mmap.get(c + 2).get(row, col) < 0) {
            mmap.get(c + 2).set(row, col, 0);
          }
        }
      }
      if (mmap.get(c + 2).hasNaN()) {
        mmap.get(c + 2).zero();
      }
      mmap.put(1, new JLineMatrix(mmap.get(1).add(1, mmap.get(c + 2))));
    }

    for (int k = 0; k < K; k++) {
      mmap.get(0).set(k, k, 0);
      mmap.get(0).set(k, k, -mmap.get(0).sumRows(k) - mmap.get(1).sumRows(k));
    }

    return mmap;
  }

  public static JLineMatrix mmapCountLambda(Map<Integer, JLineMatrix> mmap) {

    // Computes the arrival rate of the counting process, for the given Marked MAP.
    // Input: mmap: the Marked MAP
    // Output: lk: the vector with the rate for each job class

    int n = mmap.get(0).getNumRows();
    int K = mmap.size() - 2;
    JLineMatrix lk = new JLineMatrix(K, 1);
    JLineMatrix e = new JLineMatrix(n, 1);
    e.ones();

    MAP map = new MAP(new DMatrixRMaj(mmap.get(0)), new DMatrixRMaj(mmap.get(1)));
    DMatrixRMaj thetaDMRM = map.ctmc(false);
    JLineMatrix theta = new JLineMatrix(thetaDMRM.numRows, thetaDMRM.numCols);
    for (int i = 0; i < theta.getNumRows(); i++) {
      for (int j = 0; j < theta.getNumCols(); j++) {
        theta.set(i, j, thetaDMRM.get(i, j));
      }
    }

    for (int k = 0; k < K; k++) {
      JLineMatrix lkTmp = new JLineMatrix(theta.mult(mmap.get(k + 2), null));
      lkTmp = new JLineMatrix(lkTmp.mult(e, null));
      lk.set(k, 0, lkTmp.get(0, 0));
    }

    return lk.transpose();
  }
}
