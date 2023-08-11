// Copyright (c) 2012-2022, Imperial College London
// All rights reserved.

package jline.util;

public class CirculantMatrix {

  // Returns a circulant matrix of order c
  public static Matrix circul(int c) {
    if (c == 1) {
      Matrix C = new Matrix(1, 1);
      C.set(0, 0, 1);
      return C;
    }

    Matrix v = new Matrix(1, c);
    v.set(0, c - 1, 1);
    return circul(v);
  }

  // Returns a circulant matrix of order c
  public static Matrix circul(Matrix c) {
    int n = c.length();
    Matrix R = new Matrix(n, n);
    R.set(0, n - 1, 1);
    for (int i = 1; i < n; i++) {
      R.set(i, i - 1, 1);
    }
    Matrix C = new Matrix(n, n);
    for (int t = 0; t < n; t++) {
      if (t > 1) {
        R = new Matrix(R.mult(R, null));
      }
      Matrix tmpC = new Matrix(0, 0);
      R.scale(c.get(0, t), tmpC);
      C = C.add(1, tmpC);
    }
    return C;
  }
}
