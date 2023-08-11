package jline.util;

import org.apache.commons.math3.special.Gamma;

public class Numerics {

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
}
