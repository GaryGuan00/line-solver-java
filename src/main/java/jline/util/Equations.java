package jline.util;

import java.util.*;

/**
 * Simple helper math functions.
 */
public class Equations {

    /**
     * Cumulative sum of an array, where the value at each index of the result is the
     * sum of all the previous values of the input.
     * @param ar Array we want the cumulative sum of.
     * @return New array that is the cumulative sum of the input.
     */
    public static int[] cumSum(int[] ar) {
        int[] result = new int[ar.length];
        result[0] = ar[0];
        for (int i = 1; i < ar.length; i++) {
            result[i] = result[i - 1] + ar[i];
        }
        return result;
    }

    /**
     * Cumulative sum of an array, where the value at each index of the result is the
     * sum of all the previous values of the input.
     * @param ar Array we want the cumulative sum of.
     * @return New array that is the cumulative sum of the input.
     */
    public static double[] cumSum(double[] ar) {
        double[] result = new double[ar.length];
        result[0] = ar[0];
        for (int i = 1; i < ar.length; i++) {
            result[i] = result[i - 1] + ar[i];
        }
        return result;
    }
    public static double[] logspace(int start, int stop, int n) {
        double base = 10;
        double logMax = Math.log10(stop);
        double logMin = Math.log10(start);
        double delta = (logMax - logMin) / (n-1);
        double exp = logMin;
        double[] vector = new double[n];
        for (int i = 0; i < n; i++) {
            vector[i] = Math.pow(base, exp);
            exp += delta;
        }
        return vector;
    }

    /**
     * Generates an integer list of n logarithmically spaced values.

     * @param start First element of array.
     * @param stop Last element of array.
     * @param n Number of values.
     * @return Array of logarithmically spaced values.
     */
    public static int[] logspacei(int start, int stop, int n) {
        // I know this sucks but im making it java 7 compatible on the last possible day, was using lambdas
        ArrayList<Integer> vector = new ArrayList<>(n);
        int i = 0;
        for (double log: logspace(start, stop, n)) {
            vector.add((int) Math.round(log));
        }
        vector = new ArrayList<Integer>(new LinkedHashSet<Integer>(vector));
        int[] result = new int[n];
        int j = 0;
        for (Integer integer : vector) {
            result[j] = integer;
            j++;
        }
        return Arrays.copyOf(result, j);
    }

    /**
     * Transposes a 2D array of values.
     * @param data 2D array to be transposed.
     * @return New 2D array of transposed data.
     */
    public static double[][] transpose(double[][] data) {
        int n = data.length;
        int m = data[0].length;
        double[][] transpose = new double[m][n];
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                transpose[r][c] = data[c][r];
            }
        }
        return transpose;
    }

    /**
     * Helper method that adds an integer to every element of an
     * integer array.
     *
     * @param ar Array to be added to.
     * @param i Integer to add.
     */
    public static void elementAdd(int[] ar, int i) {
        for (int j = 0; j < ar.length; j++) {
            ar[j] += i;
        }
    }

    /**
     * Given an integer x input, find the next integer y > x such that
     * y is a power of 2.
     *
     * @param x Input to find next power
     * @return Closest integer greater than input that is a power of two.
     */
    protected static int nextPowerOfTwo(int x) {
        int highestOneBit = Integer.highestOneBit(x);
        return (x == highestOneBit) ? x : highestOneBit << 1;
    }
}
