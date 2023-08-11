package jline.util;

import java.util.LinkedList;

public class Permutations {

  public static Matrix uniquePerms(Matrix vec) {

    // Vector is empty
    if (vec.isEmpty()) {
      return new Matrix(0, 0);
    }

    // Vector is not a single row
    if (vec.getNumRows() != 1) {
      throw new RuntimeException("JLineMatrix passed to uniquePerms has more than one row. Unsupported.");
    }

    // Number of elements in the vector
    int n = vec.length();

    // Number of unique elements in the vector
    int nu = 0;
    LinkedList<Double> uniqueElements = new LinkedList<>();
    for (int i = 0; i < vec.length(); i++) {
      double element = vec.get(0, i);
      boolean unique = true;
      for (Double uniqueElement : uniqueElements) {
        if (element == uniqueElement) {
          unique = false;
          break;
        }
      }
      if (unique) {
        uniqueElements.add(element);
      }
    }
    nu = uniqueElements.size();

    // Only one unique element
    if (nu == 1) {
      return vec;
    }

    // Every element is unique
    if (n == nu) {
      // TODO: implement this code
      System.out.println("Warning: unimplemented code reached in Permutations.uniquePerms");
    }

    Matrix[] output = new Matrix[nu];
    for (int i = 0; i < nu; i++) {
      Matrix v = vec.clone();
      // TODO: implement the rest of this code
      System.out.println("Warning: unimplemented code reached in Permutations.uniquePerms");
    }

    return new Matrix(0, 0);
  }
}
