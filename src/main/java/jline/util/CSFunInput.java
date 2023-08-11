package jline.util;

public class CSFunInput {
	/* Currently this class is designed for the usage of statelessclassswitcher
	 * state and statedep are not used
	 * This class is the input of function (csFun)
	 * Currently the csFun is the csMatrix for class switch node
	 * r is the row index and s is the column index
	 * The return value is the matrix.get(r,s)
	 * Now there are only two places using this class.
	 * 	One is creating csMask in refreshChains
	 * 	Another is obtaining the csMatrix of particular class swicther in getRoutingMatrix
	 * If future modification is done, please modify the aforementioned code as well
	 */
	public int r;
	public int s;
	public Matrix state;
	public Matrix statedep;
	
	public CSFunInput(int r, int s, Matrix state, Matrix statedep) {
		this.r = r;
		this.s = s;
		this.state = state;
		this.statedep = statedep;
	}
}
