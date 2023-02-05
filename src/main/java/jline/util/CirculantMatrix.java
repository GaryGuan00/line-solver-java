// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package jline.util;

import jline.lang.JLineMatrix;

public class CirculantMatrix {

  // Returns a circulant matrix of order c
  public static JLineMatrix circul(int c) {
    if (c == 1) {
      JLineMatrix C = new JLineMatrix(1, 1);
      C.set(0, 0, 1);
      return C;
    }

    JLineMatrix v = new JLineMatrix(1, c);
    v.set(0, c - 1, 1);
    return circul(v);
  }

  // Returns a circulant matrix of order c
  public static JLineMatrix circul(JLineMatrix c) {
    int n = c.length();
    JLineMatrix R = new JLineMatrix(n, n);
    R.set(0, n - 1, 1);
    for (int i = 1; i < n; i++) {
      R.set(i, i - 1, 1);
    }
    JLineMatrix C = new JLineMatrix(n, n);
    for (int t = 0; t < n; t++) {
      if (t > 1) {
        R = new JLineMatrix(R.mult(R, null));
      }
      JLineMatrix tmpC = new JLineMatrix(0, 0);
      R.scale(c.get(0, t), tmpC);
      C = C.add(1, tmpC);
    }
    return C;
  }
}
