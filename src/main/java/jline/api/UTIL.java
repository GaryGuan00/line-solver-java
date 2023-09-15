package jline.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jline.util.Matrix;
import jline.util.UndirectedGraph;
import org.apache.commons.math3.special.Gamma;

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

    // Error function
    public static double erf(double x) {
        // Constants
        double a1 =  0.254829592;
        double a2 = -0.284496736;
        double a3 =  1.421413741;
        double a4 = -1.453152027;
        double a5 =  1.061405429;
        double p  =  0.3275911;

        // Save the sign of x
        int sign = (x < 0) ? -1 : 1;
        x = Math.abs(x);

        // A&S formula 7.1.26
        double t = 1.0 / (1.0 + p * x);
        double y = (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t;

        return sign * (1 - y * Math.exp(-x * x));
    }

    public static double factln(double n) {
        return Math.log(Gamma.gamma(1+n));
    }

    public static Matrix factln(Matrix n) {
        Matrix ret = n.clone();
        for (int i = 0; i < n.length(); i++) {
            ret.set(i, factln(ret.get(i)));
        }
        return ret;
    }

    public static double multinomialln(Matrix n) {
        return factln(n.elementSum())- factln(n).elementSum();
    }

    public static double logsumexp(Matrix x) {
        int n = x.length();
        double a = x.elementMax();
        int k = -1;
        for (int i = 0; i < n; i++) {
            if (x.get(i) == a) {
                k = i;
                break;
            }
        }
        Matrix w = new Matrix(1, n);
        w.fill(0.0);
        double s = 0;

        for (int i = 0; i < n; i++) {
            w.set(i, Math.exp(x.get(i)-a));
            if (i != k) {
                s += w.get(i);
            }
        }
        return (a + Math.log1p(s));
    }

    // Gamma function via Lanczos approximation formula
    public static double gammaFunction(double x) {
        double[] p = {0.99999999999980993, 676.5203681218851, -1259.1392167224028, 771.32342877765313,
                -176.61502916214059, 12.507343278686905, -0.13857109526572012, 9.9843695780195716e-6,
                1.5056327351493116e-7};
        int g = 7;
        if (x < 0.5) return Math.PI / (Math.sin(Math.PI * x) * gammaFunction(1 - x));
        x -= 1;
        double a = p[0];
        double t = x + g + 0.5;
        for (int i = 1; i < p.length; i++) {
            a += p[i] / (x + i);
        }
        return Math.sqrt(2 * Math.PI) * Math.pow(t, x + 0.5) * Math.exp(-t) * a;
    }

    public static Matrix decorate(Matrix inSpace1, Matrix inSpace2) {

      // TODO: upfront if clause for 1 parameter, lines 7 to 14

      if (inSpace1.isEmpty()) {
        inSpace1 = inSpace2.clone();
        return inSpace1;
      }

      if (inSpace2.isEmpty()) {
        return inSpace1;
      }

      int n1 = inSpace1.getNumRows();
      int m1 = inSpace1.getNumCols();
      int n2 = inSpace2.getNumRows();
      int m2 = inSpace2.getNumCols();

      inSpace1.repmat(n2, 1);
      int curStatesStart = 0;
      int curStatesEnd = n1;

      for (int s = 0; s < n2; s++) {
        Matrix tmp = new Matrix(1, inSpace2.getNumCols());
        Matrix.extractRows(inSpace2, s, s + 1, tmp);
        tmp.repmat(curStatesEnd - 1, 1);

        inSpace1.expandMatrix(
            inSpace1.getNumRows() + curStatesEnd - 1,
            inSpace1.getNumCols() + m2,
            (inSpace1.getNumRows() + curStatesEnd - 1) * (inSpace1.getNumCols() + m2));
        for (int i = curStatesStart; i < curStatesEnd; i++) {
          for (int j = m1; j < m1 + m2; j++) {
            inSpace1.set(i, j, tmp.get(i - curStatesStart, j - m1));
          }
        }
        curStatesStart += n1;
        curStatesEnd += n1;
      }

      return inSpace1;
    }

    public static Matrix multiChoose(double n, double k) {

      Matrix v = new Matrix(1, (int) n);
      v.zero();

      if (n == 1) {
        v = new Matrix(1, 1);
        v.set(0, 0, k);
      } else if (k != 0) {
        List<Matrix> tmpSSRows = new ArrayList<>();
        for (int i = 0; i <= k; i++) {
          Matrix w = multiChoose(n - 1, k - i);
          Matrix tmpSSRow = new Matrix(w.getNumRows(), w.getNumCols() + 1);
          for (int j = 0; j < w.getNumRows(); j++) {
            tmpSSRow.set(j, 0, i);
            for (int l = 1; l < w.getNumCols() + 1; l++) {
              tmpSSRow.set(j, l, w.get(j, l - 1));
            }
          }
          tmpSSRows.add(tmpSSRow);
        }
        int rowForV = 0;
        for (int i = 0; i < tmpSSRows.size(); i++) {
          int rowForTmpSSRows = 0;
          for (int j = rowForV; j < tmpSSRows.get(i).getNumRows(); j++) {
            for (int l = 0; l < tmpSSRows.get(i).getNumCols(); l++) {
              v.set(j, l, tmpSSRows.get(i).get(rowForTmpSSRows, l));
            }
            rowForV++;
            rowForTmpSSRows++;
          }
        }
      }

      return v;
    }

    // TODO: polymorphic version that also returns nanMean
    // Return mean absolute percentage error of approx with respect to exact
    public static double mape(Matrix approx, Matrix exact) {

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

    public static <T extends Object> List<T> unique(List<T> list) {
        return new ArrayList<T>(new HashSet<>(list));
    }

}
