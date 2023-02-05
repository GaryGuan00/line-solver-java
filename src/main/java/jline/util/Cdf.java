package jline.util;

import java.io.Serializable;
import java.util.*;

public class Cdf<T> implements Serializable {
    protected Collection<Pair<Double, T>> pdf;
    protected Random random;
    protected int numElements;

    public Cdf(Random random) {
        pdf = new ArrayList<Pair<Double, T>>();
        this.random = random;
        this.numElements = 0;
    }

    public void addElement(T elem, double prob) {
        Pair<Double, T> elementPair = new Pair<Double, T>(prob, elem);
        this.pdf.add(elementPair);
        this.numElements++;
    }

    public T generate() {
        double serialProb = this.random.nextDouble();
        double cumProb = 0;
        Iterator<Pair<Double, T>> pdfIter = this.pdf.iterator();
        while (pdfIter.hasNext()) {
            Pair<Double, T> tPair = pdfIter.next();
            cumProb += (tPair.getLeft());
            if (cumProb >= serialProb) {
                return tPair.getRight();
            }
        }
        return null;
    }

    public ArrayList<T> getPossibleEvents(){
        ArrayList<T> arrayList = new ArrayList<>();
        Iterator<Pair<Double, T>> pdfIter = this.pdf.iterator();
        while (pdfIter.hasNext()) {
            Pair<Double, T> tPair = pdfIter.next();
            arrayList.add(tPair.getRight());
        }
        return arrayList;
    }

    public ArrayList<Pair<Double, T>> getPossibleEventProbability(){
        ArrayList<Pair<Double, T>> arrayList = new ArrayList<>();
        Iterator<Pair<Double, T>> pdfIter = this.pdf.iterator();
        while (pdfIter.hasNext()) {
            Pair<Double, T> tPair = pdfIter.next();
            arrayList.add(tPair);
        }
        return arrayList;
    }


    public void normalize(double factor) {
        Iterator<Pair<Double, T>> pdfIter = this.pdf.iterator();
        while (pdfIter.hasNext()) {
            Pair<Double, T> tPair = pdfIter.next();
            double p0 = tPair.getLeft();
            p0 /= factor;
            tPair.setLeft(p0);
        }
    }
}
