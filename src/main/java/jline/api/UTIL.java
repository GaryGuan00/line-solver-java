package jline.api;

import java.util.List;
import java.util.Set;

import jline.util.Matrix;
import jline.util.UndirectedGraph;

/**
 * APIs for utility functions.
 * TODO: refactor to the util package
 */
public class UTIL {

	/**
	 * Weakly-connected components of a sub-matrix.
	 *
	 * @param param Input matrix
	 * @param colsToIgnore Indexes to be ignored
	 * @return Weighted connected components
	 */
	public static Set<Set<Integer>> weaklyConnect(Matrix param, Set<Integer> colsToIgnore) {
		UndirectedGraph graph = new UndirectedGraph(param, colsToIgnore);
		graph.computeWeaklyConnectedComponents();
		return graph.getWCC();
	}

	/**
	 * Decrease by one an element of an integer vector.
	 *
	 * @param N integer vector
	 * @param r dimension to decrease
	 * @return Decreased vector
	 */
	public static Matrix oner(Matrix N, List<Integer> r) {
		Matrix res = N.clone();
		for (Integer s : r) {
			if (s >= 0)
				res.set(s, res.get(s) - 1);
		}
		return res;
	}

	/**
	 * Softmin function.
	 *
	 * @param x first term to compare
	 * @param y second term to compare
	 * @param alpha softmin smoothing parameter
	 * @return Softmin function value
	 */
	public static double softmin(double x, double y, double alpha) {
		return -((-x) * Math.exp(-alpha * x) - y * Math.exp(-alpha * y))
				/ (Math.exp(-alpha * x) + Math.exp(-alpha * y));
	}

	/**
	 * Returns the max of two numbers. If one of the two is NaN, it returns the other.
	 * @param x - the first number to be compared
	 * @param y - the second number to be compared
	 * @return - the max between the two
	 */
	public static double max(double x, double y){
		if(!Double.isNaN(x) && !Double.isNaN(y))
			return Math.max(x, y);
		else if (Double.isNaN(x))
			return y;
		return x;
	}

	/**
	 * Returns the min of two numbers. If one of the two is NaN, it returns the other.
	 * @param x - the first number to be compared
	 * @param y - the second number to be compared
	 * @return - the min between the two
	 */
	public static double min(double x, double y){
		if(!Double.isNaN(x) && !Double.isNaN(y))
			return Math.min(x, y);
		else if (Double.isNaN(x))
			return y;
		return x;
	}

	/**
	 * Returns the position of the given row in the corresponding matrix
	 * @param matrix - the matrix to be searched
	 * @param row - the row
	 * @return - Position of the given row in the matrix, or -1 otherwise
	 */
	public static int matchrow(Matrix matrix, Matrix row){
		if(matrix.getNumCols() != row.getNumCols())
			return -1;
		for(int i = 0; i < matrix.getNumRows(); i++){
			boolean rowsEqual = true;
			for(int j = 0; j < matrix.getNumCols(); j++){
				if(matrix.get(i, j) != row.get(j)){
					rowsEqual = false;
					break;
				}
			}
			if(rowsEqual)
				return i;
		}
		return -1;
	}

	/**
	 * Computes the combinations of the elements in v taken k at a time
	 * @param v - vector of elements
	 * @param k - how many elements to pick at a time
	 * @return - the combinations of the elements in v taken k at a time
	 */
	public static Matrix nchoosek(Matrix v, int k){
		int n = v.length();
		if(k < 0 || k > n){
			return null;
		}
		int a = 1, b = 1, c = 1; // a == n!, b == k!, c == (n-k)!
		for(int i = 2; i <= n; i++){
			a *= i;
			if(i <= k){
				b *= i;
			}
			if(i <= n - k){
				c *= i;
			}
		}
		Matrix res = new Matrix(a/(b*c), k);
		int row = 0;
		int[] indexes = new int[k];
		for(int i = 0; i < indexes.length; i++){
			indexes[i] = i;
		}
		while(true){
			for(int i = 0; i < k; i++){
				res.set(row, i, v.get(indexes[i]));
			}
			row++;
			int last = k - 1;
			while(last >= 0 && indexes[last] == n - k + last){
				last--;
			}
			if(last == -1){
				break;
			}
			indexes[last]++;
			for(int i = last + 1; i < k; i++){
				indexes[i] = indexes[i - 1] + 1;
			}
		}
		return res;
	}
}
