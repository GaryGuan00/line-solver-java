// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package jline.util;

import jline.lang.JLineMatrix;

public class Sequence {

  // Return a sequence of non-negative vectors less than a given vector - init
  public static JLineMatrix pprod(JLineMatrix n) {
    return new JLineMatrix(n.getNumRows(), n.getNumCols());
  }

  // Return a sequence of non-negative vectors less than a given vector - next state
  public static JLineMatrix pprod(JLineMatrix n, JLineMatrix N) {

    int R = N.length();
    int countEqual = 0;
    for (int i = 0; i < N.getNumRows(); i++) {
      for (int j = 0; j < N.getNumCols(); j++) {
        if (n.get(i, j) == N.get(i, j)) {
          countEqual++;
        }
      }
    }
    if (countEqual == R) {
      n = new JLineMatrix(1, 1);
      n.set(0, 0, -1);
      return n;
    }

    int s = R - 1;
    while (s > 0 && n.get(0, s) == N.get(0, s)) {
      n.set(0, s, 0);
      s--;
    }

    if (s == 0) {
      return n;
    }

    n.set(0, s, n.get(0, s) + 1);
    return n;
  }
}
