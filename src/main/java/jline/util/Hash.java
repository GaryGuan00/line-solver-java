package jline.util;

public class Hash {
    public static int hashpop(Matrix n, Matrix N){
        int idx = 0;
        int R = N.length();
        for(int r = 0; r < R; r++){
            double prod = 1;
            for(int j = 0; j < r; j++){
                prod *= (N.get(j) + 1);
            }
            idx += prod * n.get(r);
        }
        return idx;
    }

    public static int hashpop(Matrix n, Matrix N, int R, Matrix prods){
        int idx = 0;
        for(int r = 0; r < R; r++){
            idx += prods.get(r) * n.get(r);
        }
        return idx;
    }
}
