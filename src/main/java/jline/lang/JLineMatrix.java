package jline.lang;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.simple.SimpleMatrix;
import org.ejml.sparse.csc.CommonOps_DSCC;

import org.jblas.DoubleMatrix;


public class JLineMatrix extends DMatrixSparseCSC{

	public JLineMatrix(int numRows, int numCols, int arrayLength) {
		super(numRows, numCols, arrayLength);
	}
	
	public JLineMatrix(int numRows, int numCols) {
		super(numRows, numCols, 0);
	}
	
	public JLineMatrix(JLineMatrix matrix) {
		super(matrix.copy());
	}
	
	public JLineMatrix(DMatrixSparseCSC matrix) {
		super(matrix);
	}

	public JLineMatrix(SimpleMatrix matrix) {
		super(matrix.numRows(), matrix.numCols());
		for (int i = 0; i < matrix.numRows(); i++) {
			for (int j = 0; j < matrix.numCols(); j++) {
				this.set(i, j, matrix.get(i, j));
			}
		}
	}

	public static List<Double> intersect(JLineMatrix matrixA, JLineMatrix matrixB) {

		Set<Double> matrixAValues = new HashSet<>();
		Set<Double> matrixBValues = new HashSet<>();
		List<Double> outputValues = new LinkedList<>();

		int rows = matrixA.getNumRows();
		int cols = matrixA.getNumCols();
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				matrixAValues.add(matrixA.get(row, col));
			}
		}

		rows = matrixB.getNumRows();
		cols = matrixB.getNumCols();
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				matrixBValues.add(matrixB.get(row, col));
			}
		}

		for (double value : matrixAValues) {
			if (matrixBValues.contains(value)) {
				outputValues.add(value);
			}
		}

		return outputValues;
	}

	public DMatrixSparseCSC JLineMatrix2DMatrixSparseCSC() {
		return this.copy();
	}

	public DMatrixSparseCSC JLineMatrix2DMatrixSparseCSC(JLineMatrix matrix) {
		return matrix.copy();
	}
		
	public void expandMatrix(int rows, int cols, int nz_length) {
		if (rows < this.getNumRows() || cols < this.getNumCols()) {
			return;
		}
		
		DMatrixSparseTriplet nodeRouting = new DMatrixSparseTriplet(rows, cols, nz_length);	
		for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
			int col1 = this.col_idx[colIdx];
			int col2 = this.col_idx[colIdx+1];
			
			for(int i = col1; i < col2; i++) {
				int rowIdx = this.nz_rows[i];
				double value = this.nz_values[i];
				nodeRouting.addItem(rowIdx, colIdx, value);
			}
		}
		this.setTo(DConvertMatrixStruct.convert(nodeRouting, (DMatrixSparseCSC)null));
	}
	
	public boolean isDiag() {
		if (this.numCols != this.numRows)
			return false;
		
		if (this.getNonZeroLength() != this.numCols)
			return false;
		
		for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
			int col1 = this.col_idx[colIdx];
			int col2 = this.col_idx[colIdx+1];
			
			for(int i = col1; i < col2; i++) {
				int rowIdx = this.nz_rows[i];
				if (rowIdx != colIdx)
					return false;
			}
		}
		
		return true;
	}
	
	public JLineMatrix clone() {
		return new JLineMatrix(this);
	}
	
	public boolean hasNaN() {
		for(int i = 0; i < this.nz_length; i++) {
			if (Double.isNaN(this.nz_values[i]))
				return true;
		}
		return false;
	}
	
	public double get(int idx) {
		if (idx >= this.numCols * this.numRows) 
			throw new RuntimeException("Index out of matrix");
		
		int row = idx % this.getNumRows();
		int col = idx / this.getNumRows();
		
		return super.get(row, col);
	}
	
	public void set(int idx, double val) {
		if (idx >= this.numCols * this.numRows)
			throw new RuntimeException("Index out of matrix");
		
		int row = idx % this.getNumRows();
		int col = idx / this.getNumRows();
		
		this.set(row, col, val);
	}
	
	public void set(int row, int col, double val) {
		super.set(row, col, val);
		
		//This to ensure the value NaN is replaced
		if (val == 0)
			super.remove(row, col); //Remove to ensure that getNonZeroElement not contains the value with 0
	}
	
	public JLineMatrix cumsumViaRow() {
		JLineMatrix res = new JLineMatrix(this.numRows, this.numCols, this.numRows * this.numCols);
		for(int i = 0; i < this.numRows; i++)
			res.set(i, 0, this.get(i, 0));
		
		for(int i = 0; i < this.numRows; i++) {
			for(int j = 1; j < this.numCols; j++) {
				res.set(i, j, this.get(i, j) + res.get(i, j-1));
			}
		}
		return res;
	}
	
	public JLineMatrix cumsumViaCol() {
		JLineMatrix res = new JLineMatrix(this.numRows, this.numCols, this.numRows * this.numCols);
		for(int i = 0; i < this.numCols; i++)
			res.set(0, i, this.get(0, i));
		
		for(int i = 0; i < this.numCols; i++) {
			for(int j = 1; j < this.numRows; j++) {
				res.set(j, i, this.get(j, i) + res.get(j-1, i));
			}
		}
		return res;
	}
	
	public double sumRows(int row) {
		double sum = 0;
		for(int i = 0; i < this.numCols; i++) {
			sum += this.get(row, i);
		}
		return sum;
	}
	
	public JLineMatrix sumRows() {
		DMatrixRMaj sumrows = CommonOps_DSCC.sumRows(this, null);
		DMatrixSparseCSC tmp = new DMatrixSparseCSC(0,0);
		DConvertMatrixStruct.convert(sumrows, tmp);
		return new JLineMatrix(tmp);
	}
	public double sumCols(int col) {
		double sum = 0;
		for(int i = 0; i < this.numRows; i++) {
			sum += this.get(i, col);
		}
		return sum;
	}
	
	public JLineMatrix sumCols() {
		DMatrixRMaj sumcols = CommonOps_DSCC.sumCols(this, null);
		DMatrixSparseCSC tmp = new DMatrixSparseCSC(0,0);
		DConvertMatrixStruct.convert(sumcols, tmp);
		return new JLineMatrix(tmp);
	}
	public JLineMatrix repmat(int rows, int cols) {
		JLineMatrix res = this.clone();
		for(int i = 1; i < rows; i++) {
			JLineMatrix tmp = new JLineMatrix(0,0,0);
			CommonOps_DSCC.concatRows(res, this, tmp);
			res = tmp;
		}
		for(int i = 1; i < cols; i++) {
			JLineMatrix tmp = new JLineMatrix(0,0,0);
			CommonOps_DSCC.concatColumns(res, res, tmp);
			res = tmp;
		}
		return res;
	}

	public JLineMatrix find() {
		JLineMatrix res = new JLineMatrix(this.nz_length, 1, this.nz_length);
		int count = 0;
		for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
			int col1 = this.col_idx[colIdx];
			int col2 = this.col_idx[colIdx+1];
			
			for(int i = col1; i < col2; i++) {
				int rowIdx = this.nz_rows[i];
				res.set(count++, 0, colIdx * this.numRows + rowIdx);
			}
		}
		return res;
	}
	
	public JLineMatrix findNonNegative() {
		List<Integer> array = new ArrayList<Integer>();
		for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
			for(int rowIdx = 0; rowIdx < this.numRows; rowIdx++) {
				if (this.get(rowIdx, colIdx) >= 0)
					array.add(colIdx * this.numRows + rowIdx);
			}
		}
		
		JLineMatrix res = new JLineMatrix(array.size(), 1, array.size());
		for(int i = 0; i < array.size(); i++)
			res.set(i, 0, array.get(i));
		return res;
	}

	public int count(double val) {
		if (val == 0) {
			return this.getNumCols() * this.getNumRows() - this.getNonZeroLength();
		} else {
			int res = 0;
			for(int i = 0; i < this.nz_length; i++) {
				if (this.nz_values[i] == val)
					res++;
			}
			return res;
		}
	}
	
	public JLineMatrix countEachRow(double val) {
		JLineMatrix res = new JLineMatrix(this.numRows, 1);
		for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
			int col1 = this.col_idx[colIdx];
			int col2 = this.col_idx[colIdx+1];
			
			for(int i = col1; i < col2; i++) {
				if (this.nz_values[i] == val) {
					int rowIdx = this.nz_rows[i];
					res.set(rowIdx, 0, res.get(rowIdx,0) + 1);	
				}
			}
		}
		if (val == 0) {
			for(int i = 0; i < this.getNumRows(); i++) {
				for(int j = 0; j < this.getNumCols(); j++) {
					if (this.get(i,j) == 0) {
						res.set(i, 0, res.get(i,0) + 1);
					}
				}
			}
		}
		return res;
	}
	
	public int length() {
		return Math.max(numRows, numCols);
	}
	
	public void abs() {
		for(int i = 0; i < nz_length; i++) {
			this.nz_values[i] = Math.abs(this.nz_values[i]);
		}
	}
	
	public void removeNegative() {
        int offset = 0;
        for (int i = 0; i < this.numCols; i++) {
            int idx0 = this.col_idx[i] + offset;
            int idx1 = this.col_idx[i + 1];

            for (int j = idx0; j < idx1; j++) {
                double val = this.nz_values[j];
                if (val > 0) {
                	this.nz_rows[j - offset] = this.nz_rows[j];
                	this.nz_values[j - offset] = val;
                } else {
                    offset++;
                }
            }
            this.col_idx[i + 1] -= offset;
        }
        this.nz_length -= offset;
	}
	
	public void removeNaN() {
		if (!hasNaN())
			return;
		
        int offset = 0;
        for (int i = 0; i < this.numCols; i++) {
            int idx0 = this.col_idx[i] + offset;
            int idx1 = this.col_idx[i + 1];

            for (int j = idx0; j < idx1; j++) {
                double val = this.nz_values[j];
                if (!Double.isNaN(val)) {
                	this.nz_rows[j - offset] = this.nz_rows[j];
                	this.nz_values[j - offset] = val;
                } else {
                    offset++;
                }
            }
            this.col_idx[i + 1] -= offset;
        }
        this.nz_length -= offset;
	}
	
	public boolean isEmpty() {
		return (this.numCols == 0 || this.numRows == 0);
	}
	
	public void apply(double source, double target, String op) {
		double tol = 1e-20;
		switch (op) {
		case "equal":
			if (Math.abs(source - 0) < tol) {
				if (Math.abs(target - 0) < tol) return;
				for(int i = 0; i < this.numRows; i++) {
					for(int j = 0; j < this.numCols; j++) {
						if (Math.abs(this.get(i,j) - 0) < tol)
							super.set(i, j, target);
					}
				}
			} else if (Double.isNaN(source)) {
				for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
					int col1 = this.col_idx[colIdx];
					int col2 = this.col_idx[colIdx+1];
					
					for(int i = col1; i < col2; i++) {
						if (Double.isNaN(this.nz_values[i])) {
							super.set(this.nz_rows[i], colIdx, target);
						}
					}
				}
			} else if (Double.isInfinite(source)) {
				for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
					int col1 = this.col_idx[colIdx];
					int col2 = this.col_idx[colIdx+1];
					
					for(int i = col1; i < col2; i++) {
						if (Double.isInfinite(this.nz_values[i])) {
							super.set(this.nz_rows[i], colIdx, target);
						}
					}
				}
			} else {
				for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
					int col1 = this.col_idx[colIdx];
					int col2 = this.col_idx[colIdx+1];
					
					for(int i = col1; i < col2; i++) {
						if (Math.abs(this.nz_values[i] - source) < tol){
							super.set(this.nz_rows[i], colIdx, target);
						}
					}
				}
			}
			break;
		case "notequal":
			if (Math.abs(source - 0) < tol) {
				if (Math.abs(target - 0) < tol) this.zero();
				for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
					int col1 = this.col_idx[colIdx];
					int col2 = this.col_idx[colIdx+1];
					
					for(int i = col1; i < col2; i++) {
						if ((Math.abs(this.nz_values[i] - 0) >= tol) || (Double.isNaN(this.nz_values[i]))) {
							super.set(this.nz_rows[i], colIdx, target);
						}
					}
				}
			} else if (Double.isNaN(source)) {
				for(int row = 0; row < this.numRows; row++) {
					for(int col = 0; col < this.numCols; col++) {
						if (!Double.isNaN(this.get(row, col))) {
							super.set(row, col, target);
						}
					}
				}
			} else if (Double.isInfinite(source)) {
				for(int row = 0; row < this.numRows; row++) {
					for(int col = 0; col < this.numCols; col++) {
						if (!Double.isInfinite(this.get(row, col))) {
							super.set(row, col, target);
						}
					}
				}
			} else {
				for(int row = 0; row < this.numRows; row++) {
					for(int col = 0; col < this.numCols; col++) {
						if ((Math.abs(this.get(row, col) - source) >= tol) || (Double.isNaN(this.get(row, col)))) {
							super.set(row, col, target);
						}
					}
				}
			}
			break;
		case "great":
			if (Math.abs(source - 0) < tol) {
				for(int i = 0; i < this.numRows; i++) {
					for(int j = 0; j < this.numCols; j++) {
						if (Math.abs(this.get(i,j) - 0) >= tol && Double.compare(this.get(i,j), 0) > 0) {
							super.set(i, j, target);
						}
					}
				}
			} else if (Double.isNaN(source)) {
				throw new RuntimeException("Cannot compare with NaN");
			} else if (Double.isInfinite(source)) {
				throw new RuntimeException("Cannot compare with Infinite");
			} else {
				for (int row = 0; row < this.numRows; row++) {
					for(int col = 0; col < this.numCols; col++) {
						if (Math.abs(this.get(row, col) - source) >= tol && Double.compare(this.get(row, col), source) > 0) {
							super.set(row, col, target);
						}
					}
				}
			}
			break;
		case "greatequal":
			if (Math.abs(source - 0) < tol) {
				for(int i = 0; i < this.numRows; i++) {
					for(int j = 0; j < this.numCols; j++) {
						if ((Math.abs(this.get(i,j) - 0) < tol) ||
								(Math.abs(this.get(i,j) - 0) >= tol && Double.compare(this.get(i,j), 0) > 0)) {
							super.set(i, j, target);
						}
					}
				}
			} else if (Double.isNaN(source)) {
				throw new RuntimeException("Cannot compare with NaN");
			} else if (Double.isInfinite(source)) {
				throw new RuntimeException("Cannot compare with Infinite");
			} else {
				for (int row = 0; row < this.numRows; row++) {
					for(int col = 0; col < this.numCols; col++) {
						if ((Math.abs(this.get(row, col) - source) < tol) ||
								(Math.abs(this.get(row, col) - source) >= tol && Double.compare(this.get(row, col), source) > 0)) {
							super.set(row, col, target);
						}
					}
				}
			}
			break;
		case "less":
			if (Math.abs(source - 0) < tol) {
				for(int i = 0; i < this.numRows; i++) {
					for(int j = 0; j < this.numCols; j++) {
						if (Math.abs(this.get(i,j) - 0) >= tol && Double.compare(this.get(i,j), 0) < 0) {
							super.set(i, j, target);
						}
					}
				}
			} else if (Double.isNaN(source)) {
				throw new RuntimeException("Cannot compare with NaN");
			} else if (Double.isInfinite(source)) {
				throw new RuntimeException("Cannot compare with Infinite");
			} else {
				for (int row = 0; row < this.numRows; row++) {
					for(int col = 0; col < this.numCols; col++) {
						if (Math.abs(this.get(row, col) - source) >= tol && Double.compare(this.get(row, col), source) < 0) {
							super.set(row, col, target);
						}
					}
				}
			}
			break;
		case "lessequal":
			if (Math.abs(source - 0) < tol) {
				for(int i = 0; i < this.numRows; i++) {
					for(int j = 0; j < this.numCols; j++) {
						if ((Math.abs(this.get(i,j) - 0) < tol) ||
								(Math.abs(this.get(i,j) - 0) >= tol && Double.compare(this.get(i,j), 0) < 0)) {
							super.set(i, j, target);
						}
					}
				}
			} else if (Double.isNaN(source)) {
				throw new RuntimeException("Cannot compare with NaN");
			} else if (Double.isInfinite(source)) {
				throw new RuntimeException("Cannot compare with Infinite");
			} else {
				for (int row = 0; row < this.numRows; row++) {
					for(int col = 0; col < this.numCols; col++) {
						if ((Math.abs(this.get(row, col) - source) < tol) ||
								(Math.abs(this.get(row, col) - source) >= tol && Double.compare(this.get(row, col), source) < 0)) {
							super.set(row, col, target);
						}
					}
				}
			}
			break;
		default:
			throw new RuntimeException("Operation is not supproted");
		}
		
		if (target == 0)
			CommonOps_DSCC.removeZeros(this, 0);
	}
	
	public JLineMatrix elementIncrease(double val) {
		JLineMatrix res = this.clone();
		for(int row = 0; row < this.numRows; row++) {
			for(int col = 0; col < this.numCols; col++) {
				res.set(row, col, res.get(row, col) + val);
			}
		}
		return res;
	}
	
	public JLineMatrix meanCol() {
		JLineMatrix res = new JLineMatrix(1, this.numCols);
		for(int col = 0; col < this.numCols; col++) {
			res.set(0, col, this.sumCols(col) / this.numRows);
		}
		return res;
	}
	
	public JLineMatrix meanRow() {
		JLineMatrix res = new JLineMatrix(this.numRows, 1);
		for(int row = 0; row < this.numRows; row++) {
			res.set(row, 0, this.sumRows(row) / this.numCols);
		}
		return res;
	}
	
	public JLineMatrix power(double t) {
		JLineMatrix res = this.clone();
		if (t == 0) {
			CommonOps_DSCC.fill(res, 1);
		} else if (t != 1) {
			for(int colIdx = 0; colIdx < this.numCols; colIdx++) {
				int col1 = this.col_idx[colIdx];
				int col2 = this.col_idx[colIdx+1];
				
				for(int i = col1; i < col2; i++) {
					int rowIdx = this.nz_rows[i];
					res.set(rowIdx, colIdx, Math.pow(res.get(rowIdx,colIdx), t));	
				}
			}
		}
		return res;
	}
	
	public JLineMatrix array2DtoJLineMatrix(int[][] matrix){
        for (int i = 0; i< matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                set(i,j,matrix[i][j]);
            }
        }
        return this;
    }

    public JLineMatrix array2DtoJLineMatrix(double[][] matrix){
        for (int i = 0; i< matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                set(i,j,matrix[i][j]);
            }
        }
        return this;
    }	

	public void fill(double val) {
		CommonOps_DSCC.fill(this, val);
	}
	
	public JLineMatrix transpose() {
		JLineMatrix res = new JLineMatrix(0,0);
		CommonOps_DSCC.transpose(this, res, null);
		return res;
	}
	
    public JLineMatrix sub(double alpha, JLineMatrix matrix) {
    	return new JLineMatrix(CommonOps_DSCC.add(1, this, -alpha, matrix, null, null, null));
    }

    public JLineMatrix add(double alpha, JLineMatrix matrix) {
    	return new JLineMatrix(CommonOps_DSCC.add(1, this, alpha, matrix, null, null, null));
    }
    
    public void divide(double scalar, JLineMatrix outputB, boolean flag) {
    	if(flag)
    		CommonOps_DSCC.divide(this, scalar, outputB);
    	else
    		CommonOps_DSCC.divide(scalar, this, outputB);
    }
    
    public void divideRows(double[] diag, int offset) {
    	CommonOps_DSCC.divideRows(diag, offset, this);
    }
    
    public JLineMatrix mult(JLineMatrix B, JLineMatrix out) {
    	return new JLineMatrix(CommonOps_DSCC.mult(this, B, out));
    }
    
    public double elementSum() {
    	return CommonOps_DSCC.elementSum(this);
            }
    public double elementMin() {
    	return CommonOps_DSCC.elementMin(this);
        }
    public double elementMax() {
    	return CommonOps_DSCC.elementMax(this);
    }

	public JLineMatrix elementMult(JLineMatrix B, JLineMatrix output) {
		return new JLineMatrix(CommonOps_DSCC.elementMult(this, B, output, null, null));
	}

    public void removeZeros(double val) {
    	CommonOps_DSCC.removeZeros(this, val);
    }
    
    public void changeSign() {
    	CommonOps_DSCC.changeSign(this, this);
    }
	
	public static void extract(JLineMatrix src, int srcX0, int srcX1, int srcY0, int srcY1,
			JLineMatrix dst, int dstY0, int dstX0) {
		CommonOps_DSCC.extract(src, srcX0, srcX1, srcY0, srcY1, dst, dstY0, dstX0);
	}
	
	public static JLineMatrix extractRows( JLineMatrix A, int row0, int row1, JLineMatrix out ) {
		if (out == null) {
			return new JLineMatrix(CommonOps_DSCC.extractRows(A, row0, row1, out));	
		} else {
			CommonOps_DSCC.extractRows(A, row0, row1, out);
			return out;
		}
	}
	
	public static JLineMatrix extractColumn( JLineMatrix A, int column, JLineMatrix out ) {
		if (out == null) {
			return new JLineMatrix(CommonOps_DSCC.extractColumn(A, column, out));
		} else {
			CommonOps_DSCC.extractColumn(A, column, out);
			return out;
		}
	}
	
	public static void extractDiag( JLineMatrix A, JLineMatrix outputB ) {
		CommonOps_DSCC.extractDiag(A, outputB);
	}
	
	public static JLineMatrix concatColumns( JLineMatrix left, JLineMatrix right, JLineMatrix out ) {
		if (out == null) {
			return new JLineMatrix(CommonOps_DSCC.concatColumns(left, right, out));
		} else {
			CommonOps_DSCC.concatColumns(left, right, out);
			return out;
            }
        }

	public static JLineMatrix concatRows(JLineMatrix top, JLineMatrix bottom, JLineMatrix out) {
		if (out == null) {
			return new JLineMatrix(CommonOps_DSCC.concatRows(top, bottom, out));
		} else {
			CommonOps_DSCC.concatRows(top, bottom, out);
			return out;
		}
	}

	public static JLineMatrix diagMatrix(JLineMatrix A, double[] values, int offset, int length) {
		return new JLineMatrix(CommonOps_DSCC.diag(A, values, offset, length));
	}
	
	public static JLineMatrix diag( double... values ) {
		return new JLineMatrix(CommonOps_DSCC.diag(values));
    }

    public double[][] toArray2D(){
        double[][] array = new double[numRows][numCols];
        for (int i=0;i<numRows;i++){
            for(int j=0;j<numCols;j++){
                array[i][j] = this.get(i,j);
            }
        }
        return array;
    }

	public static boolean solve(JLineMatrix a, JLineMatrix b, JLineMatrix x ) {
		return CommonOps_DSCC.solve(a, b, x);
	}

	public double sumSubMatrix(int startRow, int endRow, int startCol, int endCol) {
		// endRow and endCol are EXCLUSIVE i.e. sums up to but not including that row/col
		double sum = 0;
		for (int i = startRow; i < endRow; i++) {
			for (int j = startCol; j < endCol; j++) {
				sum += this.get(i, j);
			}
		}
		return sum;
	}

	public JLineMatrix sumRows(int startCol, int endCol) {
		// endCol is EXCLUSIVE i.e. sums up to but not including that col

		JLineMatrix result = new JLineMatrix(this.getNumRows(), 1);

		for (int i = 0; i < this.getNumRows(); i++) {
			double sum = 0;
			for (int j = startCol; j < endCol; j++) {
				sum += this.get(i, j);
			}
			result.set(i, 0, sum);
		}
		return result;
	}

	public JLineMatrix sumCols(int startRow, int endRow) {
		// endRow is EXCLUSIVE i.e. sums up to but not including that row

		JLineMatrix result = new JLineMatrix(1, this.getNumCols());

		for (int i = 0; i < this.getNumCols(); i++) {
			double sum = 0;
			for (int j = startRow; j < endRow; j++) {
				sum += this.get(i, j);
			}
			result.set(0, i, sum);
		}
		return result;
	}

	public void ones() {
		int rows = this.getNumRows();
		int cols = this.getNumCols();

		if (this.getNumElements() != rows * cols) {
			throw new RuntimeException("Matrix is too small to fill with ones");
		}

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.set(i, j, 1);
			}
		}
	}

	// Kronecker sum of matrices A and B
	public JLineMatrix krons(JLineMatrix other) {

		DMatrixRMaj A = new DMatrixRMaj(this);
		DMatrixRMaj B = new DMatrixRMaj(other);
		DMatrixRMaj C = CommonOps_DDRM.kron(A, CommonOps_DDRM.identity(B.numRows), null);
		DMatrixRMaj D = CommonOps_DDRM.kron(CommonOps_DDRM.identity(A.numRows), B, null);
		DMatrixRMaj output = CommonOps_DDRM.add(C, D, null);
		return new JLineMatrix(SimpleMatrix.wrap(output));
	}

	public JLineMatrix createBlockDiagonal(JLineMatrix matrix2) {
		int m1rows = this.getNumRows();
		int m2rows = 0;
		int m1cols = this.getNumCols();
		int m2cols = 0;

		if (matrix2 != null) {
			m2rows = matrix2.getNumRows();
			m2cols = matrix2.getNumCols();
		}

		JLineMatrix output = new JLineMatrix(m1rows + m2rows, m1cols + m2cols);
		for (int i = 0; i < m1rows; i++) {
			for (int j = 0; j < m1cols; j++) {
				output.set(i, j, this.get(i, j));
			}
		}
		for (int i = 0; i < m2rows; i++) {
			for (int j = 0; j < m2cols; j++) {
				output.set(i + m1rows, j + m1cols, matrix2.get(i, j));
			}
		}

		return output;
	}

	public static JLineMatrix identity(int length) {
		return new JLineMatrix(CommonOps_DSCC.identity(length));
	}

	public JLineMatrix expm() {

		DoubleMatrix inputToEXPM = new DoubleMatrix(this.numRows, this.numCols);
		for (int i = 0; i < this.numRows; i++) {
			for (int j = 0; j < this.numCols; j++) {
				inputToEXPM.put(i, j, this.get(i, j));
			}
		}

		DoubleMatrix outputFromEXPM = org.jblas.MatrixFunctions.expm(inputToEXPM);
        JLineMatrix output = new JLineMatrix(outputFromEXPM.rows, outputFromEXPM.columns);
		for (int i = 0; i < output.numRows; i++) {
			for (int j = 0; j < output.numCols; j++) {
				output.set(i, j, outputFromEXPM.get(i, j));
			}
		}

		return output;
	}

	public double elementMaxAbs() {
		return CommonOps_DSCC.elementMaxAbs(this);
	}

	public void scale(double scalar, JLineMatrix output) {
		CommonOps_DSCC.scale(scalar, this, output);
	}
}
